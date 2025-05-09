package com.example.artsyapplication.screenviews
import androidx.compose.material3.ButtonDefaults
import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
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
import com.example.artsyapplication.Favorite
import com.example.artsyapplication.LoggedInUser
import com.example.artsyapplication.network.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import androidx.compose.ui.focus.FocusDirection

data class LoginRequest(val email: String, val password: String)

interface LoginApiService {
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<ResponseBody>
}

object LoginClient {
    val api: LoginApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://mahinartsyappassignment3.wl.r.appspot.com/")
            .client(Network.client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LoginApiService::class.java)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (LoggedInUser) -> Unit,
    onCancel:       () -> Unit,
    onRegister:     () -> Unit
) {
    val focusManager        = LocalFocusManager.current
    val topBarBlue          = Color(0xFFbfcdf2)
    val isDarkTheme         = isSystemInDarkTheme()
    val topBarColor         = if (isDarkTheme) Color(0xFF223D6B) else topBarBlue

    var email               by remember { mutableStateOf("") }
    var emailError          by remember { mutableStateOf<String?>(null) }
    var emailTouched        by remember { mutableStateOf(false) }

    var password            by remember { mutableStateOf("") }
    var passwordError       by remember { mutableStateOf<String?>(null) }
    var passwordTouched     by remember { mutableStateOf(false) }

    var isLoggingIn         by remember { mutableStateOf(false) }
    var loginError          by remember { mutableStateOf<String?>(null) }

    val scope               = rememberCoroutineScope()
    val snackbarHostState   = remember { SnackbarHostState() }

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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (emailError != null) emailError = null
                },
                label       = { Text("Email") },
                isError     = emailError != null,
                singleLine  = true,
                modifier    = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { fs ->
                        if (fs.isFocused) {
                            if (!emailTouched) emailTouched = true
                            emailError = when {
                                email.isBlank() -> "Email cannot be empty"

                                else -> null
                            }
                        } else if (emailTouched) {
                            emailError = when {
                                email.isBlank() -> "Email cannot be empty"
                                else -> null
                            }
                        }
                    },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )
            emailError?.let {
                Text(
                    it,
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
                            if (!passwordTouched) passwordTouched = true
                            passwordError = if (password.isBlank()) {
                                "Password cannot be empty"
                            } else null
                        } else if (passwordTouched) {
                            passwordError = if (password.isBlank()) {
                                "Password cannot be empty"
                            } else null
                        }
                    },
                keyboardOptions     = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions     = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )
            passwordError?.let {
                Text(
                    it,
                    color    = MaterialTheme.colorScheme.error,
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                        emailError = "Invalid email address"
                        return@Button
                    }
                    scope.launch {
                        isLoggingIn = true
                        loginError  = null

                        val (code, body) = withContext(Dispatchers.IO) {
                            try {
                                val resp = LoginClient.api.login(LoginRequest(email, password))
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
                                    loginError = obj.getString("message")
                                }
                                obj.has("_id") -> {
                                    val favsJson = obj.optJSONArray("favourites") ?: JSONArray()
                                    val favsList = mutableListOf<Favorite>()
                                    for (i in 0 until favsJson.length()) {
                                        val f = favsJson.getJSONObject(i)
                                        favsList += Favorite(
                                            artistId    = f.optString("artistId",""),
                                            title       = f.optString("title",""),
                                            birthyear   = f.optString("birthyear",""),
                                            nationality = f.optString("nationality",""),
                                            addedAt     = f.optString("addedAt","")
                                        )
                                    }
                                    val user = LoggedInUser(
                                        _id        = obj.getString("_id"),
                                        fullname   = obj.getString("fullname"),
                                        gravatar   = obj.getString("gravatar"),
                                        favourites = favsList
                                    )

                                    snackbarHostState.showSnackbar("Logged in successfully")
                                    onLoginSuccess(user)
                                }
                                else -> {
                                    loginError = "Unexpected response from server"
                                }
                            }
                        } else {
                            loginError = "Network error (code=$code)"
                        }

                        isLoggingIn = false
                    }
                },

                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLoggingIn) Color(0xFF86868B)
                    else MaterialTheme.colorScheme.primary
                )
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

            loginError?.let {
                Text(
                    it,
                    color    = MaterialTheme.colorScheme.error,
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Row {
                Text("Don't have an account yet? ")
                Text(
                    "Register",
                    color           = MaterialTheme.colorScheme.primary,
                    textDecoration  = TextDecoration.Underline,
                    modifier        = Modifier.clickable(onClick = onRegister)
                )
            }
        }
    }
}
