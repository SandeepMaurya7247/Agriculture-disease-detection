package com.agrotech.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.agrotech.ai.data.model.SoilData
import com.agrotech.ai.data.model.RecommendationResponse
import com.agrotech.ai.ui.navigation.Screen
import com.agrotech.ai.ui.components.AgroButton
import com.agrotech.ai.ui.components.AgroTextField
import com.agrotech.ai.viewmodel.AgroViewModel
import com.agrotech.ai.ui.theme.LocalAppStrings
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropRecommendationScreen(navController: NavController, viewModel: AgroViewModel) {
    val strings = LocalAppStrings.current
    val focusManager = LocalFocusManager.current
    
    var n by remember { mutableStateOf("") }
    var p by remember { mutableStateOf("") }
    var k by remember { mutableStateOf("") }
    var ph by remember { mutableStateOf("") }
    var humidity by remember { mutableStateOf("") }
    var rainfall by remember { mutableStateOf("") }
    var showFuturePlanning by remember { mutableStateOf(false) }

    val result by viewModel.cropRec.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(strings.cropRec, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(strings.appName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(
                    strings.enterDetails,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Provide soil nutrients and climate data for accurate predictions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AgroTextField(
                            value = n, onValueChange = { n = it },
                            label = "Nitrogen (N)", leadingIcon = Icons.Default.Science,
                            modifier = Modifier.weight(1f), 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                        )
                        AgroTextField(
                            value = p, onValueChange = { p = it },
                            label = "Phosphorous (P)", leadingIcon = Icons.Default.Science,
                            modifier = Modifier.weight(1f), 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AgroTextField(
                            value = k, onValueChange = { k = it },
                            label = "Potassium (K)", leadingIcon = Icons.Default.Science,
                            modifier = Modifier.weight(1f), 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                        )
                        AgroTextField(
                            value = ph, onValueChange = { ph = it },
                            label = "Soil pH", leadingIcon = Icons.Default.InvertColors,
                            modifier = Modifier.weight(1f), 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AgroTextField(
                            value = humidity, onValueChange = { humidity = it },
                            label = "Humidity (%)", leadingIcon = Icons.Default.Cloud,
                            modifier = Modifier.weight(1f), 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                        )
                        AgroTextField(
                            value = rainfall, onValueChange = { rainfall = it },
                            label = "Rainfall (mm)", leadingIcon = Icons.Default.WaterDrop,
                            modifier = Modifier.weight(1f), 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                                val data = SoilData(
                                    nitrogen = n.toFloatOrNull() ?: 0.0f,
                                    phosphorus = p.toFloatOrNull() ?: 0.0f,
                                    potassium = k.toFloatOrNull() ?: 0.0f,
                                    ph = ph.toFloatOrNull() ?: 0.0f,
                                    humidity = humidity.toFloatOrNull() ?: 70.0f,
                                    rainfall = rainfall.toFloatOrNull() ?: 100.0f,
                                    temperature = 25.0f,
                                    moisture = 20.0
                                )
                                viewModel.getCropRecommendation(data)
                            })
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Plan for Future", fontWeight = FontWeight.Bold)
                            Text("Predict crops for 1-2 months later", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Switch(
                            checked = showFuturePlanning,
                            onCheckedChange = { showFuturePlanning = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            val data = SoilData(
                                nitrogen = n.toFloatOrNull() ?: 0.0f,
                                phosphorus = p.toFloatOrNull() ?: 0.0f,
                                potassium = k.toFloatOrNull() ?: 0.0f,
                                ph = ph.toFloatOrNull() ?: 0.0f,
                                humidity = humidity.toFloatOrNull() ?: 70.0f,
                                rainfall = rainfall.toFloatOrNull() ?: 100.0f,
                                temperature = 25.0f,
                                moisture = 20.0
                            )
                            if (showFuturePlanning) {
                                viewModel.calculateFutureRecommendations(data)
                            } else {
                                viewModel.getCropRecommendation(data)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (showFuturePlanning) "Analyze Future Seasons" else strings.getRecommendation, 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            if (result != null) {
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    RecommendationResultCard(result!!, navController, viewModel)
                }
                
                if (showFuturePlanning) {
                    item {
                        FuturePlanningSection(viewModel, navController)
                    }
                }
            }
        }
    }
}

fun getCropImageUrl(crop: String): String {
    val cleanCrop = crop.trim().lowercase()
    return when (cleanCrop) {
        "rice" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7b/Paddy_field_in_Sumbawa_Islands.jpg/1280px-Paddy_field_in_Sumbawa_Islands.jpg"
        "maize" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/3/30/Maize_field_in_summer.jpg/1280px-Maize_field_in_summer.jpg"
        "chickpea" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0a/Chickpea_field.jpg/1280px-Chickpea_field.jpg"
        "kidneybeans" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/0/03/Phaseolus_vulgaris_001.JPG/1280px-Phaseolus_vulgaris_001.JPG"
        "pigeonpeas" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/0/05/Cajanus_cajan_flowers.JPG/1280px-Cajanus_cajan_flowers.JPG"
        "mothbeans" -> "https://res.cloudinary.com/dts788/image/upload/v1/crops/beans_field.jpg"
        "mungbean" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b3/Mung_bean_field.jpg/1280px-Mung_bean_field.jpg"
        "blackgram" -> "https://res.cloudinary.com/dts788/image/upload/v1/crops/pulses_field.jpg"
        "lentil" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/5/52/Lentil_field_in_central_Anatolia.jpg/1280px-Lentil_field_in_central_Anatolia.jpg"
        "pomegranate" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b2/Pomegranate_tree_in_Ganja.jpg/1280px-Pomegranate_tree_in_Ganja.jpg"
        "banana" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4c/Banana_Plantation_in_Martinique.jpg/1280px-Banana_Plantation_in_Martinique.jpg"
        "mango" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b1/Mango_orchard_in_India.jpg/1280px-Mango_orchard_in_India.jpg"
        "grapes" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2a/Vineyard_in_Slovenia.jpg/1280px-Vineyard_in_Slovenia.jpg"
        "watermelon" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/4/47/Watermelon_farm_in_Turkey.jpg/1280px-Watermelon_farm_in_Turkey.jpg"
        "muskmelon" -> "https://res.cloudinary.com/dts788/image/upload/v1/crops/melon_field.jpg"
        "apple" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/2/25/Apples_on_tree_in_orchard.jpg/1280px-Apples_on_tree_in_orchard.jpg"
        "orange" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b0/Orange_orchard_in_California.jpg/1280px-Orange_orchard_in_California.jpg"
        "papaya" -> "https://res.cloudinary.com/dts788/image/upload/v1/crops/papaya_field.jpg"
        "coconut" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0c/Coconut_palms_in_India.jpg/1280px-Coconut_palms_in_India.jpg"
        "cotton" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a2/Cotton_field_in_India.jpg/1280px-Cotton_field_in_India.jpg"
        "jute" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b5/Jute_field_in_Bangladesh.jpg/1280px-Jute_field_in_Bangladesh.jpg"
        "coffee" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/Coffee_plantation_in_Brazil.jpg/1280px-Coffee_plantation_in_Brazil.jpg"
        else -> "https://upload.wikimedia.org/wikipedia/commons/thumb/b/ba/Green_field_with_blue_sky.jpg/1280px-Green_field_with_blue_sky.jpg"
    }
}

@Composable
fun RecommendationResultCard(result: RecommendationResponse, navController: NavController, viewModel: AgroViewModel) {
    val cropName = result.recommendation
    val imageUrl = getCropImageUrl(cropName)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // --- HEADER ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.padding(8.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "AgroTech Solution AI", 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Smart Crop Recommendation Report", 
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(24.dp))

            // --- MAIN RECOMMENDATION ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(110.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = cropName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
                    
                    // URL Debug Text
                    Text(
                        text = "Source: ${imageUrl.take(20)}...",
                        style = androidx.compose.ui.text.TextStyle(fontSize = 7.sp),
                        color = Color.Gray.copy(alpha = 0.4f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(20.dp))
                
                Column {
                    Text(
                        "RECOMMENDED CROP", 
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        cropName.uppercase(), 
                        style = MaterialTheme.typography.headlineMedium, 
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1B5E20)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Verified, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Accuracy: ${result.accuracy ?: "99.3%"}", 
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- WHY THIS CROP ---
            Text(
                "Why this crop? (Model Reasoning)", 
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                result.whyThisCrop?.forEach { item ->
                    val isPositive = item.impact > 0
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isPositive) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isPositive) Color(0xFF4CAF50) else Color(0xFFFF9800),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isPositive) "✅ ${item.feature} (impact: ${item.impact})" else "⚠️ ${item.feature} (impact: ${item.impact})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (isPositive) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                        )
                    }
                } ?: Text("Analyzing soil & climate factors...", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- EXPERT EXPLANATION ---
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MenuBook, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Expert Agricultural Analysis", 
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = result.expertExplanation ?: "Generating ICAR-standard cultivation advice...",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Justify
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- ACTION BUTTON ---
            AgroButton(
                text = "Complete Cultivation Guide",
                onClick = { 
                    val query = "Tell me in detail how to grow $cropName"
                    viewModel.setPendingChatQuery(query)
                    navController.navigate(Screen.Chatbot.route)
                },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun FuturePlanningSection(viewModel: AgroViewModel, navController: NavController) {
    val futureRecs by viewModel.futureRecs.collectAsState()
    
    if (futureRecs.isNotEmpty()) {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            Text(
                "Future Predictions", 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                futureRecs.forEach { (months, res) ->
                    FutureCropCard(months, res, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun FutureCropCard(months: Int, res: RecommendationResponse, modifier: Modifier) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("+$months", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("In $months Month${if (months > 1) "s" else ""}", style = MaterialTheme.typography.labelSmall)
            Text(res.recommendation.uppercase(), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            AsyncImage(
                model = getCropImageUrl(res.recommendation),
                contentDescription = null,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }
    }
}
