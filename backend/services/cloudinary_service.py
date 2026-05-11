import cloudinary
import cloudinary.uploader
import cloudinary.api
import os
from dotenv import load_dotenv

load_dotenv()

# Configuration 
cloudinary.config( 
  cloud_name = os.getenv("CLOUDINARY_CLOUD_NAME"), 
  api_key = os.getenv("CLOUDINARY_API_KEY"), 
  api_secret = os.getenv("CLOUDINARY_API_SECRET"),
  secure = True
)

def upload_image(file_source):
    """
    Uploads an image to Cloudinary and returns the URL.
    file_source can be a local path or a base64 string (data:image/jpeg;base64,...)
    """
    try:
        response = cloudinary.uploader.upload(file_source, folder="agrotech_ai/stress_detection")
        return response.get("secure_url")
    except Exception as e:
        print(f"Error uploading to Cloudinary: {e}")
        return None
