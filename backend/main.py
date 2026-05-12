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
from datetime import datetime
from flask import Flask, request, jsonify, session
from flask_cors import CORS
from PIL import Image

# Import Custom Modules
from db.mongo_db import (
    get_user_by_email, create_user, save_report, save_stress_analysis
)
from services.cloudinary_service import upload_image
from ml.ml import generate_report as get_fertilizer_report
from ml import crop_rec_ml as crop_ml

app = Flask(__name__)
CORS(app, resources={r"/api/*": {"origins": "*"}})
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
    lat = request.args.get('lat', '28.6139')
    lon = request.args.get('lon', '77.2090')
    api_key = os.getenv("WEATHER_API_KEY")
    
    if not api_key:
        return jsonify({"error": "Weather API Key is missing in environment variables"}), 500
        
    url = f"https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={api_key}&units=metric"
    
    try:
        r = requests.get(url, timeout=10)
        if r.ok:
            data = r.json()
            return jsonify({
                "temperature": data['main']['temp'],
                "humidity": data['main']['humidity'],
                "condition": data['weather'][0]['main'],
                "windSpeed": data['wind']['speed'],
                "iconUrl": f"https://openweathermap.org/img/wn/{data['weather'][0]['icon']}@2x.png",
                "location": data.get('name', 'Bhopal'),
                "pressure": data['main']['pressure'],
                "description": data['weather'][0]['description']
            })
    except Exception as e:
        print(f"Weather API Exception: {e}")
    
    # MOCK FALLBACK (Ensure app never shows error)
    print("Using Mock Weather Fallback")
    return jsonify({
        "temperature": 28.5,
        "humidity": 65,
        "condition": "Cloudy",
        "windSpeed": 4.2,
        "iconUrl": "https://openweathermap.org/img/wn/03d@2x.png",
        "location": "Bhopal (Demo)",
        "pressure": 1012,
        "description": "scattered clouds"
    })

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
    
    # Save to MongoDB in Background
    import threading
    threading.Thread(target=save_report, args=({
        "type": "crop_recommendation",
        "input": data,
        "result": report,
        "timestamp": datetime.utcnow()
    },)).start()
    
    # Standardize response key
    return jsonify({
        "success": True,
        "recommendation": report.get("Recommended Crop", "Unknown"),
        "details": report.get("note", "Suitable for your climate.")
    })

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
        "deficiency": report.get("Nutrient Deficiency", {}),
        "schedule": report.get("Application Schedule", []),
        "details": f"Optimized for {soil_type} and {crop}."
    })

import numpy as np
import onnxruntime as ort

