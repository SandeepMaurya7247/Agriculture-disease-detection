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
import androidx.compose.material.icons.filled.Close
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
import com.agrotech.ai.data.model.VideoLesson
import com.agrotech.ai.viewmodel.AgroViewModel
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScreen(navController: NavController, viewModel: AgroViewModel) {
    val userState by viewModel.userState.collectAsState()
    val isAdmin = userState?.email == "admin@agrotech.com"
    val lessons by viewModel.lessons.collectAsState()

    var playingVideoUri by remember { mutableStateOf<String?>(null) }

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
                uploadedBy = userState?.email ?: "user",
                videoUri = it.toString()
            )
            viewModel.addLesson(newLesson)
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
                        onApprove = { viewModel.approveLesson(lesson.id) },
                        onPlay = { playingVideoUri = lesson.videoUri }
                    )
                }
            }
        }
    }

    playingVideoUri?.let { uri ->
        VideoPlayerDialog(videoUri = uri, onDismiss = { playingVideoUri = null })
    }
}

@Composable
fun LessonItem(lesson: VideoLesson, isAdmin: Boolean, onApprove: () -> Unit, onPlay: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { if (lesson.status == "APPROVED") onPlay() }
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

@Composable
fun VideoPlayerDialog(videoUri: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(videoUri)))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Box(contentAlignment = Alignment.Center) {
                AndroidView(
                    factory = {
                        PlayerView(context).apply {
                            player = exoPlayer
                            useController = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().aspectRatio(16/9f)
                )
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }
}
