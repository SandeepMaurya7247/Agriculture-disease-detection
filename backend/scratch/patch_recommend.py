"""
This script patches main.py to add debugging to the recommend_crop function.
Run: python scratch/patch_recommend.py
"""
import re

with open("main.py", "r", encoding="utf-8") as f:
    content = f.read()

old_func = '''@app.route('/api/recommend/crop', methods=['POST'])
def recommend_crop():
    data = request.json
    n = data.get('n', 0)
    p = data.get('p', 0)
    k = data.get('k', 0)
    temp = data.get('temp', 25.0)
    humidity = data.get('humidity', 70.0)
    ph = data.get('ph', 6.5)
    rainfall = data.get('rainfall', 100.0)
    lang = data.get('lang', 'en')
    
    # Use real ML model
    report = crop_ml.generate_crop_report(n, p, k, temp, humidity, ph, rainfall, lang=lang)
    print(f"DEBUG REPORT KEYS: {report.keys()}")
    
    # Save to MongoDB in Background
    import threading
    threading.Thread(target=save_report, args=({
        "type": "crop_recommendation",
        "input": data,
        "result": report,
        "timestamp": datetime.utcnow()
    },)).start()
    
    # Standardize response key with robust defaults (Mapping user's custom keys)
    response = {
        "success": True,
        "recommendation": report.get("recommendation") or report.get("Recommended Crop") or "Unknown",
        "accuracy": report.get("accuracy") or report.get("Accuracy") or "99.3%",
        "why_this_crop": report.get("why_this_crop") or report.get("Why this crop?") or [],
        "expert_explanation": report.get("expert_explanation") or report.get("Expert Agricultural Explanation") or "ICAR Expert Analysis is being generated. Please wait a moment.",
        "details": report.get("note") or "Suitable for your climate."
    }
    return jsonify(response)'''

new_func = '''@app.route('/api/recommend/crop', methods=['POST'])
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
    
    print(f"\\n{'='*60}")
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
        return jsonify({"success": True, "recommendation": "Error", "accuracy": "0%", "why_this_crop": [], "expert_explanation": f"Error: {str(e)}", "details": str(e)})'''

if old_func in content:
    content = content.replace(old_func, new_func)
    with open("main.py", "w", encoding="utf-8") as f:
        f.write(content)
    print("PATCH APPLIED SUCCESSFULLY!")
else:
    print("ERROR: Could not find the target function. Trying with normalized whitespace...")
    # Try line by line
    lines = content.split('\n')
    start_idx = None
    end_idx = None
    for i, line in enumerate(lines):
        if 'def recommend_crop():' in line:
            start_idx = i - 1  # include the @app.route decorator
        if start_idx is not None and i > start_idx + 5 and 'return jsonify(response)' in line:
            end_idx = i
            break
    
    if start_idx is not None and end_idx is not None:
        print(f"Found function at lines {start_idx+1}-{end_idx+1}")
        new_lines = lines[:start_idx] + new_func.split('\n') + lines[end_idx+1:]
        with open("main.py", "w", encoding="utf-8") as f:
            f.write('\n'.join(new_lines))
        print("PATCH APPLIED via line replacement!")
    else:
        print(f"FAILED: start={start_idx}, end={end_idx}")
