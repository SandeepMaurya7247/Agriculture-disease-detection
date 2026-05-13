package com.agrotech.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.agrotech.ai.viewmodel.AgroViewModel
import com.agrotech.ai.data.model.MarketRecord
import com.agrotech.ai.ui.theme.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketPriceScreen(navController: NavController, viewModel: AgroViewModel) {
    val strings = LocalAppStrings.current
    val marketPrices by viewModel.marketPrices.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorState.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.fetchMarketPrices()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.marketPrice, fontWeight = FontWeight.Bold) },
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
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    var state by remember { mutableStateOf("") }
                    var district by remember { mutableStateOf("") }
                    var crop by remember { mutableStateOf("") }

                    com.agrotech.ai.ui.components.AgroTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = "State (e.g. Madhya Pradesh)",
                        leadingIcon = Icons.Default.Map
                    )
                    Spacer(Modifier.height(8.dp))
                    com.agrotech.ai.ui.components.AgroTextField(
                        value = district,
                        onValueChange = { district = it },
                        label = "District (e.g. Rewa)",
                        leadingIcon = Icons.Default.LocationOn
                    )
                    Spacer(Modifier.height(8.dp))
                    com.agrotech.ai.ui.components.AgroTextField(
                        value = crop,
                        onValueChange = { crop = it },
                        label = "Crop/Phasal (e.g. Wheat)",
                        leadingIcon = Icons.Default.Grass
                    )
                    Spacer(Modifier.height(16.dp))
                    com.agrotech.ai.ui.components.AgroButton(
                        text = "Mandi Price Check Karein",
                        onClick = {
                            viewModel.fetchMarketPrices(state.takeIf { it.isNotBlank() }, district.takeIf { it.isNotBlank() }, crop.takeIf { it.isNotBlank() })
                            focusManager.clearFocus()
                        }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (marketPrices.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Text(if (error != null) error!! else "No market data found", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(marketPrices) { record ->
                        MarketPriceCard(record)
                    }
                }
            }
        }
    }
}

@Composable
fun MarketPriceCard(record: MarketRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.commodity,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${record.market}, ${record.district}, ${record.state}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = record.arrivalDate,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                PriceItem("Min Price", "Γé╣${record.minPrice}", Color(0xFFD32F2F))
                PriceItem("Modal Price", "Γé╣${record.modalPrice}", MaterialTheme.colorScheme.primary)
                PriceItem("Max Price", "Γé╣${record.maxPrice}", Color(0xFF388E3C))
            }
        }
    }
}

@Composable
fun PriceItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = color)
    }
}
