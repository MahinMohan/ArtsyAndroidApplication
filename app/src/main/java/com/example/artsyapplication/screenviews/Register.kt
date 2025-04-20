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
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// --- Retrofit setup in this file ---
data class RegisterRequest(val fullname: String, val email: String, val password: String)

interface RegisterApiService {
    @POST("api/createaccount")
    suspend fun register(@Body request: RegisterRequest): Response<ResponseBody>
}

object RegisterClient {
    val api: RegisterApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/")
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RegisterApiService::class.java)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onCancel: () -> Unit,
    onLogin: () -> Unit
) {
    val focusManager = LocalFocusManager.current
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

    var isRegistering by remember { mutableStateOf(false) }
    var registerError by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // enable only when all fields non‑blank and no field errors
    val formValid = fullName.isNotBlank() &&
            email.isNotBlank() &&
            password.isNotBlank() &&
            fullNameError == null &&
            emailError == null &&
            passwordError == null

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
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
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
                        if (fs.isFocused && !fullNameTouched) {
                            fullNameTouched = true
                        } else if (!fs.isFocused && fullNameTouched) {
                            fullNameError = if (fullName.isBlank()) "Fullname cannot be empty" else null
                        }
                    },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                )
            )
            fullNameError?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
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
                        if (fs.isFocused && !emailTouched) {
                            emailTouched = true
                        } else if (!fs.isFocused && emailTouched) {
                            emailError = when {
                                email.isBlank() -> "Email cannot be empty"
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
            emailError?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
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
                        if (fs.isFocused && !passwordTouched) {
                            passwordTouched = true
                        } else if (!fs.isFocused && passwordTouched) {
                            passwordError = if (password.isBlank()) "Password cannot be empty" else null
                        }
                    },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )
            passwordError?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Register button: disabled until valid, spinner while registering
            Button(
                onClick = {
                    scope.launch {
                        isRegistering = true
                        registerError = null
                        emailError = null

                        // network call off main thread
                        val (code, body) = withContext(Dispatchers.IO) {
                            try {
                                val resp = RegisterClient
                                    .api
                                    .register(RegisterRequest(fullName, email, password))
                                val text = resp.errorBody()?.string()
                                    ?: resp.body()?.string()
                                    ?: ""
                                Pair(resp.code(), text)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Pair(-1, "")
                            }
                        }

                        // debug print
                        System.out.println("Register API response: $body")

                        val msg = body.takeIf { it.isNotBlank() }?.let {
                            JSONObject(it).optString("message", null)
                        }

                        when {
                            msg == "User with this email already exists" ->
                                emailError = "Email already exists"
                            code == 200 -> {
                                snackbarHostState.showSnackbar("Registered successfully")
                                onRegisterSuccess()
                            }
                            else ->
                                registerError = msg ?: "Registration failed"
                        }

                        isRegistering = false
                    }
                },
                enabled = !isRegistering && formValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isRegistering) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Register")
                }
            }

            registerError?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Already have an account?
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
