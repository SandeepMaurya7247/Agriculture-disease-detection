package com.agrotech.ai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrotech.ai.data.model.*
import com.agrotech.ai.data.repository.AgroRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AgroViewModel(private val repository: AgroRepository) : ViewModel() {

    private val _weatherState = MutableStateFlow<WeatherData?>(null)
    val weatherState: StateFlow<WeatherData?> = _weatherState

    private val _userState = MutableStateFlow<User?>(User("1", "Admin Farmer", "admin@agrotech.com"))
    val userState: StateFlow<User?> = _userState

    private val _cropRec = MutableStateFlow<RecommendationResponse?>(null)
    val cropRec: StateFlow<RecommendationResponse?> = _cropRec

    private val _fertilizerRec = MutableStateFlow<RecommendationResponse?>(null)
    val fertilizerRec: StateFlow<RecommendationResponse?> = _fertilizerRec

    private val _stressResult = MutableStateFlow<StressDetectionResponse?>(null)
    val stressResult: StateFlow<StressDetectionResponse?> = _stressResult

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState

    // ── Selected language ──
    private val _selectedLanguage = MutableStateFlow("en")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _pendingChatQuery = MutableStateFlow<String?>(null)
    val pendingChatQuery: StateFlow<String?> = _pendingChatQuery.asStateFlow()

    fun setPendingChatQuery(query: String?) {
        _pendingChatQuery.value = query
    }

    fun setLanguage(code: String) {
        _selectedLanguage.value = code
    }

    fun login(email: String, pass: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            
            // LOCAL BYPASS for admin
            if (email == "admin@agrotech.com" && pass == "123456") {
                _userState.value = User("1", "Admin Farmer", email)
                onResult(null)
                _isLoading.value = false
                return@launch
            }

            try {
                val response = repository.login(email, pass)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        _userState.value = body.user
                        onResult(null) // Success
                    } else {
                        onResult(body?.error ?: "Invalid credentials")
                    }
                } else {
                    // Fallback for demo independence
                    _userState.value = User("101", email.split("@")[0], email)
                    onResult(null)
                }
            } catch (e: Exception) {
                // Connection error fallback: Allow login for demo
                _userState.value = User("101", email.split("@")[0], email)
                onResult(null)
            }
            _isLoading.value = false
        }
    }

    fun signup(name: String, email: String, pass: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.signup(name, email, pass)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        _userState.value = body.user
                        onResult(null) // Success
                    } else {
                        onResult(body?.error ?: "Registration failed")
                    }
                } else {
                    // Fallback
                    _userState.value = User("202", name, email)
                    onResult(null)
                }
            } catch (e: Exception) {
                // Connection error fallback
                _userState.value = User("202", name, email)
                onResult(null)
            }
            _isLoading.value = false
        }
    }

    fun fetchWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            _isRefreshing.value = true
            _errorState.value = null
            try {
                val response = repository.getWeather(lat, lon)
                if (response.isSuccessful) {
                    _weatherState.value = response.body()
                } else {
                    _errorState.value = "Failed to fetch weather: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorState.value = "Weather Sync Error: ${e.message ?: "Connection lost"}"
                e.printStackTrace()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun getCropRecommendation(soilData: SoilData) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getCropRec(soilData)
                if (response.isSuccessful) {
                    _cropRec.value = response.body()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _isLoading.value = false
        }
    }

    fun getFertilizerRecommendation(data: FertilizerRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorState.value = null
            _fertilizerRec.value = null // Clear old result
            try {
                val response = repository.getFertilizerRec(data)
                if (response.isSuccessful) {
                    _fertilizerRec.value = response.body()
                } else {
                    _errorState.value = "Server Error: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorState.value = "Network Error: ${e.message}"
                e.printStackTrace()
            }
            _isLoading.value = false
        }
    }

    fun detectStress(imageUri: String, context: android.content.Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Convert URI to Base64
                val base64Image = convertUriToBase64(imageUri, context)
                if (base64Image != null) {
                    val response = repository.detectStress(base64Image, selectedLanguage.value)
                    if (response.isSuccessful) {
                        _stressResult.value = response.body()
                    } else {
                        _errorState.value = "AI Analysis Failed: ${response.code()}"
                    }
                } else {
                    _errorState.value = "Failed to process image"
                }
            } catch (e: Exception) {
                _errorState.value = "Error: ${e.message}"
                e.printStackTrace()
            }
            _isLoading.value = false
        }
    }

    private fun convertUriToBase64(uriString: String, context: android.content.Context): String? {
        return try {
            val uri = android.net.Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun sendChatMessage(text: String, lang: String) {
        println("DEBUG: sendChatMessage called with: $text")
        viewModelScope.launch {
            try {
                val userMsg = ChatMessage(text, true)
                _chatMessages.value = _chatMessages.value + userMsg
                println("DEBUG: User message added to state")
                
                println("DEBUG: Calling repository.chat...")
                val aiResponse = repository.chat(text, lang)
                println("DEBUG: AI Response received: $aiResponse")
                
                val aiMsg = ChatMessage(aiResponse, false)
                _chatMessages.value = _chatMessages.value + aiMsg
            } catch (e: Exception) {
                println("DEBUG: Chat Error: ${e.message}")
                e.printStackTrace()
                _chatMessages.value = _chatMessages.value + ChatMessage("Connection Error: ${e.message}", false)
            }
        }
    }
}
