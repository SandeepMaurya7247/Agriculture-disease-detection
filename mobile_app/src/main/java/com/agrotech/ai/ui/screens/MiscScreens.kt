package com.agrotech.ai.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.agrotech.ai.ui.components.*
import com.agrotech.ai.ui.navigation.Screen
import com.agrotech.ai.viewmodel.AgroViewModel
import com.agrotech.ai.ui.theme.LocalAppStrings
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val strings = LocalAppStrings.current
    LaunchedEffect(Unit) {
        delay(2000)
        navController.navigate(Screen.Login.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary)) {
        Image(
            painter = painterResource(id = com.agrotech.ai.R.drawable.ttttt),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FertilizerRecommendationScreen(navController: NavController, viewModel: AgroViewModel) {
    val strings = LocalAppStrings.current
    var nitrogen by remember { mutableStateOf("") }
    var phosphorous by remember { mutableStateOf("") }
    var potassium by remember { mutableStateOf("") }
    var moisture by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("") }
    var humidity by remember { mutableStateOf("") }
    var soilType by remember { mutableStateOf("") }
    var cropType by remember { mutableStateOf("") }

    val fertResult by viewModel.fertilizerRec.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.fertRec) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(strings.enterDetails, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                AgroTextField(value = nitrogen, onValueChange = { nitrogen = it }, label = strings.nitrogen, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                AgroTextField(value = phosphorous, onValueChange = { phosphorous = it }, label = strings.phosphorous, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                AgroTextField(value = potassium, onValueChange = { potassium = it }, label = strings.potassium, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                AgroTextField(value = moisture, onValueChange = { moisture = it }, label = strings.moisture, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                AgroTextField(value = temperature, onValueChange = { temperature = it }, label = strings.temperature, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                AgroTextField(value = humidity, onValueChange = { humidity = it }, label = strings.humidity, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            AgroTextField(value = soilType, onValueChange = { soilType = it }, label = strings.soilType)
            Spacer(modifier = Modifier.height(8.dp))
            AgroTextField(value = cropType, onValueChange = { cropType = it }, label = strings.cropType)

            Spacer(modifier = Modifier.height(24.dp))
            AgroButton(
                text = if (isLoading) "Analyzing..." else strings.predictFertilizer,
                onClick = {
                    viewModel.getFertilizerRecommendation(
                        mapOf(
                            "n" to (nitrogen.toIntOrNull() ?: 0),
                            "p" to (phosphorous.toIntOrNull() ?: 0),
                            "k" to (potassium.toIntOrNull() ?: 0),
                            "moisture" to (moisture.toDoubleOrNull() ?: 0.0),
                            "temp" to (temperature.toDoubleOrNull() ?: 0.0),
                            "humidity" to (humidity.toDoubleOrNull() ?: 0.0),
                            "soil_type" to soilType,
                            "crop" to cropType
                        )
                    )
                }
            )

            if (fertResult != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Recommended Fertilizer:", style = MaterialTheme.typography.labelSmall)
                        Text(fertResult!!.recommendation, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        if (fertResult!!.details != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(fertResult!!.details!!, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NDVIScreen(navController: NavController, @Suppress("UNUSED_PARAMETER") viewModel: AgroViewModel) {
    val strings = LocalAppStrings.current
    Scaffold(topBar = { TopAppBar(title = { Text(strings.ndviMap) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp).background(Color.LightGray), contentAlignment = Alignment.Center) {
                Text(strings.satelliteMap)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("${strings.healthScore}: 0.82", fontWeight = FontWeight.Bold)
                    Text("The crop in Zone A shows optimal chlorophyll levels. Zone B requires attention.")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, viewModel: AgroViewModel) {
    val userState by viewModel.userState.collectAsState()

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // 1. Header with Gradient Background
            item {
                Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                                )
                            )
                    )
                    
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 0.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            modifier = Modifier
                                .size(100.dp)
                                .border(4.dp, Color.White, CircleShape),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Person, 
                                    null, 
                                    modifier = Modifier.size(60.dp), 
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            userState?.name ?: "Admin Farmer", 
                            style = MaterialTheme.typography.headlineSmall, 
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            userState?.email ?: "admin@agrotech.com", 
                            style = MaterialTheme.typography.bodySmall, 
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // 2. Stats Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val strings = LocalAppStrings.current
                    StatItem(strings.farmSize, "12.5", strings.acres)
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(MaterialTheme.colorScheme.outlineVariant))
                    StatItem(strings.crops, "4", strings.types)
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(MaterialTheme.colorScheme.outlineVariant))
                    StatItem(strings.experience, "8", strings.years)
                }
            }

            // 3. Action Cards (Expert Learning)
            item {
                val strings = LocalAppStrings.current
                SectionHeader(
                    title = strings.learning, 
                    actionText = strings.viewAll, 
                    modifier = Modifier.padding(horizontal = 8.dp),
                    onActionClick = { navController.navigate(Screen.Learning.route) }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { ExpertVideoCard("Modern Wheat Farming", "12:30", Color(0xFF1976D2)) { navController.navigate(Screen.Learning.route) } }
                    item { ExpertVideoCard("Organic Fertilizers", "08:45", Color(0xFF43A047)) { navController.navigate(Screen.Learning.route) } }
                    item { ExpertVideoCard("Pest Control Basics", "15:20", Color(0xFFD32F2F)) { navController.navigate(Screen.Learning.route) } }
                }
            }

            // 4. Settings Sections
            item {
                val strings = LocalAppStrings.current
                Spacer(modifier = Modifier.height(24.dp))
                
                SettingsGroup(title = strings.accountSettings) {
                    ProfileMenuItem(title = strings.farmProfiles, icon = Icons.Default.Landscape, description = strings.manageFieldData)
                    ProfileMenuItem(title = strings.overallHistory, icon = Icons.Default.History, description = strings.viewPastActivities)
                }

                SettingsGroup(title = strings.preferences) {
                    ProfileMenuItem(title = strings.languageSettings, icon = Icons.Default.Language, description = "Hindi, English, Punjabi")
                    ProfileMenuItem(title = strings.notification, icon = Icons.Default.NotificationsActive, description = strings.weatherAlerts)
                }

                SettingsGroup(title = strings.support) {
                    ProfileMenuItem(title = strings.helpCenter, icon = Icons.Default.HelpCenter)
                    ProfileMenuItem(title = strings.privacyPolicy, icon = Icons.Default.PrivacyTip)
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    AgroButton(
                        text = strings.logout,
                        onClick = { 
                            viewModel.setLanguage("en")
                            navController.navigate(Screen.Login.route) 
                        },
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
                    )
                }
                
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.width(2.dp))
            Text(unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontSize = 8.sp)
        }
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun ProfileMenuItem(title: String, icon: ImageVector, description: String? = null) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge) },
        supportingContent = if (description != null) { { Text(description, style = MaterialTheme.typography.bodySmall) } } else null,
        leadingContent = { 
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
        },
        trailingContent = { Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { /* Action */ }
    )
}

@Composable
fun ExpertVideoCard(title: String, duration: String, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(color.copy(alpha = 0.7f), color)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayCircleFilled, 
                    contentDescription = null, 
                    tint = Color.White, 
                    modifier = Modifier.size(48.dp)
                )
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                ) {
                    Text(
                        duration, 
                        color = Color.White, 
                        fontSize = 10.sp, 
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = title, 
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "Expert Tutorial", 
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropDetailsScreen(navController: NavController, @Suppress("UNUSED_PARAMETER") viewModel: AgroViewModel) {
    val crops = listOf(
        CropInfo("Wheat", "120 kg/ha N, 60 kg/ha P, 40 kg/ha K", "Rabi (Oct-Mar)", "Loamy, Clay Loam"),
        CropInfo("Rice", "120 kg/ha N, 60 kg/ha P, 40 kg/ha K", "Kharif (Jun-Nov)", "Clay, Silty Clay"),
        CropInfo("Maize", "150 kg/ha N, 75 kg/ha P, 50 kg/ha K", "Kharif/Rabi", "Sandy Loam, Loamy"),
        CropInfo("Cotton", "100 kg/ha N, 50 kg/ha P, 50 kg/ha K", "Kharif (Apr-Oct)", "Black Soil, Alluvial"),
        CropInfo("Sugarcane", "250 kg/ha N, 115 kg/ha P, 115 kg/ha K", "Feb-Mar planting", "Loamy, Clay Loam")
    )

    val strings = LocalAppStrings.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.cropInfo) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            crops.forEach { crop ->
                CropInfoCard(crop)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

data class CropInfo(val name: String, val nutrients: String, val season: String, val soilType: String)

@Composable
fun CropInfoCard(crop: CropInfo) {
    val strings = LocalAppStrings.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(crop.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Row { Icon(Icons.Default.Science, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text("${strings.nutrients} ${crop.nutrients}", style = MaterialTheme.typography.bodySmall) }
            Spacer(modifier = Modifier.height(4.dp))
            Row { Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text("${strings.season} ${crop.season}", style = MaterialTheme.typography.bodySmall) }
            Spacer(modifier = Modifier.height(4.dp))
            Row { Icon(Icons.Default.Grass, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text("${strings.soil} ${crop.soilType}", style = MaterialTheme.typography.bodySmall) }
        }
    }
}
