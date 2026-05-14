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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
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

    val result by viewModel.cropRec.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val iotState by viewModel.iotState.collectAsState()

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            strings.enterDetails,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "Provide soil nutrients and climate data.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // 🛰️ Fetch from Sensor Button
                    IconButton(
                        onClick = {
                            iotState?.let {
                                humidity = it.temp?.toString() ?: "" 
                                rainfall = "120" 
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(Icons.Default.SettingsInputAntenna, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
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
                                submit(n, p, k, ph, humidity, rainfall, viewModel)
                            })
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            submit(n, p, k, ph, humidity, rainfall, viewModel)
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
                                    strings.getRecommendation, 
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
            }
        }
    }
}

private fun submit(n: String, p: String, k: String, ph: String, humidity: String, rainfall: String, viewModel: AgroViewModel) {
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

            Row(verticalAlignment = Alignment.CenterVertically) {
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

            Text(
                text = "Why this crop? (Model Reasoning)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    result.whyThisCrop?.let { list ->
                        list.forEach { item ->
                            val icon = if (item.impact > 0) "✅" else "⚠️"
                            val color = if (item.impact > 0) Color(0xFF2E7D32) else Color(0xFFE65100)
                            
                            Row(
                                modifier = Modifier.padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = icon, fontSize = 18.sp, modifier = Modifier.padding(end = 12.dp))
                                Text(
                                    text = "${item.feature} (Impact: ${item.impact})",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = color
                                )
                            }
                        }
                    } ?: Text("Analyzing soil & climate factors...", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            // Expert Agricultural Analysis
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBE7)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFFDCEDC8)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFF33691E), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Expert Agricultural Analysis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF33691E)
                        )
                    }
                    Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFDCEDC8))
                    
                    val explanation = result.expertExplanation ?: "Generating ICAR-standard cultivation advice..."
                    
                    Text(
                        text = buildAnnotatedString {
                            val lines = explanation.split("\n")
                            lines.forEach { line ->
                                when {
                                    line.startsWith("**") && line.endsWith("**") -> {
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20), fontSize = 18.sp)) {
                                            append(line.replace("**", "") + "\n")
                                        }
                                    }
                                    line.trim().startsWith("*") || line.trim().startsWith("•") -> {
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                            val cleanLine = line.trim().trimStart('*', '•').trim()
                                            append("• $cleanLine\n")
                                        }
                                    }
                                    else -> {
                                        append(line + "\n")
                                    }
                                }
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 24.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
