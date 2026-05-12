"""
🌾 NDVI-Based Crop Health ML Interpreter
=========================================
Placeholder ML model that classifies crop health and generates
irrigation/intervention advice based on NDVI statistics.

HOW TO PLUG IN YOUR REAL MODEL:
  1. Replace the `_rule_based_prediction()` function body with a call to your
     pre-trained model (e.g., scikit-learn, XGBoost, ONNX).
  2. Ensure your model accepts the feature vector defined in `build_feature_vector()`.
  3. Update `NDVI_CLASSES` if your model outputs different labels.

Example (XGBoost):
    import joblib
    _model = joblib.load("path/to/model.pkl")

    def predict(ndvi_stats, extra_inputs=None):
        features = build_feature_vector(ndvi_stats, extra_inputs)
        label_idx = int(_model.predict([features])[0])
        ...
"""

import numpy as np


# ── Classification labels ─────────────────────────────────────────────────────
NDVI_CLASSES = {
    0: "Critical Stress",       # NDVI very low  → severe drought / disease
    1: "Moderate Stress",       # NDVI low       → mild nutrient deficiency / drought
    2: "Healthy Vegetation",    # NDVI optimal   → crop is thriving
    3: "Sparse / Bare Soil",    # NDVI near zero → early growth or bare patches
    4: "Water Body / No Crop",  # NDVI negative  → water or non-vegetated area
}

# Confidence is approximated from the standard deviation (uniformity proxy)
_ADVICE_DB = {
    "Critical Stress": {
        "severity": "🔴 Critical",
        "irrigation_needed": True,
        "irrigation_recommendation": "Immediate irrigation required. Apply 40–60 mm of water within 24 hours.",
        "nutrient_action": "Apply foliar spray of NPK 0-52-34 (Mono Potassium Phosphate) at 5 g/L to aid rapid recovery.",
        "field_action": "Scout for pests and diseases immediately. Consider emergency intervention.",
        "next_check_days": 2
    },
    "Moderate Stress": {
        "severity": "🟠 Moderate",
        "irrigation_needed": True,
        "irrigation_recommendation": "Schedule irrigation within 48–72 hours. Apply 25–35 mm of water.",
        "nutrient_action": "Apply urea top-dressing at 50 kg/hectare if nitrogen deficiency is suspected.",
        "field_action": "Check soil moisture at 15 cm depth. Inspect lower leaves for discolouration.",
        "next_check_days": 5
    },
    "Healthy Vegetation": {
        "severity": "🟢 Optimal",
        "irrigation_needed": False,
        "irrigation_recommendation": "No immediate irrigation required. Maintain current watering schedule.",
        "nutrient_action": "Continue balanced fertiliser programme. Monitor with Leaf Color Chart (LCC).",
        "field_action": "Crop is performing well. Scout field for early pest signs as a precaution.",
        "next_check_days": 10
    },
    "Sparse / Bare Soil": {
        "severity": "🟡 Low Activity",
        "irrigation_needed": False,
        "irrigation_recommendation": "Light irrigation recommended if less than 5 days since last rain.",
        "nutrient_action": "Ensure basal fertiliser is applied at sowing stage.",
        "field_action": "Could indicate early growth stage or patchy germination. Verify crop stand.",
        "next_check_days": 7
    },
    "Water Body / No Crop": {
        "severity": "⚪ Non-Agricultural",
        "irrigation_needed": False,
        "irrigation_recommendation": "Not applicable – area does not appear to contain active crop.",
        "nutrient_action": "Not applicable.",
        "field_action": "Verify the coordinates. The selected area may contain a water body or fallow land.",
        "next_check_days": 14
    }
}


def build_feature_vector(ndvi_stats: dict, extra_inputs: dict = None) -> list:
    """
    Constructs the feature vector that will be passed to the ML model.

    Current features (5):
      [mean_ndvi, max_ndvi, min_ndvi, std_ndvi, pixel_count_normalized]

    If you add weather or soil inputs in the future, extend extra_inputs:
      extra_inputs = {"temperature": 28.5, "humidity": 65, "soil_ph": 6.5, ...}

    Args:
        ndvi_stats   (dict): Output of sentinel_service.extract_ndvi_stats()
        extra_inputs (dict): Optional additional sensor/weather data.

    Returns:
        list: Feature vector ready for model.predict([features])
    """
    features = [
        ndvi_stats.get("mean_ndvi",   0.0),
        ndvi_stats.get("max_ndvi",    0.0),
        ndvi_stats.get("min_ndvi",    0.0),
        ndvi_stats.get("std_ndvi",    0.0),
        # Normalise pixel count to [0, 1] assuming max resolution 256x256 = 65536 px
        min(ndvi_stats.get("pixel_count", 0) / 65536.0, 1.0),
    ]

    # ── Extend with weather / soil inputs if provided ─────────────────────────
    if extra_inputs:
        features.extend([
            float(extra_inputs.get("temperature", 25.0)),
            float(extra_inputs.get("humidity",    70.0)),
            float(extra_inputs.get("rainfall",   100.0)),
            float(extra_inputs.get("soil_ph",      6.5)),
        ])

    return features


