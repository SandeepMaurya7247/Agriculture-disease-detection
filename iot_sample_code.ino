/* 
  AgroTech AI - IoT Sensor Sample Code (ESP32 / NodeMCU)
  This code sends soil moisture and temperature to the backend server.
*/

#include <WiFi.h>
#include <HTTPClient.h>

// 1. WiFi Settings
const char* ssid = "YOUR_WIFI_NAME";
const char* password = "YOUR_WIFI_PASSWORD";

// 2. Server Settings (AgroTech AI Backend)
const char* serverUrl = "http://10.91.148.102:5000/api/iot";

void setup() {
  Serial.begin(115200);
  WiFi.begin(ssid, password);
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi Connected!");
}

void loop() {
  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;
    
    // Simulate your sensor readings
    float moisture = 25.5; // Replace with your moisture sensor value
    float temperature = 28.0; // Replace with your temp sensor value
    
    // Create JSON payload
    String jsonPayload = "{\"soil\": " + String(moisture) + ", \"temp\": " + String(temperature) + "}";
    
    http.begin(serverUrl);
    http.addHeader("Content-Type", "application/json");
    
    int httpResponseCode = http.POST(jsonPayload);
    
    if (httpResponseCode > 0) {
      String response = http.getString();
      Serial.println("HTTP Response code: " + String(httpResponseCode));
      Serial.println("Server Response: " + response);
    } else {
      Serial.print("Error code: ");
      Serial.println(httpResponseCode);
    }
    
    http.end();
  }
  
  delay(5000); // Send data every 5 seconds
}