def get_expert_advice(disease, lang='en'):
    # Ultra-detailed expert guide database
    advice_db = {
        "en": {
            "Bacterial Leaf Blight": "🔍 **Detailed Symptoms (Pehchan)**:\n• Initially, water-soaked streaks appear on leaf blades which eventually turn yellow-orange.\n• These streaks start from the tips and move down along the edges, forming wavy margins.\n• In severe cases, the entire leaf may dry up and turn straw-colored, significantly reducing yield.\n\n💊 **Advanced Chemical Treatment (Rasayanik)**:\n• **Spray 1**: Mix 1.5g of Streptocycline and 25g of Copper Oxychloride in 10 liters of clean water. Spray thoroughly on both sides of leaves.\n• **Spray 2**: If the disease persists after 12-15 days, apply Kocide (Copper Hydroxide) at 2g per liter of water.\n• Avoid spraying during high winds or rain; ensure the nozzle is fine for maximum coverage.\n\n🍃 **Comprehensive Organic Treatment (Jaivik)**:\n• **NSKE 5%**: Boil 5kg of Neem seed powder in 10L water overnight, filter it, and dilute in 100L water for spraying.\n• **Cow Dung Slurry**: Mix 1kg of fresh cow dung in 10L of water, filter it twice through a fine cloth, and spray to boost plant immunity.\n• Apply Trichoderma viride enriched farmyard manure to the soil.\n\n🛡️ **Expert Prevention (Bachav)**:\n• **Nutrient Management**: Reduce Nitrogen (Urea) and increase Potassium (K) to strengthen leaf cell walls.\n• **Water Management**: Ensure the field is not continuously flooded; follow 'Alternate Wetting and Drying' (AWD).\n• **Sanitation**: Remove weeds and infected straw from the previous season to prevent the bacteria from overwintering.",
            "Brown Spot": "🔍 **Detailed Symptoms (Pehchan)**:\n• Small, circular to oval, dark brown spots with a prominent yellow halo appear on leaves.\n• On older leaves, these spots expand and their centers turn light brown or gray.\n• Can lead to 'Grain Discoloration' if the infection reaches the panicles, reducing grain quality.\n\n💊 **Advanced Chemical Treatment (Rasayanik)**:\n• **Option 1**: Spray Mancozeb 75 WP (2.5g/L) or Zineb (2g/L) as soon as spots appear.\n• **Option 2**: For systemic control, use Hexaconazole 5% EC (2ml/L) or Propiconazole 25 EC (1ml/L).\n• Always use a 'Spreader/Sticker' agent during the monsoon to prevent the chemical from washing off.\n\n🍃 **Comprehensive Organic Treatment (Jaivik)**:\n• **Cow Urine**: Mix 1 liter of well-fermented cow urine with 10 liters of water and spray every 10 days.\n• **Seed Treatment**: Soak seeds in a solution of Pseudomonas fluorescens (10g/kg) before sowing.\n• Spraying Vermicompost wash provides essential micronutrients and suppressive microbes.\n\n🛡️ **Expert Prevention (Bachav)**:\n• **Soil Health**: Apply balanced fertilizers based on a soil test; specifically address Zinc or Manganese deficiencies.\n• **Water Management**: Avoid water stress (drought) as weakened plants are more susceptible to Brown Spot.\n• **Variety**: Always plant certified, disease-resistant seeds from reliable sources.",
            "Healthy Leaf": "✅ **Health Status (Sthiti)**:\n• Your crop is currently in excellent health with no visible signs of fungal or bacterial infection.\n• Leaf color, texture, and turgidity are within the optimal range for this growth stage.\n\n🛡️ **Professional Maintenance Tips**:\n• **Monitoring**: Continue scouting the field twice a week, checking the lower canopy for early signs.\n• **Nutrition**: Apply the next dose of top-dressed Nitrogen only if required by the Leaf Color Chart (LCC).\n• **Bio-Stimulants**: You may spray Seaweed Extract (2ml/L) to enhance the crop's natural defense mechanism."
        },
        "hi": {
            "Bacterial Leaf Blight": "🔍 **विस्तृत लक्षण (Pehchan)**:\n• शुरुआती चरण में, पत्तियों पर पानी से भीगी हुई धारियां दिखाई देती हैं जो बाद में पीली-नारंगी हो जाती हैं।\n• ये धारियां नोकों से शुरू होती हैं और किनारों के साथ नीचे की ओर बढ़ती हैं, जिससे लहरदार किनारे बन जाते हैं।\n• गंभीर मामलों में, पूरी पत्ती सूख कर पुआल के रंग की हो सकती है, जिससे पैदावार काफी कम हो जाती है।\n\n💊 **उन्नत रासायनिक उपचार (Rasayanik)**:\n• **स्प्रे 1**: 10 लीटर साफ पानी में 1.5 ग्राम स्ट्रेप्टोसायक्लिन और 25 ग्राम कॉपर ऑक्सीक्लोराइड मिलाएं। पत्तियों के दोनों तरफ अच्छी तरह छिड़काव करें।\n• **स्प्रे 2**: यदि 12-15 दिनों के बाद भी बीमारी बनी रहती है, तो कोसाइड (कॉपर हाइड्रोक्साइड) का 2 ग्राम प्रति लीटर पानी में प्रयोग करें।\n• तेज हवा या बारिश के दौरान स्प्रे करने से बचें; सुनिश्चित करें कि नोजल बारीक हो।\n\n🍃 **व्यापक जैविक उपचार (Jaivik)**:\n• **नीम अर्क 5%**: 5 किलो नीम के बीज के पाउडर को रात भर 10 लीटर पानी में उबालें, इसे छान लें और छिड़काव के लिए 100 लीटर पानी में घोलें।\n• **गोबर का घोल**: 1 किलो ताज़ा गोबर को 10 लीटर पानी में मिलाएं, इसे दो बार बारीक कपड़े से छान लें, और छिड़काव करें।\n• मिट्टी में ट्राइकोडर्मा विरिडी से समृद्ध खाद डालें।\n\n🛡️ **विशेषज्ञ बचाव (Bachav)**:\n• **पोषक तत्व प्रबंधन**: नाइट्रोजन (यूरिया) कम करें और पोटाश (K) बढ़ाएं ताकि पत्तियों की दीवारें मजबूत हों।\n• **जल प्रबंधन**: सुनिश्चित करें कि खेत में लगातार पानी न भरा रहे; 'वैकल्पिक गीला और सुखाने' (AWD) की विधि अपनाएं।\n• **सफाई**: बैक्टीरिया को पनपने से रोकने के लिए पिछली फसल के अवशेषों को हटा दें।",
            "Brown Spot": "🔍 **विस्तृत लक्षण (Pehchan)**:\n• पत्तियों पर छोटे, गोल से अंडाकार, गहरे भूरे रंग के धब्बे दिखाई देते हैं जिनके चारों ओर पीला घेरा होता है।\n• पुरानी पत्तियों पर, ये धब्बे फैल जाते हैं और उनके केंद्र हल्के भूरे या धूसर हो जाते हैं।\n• यदि संक्रमण बालियों तक पहुँचता है, तो यह 'दानों के रंग बिगाड़ने' का कारण बन सकता है।\n\n💊 **उन्नत रासायनिक उपचार (Rasayanik)**:\n• **विकल्प 1**: धब्बे दिखाई देते ही मैंकोजेब 75 WP (2.5 ग्राम/लीटर) या ज़िनेब (2 ग्राम/लीटर) का छिड़काव करें।\n• **विकल्प 2**: व्यवस्थित नियंत्रण के लिए, हेक्साकोनाजोल 5% EC (2ml/L) या प्रोपिकोनाजोल 25 EC (1ml/L) का उपयोग करें।\n• मानसून के दौरान दवा को धुलने से बचाने के लिए हमेशा 'स्टिकर' का उपयोग करें।\n\n🍃 **व्यापक जैविक उपचार (Jaivik)**:\n• **गौमूत्र**: 1 लीटर अच्छी तरह सड़े हुए गौमूत्र को 10 लीटर पानी में मिलाएं और हर 10 दिन में छिड़काव करें।\n• **बीज उपचार**: बुवाई से पहले बीजों को स्यूडोमोनास फ्लोरेसेंस (10 ग्राम/किलो) के घोल में भिगो दें।\n• वर्मीकम्पोस्ट वॉश का छिड़काव सूक्ष्म पोषक तत्व और सुरक्षात्मक रोगाणु प्रदान करता है।\n\n🛡️ **विशेषज्ञ बचाव (Bachav)**:\n• **मिट्टी का स्वास्थ्य**: मिट्टी परीक्षण के आधार पर संतुलित उर्वरक डालें; विशेष रूप से जिंक या मैंगनीज की कमी को दूर करें।\n• **जल प्रबंधन**: जल तनाव (सूखा) से बचें क्योंकि कमजोर पौधे भूरे धब्बे के प्रति अधिक संवेदनशील होते हैं।\n• **बीज**: हमेशा विश्वसनीय स्रोतों से प्रमाणित, रोग-प्रतिरोधी बीज ही लगाएं।",
            "Healthy Leaf": "✅ **स्वास्थ्य स्थिति (Sthiti)**:\n• आपकी फसल वर्तमान में उत्कृष्ट स्वास्थ्य में है और कवक या जीवाणु संक्रमण का कोई दृश्य संकेत नहीं है।\n• पत्तियों का रंग, बनावट और मजबूती इस विकास चरण के लिए इष्टतम सीमा के भीतर हैं।\n\n🛡️ **पेशेवर रखरखाव युक्तियाँ**:\n• **निगरानी**: सप्ताह में दो बार खेत का निरीक्षण जारी रखें, शुरुआती संकेतों के लिए निचले हिस्से की जांच करें।\n• **पोषण**: यूरिया की अगली खुराक केवल तभी डालें जब लीफ कलर चार्ट (LCC) द्वारा आवश्यक हो।\n• **बायो-स्टिमुलेंट्स**: आप फसल की प्राकृतिक रक्षा प्रणाली को बढ़ाने के लिए समुद्री शैवाल के अर्क (2ml/L) का छिड़काव कर सकते हैं।"
        }
    }
    selected_db = advice_db.get(lang, advice_db['en'])
    return selected_db.get(disease, "⚠️ Unidentified stress. Please use Gemini for a detailed expert report.")

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
        r = requests.post(f"{GEMINI_URL}?key={GEMINI_API_KEY}", json=payload, timeout=30)
        if r.ok:
            text = r.json()['candidates'][0]['content']['parts'][0]['text']
            clean_json = text.replace("```json", "").replace("```", "").strip()
            return json.loads(clean_json)
    except:
        pass
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

        # 2. LOCAL ONLY ANALYSIS (No Gemini fallback as requested)
        print(f"DEBUG: [FINAL DIAGNOSIS] Result: {disease} | Source: {source} | Conf: {confidence:.2f}")

    except Exception as e:
        print(f"DEBUG: AI Analysis Error: {e}")
        disease = "Analysis Error"
        confidence = 0.0
        treatment = "Could not analyze the leaf. Please ensure the photo is clear and try again."
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
    app.run(debug=False, host='0.0.0.0', port=port)
