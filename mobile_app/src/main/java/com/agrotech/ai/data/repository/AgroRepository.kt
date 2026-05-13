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

    suspend fun getFertilizerRec(data: FertilizerRequest): Response<RecommendationResponse> = 
    apiService.getFertilizerRecommendation(data)

    suspend fun detectStress(base64Image: String, lang: String): Response<StressDetectionResponse> = 
    apiService.detectStress(mapOf("image" to base64Image, "lang" to lang))

    suspend fun chat(query: String, lang: String): String {
        val response = apiService.queryChatbot(mapOf("query" to query, "lang" to lang))
        return response.body()?.get("response") ?: "Error connecting to AI"
    }

    suspend fun getLatestIot(): Response<IotResponse> = apiService.getLatestIot()

    suspend fun simulateIot(soil: Double, temp: Double) = apiService.simulateIot(soil, temp)

    suspend fun analyzeCrop(request: CropAnalysisRequest): Response<CropAnalysisResponse> =
    apiService.analyzeCrop(request)

    suspend fun getFutureRecommendation(lat: Double, lon: Double, days: Int, lang: String, n: Float, p: Float, k: Float, ph: Float): Response<RecommendationResponse> =
    apiService.getFutureRecommendation(lat, lon, days, lang, n, p, k, ph)

    suspend fun getMarketPrices(apiKey: String, state: String? = null, district: String? = null, commodity: String? = null): Response<MarketPriceResponse> =
    apiService.getMarketPrices(apiKey = apiKey, state = state, district = district, commodity = commodity)
}
