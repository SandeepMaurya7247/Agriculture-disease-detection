import tensorflow as tf

# 1. Load the trained H5 model
h5_model_path = 'model/crop_stress_mobilenet.h5'
tflite_model_path = 'model/crop_stress_mobilenet.tflite'

print(f"Converting {h5_model_path} to TFLite...")

try:
    model = tf.keras.models.load_model(h5_model_path)

    # 2. Convert to TFLite
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    tflite_model = converter.convert()

    # 3. Save the TFLite model
    with open(tflite_model_path, 'wb') as f:
        f.write(tflite_model)

    print(f"✅ Success! Model saved as {tflite_model_path}")
    print("Now push this .tflite file to GitHub for Render deployment.")

except Exception as e:
    print(f"❌ Error during conversion: {e}")
    print("Make sure you have run dl.py first to generate the .h5 file.")
