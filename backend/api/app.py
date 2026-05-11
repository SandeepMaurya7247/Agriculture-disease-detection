from flask import Flask, request, jsonify
from flask_cors import CORS
import random

app = Flask(__name__)
CORS(app)

# --- Auth Routes ---
@app.route('/api/auth/login', methods=['POST'])
def login():
    data = request.json
    email = data.get('email')
    password = data.get('password')
    
    # Simple mock authentication
    if email and password:
        return jsonify({
            "token": "mock-token-12345",
            "user": {
                "id": "1",
                "name": "Arjun Sharma",
                "email": email,
                "role": "Farmer"
            }
        }), 200
    return jsonify({"error": "Invalid credentials"}), 401

@app.route('/api/auth/signup', methods=['POST'])
def signup():
    data = request.json
    return jsonify({
        "token": "mock-token-67890",
        "user": {
            "id": "2",
            "name": data.get('name', 'New User'),
            "email": data.get('email'),
            "role": "Farmer"
        }
    }), 201

# --- Weather Routes ---
@app.route('/api/weather/current', methods=['GET'])
def get_weather():
    lat = request.args.get('lat')
    lon = request.args.get('lon')
    return jsonify({
        "temperature": 32.5,
        "humidity": 45.0,
        "condition": "Sunny",
        "location": "Punjab, India"
    }), 200

# --- Recommendation Routes ---
@app.route('/api/recommend/crop', methods=['POST'])
def recommend_crop():
    data = request.json
    # Basic mock logic based on Nitrogen
    n = data.get('n', 0)
    if n > 80:
        crop = "Rice"
    elif n > 40:
        crop = "Wheat"
    else:
        crop = "Pulses"
        
    return jsonify({
        "recommendation": crop,
        "confidence": 0.95,
        "details": f"Based on your soil Nitrogen level ({n}), {crop} is highly recommended."
    }), 200

@app.route('/api/recommend/fertilizer', methods=['POST'])
def recommend_fertilizer():
    return jsonify({
        "recommendation": "Urea",
        "details": "Apply 50kg/hectare after the first irrigation."
    }), 200

# --- Detection Routes ---
@app.route('/api/detect/stress', methods=['POST'])
def detect_stress():
    return jsonify({
        "result": "Early Blight Detected",
        "severity": "Moderate",
        "suggestion": "Apply Fungicide (Mancozeb) and remove affected leaves."
    }), 200

# --- Chatbot Routes ---
@app.route('/api/chat/query', methods=['POST'])
def chat_query():
    data = request.json
    query = data.get('query', '').lower()
    
    if 'weather' in query:
        response = "Today's weather is sunny with a temperature of 32°C."
    elif 'crop' in query:
        response = "You should consider planting Rice given the current season and soil moisture."
    else:
        response = "I am your AgroTech AI assistant. How can I help you with your farm today?"
        
    return jsonify({
        "response": response
    }), 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
