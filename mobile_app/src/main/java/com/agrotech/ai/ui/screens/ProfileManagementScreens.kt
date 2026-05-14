package com.agrotech.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.agrotech.ai.viewmodel.AgroViewModel
import com.agrotech.ai.data.model.AppNotification
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmProfilesScreen(navController: NavController) {
    val farms = listOf(
        FarmPlot("Main Wheat Field", "12.5 Acres", "Wheat"),
        FarmPlot("River Side Plot", "8 Acres", "Rice"),
        FarmPlot("North Valley", "4 Acres", "Cotton")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Farm Profiles", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Add Plot */ },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Farm", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(farms) { farm ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Landscape, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(12.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(farm.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("${farm.size} • ${farm.crop}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { /* Edit */ }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

data class FarmPlot(val name: String, val size: String, val crop: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverallHistoryScreen(navController: NavController) {
    val historyItems = listOf(
        HistoryEvent("Fertilizer Recommendation", "Recommended NPK for Wheat Field", "Today, 10:30 AM", Icons.Default.Science, Color(0xFF4CAF50)),
        HistoryEvent("Disease Detection", "Scanned Leaf: Healthy", "Yesterday, 2:15 PM", Icons.Default.CameraAlt, Color(0xFF2196F3)),
        HistoryEvent("Crop Added", "Added North Valley to profiles", "May 10, 2026", Icons.Default.AddCircle, Color(0xFFFF9800)),
        HistoryEvent("NDVI Analysis", "Checked health score for Main Field", "May 08, 2026", Icons.Default.Map, Color(0xFF9C27B0))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Overall History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(historyItems) { item ->
                Row(modifier = Modifier.padding(bottom = 24.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = item.color.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(item.icon, null, tint = item.color, modifier = Modifier.padding(8.dp))
                        }
                        Box(modifier = Modifier.width(2.dp).height(40.dp).background(Color.LightGray.copy(alpha = 0.5f)).padding(top = 8.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(item.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(item.desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(item.time, style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}

data class HistoryEvent(val title: String, val desc: String, val time: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val color: Color)




