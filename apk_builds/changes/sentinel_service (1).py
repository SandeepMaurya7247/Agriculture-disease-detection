"""
🛰️ Sentinel Hub Satellite Data Service
======================================
Handles all communication with the Sentinel Hub APIs:
  - OAuth2 Authentication (token fetch & caching)
  - Bounding Box calculation from lat/lon/radius
  - NDVI data retrieval via the Process API (returns a PNG image)
  - Average NDVI extraction from the image array

DEMO MODE: When SENTINEL_HUB_CLIENT_ID is not set (or left as placeholder),
the service returns deterministic simulated NDVI data so the full ML
pipeline works without real credentials.
"""

import os
import time
import io
import requests
import numpy as np
from PIL import Image


# ──────────────────────────────────────────────
# CONFIGURATION  (loaded from .env)
# ──────────────────────────────────────────────
SENTINEL_HUB_CLIENT_ID     = os.getenv("SENTINEL_HUB_CLIENT_ID",     "")
SENTINEL_HUB_CLIENT_SECRET = os.getenv("SENTINEL_HUB_CLIENT_SECRET", "")

SENTINEL_AUTH_URL    = "https://services.sentinel-hub.com/oauth/token"
SENTINEL_PROCESS_URL = "https://services.sentinel-hub.com/api/v1/process"

# ──────────────────────────────────────────────
# SIMPLE IN-MEMORY TOKEN CACHE
# ──────────────────────────────────────────────
_token_cache = {
    "access_token": None,
    "expires_at":   0       # Unix timestamp
}

_PLACEHOLDER_IDS = {"", "your_sentinel_hub_client_id_here"}


def _is_demo_mode() -> bool:
    """Returns True when no valid Sentinel Hub Client ID is configured."""
    return not SENTINEL_HUB_CLIENT_ID or SENTINEL_HUB_CLIENT_ID.strip() in _PLACEHOLDER_IDS


def _simulate_ndvi_stats(lat: float, lon: float) -> dict:
    """
    Generates realistic, location-seeded NDVI statistics for demo/dev mode.
    The same coordinates always produce the same values (deterministic seed).

    Typical agricultural NDVI ranges for India:
        Dense healthy crops : 0.50 – 0.85
        Moderate vegetation : 0.30 – 0.55
        Sparse / dry crops  : 0.10 – 0.35
        Bare soil / fallow  : 0.00 – 0.15
    """
    seed = int(abs(round(lat, 3) * 1000 + round(lon, 3) * 100)) % (2 ** 31)
    rng  = np.random.default_rng(seed=seed)

    # Bias mean_ndvi toward moderate-healthy range (0.30 – 0.72) for India
    mean_ndvi  = float(rng.uniform(0.30, 0.72))
    spread     = float(rng.uniform(0.05, 0.18))
    max_ndvi   = min(mean_ndvi + spread + float(rng.uniform(0.02, 0.08)), 0.92)
    min_ndvi   = max(mean_ndvi - spread - float(rng.uniform(0.02, 0.08)), -0.05)
    std_ndvi   = float(rng.uniform(0.04, 0.14))
    pixel_count = int(rng.integers(18_000, 58_000))

    print(
        f"INFO  [Sentinel DEMO]: Simulated NDVI for ({lat:.4f}, {lon:.4f}) "
        f"-> mean={mean_ndvi:.3f}, max={max_ndvi:.3f}, min={min_ndvi:.3f}"
    )

    return {
        "mean_ndvi":   round(mean_ndvi,   4),
        "max_ndvi":    round(max_ndvi,    4),
        "min_ndvi":    round(min_ndvi,    4),
        "std_ndvi":    round(std_ndvi,    4),
        "pixel_count": pixel_count,
        "demo_mode":   True
    }


# ──────────────────────────────────────────────────────────────────────────────
# HIGH-LEVEL ENTRY POINT  (used by main.py /api/analyze-crop)
# ──────────────────────────────────────────────────────────────────────────────

