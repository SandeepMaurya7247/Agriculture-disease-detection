import requests

def test_weather():
    # Trying both localhost and the specific IP
    urls = [
        "http://127.0.0.1:5000/api/weather/current",
        "http://10.91.148.102:5000/api/weather/current"
    ]
    for url in urls:
        print(f"Testing {url}...")
        try:
            r = requests.get(url, timeout=5)
            print(f"Status Code: {r.status_code}")
            if r.ok:
                print("Response:", r.json())
                return
            else:
                print("Error:", r.text)
        except Exception as e:
            print(f"Failed to connect to {url}: {e}")

if __name__ == "__main__":
    test_weather()
