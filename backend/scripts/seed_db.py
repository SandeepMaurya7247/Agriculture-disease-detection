import pandas as pd
import os
from pymongo import MongoClient
from dotenv import load_dotenv

load_dotenv()

MONGO_URI = os.getenv("MONGO_URI")
MONGO_DB_NAME = os.getenv("MONGO_DB_NAME", "agrotechDatabase")

def seed():
    try:
        client = MongoClient(MONGO_URI)
        db = client.get_database(MONGO_DB_NAME)
        
        print(f"Seeding data into {MONGO_DB_NAME}...")
        
        # 1. Seed Crop Recommendation Data
        crop_df = pd.read_csv("../data/Crop_recommendation.csv")
        crop_data = crop_df.to_dict(orient="records")
        db.crop_data.delete_many({}) # Clear old data
        db.crop_data.insert_many(crop_data)
        print(f"Inserted {len(crop_data)} crop recommendation records.")
        
        # 2. Seed Soil Nutrient Data
        soil_df = pd.read_csv("../data/Soil_Nutrient.csv")
        soil_data = soil_df.to_dict(orient="records")
        db.soil_nutrients.delete_many({}) # Clear old data
        db.soil_nutrients.insert_many(soil_data)
        print(f"Inserted {len(soil_data)} soil nutrient records.")
        
        print("Seeding completed successfully!")
        
    except Exception as e:
        print(f"Seeding failed: {e}")

if __name__ == "__main__":
    seed()
