import os
from dotenv import load_dotenv
load_dotenv()
from ml import crop_rec_ml

def test():
    report = crop_rec_ml.generate_crop_report(80, 40, 40, 25.0, 70.0, 6.5, 100.0, lang='en')
    print("KEYS IN REPORT:", report.keys())
    print("RECOMMENDATION:", report.get("recommendation"))
    print("WHY THIS CROP:", report.get("why_this_crop"))
    print("EXPERT EXPLANATION LENGTH:", len(report.get("expert_explanation", "")))

if __name__ == "__main__":
    test()
