# ---------------- MODEL BUILDING (MobileNet) ----------------
import tensorflow as tf
import numpy as np
import keras
from keras import layers
from keras.models import Sequential
from keras.applications import MobileNetV2
from keras.preprocessing.image import load_img, img_to_array
from keras.applications.mobilenet_v2 import preprocess_input
import numpy as np

# Specific imports for your Aero-Agro Insights project
from keras.layers import GlobalAveragePooling2D, Dense, Dropout
import keras
from keras.src.legacy.preprocessing.image import ImageDataGenerator

image_size = (224,224)
batch_size = 32

train_path = "data/rice_leaf_diseases_split/train"
val_path   = "data/rice_leaf_diseases_split/val"
test_path  = "data/rice_leaf_diseases_split/test"

# ---------------- IMAGE GENERATORS ----------------
train_gen = ImageDataGenerator(rescale=1./255)
val_gen   = ImageDataGenerator(rescale=1./255)
test_gen  = ImageDataGenerator(rescale=1./255)

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


num_classes = train_data.num_classes  # automatically detects classes

# Load pretrained MobileNetV2 (feature extractor)
base_model = MobileNetV2(
    weights="imagenet",
    include_top=False,
    input_shape=(224, 224, 3)
)

# Freeze base model layers
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
history = model.fit(
    train_data,
    validation_data=val_data,
    epochs=10
)

model.save('model/crop_stress_mobilenet.h5')

# ---------------- EVALUATE ----------------
test_loss, test_acc = model.evaluate(test_data)
print(f"Test Accuracy = {test_acc}")

print("class index" , train_data.class_indices)