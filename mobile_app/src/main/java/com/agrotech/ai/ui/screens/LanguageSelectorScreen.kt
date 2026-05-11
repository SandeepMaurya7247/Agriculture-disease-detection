package com.agrotech.ai.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.agrotech.ai.ui.navigation.Screen
import com.agrotech.ai.viewmodel.AgroViewModel
import com.agrotech.ai.ui.theme.LocalAppStrings

data class Language(val name: String, val native: String, val code: String, val flag: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectorScreen(navController: NavController, viewModel: AgroViewModel) {
    val strings = LocalAppStrings.current
    val languages = listOf(
        Language("English", "English", "en", "🇺🇸"),
        Language("Hindi", "हिन्दी", "hi", "🇮🇳"),
        Language("Bengali", "বাংলা", "bn", "🇮🇳"),
        Language("Telugu", "తెలుగు", "te", "🇮🇳"),
        Language("Marathi", "मराठी", "mr", "🇮🇳"),
        Language("Tamil", "தமிழ்", "ta", "🇮🇳")
    )

    var selectedCode by remember { mutableStateOf("en") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(strings.selectLanguage) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(languages) { lang ->
                    LanguageItem(
                        language = lang,
                        isSelected = selectedCode == lang.code,
                        onSelect = { selectedCode = lang.code }
                    )
                }
            }
            
            Button(
                onClick = { 
                    viewModel.setLanguage(selectedCode)
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.LanguageSelector.route) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp)
            ) {
                Text(strings.continueBtn)
            }
        }
    }
}

@Composable
fun LanguageItem(language: Language, isSelected: Boolean, onSelect: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = language.flag, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = language.native, style = MaterialTheme.typography.titleMedium)
                Text(text = language.name, style = MaterialTheme.typography.bodySmall)
            }
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
