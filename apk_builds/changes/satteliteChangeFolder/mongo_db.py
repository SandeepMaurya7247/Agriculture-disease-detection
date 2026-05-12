import os
from dotenv import load_dotenv

load_dotenv()

MONGO_URI     = os.getenv("MONGO_URI", "")
MONGO_DB_NAME = os.getenv("MONGO_DB_NAME", "agrotechDatabase")

# ── Graceful init: don't crash if MONGO_URI is missing/placeholder ────────────
_PLACEHOLDER_URIS = {"", "mongodb+srv://<username>:<password>@<cluster>.mongodb.net/<dbname>"}

db = None
users_col        = None
reports_col      = None
stress_analysis_col = None

try:
    if MONGO_URI and MONGO_URI not in _PLACEHOLDER_URIS:
        from pymongo import MongoClient
        client  = MongoClient(MONGO_URI, serverSelectionTimeoutMS=5000)
        db      = client.get_database(MONGO_DB_NAME)
        users_col         = db.get_collection("users")
        reports_col       = db.get_collection("reports")
        stress_analysis_col = db.get_collection("stress_analysis")
        print("INFO  [MongoDB]: Connected to Atlas successfully.")
    else:
        print("WARN  [MongoDB]: MONGO_URI not set - running without database (demo mode).")
except Exception as e:
    print(f"WARN  [MongoDB]: Connection failed ({e}) - running without database.")


def get_user_by_email(email):
    if users_col is None:
        return None
    try:
        return users_col.find_one({"email": email})
    except Exception:
        return None

def create_user(user_data):
    if users_col is None:
        return type("FakeResult", (), {"inserted_id": "demo_id"})()
    try:
        return users_col.insert_one(user_data)
    except Exception:
        return type("FakeResult", (), {"inserted_id": "demo_id"})()

def save_report(report_data):
    if reports_col is None:
        return None
    try:
        return reports_col.insert_one(report_data)
    except Exception:
        return None

def save_stress_analysis(analysis_data):
    if stress_analysis_col is None:
        return None
    try:
        return stress_analysis_col.insert_one(analysis_data)
    except Exception:
        return None
