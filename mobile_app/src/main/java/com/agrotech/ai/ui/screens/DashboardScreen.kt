package com.agrotech.ai.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.viewinterop.AndroidView
import com.agrotech.ai.ui.components.*
import com.agrotech.ai.ui.navigation.Screen
import com.agrotech.ai.viewmodel.AgroViewModel
import com.agrotech.ai.ui.theme.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, viewModel: AgroViewModel) {
    val strings = LocalAppStrings.current
    val weather by viewModel.weatherState.collectAsState()
    val user by viewModel.userState.collectAsState()
    
    // Fetch weather data on startup
    LaunchedEffect(Unit) {
        viewModel.fetchWeather(28.6139, 77.2090) // Default to New Delhi
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp), // Increased height
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left side spacer to balance the notification icon on the right
                    Spacer(modifier = Modifier.size(24.dp))
                    
                    Text(
                        strings.appName, 
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    ) 
                    
                    IconButton(
                        onClick = { /* Handle Notifications */ },
                        modifier = Modifier.size(32.dp) // Smaller button area
                    ) {
                        Icon(
                            Icons.Default.Notifications, 
                            contentDescription = "Notifications", 
                            tint = MaterialTheme.colorScheme.primary, 
                            modifier = Modifier.size(20.dp) // Smaller icon
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.Chatbot.route) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Chat, contentDescription = "AI Assistant")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 1. Welcome Banner
            item {
                WelcomeBanner(user?.name ?: "Farmer")
            }

            // Unified Field Data Card with Title Inside
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = strings.dashboard,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Row(modifier = Modifier.fillMaxWidth()) {
                            MetricItem(
                                title = strings.soilMoisture,
                                value = "42%",
                                icon = Icons.Default.WaterDrop,
                                color = Color(0xFF1976D2),
                                modifier = Modifier.weight(1f)
                            )
                            MetricItem(
                                title = strings.healthScore,
                                value = "0.78",
                                icon = Icons.Default.Eco,
                                color = Color(0xFF388E3C),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            MetricItem(
                                title = strings.weather,
                                value = "${weather?.temperature ?: "--"}°C",
                                icon = Icons.Default.Thermostat,
                                color = Color(0xFFFF7043),
                                modifier = Modifier.weight(1f)
                            )
                            MetricItem(
                                title = strings.humidity,
                                value = "${weather?.humidity ?: "--"}%",
                                icon = Icons.Default.Cloud,
                                color = Color(0xFF26C6DA),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                        Row(modifier = Modifier.fillMaxWidth()) {
                            MetricItem(
                                title = strings.windSpeed,
                                value = "${weather?.windSpeed ?: "--"} km/h",
                                icon = Icons.Default.Air,
                                color = Color(0xFF78909C),
                                modifier = Modifier.weight(1f)
                            )
                            MetricItem(
                                title = strings.condition,
                                value = weather?.condition ?: "Clear",
                                icon = Icons.Default.WbSunny,
                                color = Color(0xFFFBC02D),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 4. NDVI Mini-map
            item {
                SectionHeader(
                    title = strings.ndviTitle, 
                    actionText = strings.viewFullMap,
                    modifier = Modifier.padding(bottom = 0.dp),
                    onActionClick = { navController.navigate(Screen.NDVIAnalysis.route) }
                )
                NDVIMiniMap(onClick = { navController.navigate(Screen.NDVIAnalysis.route) })
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun WelcomeBanner(name: String) {
    val strings = LocalAppStrings.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .height(200.dp),
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = com.agrotech.ai.R.drawable.dash_ui),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                        )
                    )
            )
            
            Column(
                modifier = Modifier.padding(20.dp).align(Alignment.CenterStart)
            ) {
                Text(
                    text = strings.welcomeBack,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "$name!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = strings.cropsHealthy,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NDVIMiniMap(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .height(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF2E7D32), Color(0xFF81C784), Color(0xFFFBC02D))
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text("Field #1: Active Analysis", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Click to expand NDVI View", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
            }
            
            Icon(
                imageVector = Icons.Default.Fullscreen,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).size(28.dp)
            )
        }
    }
}

@Composable
fun MetricItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.1f),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.padding(10.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
