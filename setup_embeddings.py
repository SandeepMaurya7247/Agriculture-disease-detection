from sentence_transformers import SentenceTransformer
import os

MODEL_DIR = "saved_models/all-MiniLM-L6-v2"

# Download model properly (model files directly inside folder)
model = SentenceTransformer("sentence-transformers/all-MiniLM-L6-v2")
model.save(MODEL_DIR)

print("✅ Model saved correctly at:", MODEL_DIR)