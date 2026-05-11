package com.agrotech.ai.data.repository

import com.agrotech.ai.data.model.WeatherData
import com.agrotech.ai.data.remote.RetrofitClient
import retrofit2.Response

class WeatherRepository {
    private val apiService = RetrofitClient.apiService

    suspend fun getCurrentWeather(lat: Double, lon: Double): Response<WeatherData> {
        return apiService.getCurrentWeather(lat, lon)
    }
}
