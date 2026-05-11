import os
import requests
from pymongo import MongoClient
from dotenv import load_dotenv
import cloudinary
import cloudinary.uploader
import cloudinary.api

load_dotenv()

def verify():
    print("VERIFYING AGROTECH CLOUD INTEGRATION...\n")
    
    # 1. MongoDB Check
    try:
        mongo_uri = os.getenv("MONGO_URI")
        db_name = os.getenv("MONGO_DB_NAME", "agrotechDatabase")
        client = MongoClient(mongo_uri, serverSelectionTimeoutMS=5000)
        db = client.get_database(db_name)
        client.admin.command('ping')
        print(f"PASS MongoDB: Connected to '{db_name}'")
        
        counts = {
            "crop_data": db.crop_data.count_documents({}),
            "soil_nutrients": db.soil_nutrients.count_documents({}),
            "users": db.users.count_documents({}),
            "reports": db.reports.count_documents({})
        }
        for col, count in counts.items():
            print(f"   - {col}: {count} records")
    except Exception as e:
        print(f"FAIL MongoDB: Connection failed! ({e})")

    # 2. Cloudinary Check
    try:
        cloudinary.config(
            cloud_name=os.getenv("CLOUDINARY_CLOUD_NAME"),
            api_key=os.getenv("CLOUDINARY_API_KEY"),
            api_secret=os.getenv("CLOUDINARY_API_SECRET")
        )
        # Test with a ping-like API call
        cloudinary.api.ping()
        print("PASS Cloudinary: Configuration valid and API reachable")
    except Exception as e:
        print(f"FAIL Cloudinary: Configuration error! ({e})")

    # 3. Backend Server Check
    try:
        # Assuming server runs on localhost:5000
        r = requests.get("http://localhost:5000/api/weather/current", timeout=5)
        if r.status_code == 200:
            data = r.json()
            print(f"PASS Backend: Server is running on port 5000")
            print(f"   - Current Weather in {data.get('location')}: {data.get('temperature')}°C, {data.get('condition')}")
        else:
            print(f"WARN Backend: Server returned status {r.status_code}")
    except Exception as e:
        print(f"WARN Backend: Server is not responding on port 5000 ({e})")

    print("\nVERIFICATION FINISHED")

if __name__ == "__main__":
    verify()
