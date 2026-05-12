import pandas as pd
import numpy as np

from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler, OneHotEncoder, LabelEncoder
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from sklearn.metrics import classification_report

from sklearn.ensemble import RandomForestClassifier
import json
from dotenv import load_dotenv
import os

load_dotenv()

# Crop nutrient requirements (kg/ha)
CROP_DATA = {
    "Wheat": {"N": 120, "P": 60, "K": 40},
    "Rice": {"N": 120, "P": 60, "K": 40},
    "Maize": {"N": 150, "P": 75, "K": 50},
    "Cotton": {"N": 100, "P": 50, "K": 50},
    "Sugarcane": {"N": 250, "P": 115, "K": 115},
    "Default": {"N": 100, "P": 50, "K": 40}
}

# Fertilizer cost (₹ per kg)
FERTILIZER_PRICES = {
    "Urea": 6,
    "DAP": 15,
    "MOP": 17,
    "17-17-17": 20,
    "10-26-26": 18
}

class FertilizerModel:
    def __init__(self):
        self.pipeline = None
        self.label_encoder = LabelEncoder()

    def build_pipeline(self, numeric_cols, categorical_cols):
        preprocessor = ColumnTransformer([
            ("num", StandardScaler(), numeric_cols),
            ("cat", OneHotEncoder(handle_unknown="ignore"), categorical_cols)
        ])

        self.pipeline = Pipeline([
            ("preprocessor", preprocessor),
            ("classifier", RandomForestClassifier(
                n_estimators=200,
                max_depth=12,
                random_state=42,
                criterion='entropy'
            ))
        ])

    def train(self, df):
        X = df.drop("Fertilizer Name", axis=1)
        y = self.label_encoder.fit_transform(df["Fertilizer Name"])

        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=0.2, random_state=42
        )

        self.build_pipeline(
            numeric_cols=["Temparature", "Humidity", "Moisture",
                        "Nitrogen", "Phosphorous", "Potassium"],
            categorical_cols=["Soil Type", "Crop Type"]
        )

        self.pipeline.fit(X_train, y_train)

        # ✅ SAVE THESE FOR LIME
        self.X_train = X_train
        self.X_train_transformed = self.pipeline.named_steps[
            "preprocessor"
        ].transform(X_train)

        self.feature_names_transformed = (
            self.pipeline.named_steps["preprocessor"]
            .get_feature_names_out()
        )

        print("Model trained")
        accuracy = self.pipeline.score(X_test, y_test)
        print(classification_report( y_test, self.pipeline.predict(X_test), target_names=self.label_encoder.classes_ ))
        print("Model Accuracy:", round(accuracy * 100, 2), "%")

    def predict_fertilizer(self, input_data: dict):
        df_input = pd.DataFrame([input_data])
        pred_idx = self.pipeline.predict(df_input)[0]
        return self.label_encoder.inverse_transform([pred_idx])[0]


class AgronomyEngine:
    @staticmethod
    def calculate_deficiency(crop, soil_n, soil_p, soil_k):
        req = CROP_DATA.get(crop, CROP_DATA["Default"])
        return {
            "N": max(0, req["N"] - soil_n),
            "P": max(0, req["P"] - soil_p),
            "K": max(0, req["K"] - soil_k)
        }

    @staticmethod
    def calculate_quantity(deficiency):
            base_qty = 100
            total_def = sum(deficiency.values())
            quantity = base_qty + (total_def * 0.2)
            return int(np.clip(quantity, 25, 250))    
    

    @staticmethod
    def fertilizer_schedule(quantity):
        return [
            {"Stage": "Basal (Sowing)", "Quantity (kg/ha)": round(quantity * 0.4, 1)},
            {"Stage": "Vegetative Growth", "Quantity (kg/ha)": round(quantity * 0.4, 1)},
            {"Stage": "Flowering / Yield", "Quantity (kg/ha)": round(quantity * 0.2, 1)}
        ]

    @staticmethod
    def sustainability_score(quantity):
        if quantity < 100:
            return "High"
        elif quantity < 150:
            return "Medium"
        return "Low"

    @staticmethod
    def generate_report(crop, fertilizer, soil_n, soil_p, soil_k):

        deficiency = AgronomyEngine.calculate_deficiency(
            crop, soil_n, soil_p, soil_k
        )

        # ✅ FIXED HERE
        quantity = AgronomyEngine.calculate_quantity(deficiency)

        schedule = AgronomyEngine.fertilizer_schedule(quantity)
        cost = quantity * FERTILIZER_PRICES.get(fertilizer, 12)
        sustainability = AgronomyEngine.sustainability_score(quantity)

        return {
            "Crop": crop,
            "Recommended Fertilizer": fertilizer,
            "Quantity (kg/ha)": quantity,
            "Cost (₹)": cost,
            "Sustainability": sustainability,
            "Nutrient Deficiency": deficiency,
            "Application Schedule": schedule
        }
    
