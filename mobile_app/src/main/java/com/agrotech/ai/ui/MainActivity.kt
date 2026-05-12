package com.agrotech.ai.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.agrotech.ai.ui.navigation.Screen
import com.agrotech.ai.ui.screens.*
import com.agrotech.ai.ui.theme.AgroTechTheme
import com.agrotech.ai.ui.theme.ProvideAppStrings
import com.agrotech.ai.data.remote.RetrofitClient
import com.agrotech.ai.data.repository.AgroRepository
import com.agrotech.ai.viewmodel.AgroViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.*
import com.agrotech.ai.ui.components.AgroBottomNavigation
import com.agrotech.ai.ui.screens.*

import android.view.WindowManager

class MainActivity : ComponentActivity() {
    private val sessionHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val sessionTimeout = 30 * 60 * 1000L // 30 Minutes

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(com.agrotech.ai.R.style.Theme_AgroTechAI)
        super.onCreate(savedInstanceState)
        
        // 🛠️ SESSION TERMINATION: Auto-close app after 30 minutes
        sessionHandler.postDelayed({
            finishAffinity() 
        }, sessionTimeout)

        // 🛡️ DEBUG MODE: Log the error instead of silently closing
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            android.util.Log.e("AgroTechCrash", "CRITICAL STARTUP ERROR", throwable)
            // For now, let it crash normally so we can see the error in Logcat or Dialog
        }

        // Ensure screenshots are allowed
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        
        // Required for WindowInsets (like keyboard detection) to work in Compose
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Manual DI for simplicity in this example
        val repository = AgroRepository(RetrofitClient.apiService)
        val viewModel = AgroViewModel(repository)

        setContent {
            val selectedLanguage by viewModel.selectedLanguage.collectAsState()
            AgroTechTheme {
                ProvideAppStrings(selectedLanguage) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AgroNavHost(viewModel)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionHandler.removeCallbacksAndMessages(null)
    }
}

@Composable
fun AgroNavHost(viewModel: AgroViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Check if keyboard is visible
    val isKeyboardVisible = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp
    
    val showBottomBar = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Learning.route,
        Screen.Profile.route,
        "crop_menu",
        Screen.StressDetection.route,
        Screen.FertilizerRecommendation.route,
        Screen.CropRecommendation.route,
        Screen.NDVIAnalysis.route,
        Screen.CropDetails.route
    ) && !isKeyboardVisible

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AgroBottomNavigation(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) { SplashScreen(navController) }
            composable(Screen.LanguageSelector.route) { LanguageSelectorScreen(navController, viewModel) }
            composable(Screen.Login.route) { LoginScreen(navController, viewModel) }
            composable(Screen.Signup.route) { SignupScreen(navController, viewModel) }
            composable(Screen.Dashboard.route) { DashboardScreen(navController, viewModel) }
            composable("crop_menu") { CropMenuScreen(navController) }
            composable(Screen.CropRecommendation.route) { CropRecommendationScreen(navController, viewModel) }
            composable(Screen.FertilizerRecommendation.route) { FertilizerRecommendationScreen(navController, viewModel) }
            composable(Screen.StressDetection.route) { StressDetectionScreen(navController, viewModel) }
            composable(Screen.NDVIAnalysis.route) { NDVIScreen(navController, viewModel) }
            composable(Screen.Learning.route) { LearningScreen(navController, viewModel) }
            composable(Screen.Chatbot.route) { ChatbotScreen(navController, viewModel) }
            composable(Screen.Profile.route) { ProfileScreen(navController, viewModel) }
            composable(Screen.CropDetails.route) { CropDetailsScreen(navController, viewModel) }
            composable(Screen.SmartIrrigation.route) { SmartIrrigationScreen(navController, viewModel) }
        }
    }
}
