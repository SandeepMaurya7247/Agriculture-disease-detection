package com.agrotech.ai.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.agrotech.ai.viewmodel.AgroViewModel

data class VideoLesson(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val expert: String,
    val duration: String,
    val crop: String,
    val status: String = "APPROVED", // "PENDING", "APPROVED"
    val uploadedBy: String = "System"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScreen(navController: NavController, viewModel: AgroViewModel) {
    val userState by viewModel.userState.collectAsState()
    val isAdmin = userState?.email == "admin@agrotech.com"

    val lessons = remember { 
        mutableStateListOf(
            VideoLesson(title = "Wheat Rust Management", expert = "Dr. S. K. Singh", duration = "12:45", crop = "Wheat"),
            VideoLesson(title = "Drip Irrigation Benefits", expert = "Engr. Ramesh Pal", duration = "08:20", crop = "General"),
            VideoLesson(title = "Organic Fertilizer Prep", expert = "Farmer Om Prakash", duration = "15:10", crop = "All Crops"),
            VideoLesson(title = "Rice Pest Control", expert = "Dr. Anita Devi", duration = "10:30", crop = "Rice"),
            VideoLesson(title = "Soil Health Testing", expert = "Dr. Vivek Mehra", duration = "14:00", crop = "General")
        )
    }

    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            val newLesson = VideoLesson(
                title = "New Community Video",
                expert = userState?.name ?: "Unknown Farmer",
                duration = "0:00",
                crop = "General",
                status = if (isAdmin) "APPROVED" else "PENDING",
                uploadedBy = userState?.email ?: "user"
            )
            lessons.add(0, newLesson)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expert Learning") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { videoLauncher.launch("video/*") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = "Upload Video")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            items(lessons) { lesson ->
                // Regular users only see APPROVED videos + their own PENDING ones
                // Admins see EVERYTHING
                val shouldShow = isAdmin || lesson.status == "APPROVED" || lesson.uploadedBy == userState?.email
                
                if (shouldShow) {
                    LessonItem(
                        lesson = lesson, 
                        isAdmin = isAdmin,
                        onApprove = {
                            val index = lessons.indexOfFirst { it.id == lesson.id }
                            if (index != -1) {
                                lessons[index] = lessons[index].copy(status = "APPROVED")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LessonItem(lesson: VideoLesson, isAdmin: Boolean, onApprove: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { /* Play Video */ }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.PlayCircle,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = if (lesson.status == "PENDING") Color.Gray else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = lesson.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    if (lesson.status == "PENDING") {
                        Surface(
                            color = Color(0xFFFFF3E0),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "PENDING", 
                                color = Color(0xFFE65100), 
                                fontSize = 10.sp, 
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(text = "By ${lesson.expert}", style = MaterialTheme.typography.bodySmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SuggestionChip(
                        onClick = { },
                        label = { Text(lesson.crop, style = MaterialTheme.typography.labelSmall) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = lesson.duration, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
            
            if (isAdmin && lesson.status == "PENDING") {
                Button(
                    onClick = onApprove,
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Approve", fontSize = 12.sp)
                }
            }
        }
    }
}
