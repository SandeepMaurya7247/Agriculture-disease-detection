# ---------------- MODEL BUILDING (MobileNet) ----------------
import tensorflow as tf
import numpy as np
import keras
from keras import layers
from keras.models import Sequential
from keras.applications import MobileNetV2
from keras.preprocessing.image import load_img, img_to_array
from keras.applications.mobilenet_v2 import preprocess_input
import numpy as np

# Specific imports for your Aero-Agro Insights project
from keras.layers import GlobalAveragePooling2D, Dense, Dropout
import keras
from keras.src.legacy.preprocessing.image import ImageDataGenerator

# load model 
model = tf.keras.models.load_model("model/crop_stress_mobilenet.h5")

CLASS_LABELS = {
    0: "Bacterial Leaf Blight",
    1: "Brown Spot",
    2: "Controlled_leaf",
    3: 'Leaf smut',
    4: "diseased_leaf"
}

def irrigation_suggestion(disease):

    if disease == "Bacterial Leaf Blight":
        return (
            "Irrigation: ⚠️ YES, but with CAUTION\n\n"
            "What to do:\n"
            "✅ Use controlled irrigation\n"
            "✅ Prefer drip irrigation or furrow irrigation\n"
            "✅ Water early morning (so leaves dry quickly)\n"
            "✅ Maintain proper field drainage\n\n"
            "What NOT to do:\n"
            "❌ Avoid flood irrigation\n"
            "❌ Avoid over-irrigation\n"
            "❌ Do NOT let water stand in the field\n"
            "❌ Avoid sprinklers (water spreads bacteria)"
        )

    elif disease == "Brown Spot":
        return (
            "Irrigation: ✅ YES (Balanced)\n\n"
            "What to do:\n"
            "✅ Provide moderate irrigation\n"
            "✅ Maintain consistent soil moisture\n"
            "✅ Irrigate as per crop stage\n"
            "✅ Ensure proper drainage\n\n"
            "What NOT to do:\n"
            "❌ Avoid water stress\n"
            "❌ Avoid over-watering\n"
            "❌ Do NOT allow waterlogging\n"
            "❌ Avoid irregular irrigation"
        )

    elif disease == "Controlled_leaf":
        return (
            "Irrigation: ✅ YES (Normal)\n\n"
            "What to do:\n"
            "✅ Follow normal irrigation schedule\n"
            "✅ Irrigate according to crop growth stage\n"
            "✅ Maintain even soil moisture\n"
            "✅ Use recommended water quantity\n\n"
            "What NOT to do:\n"
            "❌ Avoid over-irrigation\n"
            "❌ Avoid drought stress\n"
            "❌ Do NOT ignore drainage\n"
            "❌ Avoid sudden irrigation changes"
        )

    elif disease == "Leaf smut":
        return (
            "Irrigation: ⚠️ LIMITED\n\n"
            "What to do:\n"
            "✅ Use light irrigation\n"
            "✅ Improve field drainage\n"
            "✅ Keep leaf surface dry\n"
            "✅ Maintain good air circulation\n\n"
            "What NOT to do:\n"
            "❌ Avoid excess moisture\n"
            "❌ Avoid frequent irrigation\n"
            "❌ Do NOT use sprinklers\n"
            "❌ Avoid water stagnation"
        )

    else:  # diseased_leaf or unknown
        return (
            "Irrigation: ⚠️ CAREFUL\n\n"
            "What to do:\n"
            "✅ Reduce irrigation frequency\n"
            "✅ Focus watering on soil only\n"
            "✅ Improve airflow between plants\n"
            "✅ Monitor soil moisture regularly\n\n"
            "What NOT to do:\n"
            "❌ Avoid wetting leaves\n"
            "❌ Avoid high humidity\n"
            "❌ Do NOT over-irrigate\n"
            "❌ Avoid flooding"
        )
    



