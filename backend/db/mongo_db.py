import os
from pymongo import MongoClient
from dotenv import load_dotenv

load_dotenv()

MONGO_URI = os.getenv("MONGO_URI")
MONGO_DB_NAME = os.getenv("MONGO_DB_NAME", "agrotechDatabase")
client = MongoClient(MONGO_URI)
db = client.get_database(MONGO_DB_NAME)

# Collections
users_col = db.get_collection("users")
reports_col = db.get_collection("reports")
stress_analysis_col = db.get_collection("stress_analysis")

def get_user_by_email(email):
    return users_col.find_one({"email": email})

def create_user(user_data):
    return users_col.insert_one(user_data)

def save_report(report_data):
    return reports_col.insert_one(report_data)

def save_stress_analysis(analysis_data):
    return stress_analysis_col.insert_one(analysis_data)
