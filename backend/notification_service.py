import firebase_admin
from firebase_admin import credentials, messaging
import os

class NotificationService:
    _instance = None
    _initialized = False

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super(NotificationService, cls).__new__(cls)
        return cls._instance

    def __init__(self):
        if not self._initialized:
            self.init_firebase()
            self._initialized = True

    def init_firebase(self):
        key_path = os.path.join(os.path.dirname(__file__), "firebase-key.json")
        if os.path.exists(key_path):
            try:
                cred = credentials.Certificate(key_path)
                firebase_admin.initialize_app(cred)
                print("[OK] Firebase Admin initialized successfully.")
                self.is_mock = False
            except Exception as e:
                print(f"[ERROR] Firebase Initialization Error: {e}")
                self.is_mock = True
        else:
            print("[WARN] firebase-key.json not found. Running in MOCK mode (Console only).")
            self.is_mock = True

    def send_to_topic(self, topic, title, body):
        if self.is_mock:
            print(f"\n[MOCK FCM] To Topic: {topic}")
            print(f"Title: {title}")
            print(f"Body: {body}\n")
            return True

        message = messaging.Message(
            notification=messaging.Notification(
                title=title,
                body=body,
            ),
            topic=topic,
        )
        try:
            response = messaging.send(message)
            print(f"Successfully sent message to topic {topic}: {response}")
            return True
        except Exception as e:
            print(f"Error sending FCM message: {e}")
            return False

# Singleton instance
notifier = NotificationService()
