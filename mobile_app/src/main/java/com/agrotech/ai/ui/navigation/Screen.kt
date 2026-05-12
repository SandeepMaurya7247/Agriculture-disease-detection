package com.agrotech.ai.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object LanguageSelector : Screen("language_selector")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Dashboard : Screen("dashboard")
    object CropRecommendation : Screen("crop_rec")
    object FertilizerRecommendation : Screen("fert_rec")
    object StressDetection : Screen("stress_detect")
    object NDVIAnalysis : Screen("ndvi")
    object Chatbot : Screen("chatbot")
    object Learning : Screen("learning")
    object CropDetails : Screen("crop_details")
    object Profile : Screen("profile")
    object SmartIrrigation : Screen("smart_irrigation")
}

