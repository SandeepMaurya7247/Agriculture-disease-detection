package com.agrotech.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.agrotech.ai.ui.components.AgroButton
import com.agrotech.ai.ui.components.AgroTextField
import com.agrotech.ai.ui.navigation.Screen
import com.agrotech.ai.viewmodel.AgroViewModel
import com.agrotech.ai.ui.theme.LocalAppStrings
import com.agrotech.ai.ui.theme.AppStrings
import com.agrotech.ai.data.model.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Agriculture

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonalPlannerScreen(navController: NavController, viewModel: AgroViewModel) {
    val strings = LocalAppStrings.current
    var selectedTimeframe by remember { mutableStateOf(1) }
    var showDetails by remember { mutableStateOf(false) }
    var selectedResult by remember { mutableStateOf<Pair<Int, RecommendationResponse>?>(null) }

    var n by remember { mutableStateOf("80") }
    var p by remember { mutableStateOf("40") }
    var k by remember { mutableStateOf("40") }
    var ph by remember { mutableStateOf("6.5") }

    val futureRecs by viewModel.futureRecs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.seasonalPlanner, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, null, tint = Color.White, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = strings.futurePlanning,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = strings.planningDesc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Science, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(strings.soilNutrientsPlanning, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            AgroTextField(value = n, onValueChange = { n = it }, label = "N", modifier = Modifier.weight(1f))
                            AgroTextField(value = p, onValueChange = { p = it }, label = "P", modifier = Modifier.weight(1f))
                            AgroTextField(value = k, onValueChange = { k = it }, label = "K", modifier = Modifier.weight(1f))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text(strings.whenToSow, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 4.dp))
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TimeframeCard(
                        months = 1,
                        label = strings.afterOneMonth,
                        isSelected = selectedTimeframe == 1,
                        onClick = { selectedTimeframe = 1 },
                        modifier = Modifier.weight(1f)
                    )
                    TimeframeCard(
                        months = 2,
                        label = strings.afterTwoMonth,
                        isSelected = selectedTimeframe == 2,
                        onClick = { selectedTimeframe = 2 },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Button(
                    onClick = {
                        val data = SoilData(
                            nitrogen = n.toFloatOrNull() ?: 80f,
                            phosphorus = p.toFloatOrNull() ?: 40f,
                            potassium = k.toFloatOrNull() ?: 40f,
                            ph = ph.toFloatOrNull() ?: 6.5f,
                            humidity = 70f,
                            rainfall = 100f,
                            temperature = 25f,
                            moisture = 20.0
                        )
                        viewModel.calculateFutureRecommendations(data)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, null)
                        Spacer(Modifier.width(8.dp))
                        Text(strings.seeFuturePrediction, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(strokeWidth = 3.dp)
                    }
                }
            } else if (futureRecs.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(
                        strings.predictedResults, 
                        style = MaterialTheme.typography.titleLarge, 
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(futureRecs.toList()) { (months, res) ->
                    PlanningResultCard(months, res, strings) {
                        selectedResult = Pair(months, res)
                        showDetails = true
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        if (showDetails && selectedResult != null) {
            ModalBottomSheet(
                onDismissRequest = { showDetails = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                PlanningDetailsSheet(selectedResult!!.first, selectedResult!!.second, strings)
            }
        }
    }
}

@Composable
fun PlanningResultCard(months: Int, res: RecommendationResponse, strings: AppStrings, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("+$months", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
            }
            Spacer(Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (months == 1) strings.afterOneMonth else strings.afterTwoMonth, 
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    res.recommendation, 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Black, 
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${strings.accuracy}: ${res.accuracy ?: "98.5%"}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
            IconButton(onClick = onClick) {
                Icon(Icons.Default.ArrowForwardIos, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun PlanningDetailsSheet(months: Int, res: RecommendationResponse, strings: AppStrings) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 48.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(40.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        )
        
        Spacer(Modifier.height(24.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Agriculture, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Detailed Prediction Report",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
        }
        
        Spacer(Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("RECOMMENDED CROP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(res.recommendation, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(8.dp))
                Text("Estimated for ${if (months==1) "next month" else "2 months from now"}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("Expert Analysis:", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            text = res.expertExplanation ?: "Loading detailed analysis...",
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(24.dp))

        Text("Technical Insights:", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        
        res.reasons?.forEach { reason ->
            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp).padding(top = 4.dp))
                Spacer(Modifier.width(12.dp))
                Text(reason, style = MaterialTheme.typography.bodySmall)
            }
        }
        
        Spacer(Modifier.height(32.dp))
        
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50))
                Spacer(Modifier.width(12.dp))
                Text("AI Confidence Score: ${res.accuracy ?: "98.5%"}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}


@Composable
fun TimeframeCard(months: Int, label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
        ),
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)) else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
