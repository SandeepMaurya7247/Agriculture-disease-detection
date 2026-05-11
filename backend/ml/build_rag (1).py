from langchain_community.document_loaders import PyPDFLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_community.vectorstores import FAISS

loader = PyPDFLoader("data/ICAR-AR 2025 Hindi.pdf")

docs = []
for i, doc in enumerate(loader.lazy_load()):
    docs.append(doc)
    if i == 100:   # adjust later
        break

splitter = RecursiveCharacterTextSplitter(
    chunk_size=800,
    chunk_overlap=100
)

chunks = splitter.split_documents(docs)

embeddings = HuggingFaceEmbeddings(
    model_name="saved_models/all-MiniLM-L6-v2"
)

db = FAISS.from_documents(chunks, embeddings)
db.save_local("saved_models/faiss_index")

print("✅ FAISS index built successfully")