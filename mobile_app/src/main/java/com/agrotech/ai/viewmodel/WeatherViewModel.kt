package com.agrotech.ai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrotech.ai.data.model.WeatherData
import com.agrotech.ai.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _weatherState = MutableStateFlow<WeatherData?>(null)
    val weatherState: StateFlow<WeatherData?> = _weatherState

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    fun fetchWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            _isRefreshing.value = true
            _errorState.value = null
            try {
                val response = repository.getCurrentWeather(lat, lon)
                if (response.isSuccessful) {
                    _weatherState.value = response.body()
                } else {
                    _errorState.value = "Error: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                _errorState.value = "Network Error: ${e.localizedMessage}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