import pickle

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_PATH = os.path.join(BASE_DIR, "..", "..", "data", "Soil_Nutrient.csv")
MODEL_FILE = os.path.join(BASE_DIR, "..", "..", "saved_models", "fertilizer_model.pkl")

# Lazy loaders
_explainer = None
_embeddings = None
_vector_db = None
_llm = None

def get_explainer():
    global _explainer
    if _explainer is None:
        _explainer = LimeTabularExplainer(
            training_data=model.X_train_transformed,
            feature_names=model.feature_names_transformed,
            class_names=model.label_encoder.classes_,
            mode="classification"
        )
    return _explainer

def get_rag_components():
    global _embeddings, _vector_db, _llm
    if _embeddings is None:
        try:
            _embeddings = HuggingFaceEmbeddings(model_name="saved_models/all-MiniLM-L6-v2")
            _vector_db = FAISS.load_local("saved_models/faiss_index", _embeddings, allow_dangerous_deserialization=True)
            _llm = ChatGroq(model="llama-3.1-8b-instant")
        except Exception as e:
            print(f"Lazy RAG failed: {e}")
    return _vector_db, _llm

# Load or Train Model
df = pd.read_csv(DATA_PATH)
model = FertilizerModel()

loaded = False
if os.path.exists(MODEL_FILE):
    print("Loading pre-trained model...")
    try:
        with open(MODEL_FILE, 'rb') as f:
            model = pickle.load(f)
            loaded = True
    except (EOFError, pickle.UnpicklingError):
        print("Corrupted model file detected, re-training...")

if not loaded:
    print("Training new model...")
    model.train(df)
    os.makedirs(os.path.dirname(MODEL_FILE), exist_ok=True)
    with open(MODEL_FILE, 'wb') as f:
        pickle.dump(model, f)

def generate_report(crop, n, p, k, moisture, temp , humidity , soil_type):
    input_data = {
        "Temparature": temp,
        "Humidity": humidity,
        "Moisture": moisture,
        "Soil Type": soil_type,
        "Crop Type": crop,
        "Nitrogen": n,
        "Phosphorous": p,
        "Potassium": k
    }

    fertilizer = model.predict_fertilizer(input_data)
    return AgronomyEngine.generate_report(crop, fertilizer, n, p, k)

def explain_with_lime(input_data):
    explainer = get_explainer()
    X = pd.DataFrame([input_data])
    X_trans = model.pipeline.named_steps["preprocessor"].transform(X)
    exp = explainer.explain_instance(X_trans[0], lime_predict_fn, num_features=8)
    return model.predict_fertilizer(input_data), [{"feature": f, "impact": round(w, 3)} for f, w in exp.as_list()]

def rag_explanation(fertilizer, lime_output):
    vector_db, llm = get_rag_components()
    if not vector_db or not llm: return "RAG Explanation unavailable."
    
    retriever = vector_db.as_retriever(search_kwargs={"k": 3})

    # lime_output MUST be list[dict]
    lime_text = "\n".join(
        f"{x['feature']} → {x['impact']}"
        for x in lime_output
    )

    docs = retriever.invoke(
        f"Why is {fertilizer} recommended for soil and crop?"
    )

    context = "\n".join(d.page_content for d in docs)

    prompt = f"""
You are an agriculture expert.
Use only ICAR / Government knowledge.

Context:
{context}

Model Reasoning:
{lime_text}

Explain why {fertilizer} is recommended.
"""

    return llm.invoke(prompt).content


if __name__ == "__main__":
    # Load dataset
    # df = pd.read_csv("data/Soil_Nutrient.csv")

    # # Train model
    # model = FertilizerModel()
    # model.train(df)

    # Sample input
    sample_input = {
        "Temparature": 26,
        "Humidity": 60,
        "Moisture": 0.6,
        "Soil Type": "Black Soil",
        "Crop Type": "Wheat",
        "Nitrogen": 45,
        "Phosphorous": 50,
        "Potassium": 90
    }

    fertilizer = model.predict_fertilizer(sample_input)

    report = AgronomyEngine.generate_report(
        crop=sample_input["Crop Type"],
        fertilizer=fertilizer,
        soil_n=sample_input["Nitrogen"],
        soil_p=sample_input["Phosphorous"],
        soil_k=sample_input["Potassium"]
    )
    fertilizer, lime_output = explain_with_lime(sample_input)
    explanation = rag_explanation(fertilizer, lime_output)
    

    print("\n🌾 FINAL RECOMMENDATION REPORT")
    print("-" * 40)
    for k, v in report.items():
        print(f"{k}: {v}")
