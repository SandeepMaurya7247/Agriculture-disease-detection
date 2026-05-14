import os
from dotenv import load_dotenv
load_dotenv() # Load this FIRST

# Debug: Print keys (masked for safety) to verify they are loaded
print(f"DEBUG: GROQ_API_KEY loaded: {'Yes' if os.getenv('GROQ_API_KEY') else 'No'}")
print(f"DEBUG: TAVILY_API_KEY loaded: {'Yes' if os.getenv('TAVILY_API_KEY') else 'No'}")
print(f"DEBUG: WEATHER_API_KEY loaded: {'Yes' if os.getenv('WEATHER_API_KEY') else 'No'}")

import base64
import requests
import json
import io
import random
import uuid
import numpy as np
from datetime import datetime
import threading
from flask import Flask, request, jsonify, session
from flask_cors import CORS
from PIL import Image

# Import Custom Modules
from db.mongo_db import (
    get_user_by_email, create_user, save_report, save_stress_analysis
)
from services.cloudinary_service import upload_image
from services import sentinel_service                      # 🛰️ Sentinel Hub
from ml.ml import generate_report as get_fertilizer_report
from ml import crop_rec_ml as crop_ml
from ml import crop_health_ml                             # 🌾 Crop Health ML

# Global state for diagnostics
last_iot_data = {"status": "No data yet", "timestamp": None}

app = Flask(__name__)
CORS(app, resources={r"/api/*": {"origins": "*"}})
app.config['SECRET_KEY'] = os.getenv("SECRET_KEY", "agrotech-ai-key-2024")
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024

@app.before_request
def monitor_traffic():
    if "/api/iot/latest" not in request.path:
        print(f"DEBUG: [INCOMING] {request.method} {request.path} from {request.remote_addr}")

# Configuration
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY") 
GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"

class AgroBackend:
    @staticmethod
    def get_gemini_response(prompt, image_url=None):
        payload = {
            "contents": [{
                "parts": [{"text": prompt}]
            }],
            "generationConfig": {"temperature": 0.7}
        }
        
        if image_url:
            # If we have a Cloudinary URL, we can either download it or use Gemini's image part if supported via URL (usually Gemini prefers base64 or File API)
            # For simplicity, we'll fetch the image and convert to base64 if needed, 
            # or just rely on the fact that we already have the base64 from the request.
            pass

        try:
            r = requests.post(f"{GEMINI_URL}?key={GEMINI_API_KEY}", json=payload, timeout=30)
            if r.ok:
                return r.json()['candidates'][0]['content']['parts'][0]['text']
            return f"Error: {r.status_code} - {r.text}"
        except Exception as e:
            return str(e)

# ----------------------------
# 🔐 AUTH MODULE (MongoDB Atlas)
# ----------------------------

@app.route('/api/auth/login', methods=['POST'])
def login():
    data = request.json
    email = data.get('email')
    password = data.get('password')
    
    user = get_user_by_email(email)
    if user and user.get('password') == password:
        return jsonify({
            'success': True,
            'token': f"auth_token_{random.randint(1000, 9999)}",
            'user': {
                'id': str(user['_id']),
                'name': user.get('name'),
                'email': user.get('email')
            }
        })
    return jsonify({'success': False, 'error': 'Invalid credentials'}), 401

@app.route('/api/auth/signup', methods=['POST'])
def signup():
    data = request.json
    name = data.get('name')
    email = data.get('email')
    password = data.get('password')
    
    if not name or not email or not password:
        return jsonify({'success': False, 'error': 'Missing required fields'}), 400
        
    if get_user_by_email(email):
        return jsonify({'success': False, 'error': 'User already exists'}), 409
        
    user_data = {
        "name": name,
        "email": email,
        "password": password,
        "created_at": datetime.utcnow()
    }
    
    result = create_user(user_data)
    
    return jsonify({
        'success': True,
        'token': "new_user_token",
        'user': {'id': str(result.inserted_id), 'name': name, 'email': email}
    })

# ----------------------------
# ⛅ WEATHER MODULE
# ----------------------------

def get_weather_data(lat='28.6139', lon='77.2090'):
    api_key = os.getenv("WEATHER_API_KEY")
    if not api_key:
        return {"condition": "Unknown", "temperature": 0}
    
    url = f"https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={api_key}&units=metric"
    try:
        r = requests.get(url, timeout=5)
        if r.ok:
            data = r.json()
            return {
                "temperature": data['main']['temp'],
                "humidity": data['main']['humidity'],
                "condition": data['weather'][0]['main'],
                "windSpeed": data['wind']['speed'],
                "location": data.get('name', 'Bhopal')
            }
    except:
        pass
    return {"condition": "Mock Rain", "temperature": 28.5} # Fallback for demo

def send_alert(title, body):
    # This will be replaced by Real FCM Logic
    print(f"\n🔔 [NOTIFICATION ALERT] 🔔")
    print(f"Title: {title}")
    print(f"Body: {body}")
    print(f"Time: {datetime.utcnow()}\n")

def irrigation_decision(soil_moisture, temperature):
    # Thresholds from user code
    SOIL_DRY = 40
    SOIL_WET = 70
    HIGH_TEMP = 32

    if soil_moisture < SOIL_DRY and temperature > HIGH_TEMP:
        return "ON", "Soil is dry and temperature is high"
    elif soil_moisture < SOIL_DRY:
        return "ON", "Soil moisture is low"
    elif soil_moisture > SOIL_WET:
        return "OFF", "Soil has sufficient moisture"
    else:
        return "OFF", "Soil condition is normal"

from notification_service import notifier

def send_alert(title, body):
    # Sends to 'all_farmers' topic so all app users get it
    notifier.send_to_topic("all_farmers", title, body)
    print(f"Time: {datetime.utcnow()}\n")

