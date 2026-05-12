package com.agrotech.ai.data.remote

import com.agrotech.ai.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: Map<String, String>): Response<AuthResponse>

    @POST("auth/signup")
    suspend fun signup(@Body request: Map<String, String>): Response<AuthResponse>

    @GET("weather/current")
    suspend fun getCurrentWeather(@Query("lat") lat: Double, @Query("lon") lon: Double): Response<WeatherData>

    @POST("recommend/crop")
    suspend fun getCropRecommendation(@Body data: SoilData): Response<RecommendationResponse>

    @POST("recommend/fertilizer")
    suspend fun getFertilizerRecommendation(@Body data: FertilizerRequest): Response<RecommendationResponse>

    @POST("detect/stress")
    suspend fun detectStress(@Body request: Map<String, String>): Response<StressDetectionResponse>

    @POST("ai/ask")
    suspend fun queryChatbot(@Body request: Map<String, String>): Response<Map<String, String>>
}
