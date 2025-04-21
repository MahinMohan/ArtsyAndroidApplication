// MainActivity.kt
package com.example.artsyapplication
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.artsyapplication.screenviews.HomeScreen
import com.example.artsyapplication.screenviews.LoginScreen
import com.example.artsyapplication.screenviews.RegisterScreen
import com.example.artsyapplication.screenviews.ArtistDetailsScreen
import com.example.artsyapplication.ui.theme.ArtsyApplicationTheme

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient


@Serializable
private data class SerializableCookie(
    val name: String,
    val value: String,
    val domain: String,
    val path: String,
    val expiresAt: Long,
    val secure: Boolean,
    val httpOnly: Boolean
)

class PersistentCookieJar(context: Context) : CookieJar {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("cookie_prefs", Context.MODE_PRIVATE)

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val serial = cookies.map {
            SerializableCookie(
                name      = it.name,
                value     = it.value,
                domain    = it.domain,
                path      = it.path,
                expiresAt = it.expiresAt,
                secure    = it.secure,
                httpOnly  = it.httpOnly
            )
        }
        prefs.edit()
            .putString(
                url.host,
                Json.encodeToString(ListSerializer(SerializableCookie.serializer()), serial)
            )
            .apply()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now  = System.currentTimeMillis()
        val text = prefs.getString(url.host, null) ?: return emptyList()
        val stored = Json.decodeFromString(
            ListSerializer(SerializableCookie.serializer()), text
        )
        val valid = stored.filter { it.expiresAt > now }.map {
            Cookie.Builder()
                .name(it.name)
                .value(it.value)
                .domain(it.domain)
                .path(it.path)
                .expiresAt(it.expiresAt)
                .apply { if (it.secure)   secure() }
                .apply { if (it.httpOnly) httpOnly() }
                .build()
        }
        // overwrite to drop expired
        saveFromResponse(url, valid)
        return valid
    }
}
// --------------------------------------------------------------

data class LoggedInUser(
    val _id: String,
    val fullname: String,
    val gravatar: String
)

class MainActivity : ComponentActivity() {
    companion object {
        // Will be initialized in onCreate
        lateinit var cookieJar: PersistentCookieJar

        /** Use this single client everywhere */
        fun getHttpClient(): OkHttpClient =
            OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize persistent cookie jar
        cookieJar = PersistentCookieJar(applicationContext)

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
    var currentUser by rememberSaveable { mutableStateOf<LoggedInUser?>(null) }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                user             = currentUser,
                onLogin          = { navController.navigate("login") },
                onLogout         = { currentUser = null },
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
                user          = currentUser,
                artistId      = artistId,
                artistName    = artistName,
                navController = navController,
                onBack        = { navController.popBackStack() }
            )
        }
    }
}