@app.route('/api/weather/current', methods=['GET'])
def get_weather():
    lat = request.args.get('lat', '28.6139')
    lon = request.args.get('lon', '77.2090')
    data = get_weather_data(lat, lon)
    return jsonify(data)

# ----------------------------
# 🔌 IOT MODULE (Sensor Data)
# ----------------------------

@app.route("/api/iot/test", methods=["GET"])
def test_iot():
    return jsonify({
        "status": "active",
        "message": "AgroTech IoT Server is running",
        "server_ip": "10.189.210.102",
        "port": 5000,
        "endpoint": "/api/iot",
        "last_received": last_iot_data
    })

@app.route("/api/iot/latest", methods=["GET"])
def get_latest_iot():
    print(f"DEBUG: Mobile App Polling -> Current State: {last_iot_data}")
    return jsonify({
        "success": True,
        "data": last_iot_data
    })

@app.route("/api/iot/history", methods=["GET"])
def get_iot_history():
    return jsonify({
        "success": True,
        "history": iot_history_log
    })

@app.route("/iot/monitor")
def iot_monitor():
    log_html = "".join([f"<li>[{d['timestamp']}] Soil: {d['soil']}% | Temp: {d['temp']}°C -> {d['decision']}</li>" for d in reversed(iot_history_log)])
    return f"""
    <html>
        <head><title>IoT Traffic Monitor</title><meta http-equiv='refresh' content='2'></head>
        <body style='font-family:sans-serif; padding:40px; background:#f4f4f9;'>
            <h1 style='color:#2e7d32;'>AgroTech AI - Live IoT Monitor</h1>
            <p>Last 50 sensor requests received by this server:</p>
            <div style='background:white; padding:20px; border-radius:12px; box-shadow:0 4px 12px rgba(0,0,0,0.1);'>
                <ul>{log_html or "<li>No data received yet. Connect your sensor to http://10.189.210.102:5000/api/iot</li>"}</ul>
            </div>
            <p style='color:gray; font-size:12px;'>Page refreshes automatically every 10 seconds.</p>
        </body>
    </html>
    """

@app.route("/sensor-data", methods=["POST"], strict_slashes=False)
@app.route("/api/iot/data", methods=["POST"], strict_slashes=False)
@app.route("/api/iot", methods=["POST", "GET"], strict_slashes=False)
def receive_iot_data():
    global last_iot_data, iot_history_log
    try:
        # 1. Try to get data from multiple sources (JSON, Form, Args)
        if request.is_json:
            data = request.json
        elif request.form:
            data = request.form.to_dict()
        else:
            data = request.args.to_dict()
            
        if not data:
            return jsonify({"status": "error", "message": "No data found in request"}), 400
            
        # Normalize keys to lowercase for universal matching
        data_lower = {k.lower(): v for k, v in data.items()}
        
        # 2. Extract and Normalize (support user's specific keys)
        soil = float(data_lower.get("soil_moisture") or data_lower.get("soil") or data_lower.get("moisture") or 0)
        temp = float(data_lower.get("temperature") or data_lower.get("temp") or 0)
        humidity = float(data_lower.get("humidity") or 0)
        
        print(f"DEBUG: IoT Data Received -> Soil: {soil}, Temp: {temp} | Keys: {list(data.keys())}")

        # 3. Decision Logic
        if 0 < soil < 30:
            decision = "START IRRIGATION"
            send_alert("Soil is Dry!", f"Moisture is {soil}%. Please turn on the motor.")
        elif soil == 0:
            decision = "Waiting for Sensor..."
        else:
            decision = "NO IRRIGATION"

        # 4. Check Weather for Rain Alert
        try:
            weather_data = get_weather_data(28.6139, 77.2090) # Default coords, can be dynamic
            if "Rain" in weather_data.get('condition', ''):
                send_alert("Rain Alert!", "Rain is expected. You can stop manual irrigation.")
        except:
            pass

        # Update global state AFTER decision
        global last_iot_data
        last_iot_data = {
            "soil": soil,
            "temp": temp,
            "decision": decision,
            "timestamp": str(datetime.utcnow())
        }

        # Save to MongoDB for history in Background
        threading.Thread(target=save_report, args=({
            "type": "iot_sensor_data",
            "soil_moisture": soil,
            "temperature": temp,
            "decision": decision,
            "timestamp": datetime.utcnow()
        },)).start()

        return jsonify({
            "status": "received",
            "decision": decision,
            "soil": soil,
            "temp": temp,
            "timestamp": datetime.utcnow()
        })
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

# ----------------------------
# 🌾 CROP RECOMMENDATION (ML + MongoDB)
# ----------------------------

@app.route('/api/recommend/crop', methods=['POST'])
def recommend_crop():
    import time
    data = request.json
    n = data.get('n', 0)
    p = data.get('p', 0)
    k = data.get('k', 0)
    temp = data.get('temp', 25.0)
    humidity = data.get('humidity', 70.0)
    ph = data.get('ph', 6.5)
    rainfall = data.get('rainfall', 100.0)
    lang = data.get('lang', 'en')
    
    print(f"\n{'='*60}")
    print(f"CROP REC REQUEST: N={n}, P={p}, K={k}, T={temp}, H={humidity}, pH={ph}, R={rainfall}")
    
    try:
        t1 = time.time()
        print(f"[STEP 1] Running predict_crop_with_lime...")
        input_data = {"N": n, "P": p, "K": k, "temperature": temp, "humidity": humidity, "ph": ph, "rainfall": rainfall}
        crop, lime_output = crop_ml.predict_crop_with_lime(input_data)
        t2 = time.time()
        print(f"[STEP 1 DONE] Crop={crop}, LIME items={len(lime_output)}, Time={t2-t1:.1f}s")
        
        print(f"[STEP 2] Running Groq AI explanation...")
        expert_explanation = crop_ml.final_crop_explaination(crop, lime_output, lang=lang)
        t3 = time.time()
        print(f"[STEP 2 DONE] Expert len={len(str(expert_explanation))}, Time={t3-t2:.1f}s")
        print(f"[STEP 2 PREVIEW] {str(expert_explanation)[:200]}...")
        
        threading.Thread(target=save_report, args=({"type": "crop_recommendation", "input": data, "result": {"crop": crop}, "timestamp": datetime.utcnow()},)).start()
        
        response = {
            "success": True,
            "recommendation": crop,
            "accuracy": "99.3%",
            "why_this_crop": lime_output,
            "expert_explanation": expert_explanation,
            "details": f"{crop} is highly recommended for these conditions."
        }
        print(f"[RESPONSE] Keys={list(response.keys())}, why_count={len(lime_output)}, expert_len={len(str(expert_explanation))}")
        return jsonify(response)
        
    except Exception as e:
        import traceback
        print(f"[CROP REC ERROR] {e}")
        traceback.print_exc()
        return jsonify({"success": True, "recommendation": "Error", "accuracy": "0%", "why_this_crop": [], "expert_explanation": f"Error: {str(e)}", "details": str(e)})

