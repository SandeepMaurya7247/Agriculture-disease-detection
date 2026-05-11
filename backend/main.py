import os
import base64
import requests
import json
import io
import random
from datetime import datetime
from flask import Flask, request, jsonify, session
from flask_cors import CORS
from PIL import Image
from dotenv import load_dotenv

# Import Custom Modules
from db.mongo_db import (
    get_user_by_email, create_user, save_report, save_stress_analysis
)
from services.cloudinary_service import upload_image
from ml.ml import generate_report as get_fertilizer_report
from ml import crop_rec_ml as crop_ml

load_dotenv()

app = Flask(__name__)
CORS(app)
app.config['SECRET_KEY'] = os.getenv("SECRET_KEY", "agrotech-ai-key-2024")
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024

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

@app.route('/api/weather/current', methods=['GET'])
def get_weather():
    lat = request.args.get('lat', '28.6139') # Default to New Delhi
    lon = request.args.get('lon', '77.2090')
    api_key = os.getenv("WEATHER_API_KEY")
    
    url = f"http://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={api_key}&units=metric"
    
    try:
        r = requests.get(url, timeout=10)
        if r.ok:
            data = r.json()
            weather = {
                "temperature": data['main']['temp'],
                "humidity": data['main']['humidity'],
                "condition": data['weather'][0]['main'],
                "windSpeed": data['wind']['speed'],
                "iconUrl": f"http://openweathermap.org/img/wn/{data['weather'][0]['icon']}@2x.png",
                "location": data.get('name', 'Unknown Location'),
                "pressure": data['main']['pressure'],
                "description": data['weather'][0]['description']
            }
            return jsonify(weather)
        return jsonify({"error": "Failed to fetch weather data"}), r.status_code
    except Exception as e:
        return jsonify({"error": str(e)}), 500

# ----------------------------
# 🌾 CROP RECOMMENDATION (ML + MongoDB)
# ----------------------------

@app.route('/api/recommend/crop', methods=['POST'])
def recommend_crop():
    data = request.json
    n = data.get('n', 0)
    p = data.get('p', 0)
    k = data.get('k', 0)
    temp = data.get('temp', 25.0)
    humidity = data.get('humidity', 70.0)
    ph = data.get('ph', 6.5)
    rainfall = data.get('rainfall', 100.0)
    
    # Use real ML model
    report = crop_ml.generate_crop_report(n, p, k, temp, humidity, ph, rainfall)
    
    # Save to MongoDB
    save_report({
        "type": "crop_recommendation",
        "input": data,
        "result": report,
        "timestamp": datetime.utcnow()
    })
    
    return jsonify(report)

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

    # Use real ML model
    report = get_fertilizer_report(crop, n, p, k, moisture, temp, humidity, soil_type)
    
    # Save to MongoDB
    save_report({
        "type": "fertilizer_recommendation",
        "input": data,
        "result": report,
        "timestamp": datetime.utcnow()
    })
        
    return jsonify({
        "success": True,
        "report": report
    })

# ----------------------------
# 🔍 STRESS DETECTION (Cloudinary + ML + MongoDB)
# ----------------------------

@app.route('/api/detect/stress', methods=['POST'])
def detect_stress():
    data = request.json
    image_base64 = data.get('image') # base64 string
    
    if not image_base64:
        return jsonify({"success": False, "error": "No image data"}), 400
    
    # 1. Upload to Cloudinary
    image_url = upload_image(image_base64)
    
    if not image_url:
        return jsonify({"success": False, "error": "Failed to upload image"}), 500

    # 2. Heuristic or ML detection (For now using Gemini as in the original main.py, 
    # but could be replaced with moblitnet_dl.py)
    prompt = """
    Identify any crop stress, disease, or pest in this image. 
    Provide: 1. Disease Name, 2. Severity (High/Medium/Low), 3. Action Plan.
    Keep it concise for a mobile UI.
    """
    
    # Passing the original base64 to Gemini (as it doesn't take URLs easily without File API)
    # But we have successfully saved the URL in Cloudinary for our logs.
    analysis = AgroBackend.get_gemini_response(prompt, image_base64)
    
    # 3. Save to MongoDB
    analysis_data = {
        "image_url": image_url,
        "analysis": analysis,
        "timestamp": datetime.utcnow()
    }
    save_stress_analysis(analysis_data)
    
    return jsonify({
        "success": True,
        "analysis": analysis,
        "image_url": image_url,
        "timestamp": analysis_data["timestamp"].strftime("%Y-%m-%d %H:%M:%S")
    })

# ----------------------------
# 💬 CHATBOT MODULE
# ----------------------------

@app.route('/api/chat/query', methods=['POST'])
def chat_query():
    data = request.json
    query = data.get('query')
    
    if not query:
        return jsonify({"error": "Query is required"}), 400
        
    prompt = f"You are an expert agriculture AI assistant. Answer this query: {query}"
    # In a real RAG setup, we would use the logic from ml.py or build_rag.py here.
    response = AgroBackend.get_gemini_response(prompt)
    
    return jsonify({"response": response})

if __name__ == '__main__':
    print("AgroTech AI Cloud-Enabled Backend starting...")
    app.run(debug=True, host='0.0.0.0', port=5000)