def get_ndvi_stats(lat: float, lon: float, radius_m: float) -> dict:
    """
    High-level entry point for the analyze-crop endpoint.

    • DEMO MODE  (no credentials): returns deterministic simulated NDVI stats.
    • LIVE MODE  (credentials set): authenticates → fetches Sentinel-2 NDVI
      image → extracts real stats.

    Args:
        lat      (float): Farm centre latitude.
        lon      (float): Farm centre longitude.
        radius_m (float): Analysis radius in metres.

    Returns:
        dict: NDVI statistics with optional 'bbox' and 'demo_mode' keys.

    Raises:
        ValueError:     on invalid coordinates / radius.
        RuntimeError:   on Sentinel Hub API failures (live mode only).
    """
    # Validate + compute bbox regardless of mode
    bbox = calculate_bounding_box(lat, lon, radius_m)
    print(f"DEBUG [Sentinel]: BBox = {bbox}")

    if _is_demo_mode():
        print("INFO  [Sentinel]: DEMO MODE active — SENTINEL_HUB_CLIENT_ID not set.")
        stats = _simulate_ndvi_stats(lat, lon)
        stats["bbox"] = bbox
        return stats

    # ── Live mode ─────────────────────────────────────────────────────────────
    token     = get_sentinel_token()
    png_bytes = fetch_ndvi_image(bbox, token)
    stats     = extract_ndvi_stats(png_bytes)
    stats["bbox"]      = bbox
    stats["demo_mode"] = False
    return stats


# ──────────────────────────────────────────────────────────────────────────────
# LOW-LEVEL HELPERS
# ──────────────────────────────────────────────────────────────────────────────

def get_sentinel_token() -> str:
    """
    Authenticates with Sentinel Hub using OAuth2 Client Credentials flow.
    Caches the access token until it expires (minus a 60-second safety buffer).

    Returns:
        str: A valid Bearer access token.

    Raises:
        RuntimeError: If authentication fails or credentials are missing.
    """
    global _token_cache

    # Return cached token if still valid
    if _token_cache["access_token"] and time.time() < _token_cache["expires_at"]:
        print("DEBUG [Sentinel]: Using cached access token.")
        return _token_cache["access_token"]

    if _is_demo_mode():
        raise RuntimeError(
            "Sentinel Hub credentials are missing. "
            "Set SENTINEL_HUB_CLIENT_ID and SENTINEL_HUB_CLIENT_SECRET in backend/.env"
        )

    print("DEBUG [Sentinel]: Fetching new access token...")
    try:
        response = requests.post(
            SENTINEL_AUTH_URL,
            data={
                "grant_type":    "client_credentials",
                "client_id":     SENTINEL_HUB_CLIENT_ID,
                "client_secret": SENTINEL_HUB_CLIENT_SECRET,
            },
            timeout=15
        )
        response.raise_for_status()
        token_data = response.json()

        _token_cache["access_token"] = token_data["access_token"]
        _token_cache["expires_at"]   = time.time() + token_data.get("expires_in", 3600) - 60

        print("DEBUG [Sentinel]: Access token obtained successfully.")
        return _token_cache["access_token"]

    except requests.exceptions.Timeout:
        raise RuntimeError("Sentinel Hub authentication timed out. Please try again.")
    except requests.exceptions.RequestException as e:
        raise RuntimeError(f"Sentinel Hub authentication failed: {e}")


def calculate_bounding_box(lat: float, lon: float, radius_m: float) -> list:
    """
    Calculates a bounding box [min_lon, min_lat, max_lon, max_lat] from a
    centre point and radius.

    Conversion:
        1 degree latitude  ≈ 111,320 metres
        1 degree longitude ≈ 111,320 * cos(latitude) metres

    Args:
        lat      (float): Latitude of the centre point (decimal degrees).
        lon      (float): Longitude of the centre point (decimal degrees).
        radius_m (float): Radius in metres.

    Returns:
        list: [min_lon, min_lat, max_lon, max_lat]

    Raises:
        ValueError: If coordinates or radius are out of valid ranges.
    """
    if not (-90 <= lat <= 90):
        raise ValueError(f"Invalid latitude: {lat}. Must be between -90 and 90.")
    if not (-180 <= lon <= 180):
        raise ValueError(f"Invalid longitude: {lon}. Must be between -180 and 180.")
    if radius_m <= 0:
        raise ValueError(f"Radius must be a positive value. Got: {radius_m}")
    if radius_m > 500_000:
        raise ValueError("Radius too large. Maximum allowed is 500,000 metres (500 km).")

    lat_delta = radius_m / 111_320.0
    lon_delta = radius_m / (111_320.0 * np.cos(np.radians(lat)))

    return [
        round(lon - lon_delta, 6),
        round(lat - lat_delta, 6),
        round(lon + lon_delta, 6),
        round(lat + lat_delta, 6),
    ]


