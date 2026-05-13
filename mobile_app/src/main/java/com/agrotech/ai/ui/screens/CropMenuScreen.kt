package com.agrotech.ai.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.agrotech.ai.ui.components.ActionCard
import com.agrotech.ai.ui.components.SectionHeader
import com.agrotech.ai.ui.navigation.Screen
import com.agrotech.ai.ui.theme.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropMenuScreen(navController: NavController) {
    val strings = LocalAppStrings.current

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    text = strings.cropManagement,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 1. Featured Action (Recommendation)
            item {
                FeaturedActionCard(
                    title = strings.cropRec,
                    description = "Get personalized crop suggestions based on your soil and climate.",
                    icon = Icons.Default.AutoAwesome,
                    color = Color(0xFF2E7D32),
                    onClick = { navController.navigate(Screen.CropRecommendation.route) }
                )
            }

            // Future Planning Section
            item {
                FeaturedActionCard(
                    title = "Future Crop Planning",
                    description = "Predict best crops for the next season using AI market trends and weather forecasts.",
                    icon = Icons.Default.Timeline,
                    color = Color(0xFF1565C0), // Deep Blue for Planning
                    onClick = { navController.navigate(Screen.FutureRecommendation.route) }
                )
            }

            // 2. Services Grid
            item {
                SectionHeader(title = strings.coreServices, modifier = Modifier.padding(top = 8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    CategoryActionCard(
                        title = strings.cropLibrary,
                        icon = Icons.Default.MenuBook,
                        color = Color(0xFF1976D2),
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.CropDetails.route) }
                    )
                    CategoryActionCard(
                        title = strings.marketPrice,
                        icon = Icons.Default.CurrencyRupee,
                        color = Color(0xFFFFA000),
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.MarketPrice.route) }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    CategoryActionCard(
                        title = strings.soilHealth,
                        icon = Icons.Default.Thermostat,
                        color = Color(0xFFF57C00),
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.FertilizerRecommendation.route) }
                    )
                    CategoryActionCard(
                        title = "Seasonal Planning",
                        icon = Icons.Default.CalendarMonth,
                        color = Color(0xFF7B1FA2),
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.SeasonalPlanner.route) }
                    )
                }
            }

            // 3. Crop Advisor (Horizontal List)
            item {
                SectionHeader(title = strings.cropAdvisor, actionText = strings.viewAll)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { AdvisorCard("Best time to sow Wheat", "Nov - Dec", Icons.Default.CalendarMonth, Color(0xFFFFA000)) }
                    item { AdvisorCard("Fertilizer tips for Rice", "Use NPK 12:32:16", Icons.Default.Science, Color(0xFF43A047)) }
                    item { AdvisorCard("Watering schedule", "Every 4 days", Icons.Default.WaterDrop, Color(0xFF0288D1)) }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun FeaturedActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(30.dp))
                }
            }
        }
    }
}

@Composable
fun CategoryActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .padding(8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun AdvisorCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(
        modifier = Modifier.width(220.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}
