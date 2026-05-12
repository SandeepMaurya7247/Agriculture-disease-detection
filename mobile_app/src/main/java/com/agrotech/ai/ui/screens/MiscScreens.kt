package com.agrotech.ai.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
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
import com.agrotech.ai.data.model.*
import com.agrotech.ai.ui.theme.LocalAppStrings
import kotlinx.coroutines.delay
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction

@Composable
fun SplashScreen(navController: NavController) {
    val strings = LocalAppStrings.current
    LaunchedEffect(Unit) {
        delay(2000)
        navController.navigate(Screen.Login.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = com.agrotech.ai.R.drawable.ttttt),
                contentDescription = "AgroTech Logo",
                modifier = Modifier.size(200.dp).clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
        }
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
    
    // Dropdown States
    var soilType by remember { mutableStateOf("Loamy") }
    var cropType by remember { mutableStateOf("Wheat") }
    var soilExpanded by remember { mutableStateOf(false) }
    var cropExpanded by remember { mutableStateOf(false) }

    val soilTypes = listOf("Sandy", "Loamy", "Black", "Red", "Clay")
    val cropTypes = listOf("Wheat", "Rice", "Maize", "Cotton", "Sugarcane", "Pulses", "Tobacco", "Oil seeds", "Barley", "Millets")

    val fertResult by viewModel.fertilizerRec.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.fertRec, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = strings.enterDetails,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Provide soil test values for precise results",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // N, P, K Row
            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    AgroTextField(
                        value = nitrogen, 
                        onValueChange = { if(it.length <= 3) nitrogen = it }, 
                        label = "N (Nitrogen)", 
                        modifier = Modifier.weight(1f),
                        leadingIcon = Icons.Default.Science,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    AgroTextField(
                        value = phosphorous, 
                        onValueChange = { if(it.length <= 3) phosphorous = it }, 
                        label = "P (Phos.)", 
                        modifier = Modifier.weight(1f),
                        leadingIcon = Icons.Default.Science,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // K, Moisture Row
            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    AgroTextField(
                        value = potassium, 
                        onValueChange = { if(it.length <= 3) potassium = it }, 
                        label = "K (Potass.)", 
                        modifier = Modifier.weight(1f),
                        leadingIcon = Icons.Default.Science,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    AgroTextField(
                        value = moisture, 
                        onValueChange = { if(it.length <= 3) moisture = it }, 
                        label = "Moisture", 
                        modifier = Modifier.weight(1f),
                        leadingIcon = Icons.Default.WaterDrop,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Temp, Humidity Row
            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    AgroTextField(
                        value = temperature, 
                        onValueChange = { temperature = it }, 
                        label = "Temp (°C)", 
                        modifier = Modifier.weight(1f),
                        leadingIcon = Icons.Default.Thermostat,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    AgroTextField(
                        value = humidity, 
                        onValueChange = { humidity = it }, 
                        label = "Humidity", 
                        modifier = Modifier.weight(1f),
                        leadingIcon = Icons.Default.Cloud,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Dropdowns
            item {
                Text("Environmental Context", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                ExposedDropdownMenuBox(
                    expanded = soilExpanded,
                    onExpandedChange = { soilExpanded = !soilExpanded }
                ) {
                    OutlinedTextField(
                        value = soilType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Soil Type") },
                        leadingIcon = { Icon(Icons.Default.Landscape, null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = soilExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = soilExpanded, onDismissRequest = { soilExpanded = false }) {
                        soilTypes.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = { soilType = selectionOption; soilExpanded = false }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = cropExpanded,
                    onExpandedChange = { cropExpanded = !cropExpanded }
                ) {
                    OutlinedTextField(
                        value = cropType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Crop Type") },
                        leadingIcon = { Icon(Icons.Default.Agriculture, null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cropExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = cropExpanded, onDismissRequest = { cropExpanded = false }) {
                        cropTypes.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = { cropType = selectionOption; cropExpanded = false }
                            )
                        }
                    }
                }
            }

            item {
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                val context = androidx.compose.ui.platform.LocalContext.current
                Spacer(modifier = Modifier.height(16.dp))
                AgroButton(
                    text = if (isLoading) "Analyzing Soil Data..." else strings.predictFertilizer,
                    onClick = {
                        // DIAGNOSTIC TOAST
                        android.widget.Toast.makeText(context, "Requesting AI Recommendation...", android.widget.Toast.LENGTH_SHORT).show()
                        
                        viewModel.getFertilizerRecommendation(
                            FertilizerRequest(
                                n = (nitrogen.toIntOrNull() ?: 0),
                                p = (phosphorous.toIntOrNull() ?: 0),
                                k = (potassium.toIntOrNull() ?: 0),
                                moisture = (moisture.toDoubleOrNull() ?: 0.0),
                                temp = (temperature.toDoubleOrNull() ?: 0.0),
                                humidity = (humidity.toDoubleOrNull() ?: 0.0),
                                soilType = soilType,
                                cropType = cropType
                            )
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                )
            }

            if (fertResult != null) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // --- PREMIUM FERTILIZER DASHBOARD ---
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)), // Premium Lavender
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            // Header
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF4CAF50),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.padding(6.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "AI Recommended Solution", 
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4A148C)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Fertilizer Name
                            Text(
                                text = fertResult!!.recommendation,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF4A148C)
                            )
                            Text(
                                text = fertResult!!.details ?: "Optimal for your soil & crop",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // 1. Nutrient Deficiency Section
                            Text(
                                "Nutrient Deficiency (kg/ha)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val n = fertResult!!.deficiency?.get("N") ?: 0.0
                                val p = fertResult!!.deficiency?.get("P") ?: 0.0
                                val k = fertResult!!.deficiency?.get("K") ?: 0.0
                                
                                NutrientBadge("N", n, Color(0xFFE91E63), Modifier.weight(1f))
                                NutrientBadge("P", p, Color(0xFFFF9800), Modifier.weight(1f))
                                NutrientBadge("K", k, Color(0xFF2196F3), Modifier.weight(1f))
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            Divider(color = Color.Black.copy(alpha = 0.05f))
                            Spacer(modifier = Modifier.height(16.dp))

                            // 2. Application Schedule Timeline
                            Text(
                                "Application Schedule",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4A148C)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            fertResult!!.schedule?.forEachIndexed { index, item ->
                                ScheduleRow(
                                    stage = item.stage,
                                    qty = item.quantity,
                                    isLast = index == (fertResult!!.schedule?.size ?: 0) - 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NutrientBadge(label: String, value: Double, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontWeight = FontWeight.Bold, color = color, fontSize = 12.sp)
            Text(value.toInt().toString(), fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.DarkGray)
        }
    }
}

@Composable
fun ScheduleRow(stage: String, qty: Double, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF4A148C),
                modifier = Modifier.size(10.dp)
            ) {}
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(Color(0xFF4A148C).copy(alpha = 0.2f))
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(stage, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text("$qty kg/ha", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
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
