package com.example.artsyapplication.screenviews

import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.smallTopAppBarColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onCancel: () -> Unit,
    onLogin: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    // same blue as your other screens
    val topBarBlue = Color(0xFFbfcdf2)

    var fullName by remember { mutableStateOf("") }
    var fullNameTouched by remember { mutableStateOf(false) }
    var fullNameError by remember { mutableStateOf<String?>(null) }

    var email by remember { mutableStateOf("") }
    var emailTouched by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }

    var password by remember { mutableStateOf("") }
    var passwordTouched by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = smallTopAppBarColors(containerColor = topBarBlue)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Full Name field
            OutlinedTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    if (fullNameError != null) fullNameError = null
                },
                label = { Text("Full Name") },
                isError = fullNameError != null,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { fs ->
                        if (fs.isFocused) {
                            if (!fullNameTouched) fullNameTouched = true
                        } else if (fullNameTouched) {
                            fullNameError = if (fullName.isBlank()) {
                                "Fullname cannot be empty"
                            } else null
                        }
                    },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                )
            )
            if (fullNameError != null) {
                Text(
                    text = fullNameError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (emailError != null) emailError = null
                },
                label = { Text("Email") },
                isError = emailError != null,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { fs ->
                        if (fs.isFocused) {
                            if (!emailTouched) emailTouched = true
                        } else if (emailTouched) {
                            emailError = when {
                                email.isBlank() ->
                                    "Email cannot be empty"
                                !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                                    "Invalid email format"
                                else -> null
                            }
                        }
                    },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                )
            )
            if (emailError != null) {
                Text(
                    text = emailError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (passwordError != null) passwordError = null
                },
                label = { Text("Password") },
                isError = passwordError != null,
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { fs ->
                        if (fs.isFocused) {
                            if (!passwordTouched) passwordTouched = true
                        } else if (passwordTouched) {
                            passwordError = if (password.isBlank()) {
                                "Password cannot be empty"
                            } else null
                        }
                    },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )
            if (passwordError != null) {
                Text(
                    text = passwordError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Register button
            Button(
                onClick = onRegisterSuccess,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register")
            }

            Spacer(Modifier.height(16.dp))

            // Already have account?
            Row {
                Text("Already have an account? ")
                Text(
                    "Login",
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable(onClick = onLogin)
                )
            }
        }
    }
}