# ----------------------------
# 🧪 FERTILIZER RECOMMENDATION (ML + MongoDB)
# ----------------------------

@app.route('/api/recommend/fertilizer', methods=['POST'])
def recommend_fertilizer():
    data = request.json
    crop = data.get('crop_type', 'Wheat')
    n = data.get('n', 0)
    p = data.get('p', 0)
    k = data.get('k', 0)
    moisture = data.get('moisture', 20)
    temp = data.get('temp', 25)
    humidity = data.get('humidity', 60)
    soil_type = data.get('soil_type', 'Loamy')
    lang = data.get('lang', 'en')

    # Use real ML model
    report = get_fertilizer_report(crop, n, p, k, moisture, temp, humidity, soil_type, lang=lang)
    
    # Save to MongoDB in Background (Turbo Boost)
    import threading
    threading.Thread(target=save_report, args=({
        "type": "fertilizer_recommendation",
        "input": data,
        "result": report,
        "timestamp": datetime.utcnow()
    },)).start()
        
    return jsonify({
        "success": True,
        "recommendation": report.get("Recommended Fertilizer", "NPK 19-19-19"),
        "accuracy": report.get("Accuracy", "98.7%"),
        "why_this_fertilizer": report.get("Why this fertilizer?", []),
        "expert_explanation": report.get("Expert Agricultural Explanation", ""),
        "deficiency": report.get("Nutrient Deficiency", {}),
        "schedule": report.get("Application Schedule", []),
        "details": f"Optimized for {soil_type} and {crop}."
    })

# ----------------------------
# 🔮 FUTURE RECOMMENDATION (Weather Estimation + ML)
# ----------------------------

def estimate_future_weather(base_days, total_days):
    """
    Estimates future weather based on real forecast averages
    """
    if not base_days:
        return []
        
    avg_temp = np.mean([d.get("temp", {}).get("day", 25) for d in base_days])
    avg_rain = np.mean([d.get("rain", 0) for d in base_days])
    avg_humidity = np.mean([d.get("humidity", 60) for d in base_days])

    estimated_days = []
    for i in range(total_days - len(base_days)):
        estimated_days.append({
            "temp": {"day": float(avg_temp + np.random.uniform(-1.5, 1.5))},
            "rain": float(max(0, avg_rain + np.random.uniform(-2, 2))),
            "humidity": float(max(0, min(100, avg_humidity + np.random.uniform(-5, 5))))
        })
    return estimated_days

@app.route('/api/recommend/future', methods=['GET'])
def recommend_future_crop():
    lat = request.args.get('lat', '23.2599')
    lon = request.args.get('lon', '77.4126')
    days = int(request.args.get('days', 30))
    lang = request.args.get('lang', 'en')
    
    # User provided soil data
    user_n = request.args.get('n', default=50, type=float)
    user_p = request.args.get('p', default=50, type=float)
    user_k = request.args.get('k', default=50, type=float)
    user_ph = request.args.get('ph', default=6.5, type=float)

    api_key = os.getenv("WEATHER_API_KEY")

    if not api_key:
        return jsonify({"success": False, "error": "Weather API Key is missing"}), 500

    # 1. Fetch real forecast (usually 5 days / 3 hours)
    url = f"https://api.openweathermap.org/data/2.5/forecast?lat={lat}&lon={lon}&appid={api_key}&units=metric"
    
    try:
        r = requests.get(url, timeout=10)
        if r.ok:
            data = r.json()
            daily_data = {}
            for item in data['list']:
                date = item['dt_txt'].split(' ')[0]
                if date not in daily_data:
                    daily_data[date] = {"temps": [], "hums": [], "rains": []}
                daily_data[date]["temps"].append(item['main']['temp'])
                daily_data[date]["hums"].append(item['main']['humidity'])
                daily_data[date]["rains"].append(item.get('rain', {}).get('3h', 0))
            
            base_days = []
            for date, vals in daily_data.items():
                base_days.append({
                    "temp": {"day": np.mean(vals["temps"])},
                    "humidity": np.mean(vals["hums"]),
                    "rain": sum(vals["rains"])
                })
            
            # 2. Estimate up to 30 or 60 days
            estimated = estimate_future_weather(base_days, days)
            final_weather = base_days + estimated
            
            # 3. Calculate Averages for ML Input
            avg_temp = np.mean([d["temp"]["day"] for d in final_weather])
            avg_hum = np.mean([d["humidity"] for d in final_weather])
            avg_rain = np.mean([d.get("rain", 0) for d in final_weather])
            
            # 4. Call Crop Recommendation ML (same pipeline as crop rec)
            import time
            t1 = time.time()
            print(f"[FUTURE] Running LIME + AI for future recommendation...")
            input_data = {"N": user_n, "P": user_p, "K": user_k, "temperature": avg_temp, "humidity": avg_hum, "ph": user_ph, "rainfall": avg_rain * days}
            crop, lime_output = crop_ml.predict_crop_with_lime(input_data)
            t2 = time.time()
            print(f"[FUTURE] Crop={crop}, LIME items={len(lime_output)}, Time={t2-t1:.1f}s")
            
            expert_explanation = crop_ml.final_crop_explaination(crop, lime_output, lang=lang)
            t3 = time.time()
            print(f"[FUTURE] Expert len={len(str(expert_explanation))}, Time={t3-t2:.1f}s")

            return jsonify({
                "success": True,
                "recommendation": crop,
                "accuracy": "99.3% (Estimated)",
                "why_this_crop": lime_output,
                "expert_explanation": expert_explanation,
                "reasons": [f"{item['feature']} (impact: {item['impact']})" for item in lime_output],
                "weather_summary": {
                    "avg_temp": float(avg_temp),
                    "avg_humidity": float(avg_hum),
                    "total_rainfall": float(avg_rain * days),
                    "days": float(days)
                }
            })
        else:
            return jsonify({"success": False, "error": f"Weather API error: {r.text}"}), r.status_code
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