def _rule_based_prediction(mean_ndvi: float) -> tuple:
    """
    Rule-based fallback classifier when no trained model is available.
    This mirrors what a well-tuned decision tree would produce for NDVI
    thresholds validated against agricultural literature.

    ┌──────────────────────────────────┬───────────────────────┐
    │  NDVI Range                      │  Classification       │
    ├──────────────────────────────────┼───────────────────────┤
    │  NDVI < 0                        │  Water / No Crop      │
    │  0.00 ≤ NDVI < 0.20             │  Bare Soil / Sparse   │
    │  0.20 ≤ NDVI < 0.40             │  Moderate Stress      │
    │  0.40 ≤ NDVI < 0.75             │  Healthy Vegetation   │
    │  NDVI < 0.20 (severe degraded)   │  Critical Stress      │
    └──────────────────────────────────┴───────────────────────┘

    Returns:
        tuple: (class_label: str, confidence: float)
    """
    if mean_ndvi < 0.0:
        label, conf = "Water Body / No Crop", 0.92
    elif mean_ndvi < 0.10:
        label, conf = "Critical Stress", 0.88
    elif mean_ndvi < 0.20:
        label, conf = "Moderate Stress", 0.80
    elif mean_ndvi < 0.40:
        label, conf = "Sparse / Bare Soil", 0.74
    elif mean_ndvi < 0.75:
        label, conf = "Healthy Vegetation", 0.91
    else:
        label, conf = "Healthy Vegetation", 0.96    # Dense, thriving canopy

    return label, conf


def predict(ndvi_stats: dict, extra_inputs: dict = None) -> dict:
    """
    Main prediction entry point.

    Steps:
      1. Build the feature vector from NDVI stats + optional extra inputs.
      2. [PLACEHOLDER] Pass features to your pre-trained ML model.
      3. Fall back to rule-based classifier if model is not yet loaded.
      4. Return a structured prediction dictionary.

    Args:
        ndvi_stats   (dict): Output of sentinel_service.extract_ndvi_stats()
        extra_inputs (dict): Optional dict containing temperature, humidity, etc.

    Returns:
        dict: Full prediction result with label, confidence, and advice.
    """
    mean_ndvi = ndvi_stats.get("mean_ndvi", -1.0)

    # ── STEP 1: Build feature vector ─────────────────────────────────────────
    features = build_feature_vector(ndvi_stats, extra_inputs)
    print(f"DEBUG [CropHealthML]: Feature vector = {features}")

    # ── STEP 2: YOUR ML MODEL GOES HERE ──────────────────────────────────────
    # TODO: Replace the block below with your actual model call.
    #
    # Example — scikit-learn / XGBoost:
    #   import joblib
    #   model = joblib.load("ml/models/crop_health_xgb.pkl")
    #   label_idx  = int(model.predict([features])[0])
    #   confidence = float(model.predict_proba([features])[0].max())
    #   label      = NDVI_CLASSES[label_idx]
    #
    # Example — ONNX Runtime (same runtime already used in main.py):
    #   import onnxruntime as ort, numpy as np
    #   sess  = ort.InferenceSession("ml/models/crop_health.onnx")
    #   inp   = {sess.get_inputs()[0].name: np.array([features], dtype=np.float32)}
    #   out   = sess.run(None, inp)[0][0]
    #   label = NDVI_CLASSES[int(np.argmax(out))]
    #   confidence = float(out.max())
    # ─────────────────────────────────────────────────────────────────────────

    # ── STEP 3: Rule-based fallback (active until real model is plugged in) ──
    label, confidence = _rule_based_prediction(mean_ndvi)

    # ── STEP 4: Build response ────────────────────────────────────────────────
    advice = _ADVICE_DB.get(label, {})

    return {
        "prediction":  label,
        "confidence":  round(confidence, 2),
        "severity":    advice.get("severity", "Unknown"),
        "ndvi_health_score": round((mean_ndvi + 1) / 2 * 100, 1),   # 0-100 normalised score
        "recommendation": {
            "irrigation_needed":          advice.get("irrigation_needed", False),
            "irrigation_action":          advice.get("irrigation_recommendation", ""),
            "nutrient_action":            advice.get("nutrient_action", ""),
            "field_action":               advice.get("field_action", ""),
            "next_satellite_check_days":  advice.get("next_check_days", 7),
        }
    }