# fertlizer suggesstion
def fertilizer_pesticide_advice(disease):
    if disease == "Bacterial Leaf Blight":    
        return (
        "🦠 Bacterial Leaf Blight (BLB)\n"
        "Fertilizer, Pesticide & Other Material – WITH REASON\n\n"

        "🔴 FIRST: Understand the Disease (Very Important)\n"
        "• BLB is caused by bacteria, NOT fungus\n"
        "• Fungicides do NOT work\n"
        "• Excess fertilizer makes the disease worse\n\n"
        "Control is done by:\n"
        "• Reducing bacterial spread\n"
        "• Strengthening plant immunity\n\n"

        "🌱 1️⃣ FERTILIZER MANAGEMENT (MOST IMPORTANT)\n\n"

        "❌ Avoid Excess Nitrogen (N)\n"
        "Why?\n"
        "• Nitrogen causes soft, tender leaf growth\n"
        "• Soft leaves are easily attacked by bacteria\n"
        "• High nitrogen = high disease severity\n\n"
        "❌ Don’t use:\n"
        "• Excess urea\n"
        "• High-nitrogen chemical fertilizers\n\n"

        "✅ Use Balanced Fertilizers\n\n"

        "✅ Potassium (K) – VERY IMPORTANT\n"
        "Examples:\n"
        "• MOP (Muriate of Potash)\n"
        "• SOP (Sulphate of Potash)\n"
        "Why?\n"
        "• Strengthens cell walls\n"
        "• Makes leaves thicker and harder\n"
        "• Reduces bacterial entry\n"
        "• Improves disease resistance\n\n"
        "Potassium = Plant bodyguard 🛡️\n\n"

        "✅ Phosphorus (P)\n"
        "Examples:\n"
        "• DAP (balanced use)\n"
        "• SSP\n"
        "Why?\n"
        "• Strengthens root system\n"
        "• Helps plants recover faster\n"
        "• Improves energy flow inside plant\n\n"

        "✅ Micronutrients (Very Helpful)\n"
        "• Zinc (Zn)\n"
        "• Silicon (Si)\n"
        "Why?\n"
        "• Improve leaf strength\n"
        "• Increase natural immunity\n"
        "• Reduce disease spread\n\n"

        "🧪 2️⃣ PESTICIDE / BACTERICIDE (KEY CONTROL STEP)\n"
        "Since BLB is bacterial, use bactericides, NOT fungicides.\n\n"

        "✅ Recommended Chemicals\n\n"

        "🔹 Copper-based Bactericides\n"
        "Examples:\n"
        "• Copper Oxychloride\n"
        "• Copper Hydroxide\n"
        "Why?\n"
        "• Kills bacteria on leaf surface\n"
        "• Stops bacterial multiplication\n"
        "• Protects healthy leaves\n\n"

        "🔹 Antibiotic Spray (Use Carefully)\n"
        "Examples:\n"
        "• Streptocycline\n"
        "• Streptomycin + Tetracycline\n"
        "Why?\n"
        "• Kills bacteria inside leaf tissues\n"
        "• Most effective in early stages\n\n"
        "⚠️ Important Warning:\n"
        "• Do NOT overuse antibiotics\n"
        "• Overuse causes resistance\n"
        "• Use only 1–2 sprays if necessary\n\n"

        "🌿 3️⃣ BIOLOGICAL & ORGANIC OPTIONS (BEST FOR LONG TERM)\n"
        "• Neem oil / Neem extract – reduces bacterial population\n"
        "• Trichoderma / Pseudomonas – improves soil health and plant immunity\n\n"

        "🚜 4️⃣ OTHER IMPORTANT FIELD PRACTICES\n"
        "• Remove infected leaves to stop spread\n"
        "• Improve field drainage (standing water spreads bacteria)\n"
        "• Use clean, disease-free seeds\n"
        "• Maintain proper plant spacing for airflow\n"
    ) 

    if disease == "Brown Spot":  
        return (
        "🍂 Brown Spot Disease (Fungal)\n\n"

        "🔴 Cause\n"
        "- Brown Spot is caused by a fungus\n"
        "- It mainly attacks weak or nutrient-deficient plants\n\n"

        "🌱 Fertilizer Recommendation (What & Why)\n"
        "❌ Avoid Excess Nitrogen (N)\n"
        "Why?\n"
        "- Too much nitrogen makes leaves soft and weak\n"
        "- Weak leaves are easily infected by fungus\n\n"

        "✅ Use Balanced NPK Fertilizer\n"
        "Why?\n"
        "- Keeps plant growth balanced\n"
        "- Prevents stress, reducing fungal attack\n\n"

        "✅ Use Potassium (K) – Very Important\n"
        "Why?\n"
        "- Strengthens leaf tissues\n"
        "- Improves resistance against fungal infection\n"
        "- Helps leaves heal faster\n\n"

        "✅ Use Micronutrients (Zinc, Iron)\n"
        "Why?\n"
        "- Brown spot often appears due to nutrient deficiency\n"
        "- Micronutrients improve leaf health and immunity\n\n"

        "🧪 Pesticide Recommendation (What & Why)\n"
        "✅ Use Fungicides (Mandatory)\n"
        "Examples:\n"
        "- Mancozeb\n"
        "- Carbendazim\n\n"
        "Why Fungicides?\n"
        "- Brown Spot is a fungal disease\n"
        "- Fungicides stop fungal growth\n"
        "- Prevent spread to healthy leaves\n\n"

        "🌿 Organic Option (Optional)\n"
        "- Neem oil spray\n"
        "Why?\n"
        "- Reduces fungal spores naturally\n"
        "- Safe for long-term use\n"
    )

    if disease == "Leaf Smut":
         return (
        "🌾 Leaf Smut Disease (Fungal)\n\n"

        "🔴 Cause\n"
        "- Leaf Smut is caused by a fungus\n"
        "- It spreads through fungal spores and infected residues\n\n"

        "🌱 Fertilizer Recommendation (What & Why)\n"
        "❌ Avoid Excess Nitrogen (N)\n"
        "Why?\n"
        "- Excess nitrogen produces soft leaves\n"
        "- Soft leaves favor fungal growth\n\n"

        "✅ Use Balanced NPK Fertilizer\n"
        "Why?\n"
        "- Maintains steady and healthy growth\n"
        "- Reduces plant stress\n\n"

        "✅ Use Potassium (K) – Very Important\n"
        "Why?\n"
        "- Strengthens leaf tissues\n"
        "- Improves resistance against fungal infection\n"
        "- Reduces spore spread\n\n"

        "✅ Use Micronutrients (Zinc, Silicon)\n"
        "Why?\n"
        "- Improves leaf strength\n"
        "- Boosts plant immunity\n\n"

        "🧪 Pesticide Recommendation (What & Why)\n"
        "✅ Use Fungicides (Mandatory)\n"
        "Examples:\n"
        "- Propiconazole\n"
        "- Mancozeb\n\n"
        "Why Fungicides?\n"
        "- Leaf Smut is a fungal disease\n"
        "- Fungicides kill fungal spores\n"
        "- Prevent spread to healthy plants\n\n"

        "⚠️ Safety & Caution\n"
        "- Do not overuse fungicides\n"
        "- Follow recommended dosage\n"
        "- Rotate fungicides to avoid resistance\n\n"

        "🌿 Organic Option (Optional)\n"
        "- Neem oil spray\n"
        "- Trichoderma\n"
        "Why?\n"
        "- Controls fungus naturally\n"
        "- Safe for long-term use\n"
    ) 

    if disease == "Controlled_leaf":
        return (
        "🌿 Controlled Leaf (Healthy Plant)\n\n"

        "🔴 Status\n"
        "- No disease detected\n"
        "- Plant is healthy and growing normally\n\n"

        "🌱 Fertilizer Recommendation (What & Why)\n"
        "✅ Use Balanced NPK Fertilizer\n"
        "Why?\n"
        "- Supports normal growth\n"
        "- Maintains strong leaves and roots\n\n"

        "✅ Use Potassium (K)\n"
        "Why?\n"
        "- Strengthens leaf tissues\n"
        "- Improves plant immunity\n\n"

        "✅ Use Micronutrients (Zinc, Iron, Silicon)\n"
        "Why?\n"
        "- Improves leaf strength and color\n"
        "- Prevents nutrient deficiency\n\n"

        "❌ Avoid Excess Nitrogen (N)\n"
        "Why?\n"
        "- Excess nitrogen makes leaves soft\n"
        "- Soft leaves attract diseases\n\n"

        "🧪 Pesticide Recommendation (What & Why)\n"
        "❌ No chemical pesticide required\n"
        "Why?\n"
        "- Healthy plants do not need chemicals\n"
        "- Prevents resistance and soil damage\n\n"

        "🌿 Preventive & Organic Care (Optional)\n"
        "- Neem oil (low concentration)\n"
        "Why?\n"
        "- Provides natural protection\n"
        "- Safe for long-term use\n\n"

        "⚠️ Caution & Safety\n"
        "- Avoid unnecessary chemical sprays\n"
        "- Follow recommended fertilizer dosage\n"
    )


    if disease == "diseased_leaf":
     return """
    <h3>⚠️ Diseased Leaf (Unidentified / Mixed Infection)</h3>

    <h4>🔴 Status</h4>
    <ul>
      <li>Disease detected but type is unclear</li>
      <li>May be fungal, bacterial, or nutrient stress</li>
    </ul>

    <h4>🌱 Fertilizer Recommendation (What & Why)</h4>
    <b>❌ Avoid Excess Nitrogen (N)</b>
    <ul>
      <li>Excess nitrogen weakens leaves</li>
      <li>Weak leaves worsen disease spread</li>
    </ul>

    <b>✅ Use Balanced NPK (Low–Moderate)</b>
    <ul>
      <li>Supports recovery without encouraging disease</li>
    </ul>

    <b>✅ Use Potassium (K)</b>
    <ul>
      <li>Strengthens leaf tissues</li>
      <li>Improves plant defense</li>
    </ul>

    <b>🧪 Pesticide Recommendation</b>
    <ul>
      <li>Avoid heavy chemicals initially</li>
      <li>Use Neem oil or Copper Oxychloride (low dose)</li>
    </ul>

    <b>⚠️ Safety</b>
    <ul>
      <li>Do not overuse pesticides</li>
      <li>Confirm disease before strong treatment</li>
    </ul>
    """


def generate_action_advice(disease):
    return {
        "irrigation" : irrigation_suggestion(disease) ,
        "fertilizer" : fertilizer_pesticide_advice(disease)
    }



def generate_image_report(image_path):
    try:
        # Load & preprocess image
        img = load_img(image_path, target_size=(224, 224))
        img_array = img_to_array(img)
        img_array = np.expand_dims(img_array, axis=0)
        img_array = preprocess_input(img_array)

        prediction = model.predict(img_array)[0]
        class_index = int(np.argmax(prediction))
        confidence = float(prediction[class_index])

        predicted_label = CLASS_LABELS[class_index]
        advice = generate_action_advice(predicted_label)
        # Confidence threshold
        if confidence < 0.60:
            return {
                "status": "uncertain",
                "prediction": "Low confidence",
                "message" : "I'm not sure about this image 🤔 upload clear image" ,
                "confidence": round(confidence * 100, 2)
            }

        return {
            "status": "success",
            "prediction": predicted_label,
            "confidence": round(confidence * 100, 2),
           "action_advice": advice
        }

    except Exception as e:
        print("❌ Prediction failed:", e)
        return {
            "status": "error",
            "prediction": "Model failure",
            "confidence": 0.0
        }



    