def fetch_ndvi_image(bbox: list, access_token: str, image_size: int = 256) -> bytes:
    """
    Calls the Sentinel Hub Process API to get an NDVI image (PNG).

    Args:
        bbox         (list):  [min_lon, min_lat, max_lon, max_lat]
        access_token (str):   Valid Sentinel Hub Bearer token.
        image_size   (int):   Width and height in pixels (default 256×256).

    Returns:
        bytes: Raw PNG image bytes.

    Raises:
        RuntimeError: If the API call fails or returns an error.
    """
    evalscript = """
//VERSION=3
function setup() {
    return {
        input: [{ bands: ["B04", "B08", "SCL"], units: "DN" }],
        output: { bands: 1, sampleType: "UINT8" }
    };
}
function evaluatePixel(sample) {
    if (sample.SCL >= 8 && sample.SCL <= 10) { return [0]; }
    var B04 = sample.B04, B08 = sample.B08;
    if (B04 + B08 === 0) { return [0]; }
    var ndvi = (B08 - B04) / (B08 + B04);
    return [Math.round((ndvi + 1) / 2 * 255)];
}
"""

    payload = {
        "input": {
            "bounds": {
                "bbox": bbox,
                "properties": {"crs": "http://www.opengis.net/def/crs/EPSG/0/4326"}
            },
            "data": [{
                "type": "sentinel-2-l2a",
                "dataFilter": {
                    "maxCloudCoverage": 30,
                    "timeRange": {
                        "from": "2024-01-01T00:00:00Z",
                        "to":   "2026-12-31T23:59:59Z"
                    }
                }
            }]
        },
        "output": {
            "width":  image_size,
            "height": image_size,
            "responses": [{"identifier": "default", "format": {"type": "image/png"}}]
        },
        "evalscript": evalscript
    }

    headers = {
        "Authorization": f"Bearer {access_token}",
        "Content-Type":  "application/json",
        "Accept":        "image/png"
    }

    try:
        print(f"DEBUG [Sentinel]: Requesting NDVI image for bbox={bbox} ...")
        response = requests.post(SENTINEL_PROCESS_URL, json=payload, headers=headers, timeout=45)
        if response.status_code == 200:
            print(f"DEBUG [Sentinel]: NDVI image received ({len(response.content)} bytes).")
            return response.content
        else:
            raise RuntimeError(
                f"Sentinel Hub Process API error {response.status_code}: {response.text[:300]}"
            )
    except requests.exceptions.Timeout:
        raise RuntimeError("Sentinel Hub request timed out after 45 seconds.")
    except requests.exceptions.RequestException as e:
        raise RuntimeError(f"Sentinel Hub network error: {e}")


def extract_ndvi_stats(png_bytes: bytes) -> dict:
    """
    Decodes the grayscale PNG and converts pixel values back to NDVI values.

    Args:
        png_bytes (bytes): Raw PNG image bytes from the Process API.

    Returns:
        dict: { mean_ndvi, max_ndvi, min_ndvi, std_ndvi, pixel_count }
    """
    image = Image.open(io.BytesIO(png_bytes)).convert("L")
    pixel_array = np.array(image, dtype=np.float32)

    valid_mask = pixel_array > 0
    if not valid_mask.any():
        print("WARN [Sentinel]: No valid (non-cloud) pixels found in the image.")
        return {"mean_ndvi": -1.0, "max_ndvi": -1.0, "min_ndvi": -1.0,
                "std_ndvi": 0.0, "pixel_count": 0}

    valid_pixels = pixel_array[valid_mask]
    ndvi_values  = (valid_pixels / 255.0) * 2.0 - 1.0

    return {
        "mean_ndvi":   round(float(np.mean(ndvi_values)), 4),
        "max_ndvi":    round(float(np.max(ndvi_values)),  4),
        "min_ndvi":    round(float(np.min(ndvi_values)),  4),
        "std_ndvi":    round(float(np.std(ndvi_values)),  4),
        "pixel_count": int(valid_pixels.size)
    }
