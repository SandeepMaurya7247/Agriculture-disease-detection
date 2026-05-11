package com.agrotech.ai.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(com.agrotech.ai.R.style.Theme_AgroTechAI)
        super.onCreate(savedInstanceState)
        
        // Ensure screenshots are allowed (clearing any default security flags)
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        
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
}

@Composable
fun AgroNavHost(viewModel: AgroViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val showBottomBar = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Learning.route,
        Screen.Chatbot.route,
        Screen.Profile.route,
        "crop_menu",
        Screen.StressDetection.route,
        Screen.FertilizerRecommendation.route,
        Screen.CropRecommendation.route,
        Screen.NDVIAnalysis.route,
        Screen.CropDetails.route
    )

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
        }
    }
}
