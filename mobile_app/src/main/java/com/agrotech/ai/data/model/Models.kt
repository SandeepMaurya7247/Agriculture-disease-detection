package com.agrotech.ai.data.model

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
    val nitrogen: Float,
    val phosphorus: Float,
    val potassium: Float,
    val ph: Float
)

data class RecommendationResponse(
    val recommendation: String,
    val confidence: Float? = null,
    val details: String? = null
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