import numpy as np
import onnxruntime as ort

def get_expert_advice(disease, lang='en'):
    # Ultra-detailed expert guide database
    advice_db = {
        "en": {
            "Bacterial Leaf Blight": "🔍 **Detailed Symptoms (Pehchan)**:\n• Initially, water-soaked streaks appear on leaf blades which eventually turn yellow-orange.\n• These streaks start from the tips and move down along the edges, forming wavy margins.\n• In severe cases, the entire leaf may dry up and turn straw-colored, significantly reducing yield.\n\n💊 **Advanced Chemical Treatment (Rasayanik)**:\n• **Spray 1**: Mix 1.5g of Streptocycline and 25g of Copper Oxychloride in 10 liters of clean water. Spray thoroughly on both sides of leaves.\n• **Spray 2**: If the disease persists after 12-15 days, apply Kocide (Copper Hydroxide) at 2g per liter of water.\n• Avoid spraying during high winds or rain; ensure the nozzle is fine for maximum coverage.\n\n🍃 **Comprehensive Organic Treatment (Jaivik)**:\n• **NSKE 5%**: Boil 5kg of Neem seed powder in 10L water overnight, filter it, and dilute in 100L water for spraying.\n• **Cow Dung Slurry**: Mix 1kg of fresh cow dung in 10L of water, filter it twice through a fine cloth, and spray to boost plant immunity.\n• Apply Trichoderma viride enriched farmyard manure to the soil.\n\n🛡️ **Expert Prevention (Bachav)**:\n• **Nutrient Management**: Reduce Nitrogen (Urea) and increase Potassium (K) to strengthen leaf cell walls.\n• **Water Management**: Ensure the field is not continuously flooded; follow 'Alternate Wetting and Drying' (AWD).\n• **Sanitation**: Remove weeds and infected straw from the previous season to prevent the bacteria from overwintering.",
            "Brown Spot": "🔍 **Detailed Symptoms (Pehchan)**:\n• Small, circular to oval, dark brown spots with a prominent yellow halo appear on leaves.\n• On older leaves, these spots expand and their centers turn light brown or gray.\n• Can lead to 'Grain Discoloration' if the infection reaches the panicles, reducing grain quality.\n\n💊 **Advanced Chemical Treatment (Rasayanik)**:\n• **Option 1**: Spray Mancozeb 75 WP (2.5g/L) or Zineb (2g/L) as soon as spots appear.\n• **Option 2**: For systemic control, use Hexaconazole 5% EC (2ml/L) or Propiconazole 25 EC (1ml/L).\n• Always use a 'Spreader/Sticker' agent during the monsoon to prevent the chemical from washing off.\n\n🍃 **Comprehensive Organic Treatment (Jaivik)**:\n• **Cow Urine**: Mix 1 liter of well-fermented cow urine with 10 liters of water and spray every 10 days.\n• **Seed Treatment**: Soak seeds in a solution of Pseudomonas fluorescens (10g/kg) before sowing.\n• Spraying Vermicompost wash provides essential micronutrients and suppressive microbes.\n\n🛡️ **Expert Prevention (Bachav)**:\n• **Soil Health**: Apply balanced fertilizers based on a soil test; specifically address Zinc or Manganese deficiencies.\n• **Water Management**: Avoid water stress (drought) as weakened plants are more susceptible to Brown Spot.\n• **Variety**: Always plant certified, disease-resistant seeds from reliable sources.",
            "Healthy Leaf": "✅ **Health Status (Sthiti)**:\n• Your crop is currently in excellent health with no visible signs of fungal or bacterial infection.\n• Leaf color, texture, and turgidity are within the optimal range for this growth stage.\n\n🛡️ **Professional Maintenance Tips**:\n• **Monitoring**: Continue scouting the field twice a week, checking the lower canopy for early signs.\n• **Nutrition**: Apply the next dose of top-dressed Nitrogen only if required by the Leaf Color Chart (LCC).\n• **Bio-Stimulants**: You may spray Seaweed Extract (2ml/L) to enhance the crop's natural defense mechanism.",
            "Leaf Smut": "🔍 **Detailed Symptoms (Pehchan)**:\n• Small, black, slightly raised spots (sori) appear on leaves.\n• These spots are often linear and follow the leaf veins.\n• Infected leaves may turn yellow and dry prematurely.\n\n💊 **Advanced Chemical Treatment (Rasayanik)**:\n• **Spray**: Apply Propiconazole 25 EC (1ml/L) or Hexaconazole 5% EC (2ml/L).\n• Ensure thorough coverage of the foliage.\n\n🍃 **Comprehensive Organic Treatment (Jaivik)**:\n• **Seed Treatment**: Treat seeds with Pseudomonas fluorescens (10g/kg).\n• **Crop Rotation**: Avoid growing rice in the same field for consecutive seasons.\n\n🛡️ **Expert Prevention (Bachav)**:\n• **Sanitation**: Burn infected crop debris after harvest.\n• **Balanced Nutrition**: Avoid excessive Nitrogen application."
        },
        "hi": {
            "Bacterial Leaf Blight": "🔍 **विस्तृत लक्षण (Pehchan)**:\n• शुरुआती चरण में, पत्तियों पर पानी से भीगी हुई धारियां दिखाई देती हैं जो बाद में पीली-नारंगी हो जाती हैं।\n• ये धारियां नोकों से शुरू होती हैं और किनारों के साथ नीचे की ओर बढ़ती हैं, जिससे लहरदार किनारे बन जाते हैं।\n• गंभीर मामलों में, पूरी पत्ती सूख कर पुआल के रंग की हो सकती है, जिससे पैदावार काफी कम हो जाती है।\n\n💊 **उन्नत रासायनिक उपचार (Rasayanik)**:\n• **स्प्रे 1**: 10 लीटर साफ पानी में 1.5 ग्राम स्ट्रेप्टोसायक्लिन और 25 ग्राम कॉपर ऑक्सीक्लोराइड मिलाएं। पत्तियों के दोनों तरफ अच्छी तरह छिड़काव करें।\n• **स्प्रे 2**: यदि 12-15 दिनों के बाद भी बीमारी बनी रहती है, तो कोसाइड (कॉपर हाइड्रोक्साइड) का 2 ग्राम प्रति लीटर पानी में प्रयोग करें।\n• तेज हवा या बारिश के दौरान स्प्रे करने से बचें; सुनिश्चित करें कि नोजल बारीक हो।\n\n🍃 **व्यापक जैविक उपचार (Jaivik)**:\n• **नीम अर्क 5%**: 5 किलो नीम के बीज के पाउडर को रात भर 10 लीटर पानी में उबालें, इसे छान लें और छिड़काव के लिए 100 लीटर पानी में घोलें।\n• **गोबर का घोल**: 1 किलो ताज़ा गोबर को 10 लीटर पानी में मिलाएं, इसे दो बार बारीक कपड़े से छान लें, और छिड़काव करें।\n• मिट्टी में ट्राइकोडर्मा विरिडी से समृद्ध खाद डालें।\n\n🛡️ **विशेषज्ञ बचाव (Bachav)**:\n• **पोषक तत्व प्रबंधन**: नाइट्रोजन (यूरिया) कम करें और पोटाश (K) बढ़ाएं ताकि पत्तियों की दीवारें मजबूत हों।\n• **जल प्रबंधन**: सुनिश्चित करें कि खेत में लगातार पानी न भरा रहे; 'वैकल्पिक गीला और सुखाने' (AWD) की विधि अपनाएं।\n• **सफाई**: बैक्टीरिया को पनपने से रोकने के लिए पिछली फसल के अवशेषों को हटा दें।",
            "Brown Spot": "🔍 **विस्तृत लक्षण (Pehchan)**:\n• पत्तियों पर छोटे, गोल से अंडाकार, गहरे भूरे रंग के धब्बे दिखाई देते हैं जिनके चारों ओर पीला घेरा होता है।\n• पुरानी पत्तियों पर, ये धब्बे फैल जाते हैं और उनके केंद्र हल्के भूरे या धूसर हो जाते हैं।\n• यदि संक्रमण बालियों तक पहुँचता है, तो यह 'दानों के रंग बिगाड़ने' का कारण बन सकता है।\n\n💊 **उन्नत रासायनिक उपचार (Rasayanik)**:\n• **विकल्प 1**: धब्बे दिखाई देते ही मैंकोजेब 75 WP (2.5 ग्राम/लीटर) या ज़िनेब (2 ग्राम/लीटर) का छिड़काव करें।\n• **विकल्प 2**: व्यवस्थित नियंत्रण के लिए, हेक्साकोनाजोल 5% EC (2ml/L) या प्रोपिकोनाजोल 25 EC (1ml/L) का उपयोग करें।\n• मानसून के दौरान दवा को धुलने से बचाने के लिए हमेशा 'स्टिकर' का उपयोग करें।\n\n🍃 **व्यापक जैविक उपचार (Jaivik)**:\n• **गौमूत्र**: 1 लीटर अच्छी तरह सड़े हुए गौमूत्र को 10 लीटर पानी में मिलाएं और हर 10 दिन में छिड़काव करें।\n• **बीज उपचार**: बुवाई से पहले बीजों को स्यूडोमोनास फ्लोरेसेंस (10 ग्राम/किलो) के घोल में भिगो दें।\n• वर्मीकम्पोस्ट वॉश का छिड़काव सूक्ष्म पोषक तत्व और सुरक्षात्मक रोगाणु प्रदान करता है।\n\n🛡️ **विशेषज्ञ बचाव (Bachav)**:\n• **मिट्टी का स्वास्थ्य**: मिट्टी परीक्षण के आधार पर संतुलित उर्वरक डालें; विशेष रूप से जिंक या मैंगनीज की कमी को दूर करें।\n• **जल प्रबंधन**: जल तनाव (सूखा) से बचें क्योंकि कमजोर पौधे भूरे धब्बे के प्रति अधिक संवेदनशील होते हैं।\n• **बीज**: हमेशा विश्वसनीय स्रोतों से प्रमाणित, रोग-प्रतिरोधी बीज ही लगाएं।",
            "Healthy Leaf": "✅ **स्वास्थ्य स्थिति (Sthiti)**:\n• आपकी फसल वर्तमान में उत्कृष्ट स्वास्थ्य में है और कवक या जीवाणु संक्रमण का कोई दृश्य संकेत नहीं है।\n• पत्तियों का रंग, बनावट और मजबूती इस विकास चरण के लिए इष्टतम सीमा के भीतर हैं।\n\n🛡️ **पेशेवर रखरखाव युक्तियाँ**:\n• **निगरानी**: सप्ताह में दो बार खेत का निरीक्षण जारी रखें, शुरुआती संकेतों के लिए निचले हिस्से की जांच करें।\n• **पोषण**: यूरिया की अगली खुराक केवल तभी डालें जब लीफ कलर चार्ट (LCC) द्वारा आवश्यक हो।\n• **बायो-स्टिमुलेंट्स**: आप फसल की प्राकृतिक रक्षा प्रणाली को बढ़ाने के लिए समुद्री शैवाल के अर्क (2ml/L) का छिड़काव कर सकते हैं।",
            "Leaf Smut": "🔍 **विस्तृत लक्षण (Pehchan)**:\n• पत्तियों पर छोटे, काले, थोड़े उभरे हुए धब्बे (sori) दिखाई देते हैं।\n• ये धब्बे अक्सर रेखीय होते हैं और पत्ती की शिराओं का अनुसरण करते हैं।\n• संक्रमित पत्तियां पीली पड़ सकती हैं और समय से पहले सूख सकती हैं।\n\n💊 **उन्नत रासायनिक उपचार (Rasayanik)**:\n• **छिड़काव**: प्रोपिकोनाजोल 25 EC (1ml/L) या हेक्साकोनाजोल 5% EC (2ml/L) का प्रयोग करें।\n• सुनिश्चित करें कि पत्तियों का हर हिस्सा दवा से ढक जाए।\n\n🍃 **व्यापक जैविक उपचार (Jaivik)**:\n• **बीज उपचार**: बुवाई से पहले बीजों को स्यूडोमोनास फ्लोरेसेंस (10 ग्राम/किलो) से उपचारित करें।\n• **फसल चक्र**: एक ही खेत में लगातार धान न लगाएं।\n\n🛡️ **विशेषज्ञ बचाव (Bachav)**:\n• **सफाई**: कटाई के बाद संक्रमित फसल के अवशेषों को जला दें।\n• **संतुलित पोषण**: यूरिया का अत्यधिक उपयोग न करें।"
        }
    }
    selected_db = advice_db.get(lang, advice_db['en'])
    return selected_db.get(disease, "🔍 Analyzing symptoms for expert diagnosis...")

