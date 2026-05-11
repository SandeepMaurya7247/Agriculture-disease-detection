package com.agrotech.ai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.agrotech.ai.ui.navigation.Screen
import com.agrotech.ai.ui.theme.LocalAppStrings

@Composable
fun AgroBottomNavigation(navController: NavController) {
    val strings = LocalAppStrings.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(85.dp), // Increased from wrapContentHeight to accommodate the 10dp offset + 70dp nav bar + text
        contentAlignment = Alignment.BottomCenter
    ) {
        // Main Navigation Bar (YouTube Style Outlined icons)
        NavigationBar(
            tonalElevation = 0.dp,
            containerColor = Color.Black,
            modifier = Modifier.height(70.dp),
            windowInsets = WindowInsets(0)
        ) {
            // 1. Home
            YouTubeNavItem(
                label = strings.home,
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                selected = currentRoute == Screen.Dashboard.route,
                onClick = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )

            // 2. Crop
            YouTubeNavItem(
                label = strings.crop,
                selectedIcon = Icons.Filled.Agriculture,
                unselectedIcon = Icons.Outlined.Agriculture,
                selected = currentRoute == "crop_menu",
                onClick = {
                    navController.navigate("crop_menu") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )

            // 3. Placeholder for Center Button
            Spacer(modifier = Modifier.weight(1f))

            // 4. Fertilizer
            YouTubeNavItem(
                label = strings.fertilizer,
                selectedIcon = Icons.Filled.Science,
                unselectedIcon = Icons.Outlined.Science,
                selected = currentRoute == Screen.FertilizerRecommendation.route,
                onClick = {
                    navController.navigate(Screen.FertilizerRecommendation.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )

            // 5. Profile
            YouTubeNavItem(
                label = strings.profile,
                selectedIcon = Icons.Filled.Person,
                unselectedIcon = Icons.Outlined.Person,
                selected = currentRoute == Screen.Profile.route,
                onClick = {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        // Floating Center Camera Button (Outlined when inactive)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset(y = (-10).dp)
                .padding(bottom = 4.dp)
        ) {
            val isSelected = currentRoute == Screen.StressDetection.route
            Surface(
                onClick = {
                    navController.navigate(Screen.StressDetection.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                shape = CircleShape,
                color = Color.Transparent, // Transparent to show gradient
                border = BorderStroke(2.dp, if (isSelected) Color.White else Color.White.copy(alpha = 0.5f)),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .size(58.dp)
                    .background(
                        brush = if (isSelected) 
                            androidx.compose.ui.graphics.Brush.linearGradient(colors = listOf(Color(0xFF2E7D32), Color(0xFF1B5E20))) // Rich Dark Green when selected
                        else 
                            androidx.compose.ui.graphics.Brush.linearGradient(colors = listOf(Color(0xFF43A047), Color(0xFF2E7D32))), // Professional Green
                        shape = CircleShape
                    )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isSelected) Icons.Filled.CameraAlt else Icons.Outlined.CameraAlt,
                        contentDescription = "Stress",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            Text(
                text = strings.stress,
                fontSize = 11.sp,
                color = Color.White,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun RowScope.YouTubeNavItem(
    label: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        icon = { 
            Icon(
                imageVector = if (selected) selectedIcon else unselectedIcon, 
                contentDescription = label,
                modifier = Modifier
                    .size(28.dp) // Increased from 26.dp
                    .padding(bottom = 4.dp) // Reduced from 6.dp to avoid overlap
            ) 
        },
        label = { 
            Text(
                text = label, 
                fontSize = 10.sp, 
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            ) 
        },
        selected = selected,
        onClick = onClick,
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.White,
            selectedTextColor = Color.White,
            unselectedIconColor = Color.White.copy(alpha = 0.6f),
            unselectedTextColor = Color.White.copy(alpha = 0.6f),
            indicatorColor = Color.Transparent
        )
    )
}
