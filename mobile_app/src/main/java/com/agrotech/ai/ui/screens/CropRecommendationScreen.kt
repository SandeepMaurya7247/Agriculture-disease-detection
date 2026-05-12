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
                    "Provide soil nutrients and climate data for accurate AI prediction",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f)) {
                                AgroTextField(
                                    value = n, 
                                    onValueChange = { n = it }, 
                                    label = "Nitrogen (N)", 
                                    leadingIcon = Icons.Default.Science,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                AgroTextField(
                                    value = p, 
                                    onValueChange = { p = it }, 
                                    label = "Phosphorous (P)", 
                                    leadingIcon = Icons.Default.Science,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f)) {
                                AgroTextField(
                                    value = k, 
                                    onValueChange = { k = it }, 
                                    label = "Potassium (K)", 
                                    leadingIcon = Icons.Default.Science,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                AgroTextField(
                                    value = ph, 
                                    onValueChange = { ph = it }, 
                                    label = "Soil pH", 
                                    leadingIcon = Icons.Default.Opacity,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f)) {
                                AgroTextField(
                                    value = humidity, 
                                    onValueChange = { humidity = it }, 
                                    label = "Humidity (%)", 
                                    leadingIcon = Icons.Default.Cloud,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                AgroTextField(
                                    value = rainfall, 
                                    onValueChange = { rainfall = it }, 
                                    label = "Rainfall (mm)", 
                                    leadingIcon = Icons.Default.WaterDrop,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        viewModel.getCropRecommendation(
                            SoilData(
                                moisture = 0.0,
                                nitrogen = n.toFloatOrNull() ?: 0f,
                                phosphorus = p.toFloatOrNull() ?: 0f,
                                potassium = k.toFloatOrNull() ?: 0f,
                                ph = ph.toFloatOrNull() ?: 0f,
                                humidity = humidity.toFloatOrNull() ?: 70f,
                                rainfall = rainfall.toFloatOrNull() ?: 100f
                            )
                        )
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
                            Text(strings.getRecommendation, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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

fun getCropImageUrl(crop: String): String {
    val cleanCrop = crop.trim().lowercase()
    return when (cleanCrop) {
        "rice" -> "https://cdn.pixabay.com/photo/2016/11/18/13/11/rice-1834366_1280.jpg"
        "maize" -> "https://cdn.pixabay.com/photo/2017/07/31/04/11/corn-2556353_1280.jpg"
        "chickpea" -> "https://cdn.pixabay.com/photo/2016/11/24/11/50/chickpeas-1856193_1280.jpg"
        "kidneybeans" -> "https://cdn.pixabay.com/photo/2014/10/24/15/45/red-beans-501254_1280.jpg"
        "pigeonpeas" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/0/05/Pigeon_peas_at_market.jpg/300px-Pigeon_peas_at_market.jpg"
        "mothbeans" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/9/91/Moth_bean_vigna_aconitifolia.jpg/300px-Moth_bean_vigna_aconitifolia.jpg"
        "mungbean" -> "https://cdn.pixabay.com/photo/2014/10/24/15/45/mung-beans-501253_1280.jpg"
        "blackgram" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/5/50/Vigna_mungo_seeds.jpg/300px-Vigna_mungo_seeds.jpg"
        "lentil" -> "https://cdn.pixabay.com/photo/2017/12/28/17/37/lentils-3045711_1280.jpg"
        "pomegranate" -> "https://cdn.pixabay.com/photo/2017/08/30/17/23/pomegranate-2697887_1280.jpg"
        "banana" -> "https://cdn.pixabay.com/photo/2018/09/24/20/12/bananas-3700718_1280.jpg"
        "mango" -> "https://cdn.pixabay.com/photo/2016/03/05/19/14/mango-1238332_1280.jpg"
        "grapes" -> "https://cdn.pixabay.com/photo/2016/03/05/19/02/grapes-1238260_1280.jpg"
        "watermelon" -> "https://cdn.pixabay.com/photo/2016/08/11/23/25/watermelon-1587213_1280.jpg"
        "muskmelon" -> "https://cdn.pixabay.com/photo/2017/06/02/18/24/melon-2367015_1280.jpg"
        "apple" -> "https://cdn.pixabay.com/photo/2016/01/19/17/41/apple-1149952_1280.jpg"
        "orange" -> "https://cdn.pixabay.com/photo/2016/01/02/02/03/orange-1117628_1280.jpg"
        "papaya" -> "https://cdn.pixabay.com/photo/2017/09/16/16/09/papaya-2755913_1280.jpg"
        "coconut" -> "https://cdn.pixabay.com/photo/2016/03/05/19/02/coconut-1238255_1280.jpg"
        "cotton" -> "https://cdn.pixabay.com/photo/2017/08/01/21/21/cotton-2568019_1280.jpg"
        "jute" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b5/Corchorus_olitorius.jpg/300px-Corchorus_olitorius.jpg"
        "coffee" -> "https://cdn.pixabay.com/photo/2016/11/19/12/54/coffee-1838933_1280.jpg"
        else -> "https://cdn.pixabay.com/photo/2016/11/19/15/40/agriculture-1840003_1280.jpg"
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
                        "AgriTech Solution AI", 
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
                Surface(
                    modifier = Modifier.size(110.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = cropName,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
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
                        style = MaterialTheme.typography.headlineLarge, 
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
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
                            text = "${item.feature} (impact: ${item.impact})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
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
