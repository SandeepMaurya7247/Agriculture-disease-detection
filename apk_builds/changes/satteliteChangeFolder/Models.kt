package com.agrotech.ai.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: String,
    val name: String,
    val email: String,
    val farmSize: String? = null,
    val location: String? = null
)

data class AuthResponse(
    val success: Boolean,
    val token: String? = null,
    val user: User? = null,
    val error: String? = null
)

data class WeatherData(
    val temperature: Double? = null,
    val humidity: Double? = null,
    val condition: String? = null,
    val windSpeed: Double? = null,
    val iconUrl: String? = null
)

data class SoilData(
    val moisture: Double,
    @SerializedName("n") val nitrogen: Float,
    @SerializedName("p") val phosphorus: Float,
    @SerializedName("k") val potassium: Float,
    @SerializedName("ph") val ph: Float,
    @SerializedName("temp") val temperature: Float = 25.0f,
    val humidity: Float = 70.0f,
    val rainfall: Float = 100.0f
)

data class RecommendationResponse(
    val success: Boolean? = true,
    val recommendation: String,
    val confidence: Float? = null,
    val accuracy: String? = null,
    @SerializedName("why_this_crop") val whyThisCrop: List<LimeItem>? = null,
    @SerializedName("expert_explanation") val expertExplanation: String? = null,
    val details: String? = null,
    val deficiency: Map<String, Double>? = null,
    val schedule: List<ScheduleItem>? = null
)

data class LimeItem(
    val feature: String,
    val impact: Double
)

data class ScheduleItem(
    @SerializedName("Stage") val stage: String,
    @SerializedName("Quantity (kg/ha)") val quantity: Double
)

data class StressDetectionResponse(
    val label: String,
    val confidence: Float,
    val treatment: String
)

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class FertilizerRequest(
    val n: Int,
    val p: Int,
    val k: Int,
    val moisture: Double,
    val temp: Double,
    val humidity: Double,
    @SerializedName("soil_type") val soilType: String,
    @SerializedName("crop_type") val cropType: String
)

// ─────────────────────────────────────────────────────────────
// 🛰️  Satellite / NDVI Analysis  (POST /api/analyze-crop)
// ─────────────────────────────────────────────────────────────

data class CropAnalysisRequest(
    val latitude:    Double,
    val longitude:   Double,
    val radius:      Double,           // metres
    // Optional enrichment inputs (improves ML accuracy)
    val temperature: Double? = null,
    val humidity:    Double? = null,
    val rainfall:    Double? = null,
    @SerializedName("soil_ph") val soilPh: Double? = null
)

data class NdviStats(
    @SerializedName("mean_ndvi")   val meanNdvi:   Double,
    @SerializedName("max_ndvi")    val maxNdvi:    Double,
    @SerializedName("min_ndvi")    val minNdvi:    Double,
    @SerializedName("std_ndvi")    val stdNdvi:    Double,
    @SerializedName("pixel_count") val pixelCount: Int
)

data class CropHealthRecommendation(
    @SerializedName("irrigation_needed")          val irrigationNeeded:   Boolean,
    @SerializedName("irrigation_action")          val irrigationAction:   String,
    @SerializedName("nutrient_action")            val nutrientAction:     String,
    @SerializedName("field_action")               val fieldAction:        String,
    @SerializedName("next_satellite_check_days")  val nextCheckDays:      Int
)

data class CropAnalysisResponse(
    val success:           Boolean,
    val prediction:        String,           // e.g. "Healthy Vegetation"
    val confidence:        Double,
    val severity:          String,           // e.g. "🟢 Optimal"
    @SerializedName("ndvi_health_score") val healthScore: Double,  // 0-100
    @SerializedName("ndvi_stats")        val ndviStats:   NdviStats,
    val recommendation:    CropHealthRecommendation,
    val error:             String? = null
)
