import os
import tensorflow as tf
import numpy as np
import keras
from keras import layers
from keras.models import Sequential
from keras.applications import MobileNetV2
from keras.layers import GlobalAveragePooling2D, Dense, Dropout
from keras.src.legacy.preprocessing.image import ImageDataGenerator

# Setup Paths (Corrected for AgroTech AI root)
ML_DIR = os.path.dirname(os.path.abspath(__file__)) # backend/ml
BACKEND_DIR = os.path.dirname(ML_DIR) # backend
ROOT_DIR = os.path.dirname(BACKEND_DIR) # AgroTech AI root

data_root = os.path.join(ROOT_DIR, "data", "rice_leaf_diseases_split")
train_path = os.path.join(data_root, "train")
val_path   = os.path.join(data_root, "val")
test_path  = os.path.join(data_root, "test")

# Create model directory in backend/ml/model
model_dir = os.path.join(ML_DIR, "model")
if not os.path.exists(model_dir):
    os.makedirs(model_dir)

image_size = (224,224)
batch_size = 32

# ---------------- IMAGE GENERATORS ----------------
train_gen = ImageDataGenerator(rescale=1./255)
val_gen   = ImageDataGenerator(rescale=1./255)
test_gen  = ImageDataGenerator(rescale=1./255)

print(f"Loading data from: {train_path}")

train_data = train_gen.flow_from_directory(
    train_path,
    target_size=image_size,
    batch_size=batch_size,
    class_mode='categorical'
)

val_data = val_gen.flow_from_directory(
    val_path,
    target_size=image_size,
    batch_size=batch_size,
    class_mode='categorical'
)

test_data = test_gen.flow_from_directory(
    test_path,
    target_size=image_size,
    batch_size=batch_size,
    class_mode='categorical'
)

num_classes = train_data.num_classes

# Load pretrained MobileNetV2
base_model = MobileNetV2(
    weights="imagenet",
    include_top=False,
    input_shape=(224, 224, 3)
)
base_model.trainable = False

# Build final model
model = Sequential([
    base_model,
    GlobalAveragePooling2D(),
    Dense(128, activation="relu"),
    Dropout(0.5),
    Dense(num_classes, activation="softmax")
])

# ---------------- COMPILE ----------------
model.compile(
    optimizer=tf.keras.optimizers.Adam(learning_rate=0.0001),
    loss="categorical_crossentropy",
    metrics=["accuracy"]
)

# ---------------- TRAIN ----------------
print("Starting training for 3 epochs...")
history = model.fit(
    train_data,
    validation_data=val_data,
    epochs=3
)

model_h5_path = os.path.join(model_dir, 'crop_stress_mobilenet.h5')
model.save(model_h5_path)
print(f"Model saved to {model_h5_path}")

# ---------------- CONVERT TO TFLITE ----------------
print("Converting to TFLite...")
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()

tflite_model_path = os.path.join(model_dir, 'crop_stress_mobilenet.tflite')
with open(tflite_model_path, 'wb') as f:
    f.write(tflite_model)
print(f"✅ TFLite Model saved to {tflite_model_path}")

# ---------------- EVALUATE ----------------
test_loss, test_acc = model.evaluate(test_data)
print(f"Test Accuracy = {test_acc}")
print("Class Indices:", train_data.class_indices)