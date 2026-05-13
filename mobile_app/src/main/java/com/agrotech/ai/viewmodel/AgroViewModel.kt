package com.agrotech.ai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrotech.ai.data.model.*
import com.agrotech.ai.data.repository.AgroRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AgroViewModel(private val repository: AgroRepository) : ViewModel() {

    private val _weatherState = MutableStateFlow<WeatherData?>(null)
    val weatherState: StateFlow<WeatherData?> = _weatherState

    private val _userState = MutableStateFlow<User?>(User("1", "Admin Farmer", "admin@agrotech.com"))
    val userState: StateFlow<User?> = _userState

    private val _cropRec = MutableStateFlow<RecommendationResponse?>(null)
    val cropRec: StateFlow<RecommendationResponse?> = _cropRec.asStateFlow()

    private val _futureRecs = MutableStateFlow<Map<Int, RecommendationResponse>>(emptyMap())
    val futureRecs: StateFlow<Map<Int, RecommendationResponse>> = _futureRecs.asStateFlow()

    private val _fertilizerRec = MutableStateFlow<RecommendationResponse?>(null)
    val fertilizerRec = _fertilizerRec.asStateFlow()

    private val _futureRec = MutableStateFlow<RecommendationResponse?>(null)
    val futureRec = _futureRec.asStateFlow()

    private val _iotState = MutableStateFlow<IotData?>(null)
    val iotState = _iotState.asStateFlow()

    private val _iotHistory = MutableStateFlow<List<IotData>>(emptyList())
    val iotHistory = _iotHistory.asStateFlow()

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

    private val _cropAnalysisResult = MutableStateFlow<CropAnalysisResponse?>(null)
    val cropAnalysisResult = _cropAnalysisResult.asStateFlow()

    private val _isSatelliteLoading = MutableStateFlow(false)
    val isSatelliteLoading = _isSatelliteLoading.asStateFlow()

    private val _pendingChatQuery = MutableStateFlow<String?>(null)
    val pendingChatQuery: StateFlow<String?> = _pendingChatQuery.asStateFlow()

    // ── Notifications (Adopted from Remote) ──
    val notifications = com.agrotech.ai.data.local.NotificationManager.notifications
    val unreadNotificationsCount = com.agrotech.ai.data.local.NotificationManager.unreadCount

    // ── Expert Learning Lessons (My Feature) ──
    private val _lessons = MutableStateFlow<List<VideoLesson>>(
        listOf(
            VideoLesson(title = "Wheat Rust Management", expert = "Dr. S. K. Singh", duration = "12:45", crop = "Wheat"),
            VideoLesson(title = "Drip Irrigation Benefits", expert = "Engr. Ramesh Pal", duration = "08:20", crop = "General"),
            VideoLesson(title = "Organic Fertilizer Prep", expert = "Farmer Om Prakash", duration = "15:10", crop = "All Crops"),
            VideoLesson(title = "Rice Pest Control", expert = "Dr. Anita Devi", duration = "10:30", crop = "Rice"),
            VideoLesson(title = "Soil Health Testing", expert = "Dr. Vivek Mehra", duration = "14:00", crop = "General")
        )
    )
    val lessons: StateFlow<List<VideoLesson>> = _lessons.asStateFlow()

    private val _marketPrices = MutableStateFlow<List<MarketRecord>>(emptyList())
    val marketPrices: StateFlow<List<MarketRecord>> = _marketPrices.asStateFlow()

    init {
        startIotPolling()
    }

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

    fun calculateFutureRecommendations(soilData: SoilData) {
        viewModelScope.launch {
            _isLoading.value = true
            val recs = mutableMapOf<Int, RecommendationResponse>()
            
            // Current month (0)
            getCropRecommendation(soilData)
            
            // Simulate +1 and +2 months
            for (monthsAhead in 1..2) {
                try {
                    // Simple seasonal adjustment logic for demonstration
                    // In a real app, this would use a weather forecast API
                    val adjustedData = soilData.copy(
                        humidity = (soilData.humidity + (monthsAhead * 5)).coerceIn(30f, 95f),
                        rainfall = (soilData.rainfall + (monthsAhead * 20)).coerceIn(50f, 300f),
                        temperature = (soilData.temperature + (monthsAhead * 1)).coerceIn(15f, 45f)
                    )
                    
                    val response = repository.getCropRec(adjustedData)
                    if (response.isSuccessful) {
                        response.body()?.let { recs[monthsAhead] = it }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            _futureRecs.value = recs
            _isLoading.value = false
        }
    }

    fun getCropRecommendation(soilData: SoilData) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val dataWithLang = soilData.copy(lang = selectedLanguage.value)
                val response = repository.getCropRec(dataWithLang)
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
                val dataWithLang = data.copy(lang = selectedLanguage.value)
                val response = repository.getFertilizerRec(dataWithLang)
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

    fun simulateSensorData(soil: Double, temp: Double) {
        viewModelScope.launch {
            try {
                repository.simulateIot(soil, temp)
            } catch (e: Exception) {
                // Ignore simulation errors
            }
        }
    }

    private fun startIotPolling() {
        viewModelScope.launch {
            while (true) {
                try {
                    val response = repository.getLatestIot()
                    if (response.isSuccessful) {
                        val newData = response.body()?.data
                        _iotState.value = newData
                        
                        if (newData != null) {
                            val currentList = _iotHistory.value.toMutableList()
                            // Only add if it's a new timestamp to avoid duplicates
                            if (currentList.isEmpty() || currentList.last().timestamp != newData.timestamp) {
                                currentList.add(newData)
                                if (currentList.size > 10) currentList.removeAt(0)
                                _iotHistory.value = currentList
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore errors during polling
                }
                delay(10000)
            }
        }
    }

    fun analyzeCrop(lat: Double, lon: Double, radius: Double) {
        viewModelScope.launch {
            _isSatelliteLoading.value = true
            _cropAnalysisResult.value = null
            _errorState.value = null
            try {
                val request = CropAnalysisRequest(
                    latitude = lat,
                    longitude = lon,
                    radius = radius,
                    temperature = _weatherState.value?.temperature,
                    humidity = _weatherState.value?.humidity
                )
                val response = repository.analyzeCrop(request)
                if (response.isSuccessful) {
                    _cropAnalysisResult.value = response.body()
                } else {
                    _errorState.value = "Satellite Service Error: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorState.value = "Connection Error: ${e.message}"
            } finally {
                _isSatelliteLoading.value = false
            }
        }
    }

    fun getFutureRecommendation(lat: Double, lon: Double, days: Int, n: Float, p: Float, k: Float, ph: Float) {
        viewModelScope.launch {
            _isLoading.value = true
            _futureRec.value = null
            _errorState.value = null
            try {
                val response = repository.getFutureRecommendation(lat, lon, days, selectedLanguage.value, n, p, k, ph)
                if (response.isSuccessful) {
                    _futureRec.value = response.body()
                } else {
                    _errorState.value = "Future Sync Error: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorState.value = "Network Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ── Lessons Management (Adopted from My Feature) ──
    fun addLesson(lesson: VideoLesson) {
        _lessons.value = listOf(lesson) + _lessons.value
        addNotification("New Video Uploaded", "${lesson.expert} uploaded: ${lesson.title}", "EXPERT_VIDEO")
    }

    fun approveLesson(lessonId: String) {
        val current = _lessons.value.toMutableList()
        val index = current.indexOfFirst { it.id == lessonId }
        if (index != -1) {
            val updated = current[index].copy(status = "APPROVED")
            current[index] = updated
            _lessons.value = current
            
            addNotification("Video Approved", "Your video '${updated.title}' has been approved by admin.", "EXPERT_VIDEO")
        }
    }

    // ── Notifications Management (Bridging with Remote NotificationManager) ──
    fun addNotification(title: String, message: String, type: String) {
        com.agrotech.ai.data.local.NotificationManager.addNotification(
            AppNotification(title = title, message = message, type = type)
        )
    }

    fun markNotificationAsRead(id: String) {
        com.agrotech.ai.data.local.NotificationManager.markAsRead(id)
    }

    fun fetchMarketPrices(state: String? = null, district: String? = null, commodity: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Using the API key provided by the user
                val apiKey = "579b464db66ec23bdd000001be6e10d971234dfe4304ab08fe8fdb69" 
                val response = repository.getMarketPrices(apiKey, state, district, commodity)
                if (response.isSuccessful) {
                    _marketPrices.value = response.body()?.records ?: emptyList()
                } else {
                    _errorState.value = "Market Data Error: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorState.value = "Market Sync Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
