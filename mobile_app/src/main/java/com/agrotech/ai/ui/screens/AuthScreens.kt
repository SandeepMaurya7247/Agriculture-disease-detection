package com.agrotech.ai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.agrotech.ai.ui.components.AgroButton
import com.agrotech.ai.ui.components.AgroTextField
import com.agrotech.ai.ui.navigation.Screen
import com.agrotech.ai.viewmodel.AgroViewModel
import kotlinx.coroutines.launch
import com.agrotech.ai.ui.theme.LocalAppStrings

@Composable
fun LoginScreen(navController: NavController, viewModel: AgroViewModel) {
    val strings = LocalAppStrings.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val isLoading by viewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.verticalScroll(scrollState).imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(strings.appName, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(strings.welcome, style = MaterialTheme.typography.titleMedium)
                
                Spacer(modifier = Modifier.height(48.dp))
                
                AgroTextField(
                    value = email, 
                    onValueChange = { email = it }, 
                    label = strings.email,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                AgroTextField(
                    value = password, 
                    onValueChange = { password = it }, 
                    label = strings.password,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = icon, contentDescription = "Toggle password visibility")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { /* Forgot Password */ }, modifier = Modifier.align(Alignment.End)) {
                    Text(strings.forgotPassword)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    AgroButton(text = strings.login, onClick = { 
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            viewModel.login(email, password) { error ->
                                if (error == null) {
                                    navController.navigate(Screen.LanguageSelector.route)
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar(error) }
                                }
                            }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("Please enter all details") }
                        }
                    })
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.dontHaveAccount)
                    TextButton(onClick = { navController.navigate(Screen.Signup.route) }) {
                        Text(strings.signup)
                    }
                }
            }
        }
    }
}

@Composable
fun SignupScreen(navController: NavController, viewModel: AgroViewModel) {
    val strings = LocalAppStrings.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.verticalScroll(scrollState).imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(strings.joinAgro, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(strings.empoweringFarmers, style = MaterialTheme.typography.bodySmall)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                AgroTextField(value = name, onValueChange = { name = it }, label = strings.fullName)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                AgroTextField(
                    value = email, 
                    onValueChange = { email = it }, 
                    label = strings.email,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                AgroTextField(
                    value = password, 
                    onValueChange = { password = it }, 
                    label = strings.password,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = icon, contentDescription = "Toggle password visibility")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    AgroButton(text = strings.signup, onClick = { 
                        if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                            viewModel.signup(name, email, password) { error ->
                                if (error == null) {
                                    navController.navigate(Screen.LanguageSelector.route)
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar(error) }
                                }
                            }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("Please fill all fields") }
                        }
                    })
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { navController.popBackStack() }) {
                    Text(strings.alreadyHaveAccount)
                }
            }
        }
    }
}