def analyze_with_gemini(image_base64, lang='en'):
    """Objective expert analysis - No false positives for healthy leaves"""
    lang_map = {'hi': 'Hindi', 'en': 'English', 'mr': 'Marathi', 'pa': 'Punjabi', 'gu': 'Gujarati'}
    lang_name = lang_map.get(lang, 'English')
    
    prompt = f"""CRITICAL INSTRUCTION: You are an objective Senior Agronomist. 
    First, determine if the leaf in the image is truly HEALTHY or STRESSED.
    
    - IF THE LEAF IS HEALTHY: Do not invent diseases. Label it as 'Healthy Leaf'. 
      Provide a confidence score near 0.99. 
      In 'treatment', just give simple maintenance tips.
    
    - IF AND ONLY IF THERE ARE CLEAR SIGNS OF DISEASE: Provide a comprehensive report. 
      Ignore lighting artifacts, shadows, or water drops as diseases.

    Structure for Stressed Leaves (ONLY if stressed):
    🔍 Detailed Symptoms (Pehchan): (3-5 bullet points)
    💊 Advanced Chemical Treatment (Rasayanik): (Specific chemicals & dosages)
    🍃 Comprehensive Organic Treatment (Jaivik): (Preparation & application)
    🛡️ Expert Prevention & Management (Bachav): (Future tips)

    Label: (Scientific & Common Name)
    Confidence: (0.1 - 1.0)
    
    Respond ONLY in {lang_name} language.
    Format your response in JSON: {{"label": "...", "confidence": 0.99, "treatment": "..."}}"""
    
    payload = {
        "contents": [{
            "parts": [
                {"text": prompt},
                {"inline_data": {"mime_type": "image/jpeg", "data": image_base64}}
            ]
        }]
    }
    
    try:
        print(f"DEBUG: Gemini API Requesting... URL: {GEMINI_URL[:30]}...")
        r = requests.post(f"{GEMINI_URL}?key={GEMINI_API_KEY}", json=payload, timeout=30)
        if r.ok:
            data = r.json()
            print("DEBUG: Gemini API Response OK")
            text = data['candidates'][0]['content']['parts'][0]['text']
            clean_json = text.replace("```json", "").replace("```", "").strip()
            return json.loads(clean_json)
        else:
            print(f"DEBUG: Gemini API Error ({r.status_code}): {r.text}")
    except Exception as e:
        print(f"DEBUG: Gemini Exception: {e}")
    return None

