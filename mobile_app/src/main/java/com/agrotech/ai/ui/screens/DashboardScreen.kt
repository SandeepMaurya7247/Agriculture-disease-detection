package com.agrotech.ai.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import com.agrotech.ai.data.model.*
import androidx.compose.foundation.Canvas

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@SuppressLint("MissingPermission")
private fun fetchCurrentLocation(
    context: Context,
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    onLocationFound: (Double, Double) -> Unit
) {
    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener { location ->
            if (location != null) {
                onLocationFound(location.latitude, location.longitude)
            } else {
                // Fallback to Delhi if location is null
                onLocationFound(28.6139, 77.2090)
            }
        }
        .addOnFailureListener {
            onLocationFound(28.6139, 77.2090)
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, viewModel: AgroViewModel) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val weather by viewModel.weatherState.collectAsState()
    val user by viewModel.userState.collectAsState()
    val error by viewModel.errorState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    
    val analysisResult by viewModel.cropAnalysisResult.collectAsState()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        ) {
            // Permission granted, fetch location
            fetchCurrentLocation(context, fusedLocationClient) { lat, lon ->
                viewModel.fetchWeather(lat, lon)
            }
        }
    }

    // Fetch weather data on startup
    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
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
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo and Text together on the left
                    Image(
                        painter = painterResource(id = com.agrotech.ai.R.drawable.agro_logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        strings.appName, 
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ) 
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    val unreadCount by viewModel.unreadNotificationsCount.collectAsState()
                    
                    Box {
                        IconButton(
                            onClick = { navController.navigate(Screen.Notifications.route) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                if (unreadCount > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications, 
                                contentDescription = "Notifications", 
                                tint = if (unreadCount > 0) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary, 
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (unreadCount > 0) {
                            Surface(
                                shape = CircleShape,
                                color = Color.Red,
                                modifier = Modifier
                                    .size(10.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = 2.dp, y = (-2).dp)
                            ) {}
                        }
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
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.dashboard,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(
                                onClick = { 
                                    fetchCurrentLocation(context, fusedLocationClient) { lat, lon ->
                                        viewModel.fetchWeather(lat, lon)
                                    }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                if (isRefreshing) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        error?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        val iotState by viewModel.iotState.collectAsState()
                        Row(modifier = Modifier.fillMaxWidth()) {
                            MetricItem(
                                title = strings.soilMoisture,
                                value = "${iotState?.soil?.toInt() ?: "--"}%",
                                icon = Icons.Default.WaterDrop,
                                color = if (iotState?.decision == "START IRRIGATION") Color.Red else Color(0xFF1976D2),
                                modifier = Modifier.weight(1f).clickable { navController.navigate(Screen.SmartIrrigation.route) }
                            )
                            MetricItem(
                                title = "Location",
                                value = weather?.location ?: "Rewa",
                                icon = Icons.Default.LocationOn,
                                color = Color(0xFF388E3C),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            MetricItem(
                                title = "Temperature",
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

            // 3. Quick Actions Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AgroQuickAction(
                        title = "Future Suggest",
                        icon = Icons.Default.Timeline,
                        color = Color(0xFF673AB7),
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.FutureRecommendation.route) }
                    )
                    AgroQuickAction(
                        title = strings.cropInfo,
                        icon = Icons.Default.MenuBook,
                        color = Color(0xFF009688),
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.CropDetails.route) }
                    )
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
                NDVIMiniMap(
                    result = analysisResult,
                    onClick = { navController.navigate(Screen.NDVIAnalysis.route) }
                )
            }

            // 5. Seasonal Planning Tip
            item {
                SectionHeader(
                    title = "Future Planning", 
                    actionText = "Plan Now",
                    onActionClick = { navController.navigate(Screen.SeasonalPlanner.route) }
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clickable { navController.navigate(Screen.SeasonalPlanner.route) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.EventNote, null, tint = Color.White, modifier = Modifier.padding(8.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("1-2 Month Planning", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text("Predict best crops for the upcoming season.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }

            // 6. Market Price Search
            item {
                SectionHeader(
                    title = strings.marketPrice, 
                    actionText = strings.viewAll,
                    onActionClick = { navController.navigate(Screen.MarketPrice.route) }
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clickable { navController.navigate(Screen.MarketPrice.route) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)), // Light Amber
                    border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFA000), // Amber
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.CurrencyRupee, null, tint = Color.White, modifier = Modifier.padding(8.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(strings.marketPrice, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text("Check real-time government mandi prices.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowForwardIos, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
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
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
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
fun NDVIMiniMap(result: CropAnalysisResponse?, onClick: () -> Unit) {
    val healthScore = result?.healthScore ?: 0.0
    val prediction = result?.prediction ?: "No Analysis Data"
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
            // Procedural "Real Map" Background
            ProceduralNDVIMap(
                modifier = Modifier.fillMaxSize(),
                healthScore = healthScore,
                seed = if (result != null) (healthScore * 1000).toInt() else 42
            )
            
            // Subtle overlay gradient for depth
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f))
                        )
                    )
            )
            
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
@Composable
fun ProceduralNDVIMap(modifier: Modifier = Modifier, healthScore: Double = 0.5, seed: Int = 42) {
    val random = remember(seed) { java.util.Random(seed.toLong()) }
    
    Canvas(modifier = modifier) {
        val rows = 8 // Fewer rows for larger plots
        val cols = 10
        val cellWidth = size.width / cols
        val cellHeight = size.height / rows
        
        // Base "Soil/Grass" layer
        drawRect(color = Color(0xFFE8F5E9))

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val noise = random.nextFloat()
                // Bias towards healthy green for a "User Friendly" look
                val score = (healthScore * 0.8 + noise * 0.2).coerceIn(0.0, 1.0)
                
                val color = when {
                    score > 0.6 -> Color(0xFF2E7D32) // Healthy Lush Green
                    score > 0.4 -> Color(0xFF66BB6A) // Moderate Green
                    score > 0.2 -> Color(0xFF9CCC65) // Light Green / Young Crop
                    else -> Color(0xFFC0CA33)        // Slightly Dry / Yellow-Green
                }
                
                // Draw irregular field-like plots
                if (random.nextFloat() > 0.2) { // 80% coverage
                    drawRect(
                        color = color.copy(alpha = 0.9f),
                        topLeft = androidx.compose.ui.geometry.Offset(c * cellWidth + 2f, r * cellHeight + 2f),
                        size = androidx.compose.ui.geometry.Size(cellWidth - 4f, cellHeight - 4f)
                    )
                }
            }
        }
        
        // Add subtle "agricultural track" lines
        for (i in 0 until 5) {
            val offset = random.nextFloat() * size.width
            drawLine(
                color = Color.Black.copy(alpha = 0.05f),
                start = androidx.compose.ui.geometry.Offset(offset, 0f),
                end = androidx.compose.ui.geometry.Offset(offset + (random.nextFloat() * 40 - 20), size.height),
                strokeWidth = 4f
            )
        }
    }
}
