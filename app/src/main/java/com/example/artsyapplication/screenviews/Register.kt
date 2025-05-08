package com.example.artsyapplication.screenviews

import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.smallTopAppBarColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.artsyapplication.LoggedInUser
import com.example.artsyapplication.network.Network
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
import com.example.artsyapplication.Favorite

data class RegisterRequest(val fullname: String, val email: String, val password: String)

interface RegisterApiService {
    @POST("api/createaccount")
    suspend fun register(@Body request: RegisterRequest): Response<ResponseBody>
}

object RegisterClient {
    val api: RegisterApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/")
            .client(Network.client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RegisterApiService::class.java)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: (LoggedInUser) -> Unit,
    onCancel:           () -> Unit,
    onLogin:            () -> Unit
) {
    val focusManager      = LocalFocusManager.current
    val topBarBlue        = Color(0xFFbfcdf2)
    val isDarkTheme    = isSystemInDarkTheme()
    val topBarColor    = if (isDarkTheme) Color(0xFF223D6B) else topBarBlue
    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()

    var fullName         by remember { mutableStateOf("") }
    var fullNameError    by remember { mutableStateOf<String?>(null) }
    var fullNameTouched  by remember { mutableStateOf(false) }

    var email            by remember { mutableStateOf("") }
    var emailError       by remember { mutableStateOf<String?>(null) }
    var emailTouched     by remember { mutableStateOf(false) }

    var password         by remember { mutableStateOf("") }
    var passwordError    by remember { mutableStateOf<String?>(null) }
    var passwordTouched  by remember { mutableStateOf(false) }

    var isRegistering    by remember { mutableStateOf(false) }
    var registerError    by remember { mutableStateOf<String?>(null) }

    val formValid = fullName.isNotBlank() &&
            email.isNotBlank() &&
            password.isNotBlank() &&
            fullNameError == null &&
            emailError == null &&
            passwordError == null

    Scaffold(
        topBar = {
            TopAppBar(
                title          = { Text("Register") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = smallTopAppBarColors(containerColor = topBarColor)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .imePadding(),
            verticalArrangement   = Arrangement.Center,
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {

            OutlinedTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    if (fullNameError != null) fullNameError = null
                },
                label      = { Text("Full Name") },
                isError    = fullNameError != null,
                singleLine = true,
                modifier   = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { fs ->
                        if (fs.isFocused) {
                            fullNameTouched = true
                        } else if (fullNameTouched && fullName.isBlank()) {
                            fullNameError = "Full name cannot be empty"
                        }
                    },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                )
            )
            if (fullNameTouched && fullNameError != null) {
                Text(
                    fullNameError!!,
                    color    = MaterialTheme.colorScheme.error,
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(16.dp))


            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (emailError != null) emailError = null
                },
                label      = { Text("Email") },
                isError    = emailError != null || (registerError?.contains("email", true) == true),
                singleLine = true,
                modifier   = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { fs ->
                        if (fs.isFocused) {
                            emailTouched = true
                        } else if (emailTouched) {
                            emailError = when {
                                email.isBlank() -> "Email cannot be empty"
                                !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                                    "Invalid format"
                                else -> null
                            }
                        }
                    },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                )
            )

            if (emailTouched && emailError != null) {
                Text(
                    emailError!!,
                    color    = MaterialTheme.colorScheme.error,
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
                )
            }

            if (registerError != null && registerError!!.contains("email", true)) {
                Text(
                    registerError!!,
                    color    = MaterialTheme.colorScheme.error,
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(16.dp))


            OutlinedTextField(
                value               = password,
                onValueChange       = {
                    password = it
                    if (passwordError != null) passwordError = null
                },
                label               = { Text("Password") },
                isError             = passwordError != null,
                singleLine          = true,
                visualTransformation= PasswordVisualTransformation(),
                modifier            = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { fs ->
                        if (fs.isFocused) {
                            passwordTouched = true
                        } else if (passwordTouched && password.isBlank()) {
                            passwordError = "Password cannot be empty"
                        }
                    },
                keyboardOptions     = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions     = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )
            if (passwordTouched && passwordError != null) {
                Text(
                    passwordError!!,
                    color    = MaterialTheme.colorScheme.error,
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        isRegistering = true
                        registerError = null

                        val (code, body) = withContext(Dispatchers.IO) {
                            try {
                                val resp = RegisterClient
                                    .api
                                    .register(RegisterRequest(fullName, email, password))
                                val text = resp.errorBody()?.string()
                                    ?: resp.body()?.string().orEmpty()
                                Pair(resp.code(), text)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Pair(-1, "")
                            }
                        }

                        if (code == 200 && body.isNotBlank()) {
                            val obj = JSONObject(body)
                            when {
                                obj.has("message") -> {

                                    registerError = obj.getString("message")
                                }
                                obj.has("_id") -> {
                                    // Successful registration
                                    val user = LoggedInUser(
                                        _id      = obj.getString("_id"),
                                        fullname = obj.getString("fullname"),
                                        gravatar = obj.getString("gravatar"),
                                        favourites = emptyList()
                                    )
                                    snackbarHostState.showSnackbar("Registered successfully")
                                    onRegisterSuccess(user)
                                }
                                else -> {
                                    registerError = "Unexpected response from server"
                                }
                            }
                        } else {
                            registerError = "Network error (code=$code)"
                        }

                        isRegistering = false
                    }
                },
                enabled  = !isRegistering && formValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isRegistering) {
                    CircularProgressIndicator(
                        color    = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Register")
                }
            }

            // only show non-email errors here
            registerError?.let { msg ->
                if (!msg.contains("email", true)) {
                    Text(
                        msg,
                        color    = MaterialTheme.colorScheme.error,
                        style    = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row {
                Text("Already have an account? ")
                Text(
                    "Login",
                    color           = MaterialTheme.colorScheme.primary,
                    textDecoration  = TextDecoration.Underline,
                    modifier        = Modifier.clickable(onClick = onLogin)
                )
            }
        }
    }
}