@app.route('/api/detect/stress', methods=['POST'])
def detect_stress():
    data = request.json
    image_base64 = data.get('image')
    lang = data.get('lang', 'en')
    
    if not image_base64:
        return jsonify({"success": False, "error": "No image data"}), 400
    
    source = "Local AI"
    try:
        # 1. PRIORITY: Try Local Analysis FIRST
        model_path = os.path.join(os.path.dirname(__file__), 'ml', 'model', 'crop_stress_mobilenet.onnx')
        if os.path.exists(model_path):
            session = ort.InferenceSession(model_path)
            img_data = base64.b64decode(image_base64)
            img = Image.open(io.BytesIO(img_data)).resize((224, 224)).convert('RGB')
            img_array = np.array(img, dtype=np.float32) / 255.0
            img_array = np.expand_dims(img_array, axis=0)
            
            input_name = session.get_inputs()[0].name
            output_name = session.get_outputs()[0].name
            output_data = session.run([output_name], {input_name: img_array})[0]
            
            classes = ["Bacterial Leaf Blight", "Brown Spot", "Healthy Leaf", "Leaf Smut", "Other Disease"]
            class_idx = np.argmax(output_data[0])
            confidence = float(output_data[0][class_idx])
            disease = classes[class_idx]
            treatment = get_expert_advice(disease, lang)
            source = "AgroTech Local AI"

            # 2. HYBRID LOGIC: Automatic Gemini Fallback for low confidence
            if confidence < 0.50 or disease == "Other Disease":
                print(f"DEBUG: Local AI unsure ({confidence:.2f}), calling Gemini...")
                gemini_result = analyze_with_gemini(image_base64, lang)
                if gemini_result:
                    disease = gemini_result.get("label", disease)
                    confidence = gemini_result.get("confidence", confidence)
                    treatment = gemini_result.get("treatment", treatment)
                    source = "AgroTeck Gemini AI (Auto-Fallback)"
                else:
                    source = "AgroTeck Local AI (Uncertain)"

        print(f"DEBUG: [FINAL DIAGNOSIS] Result: {disease} | Source: {source} | Conf: {confidence:.2f}")

    except Exception as e:
        print(f"DEBUG: AI Analysis Error: {e}")
        # Try Gemini even on local error
        try:
            gemini_result = analyze_with_gemini(image_base64, lang)
            if gemini_result:
                disease = gemini_result.get("label", "Gemini Analysis")
                confidence = gemini_result.get("confidence", 0.95)
                treatment = gemini_result.get("treatment", "Expert advice generated.")
                source = "AgroTeck Gemini AI (Error Recovery)"
            else:
                raise e
        except:
            disease = "Analysis Error"
            confidence = 0.0
            treatment = "Could not analyze the leaf. Please ensure the photo is clear."
            source = "Local AI (Error)"

    # 3. Cloudinary Upload
    image_url = ""
    try:
        image_url = upload_image(image_base64)
    except: pass

    # 4. Save to MongoDB
    try:
        save_stress_analysis({
            "image_url": image_url,
            "label": disease,
            "confidence": confidence,
            "treatment": treatment,
            "source": source,
            "timestamp": datetime.utcnow()
        })
    except Exception as e:
        print(f"DEBUG: MongoDB Save Warning: {e}")
    
    return jsonify({
        "success": True,
        "label": disease,
        "confidence": confidence,
        "treatment": treatment,
        "image_url": image_url
    })

