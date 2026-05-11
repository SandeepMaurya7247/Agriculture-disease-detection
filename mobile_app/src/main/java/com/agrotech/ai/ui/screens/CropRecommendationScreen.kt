package com.agrotech.ai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.agrotech.ai.data.model.SoilData
import com.agrotech.ai.ui.components.AgroButton
import com.agrotech.ai.ui.components.AgroTextField
import com.agrotech.ai.viewmodel.AgroViewModel
import com.agrotech.ai.ui.theme.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropRecommendationScreen(navController: NavController, viewModel: AgroViewModel) {
    val strings = LocalAppStrings.current
    var n by remember { mutableStateOf("") }
    var p by remember { mutableStateOf("") }
    var k by remember { mutableStateOf("") }
    var ph by remember { mutableStateOf("") }
    var humidity by remember { mutableStateOf("") }
    var rainfall by remember { mutableStateOf("") }

    val result by viewModel.cropRec.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.cropRec) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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

            AgroTextField(value = n, onValueChange = { n = it }, label = strings.nitrogen)
            Spacer(modifier = Modifier.height(8.dp))
            AgroTextField(value = p, onValueChange = { p = it }, label = strings.phosphorous)
            Spacer(modifier = Modifier.height(8.dp))
            AgroTextField(value = k, onValueChange = { k = it }, label = strings.potassium)
            Spacer(modifier = Modifier.height(8.dp))
            AgroTextField(value = ph, onValueChange = { ph = it }, label = strings.soilPh)
            Spacer(modifier = Modifier.height(8.dp))
            AgroTextField(value = humidity, onValueChange = { humidity = it }, label = strings.humidity)
            Spacer(modifier = Modifier.height(8.dp))
            AgroTextField(value = rainfall, onValueChange = { rainfall = it }, label = strings.rainfall)

            Spacer(modifier = Modifier.height(24.dp))

            AgroButton(text = strings.getRecommendation, onClick = {
                viewModel.getCropRecommendation(
                    SoilData(
                        moisture = 0.0, // Calculated from others or separate
                        nitrogen = n.toFloatOrNull() ?: 0f,
                        phosphorus = p.toFloatOrNull() ?: 0f,
                        potassium = k.toFloatOrNull() ?: 0f,
                        ph = ph.toFloatOrNull() ?: 0f
                    )
                )
            })

            if (result != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(strings.recommendedCrop, style = MaterialTheme.typography.labelSmall)
                        Text(result!!, style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        }
    }
}
