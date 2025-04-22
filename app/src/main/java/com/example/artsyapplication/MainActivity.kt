package com.example.artsyapplication
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.artsyapplication.network.LogoutClient
import com.example.artsyapplication.network.Network
import com.example.artsyapplication.screenviews.ArtistDetailsScreen
import com.example.artsyapplication.screenviews.HomeScreen
import com.example.artsyapplication.screenviews.LoginScreen
import com.example.artsyapplication.screenviews.RegisterScreen
import com.example.artsyapplication.ui.theme.ArtsyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class LoggedInUser(
    val _id: String,
    val fullname: String,
    val gravatar: String
)

// Me endpoint client (unchanged)
interface MeApiService {
    @GET("api/me")
    suspend fun me(): Response<ResponseBody>
}

private object MeClient {
    val api: MeApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/")
            .client(Network.client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MeApiService::class.java)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // init the shared OkHttpClient with ManualCookieJar
        Network.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            ArtsyApplicationTheme {
                AppRouter()
            }
        }
    }
}

@Composable
fun AppRouter() {
    val navController = rememberNavController()
    val scope         = rememberCoroutineScope()

    var currentUser by remember { mutableStateOf<LoggedInUser?>(null) }
    var meChecked   by remember { mutableStateOf(false) }

    // On startup, call /api/me…
    LaunchedEffect(Unit) {
        try {
            val resp = MeClient.api.me()
            val body = withContext(Dispatchers.IO) {
                resp.errorBody()?.string()
                    ?: resp.body()?.string()
                    ?: ""
            }
            System.out.println("ME response: code=${resp.code()}, body=$body")

            if (resp.code() == 200 && body.isNotBlank()) {
                val obj     = JSONObject(body)
                val message = obj.optString("message")
                if (message == "Access denied no token") {
                    currentUser = null
                } else {
                    val userJson = obj.getJSONObject("user")
                    currentUser = LoggedInUser(
                        _id      = userJson.optString("_id", ""),
                        fullname = userJson.optString("fullname", ""),
                        gravatar = userJson.optString("gravatar", "")
                    )
                }
            }
        } catch (e: Exception) {
            System.out.println("Failed to call /api/me: ${e.message}")
            e.printStackTrace()
        } finally {
            meChecked = true
        }
    }

    // Wait until /api/me has completed
    if (!meChecked) return

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                user             = currentUser,
                onLogin          = { navController.navigate("login") },
                onLogout         = {
                    scope.launch {
                        // 1) Call DELETE /api/logout
                        runCatching { LogoutClient.api.logout() }
                        // 2) Clear stored cookies
                        Network.cookieJar.clear()
                        // 3) Update UI
                        currentUser = null
                    }
                },
                onArtistSelected = { id, name ->
                    navController.navigate("artistDetails/$id/$name")
                }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = { user ->
                    currentUser = user
                    navController.popBackStack()
                },
                onCancel   = { navController.popBackStack() },
                onRegister = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { user ->
                    currentUser = user
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onCancel = { navController.popBackStack() },
                onLogin  = { navController.popBackStack() }
            )
        }

        composable("artistDetails/{artistId}/{artistName}") { backStackEntry ->
            val artistId   = backStackEntry.arguments?.getString("artistId")   ?: ""
            val artistName = backStackEntry.arguments?.getString("artistName") ?: ""
            ArtistDetailsScreen(
                user       = currentUser,
                artistId   = artistId,
                artistName = artistName,
                onBack     = { navController.popBackStack() }
            )
        }
    }
}