# ----------------------------
# 🛰️ SATELLITE PRECISION AGRICULTURE MODULE (Sentinel Hub + NDVI + ML)
# ----------------------------

@app.route('/api/analyze-crop', methods=['POST'])
def analyze_crop():
    """
    Precision Agriculture Pipeline
    ================================
    POST /api/analyze-crop
    """
    data = request.json
    if not data:
        return jsonify({"success": False, "error": "Request body must be JSON."}), 400

    # ── 1. Validate & extract inputs ─────────────────────────────────────────
    try:
        lat    = float(data["latitude"])
        lon    = float(data["longitude"])
        radius = float(data["radius"])
    except (KeyError, TypeError, ValueError) as e:
        return jsonify({
            "success": False,
            "error":   f"Missing or invalid field: {e}. Provide 'latitude', 'longitude', and 'radius'."
        }), 400

    # Optional extra sensor / weather data for the ML model
    extra_inputs = {k: v for k, v in {
        "temperature": data.get("temperature"),
        "humidity":    data.get("humidity"),
        "rainfall":    data.get("rainfall"),
        "soil_ph":     data.get("soil_ph"),
    }.items() if v is not None}

    # ── 2. Fetch NDVI stats (live Sentinel Hub OR demo simulation) ────────────
    try:
        ndvi_stats = sentinel_service.get_ndvi_stats(lat, lon, radius)
        bbox = ndvi_stats.pop("bbox", [])        # pull bbox out of stats dict
        ndvi_stats.pop("demo_mode", None)        # strip internal flag
        print(f"DEBUG [analyze-crop]: NDVI Stats = {ndvi_stats}")
    except ValueError as e:
        return jsonify({"success": False, "error": str(e)}), 422
    except RuntimeError as e:
        print(f"ERROR [analyze-crop]: Satellite fetch failed: {e}")
        return jsonify({"success": False, "error": f"Satellite data error: {e}"}), 502
    except Exception as e:
        print(f"ERROR [analyze-crop]: Unexpected NDVI error: {e}")
        return jsonify({"success": False, "error": f"NDVI processing error: {e}"}), 500

    # ── 3. Run ML model prediction ────────────────────────────────────────────
    try:
        ml_result = crop_health_ml.predict(ndvi_stats, extra_inputs or None)
        print(f"DEBUG [analyze-crop]: ML Result = {ml_result['prediction']} (conf={ml_result['confidence']})")
    except Exception as e:
        print(f"ERROR [analyze-crop]: ML prediction failed: {e}")
        return jsonify({"success": False, "error": f"ML model error: {e}"}), 500

    # ── 4. Persist the analysis result to MongoDB (background thread) ─────────
    threading.Thread(target=save_report, args=({
        "type":        "satellite_crop_analysis",
        "input":       {"latitude": lat, "longitude": lon, "radius_m": radius},
        "bbox":         bbox,
        "ndvi_stats":   ndvi_stats,
        "ml_result":    ml_result,
        "timestamp":    datetime.utcnow()
    },)).start()

    # ── 5. Return the full result ─────────────────────────────────────────────
    return jsonify({
        "success":   True,
        "location": {
            "latitude":  lat,
            "longitude": lon,
            "radius_m":  radius,
            "bbox":      {"min_lon": bbox[0], "min_lat": bbox[1], "max_lon": bbox[2], "max_lat": bbox[3]} if bbox else {}
        },
        "ndvi_stats":        ndvi_stats,
        "prediction":        ml_result["prediction"],
        "confidence":        ml_result["confidence"],
        "severity":          ml_result["severity"],
        "ndvi_health_score": ml_result["ndvi_health_score"],
        "recommendation":    ml_result["recommendation"]
    })

