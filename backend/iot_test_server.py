from flask import Flask, request, jsonify

app = Flask(__name__)
latest_sensor_data = {}

@app.route("/api/iot/data", methods=["POST"])
@app.route("/api/iot", methods=["POST"])  # Added to fix 404 from app
def receive_data():
    global latest_sensor_data
    try:
        latest_sensor_data = request.json or {}
        print("📥 Received Data:", latest_sensor_data)
        return jsonify({"status": "success", "message": "Data stored"})
    except Exception as e:
        print("❌ Error receiving data:", str(e))
        return jsonify({"status": "error", "message": str(e)}), 400

@app.route("/api/iot/latest", methods=["GET"])
def latest():
    return jsonify(latest_sensor_data)

# Catch-all to prevent 404 on other app calls
@app.errorhandler(404)
def page_not_found(e):
    return jsonify({"error": "Endpoint not found but server is ALIVE", "path": request.path}), 404

if __name__ == '__main__':
    print("AgroTech Robust Server running on http://0.0.0.0:5000")
    # debug=True enables hot reloading (restart on change)
    app.run(host="0.0.0.0", port=5000, debug=True)
