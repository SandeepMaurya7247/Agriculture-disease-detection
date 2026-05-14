package com.agrotech.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.navigation.NavController
import com.agrotech.ai.viewmodel.AgroViewModel
import com.agrotech.ai.ui.components.*
import com.agrotech.ai.ui.theme.LocalAppStrings
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FutureRecommendationScreen(navController: NavController, viewModel: AgroViewModel) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val result by viewModel.futureRec.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorState.collectAsState()
    
    var selectedDays by remember { mutableStateOf(30) }
    
    // Soil Inputs
    var nValue by remember { mutableStateOf("") }
    var pValue by remember { mutableStateOf("") }
    var kValue by remember { mutableStateOf("") }
    var phValue by remember { mutableStateOf("") }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        ) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.getFutureRecommendation(
                            location.latitude, 
                            location.longitude, 
                            selectedDays,
                            nValue.toFloatOrNull() ?: 50f,
                            pValue.toFloatOrNull() ?: 50f,
                            kValue.toFloatOrNull() ?: 50f,
                            phValue.toFloatOrNull() ?: 6.5f
                        )
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Future Crop Planning", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "Plan Your Next Season",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Enter your soil details. We will fetch future weather automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AgroTextField(
                                value = nValue, onValueChange = { nValue = it },
                                label = "Nitrogen (N)", modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            AgroTextField(
                                value = pValue, onValueChange = { pValue = it },
                                label = "Phosphorus (P)", modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AgroTextField(
                                value = kValue, onValueChange = { kValue = it },
                                label = "Potassium (K)", modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            AgroTextField(
                                value = phValue, onValueChange = { phValue = it },
                                label = "Soil pH", modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }
                }
            }

            item {
                Text("Prediction Range:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedDays == 30,
                        onClick = { selectedDays = 30 },
                        label = { Text("30 Days") },
                        leadingIcon = if (selectedDays == 30) { { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) } } else null
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    FilterChip(
                        selected = selectedDays == 60,
                        onClick = { selectedDays = 60 },
                        label = { Text("60 Days") },
                        leadingIcon = if (selectedDays == 60) { { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) } } else null
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                AgroButton(
                    text = if (isLoading) "Analyzing Data..." else "Generate Future Suggestion",
                    onClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            error?.let {
                item {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            result?.let { rec ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(28.dp))
                                Spacer(Modifier.width(12.dp))
                                Text("FUTURE AI SUGGESTION", fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32), style = MaterialTheme.typography.labelLarge)
                            }
                            
                            Text(
                                text = rec.recommendation,
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1B5E20),
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                            
                            val weather = rec.weatherSummary
                            Text(
                                text = "Based on predicted trends for the next $selectedDays days.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )

                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Weather Stats Summary from Backend
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                WeatherFutureItem("Rainfall", "${weather?.get("total_rainfall")?.toInt() ?: 0}mm", Icons.Default.WaterDrop, Color(0xFF1976D2))
                                WeatherFutureItem("Humidity", "${weather?.get("avg_humidity")?.toInt() ?: 0}%", Icons.Default.Cloud, Color(0xFF00ACC1))
                                WeatherFutureItem("Temp", "${weather?.get("avg_temp")?.toInt() ?: 0}°C", Icons.Default.Thermostat, Color(0xFFF4511E))
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Text("Why this crop? (Predictive Reasoning)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                            Spacer(Modifier.height(8.dp))
                            
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    val reasons = rec.reasons ?: emptyList()
                                    if (reasons.isEmpty() && rec.whyThisCrop != null) {
                                        // Use LIME if available
                                        rec.whyThisCrop.forEach { item ->
                                            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(if(item.impact > 0) Icons.Default.CheckCircle else Icons.Default.Warning, null, tint = if(item.impact > 0) Color(0xFF43A047) else Color(0xFFF4511E), modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text(text = "${item.feature} (Impact: ${item.impact})", style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }
                                    } else {
                                        for (reason in reasons) {
                                            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF43A047), modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text(reason, style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text("Expert Agricultural Analysis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFC8E6C9))
                            
                            val explanation = rec.expertExplanation ?: "Generating ICAR-standard cultivation advice..."
                            Text(
                                text = buildAnnotatedString {
                                    val lines = explanation.split("\n")
                                    lines.forEach { line ->
                                        when {
                                            line.startsWith("**") && line.endsWith("**") -> {
                                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20), fontSize = 16.sp)) {
                                                    append(line.replace("**", "") + "\n")
                                                }
                                            }
                                            line.trim().startsWith("*") -> {
                                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                    append("• " + line.trim().substring(1).trim() + "\n")
                                                }
                                            }
                                            else -> {
                                                append(line + "\n")
                                            }
                                        }
                                    }
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 22.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherFutureItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.1f),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.padding(12.dp))
        }
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(label, fontSize = 10.sp, color = Color.Gray)
    }
}