# ----------------------------
# 💬 ADVANCED CHATBOT MODULE (Tavily + Groq + LangGraph)
# ----------------------------
from tavily import TavilyClient
from langchain_groq import ChatGroq
from langgraph.checkpoint.memory import InMemorySaver
from langgraph.prebuilt import create_react_agent
from langchain_core.messages import HumanMessage
import uuid

# Tavily Tool
try:
    TAVILY_API_KEY = os.getenv("TAVILY_API_KEY")
    tavily_client = TavilyClient(api_key=TAVILY_API_KEY)
except Exception as e:
    print(f"Warning: Tavily initialization failed: {e}")
    tavily_client = None

def web_search(query: str):
    """Search agriculture-related information"""
    if not tavily_client:
        return "Search tool is currently unavailable."
    return tavily_client.search(query)

# System Prompt
Base_prompt = """
You are an expert agriculture assistant with a PREMIUM and PROFESSIONAL style.
ONLY answer questions related to agriculture, farming, crops, livestock, and related rural technologies.
If the user asks about anything else, politely refuse and say you are specialized in agriculture.

STYLE GUIDELINES:
1. Use EMOJIS (🌱, 🚜, 🌾, ☀️) to make the response engaging.
2. Use BOLD text for important terms and crop names.
3. Use bullet points for clear, scannable advice.
4. Keep the tone helpful, modern, and expert.
5. NEVER show technical tags like <web_search> or JSON code to the user.
6. Always prioritize practical and region-aware advice.

If you need current information, use the web_search tool, but integrate the results seamlessly into your professional answer.
"""

# Global Agent Placeholder
agent_executor = None

def get_chatbot_agent():
    global agent_executor
    if agent_executor is not None:
        return agent_executor
        
    try:
        print("DEBUG: Chatbot Init - Starting...")
        from langchain_groq import ChatGroq
        from langgraph.prebuilt import create_react_agent
        from langgraph.checkpoint.memory import MemorySaver
        print("DEBUG: Chatbot Init - Imports OK")
        
        groq_key = os.getenv("GROQ_API_KEY")
        if not groq_key:
            return None
            
        llm = ChatGroq(
            model="llama-3.1-8b-instant",
            temperature=0.4,
            api_key=groq_key
        )
        
        memory = MemorySaver()
        
        agent_executor = create_react_agent(
            model=llm,
            tools=[web_search],
            prompt=Base_prompt,
            checkpointer=memory
        )
        return agent_executor
    except Exception as e:
        print(f"DEBUG: Chatbot Init - FATAL ERROR: {e}")
        import traceback
        traceback.print_exc()
        return None

@app.route('/', methods=['GET'])
def root():
    return "AgroTech AI Backend is LIVE"

@app.route('/api/chat/test', methods=['GET'])
def chat_test():
    return jsonify({"response": "Connection Test Successful (GET)"})

@app.route('/api/ai/ask', methods=['POST'], strict_slashes=False)
def chat_query():
    data = request.json
    if not data:
        return jsonify({"error": "Invalid JSON"}), 400
        
    query = data.get('query')
    lang = data.get('lang', 'en')
    thread_id = data.get('thread_id', str(uuid.uuid4()))
    
    if not query:
        return jsonify({"error": "Query is required"}), 400
        
    executor = get_chatbot_agent()
    if not executor:
        return jsonify({"error": "Chatbot is currently offline (Check API Keys)"}), 503

    # Add language instruction
    if lang.lower() == 'hi':
        query = f"{query} (Please respond in Hindi only)"
    else:
        query = f"{query} (Please respond in English)"

    config = {"configurable": {"thread_id": thread_id}}
    
    try:
        # Run the agent using the local executor instance
        response = executor.invoke(
            {"messages": [HumanMessage(content=query)]},
            config=config
        )
        
        # Robust message extraction
        messages = response.get("messages", [])
        if not messages:
            return jsonify({"error": "No response from AI"}), 500
            
        final_message = messages[-1].content
        
        # CLEANING: Remove any <web_search> tags or similar technical noise
        import re
        final_message = re.sub(r'<web_search>.*?</web_search>', '', final_message, flags=re.DOTALL).strip()
        final_message = re.sub(r'\{"query":.*?\}', '', final_message).strip()
        
        if not final_message:
            # Fallback if content is empty (sometimes happens with tool results)
            final_message = "I'm sorry, I couldn't process that query. Please try again."
            
        print(f"DEBUG: chat_query - SUCCESS. Sending: {final_message[:50]}...")
        
        return jsonify({
            "response": str(final_message),
            "thread_id": thread_id
        })
    except Exception as e:
        print(f"DEBUG: chat_query - Primary Agent Error: {e}")
        # FALLBACK TO GEMINI
        try:
            print("DEBUG: chat_query - Attempting Gemini Fallback...")
            gemini_response = AgroBackend.get_gemini_response(query)
            if gemini_response:
                print("DEBUG: chat_query - Gemini Fallback SUCCESS")
                return jsonify({
                    "response": gemini_response,
                    "thread_id": thread_id
                })
        except Exception as ge:
            print(f"DEBUG: chat_query - Gemini Fallback FAILED: {ge}")
            
        import traceback
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    print("AgroTech AI Cloud-Enabled Backend starting...")
    # Use the port assigned by Render, or 5000 for local testing
    port = int(os.environ.get("PORT", 5000))
    app.run(debug=True, host='0.0.0.0', port=port)
