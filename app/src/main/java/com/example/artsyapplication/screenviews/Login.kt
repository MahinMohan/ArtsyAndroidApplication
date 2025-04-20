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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onCancel: () -> Unit,
    onRegister: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val topBarBlue   = Color(0xFFbfcdf2)

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var emailTouched by remember { mutableStateOf(false) }

    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordTouched by remember { mutableStateOf(false) }

    var isLoggingIn by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    val formValid = email.isNotBlank() &&
            password.isNotBlank() &&
            emailError == null &&
            passwordError == null

    Scaffold(
        topBar = {
            TopAppBar(
                title          = { Text("Login") },
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
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (emailError != null) emailError = null
                },
                label    = { Text("Email") },
                isError  = emailError != null,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { fs ->
                        if (fs.isFocused) {
                            if (!emailTouched) emailTouched = true
                        } else if (emailTouched) {
                            emailError = when {
                                email.isBlank() -> "Email cannot be empty"
                                !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                                    "Invalid email address"
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
                label    = { Text("Password") },
                isError  = passwordError != null,
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

            // Login button: disabled until valid & shows spinner while logging in
            Button(
                onClick = {
                    scope.launch {
                        isLoggingIn = true
                        loginError  = null

                        // Perform network call off main thread
                        val (responseCode, responseBody) = withContext(Dispatchers.IO) {
                            try {
                                val payload = JSONObject()
                                    .put("email", email)
                                    .put("password", password)
                                    .toString()

                                val conn = (URL("http://10.0.2.2:3000/api/login")
                                    .openConnection() as HttpURLConnection).apply {
                                    requestMethod    = "POST"
                                    setRequestProperty("Content-Type", "application/json")
                                    doOutput         = true
                                    outputStream.use { it.write(payload.toByteArray()) }
                                }

                                val body = conn.inputStream.bufferedReader().use { it.readText() }
                                System.out.println("Login API response: $body")
                                Pair(conn.responseCode, body)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Pair(-1, "")
                            }
                        }

                        // Back on main thread: check for "message" key
                        val json = responseBody.takeIf { it.isNotBlank() }?.let { JSONObject(it) }
                        if (json?.has("message") == true) {
                            loginError = "Username or password is incorrect"
                        } else {
                            onLoginSuccess()
                        }

                        isLoggingIn = false
                    }
                },
                enabled = !isLoggingIn && formValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoggingIn) {
                    CircularProgressIndicator(
                        color    = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Login")
                }
            }

            if (loginError != null) {
                Text(
                    text = loginError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Register prompt
            Row {
                Text("Don't have an account yet? ")
                Text(
                    "Register",
                    color          = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier       = Modifier.clickable(onClick = onRegister)
                )
            }
        }
    }
}
