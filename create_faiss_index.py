from langchain_huggingface import HuggingFaceEmbeddings
from langchain_community.vectorstores import FAISS
from langchain_core.documents import Document
import os

# 1. Detailed Agricultural Knowledge Base (ICAR/Scientific standards)
agri_data = [
    "Fertilizer 14-35-14 is a complex fertilizer containing Nitrogen, Phosphorus, and Potassium. It is excellent for root development and initial growth stages of crops like Wheat and Maize.",
    "Urea (46% N) is a high-nitrogen fertilizer essential for vegetative growth and green foliage. Best applied as top-dressing.",
    "DAP (18-46-0) provides high phosphorus for strong root systems and better flowering. Used mostly as basal dose during sowing.",
    "MOP (Muriate of Potash) contains 60% Potassium, which helps in water regulation, pest resistance, and improving grain quality/weight.",
    "For Wheat cultivation, NPK 14-35-14 should be applied during sowing at 100-150 kg/ha for optimal soil nutrient balance.",
    "Soil pH between 6.0 to 7.5 is ideal for most cereal crops. If pH is low, apply lime; if high, apply gypsum.",
    "Chickpea (Gram) requires low nitrogen as it is a legume and can fix nitrogen from the air, but needs high phosphorus (P) for root nodule formation.",
    "Rice (Paddy) cultivation requires high water and balanced NPK. Zinc deficiency is common in rice, appearing as rusty brown spots on leaves.",
    "Organic farming tip: Use Neem Cake with Urea to reduce nitrogen loss (nitrification inhibition) and protect against soil pests.",
    "Potassium deficiency leads to yellowing or scorching of leaf margins, starting from the older leaves first."
]

# Create Documents
documents = [Document(page_content=text) for text in agri_data]

# 2. Embedding Model
MODEL_DIR = "saved_models/all-MiniLM-L6-v2"
embeddings = HuggingFaceEmbeddings(model_name="sentence-transformers/all-MiniLM-L6-v2")

# 3. Create and Save FAISS Index
print("Creating FAISS Index...")
vector_db = FAISS.from_documents(documents, embeddings)
vector_db.save_local("saved_models/faiss_index")

print("✅ FAISS Knowledge Base created at saved_models/faiss_index")
