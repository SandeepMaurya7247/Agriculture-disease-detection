import os
from pymongo import MongoClient
from dotenv import load_dotenv

load_dotenv()

try:
    MONGO_URI = os.getenv("MONGO_URI")
    MONGO_DB_NAME = os.getenv("MONGO_DB_NAME")
    print(f"Connecting to: {MONGO_DB_NAME}...")
    client = MongoClient(MONGO_URI)
    db = client.get_database(MONGO_DB_NAME)
    
    # Test connection
    client.admin.command('ping')
    print("✅ MongoDB Connection Successful!")
    
    # List collections
    collections = db.list_collection_names()
    print(f"Collections in {MONGO_DB_NAME}: {collections}")

except Exception as e:
    print(f"❌ Connection Failed: {e}")
