package com.agrotech.ai.data.repository

import com.agrotech.ai.data.model.*
import com.agrotech.ai.data.remote.ApiService
import retrofit2.Response

class AgroRepository(private val apiService: ApiService) {

    suspend fun login(email: String, pass: String): Response<AuthResponse> = 
        apiService.login(mapOf("email" to email, "password" to pass))

    suspend fun signup(name: String, email: String, pass: String): Response<AuthResponse> = 
        apiService.signup(mapOf("name" to name, "email" to email, "password" to pass))

    suspend fun getWeather(lat: Double, lon: Double): Response<WeatherData> = 
        apiService.getCurrentWeather(lat, lon)

    suspend fun getCropRec(soilData: SoilData): Response<RecommendationResponse> = 
        apiService.getCropRecommendation(soilData)

    suspend fun getFertilizerRec(data: Map<String, Any>): Response<RecommendationResponse> = 
        apiService.getFertilizerRecommendation(data)

    suspend fun detectStress(base64Image: String): Response<StressDetectionResponse> = 
        apiService.detectStress(mapOf("image" to base64Image))

    suspend fun chat(query: String, lang: String): String {
        val response = apiService.queryChatbot(mapOf("query" to query, "lang" to lang))
        return response.body()?.get("response") ?: "Error connecting to AI"
    }
}
