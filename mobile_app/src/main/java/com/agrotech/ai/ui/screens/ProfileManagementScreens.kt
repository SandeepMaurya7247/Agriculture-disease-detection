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
fun OverallHistoryScreen(navController: NavController, viewModel: AgroViewModel) {
    val historyItems by viewModel.historyItems.collectAsState()

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
        if (historyItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.History, null, modifier = Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No history found", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(historyItems) { item ->
                    val (title, icon, color) = when (item.type) {
                        "CROP_REC" -> Triple("Crop Recommendation", Icons.Default.Agriculture, Color(0xFF4CAF50))
                        "FERT_REC" -> Triple("Fertilizer Recommendation", Icons.Default.Science, Color(0xFF2196F3))
                        "STRESS_DETECTION" -> Triple("Disease Detection", Icons.Default.CameraAlt, Color(0xFFE91E63))
                        "SATELLITE_ANALYSIS" -> Triple("Satellite Health Scan", Icons.Default.Satellite, Color(0xFF673AB7))
                        "FUTURE_REC" -> Triple("Future Planning", Icons.Default.Timeline, Color(0xFFFF9800))
                        else -> Triple("General History", Icons.Default.Event, Color.Gray)
                    }

                    Row(modifier = Modifier.padding(bottom = 24.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = CircleShape,
                                color = color.copy(alpha = 0.2f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(icon, null, tint = color, modifier = Modifier.padding(8.dp))
                            }
                            Box(modifier = Modifier.width(2.dp).height(40.dp).background(Color.LightGray.copy(alpha = 0.5f)).padding(top = 8.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Result: ${item.result}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            Text(item.details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            
                            val date = java.util.Date(item.timestamp)
                            val format = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault())
                            Text(format.format(date), style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

data class HistoryEvent(val title: String, val desc: String, val time: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val color: Color)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController, viewModel: AgroViewModel) {
    val notifications by viewModel.notifications.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NotificationsNone, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                    Spacer(Modifier.height(16.dp))
                    Text("No notifications yet", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { notif ->
                    NotificationCard(notif) {
                        viewModel.markNotificationAsRead(notif.id)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notif: AppNotification, onClick: () -> Unit) {
    val icon = when(notif.type) {
        "EXPERT_VIDEO" -> Icons.Default.PlayCircle
        "WEATHER_ALERT" -> Icons.Default.Cloud
        "IOT_ALERT" -> Icons.Default.Warning
        else -> Icons.Default.Notifications
    }
    val color = when(notif.type) {
        "EXPERT_VIDEO" -> MaterialTheme.colorScheme.primary
        "WEATHER_ALERT" -> Color(0xFFF44336)
        "IOT_ALERT" -> Color(0xFFFF9800)
        else -> Color.Gray
    }
    
    val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(notif.timestamp))

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notif.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.padding(8.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(notif.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    if (!notif.isRead) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(8.dp)) {}
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(notif.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text(timeStr, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

