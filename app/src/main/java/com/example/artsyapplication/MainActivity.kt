package com.example.artsyapplication

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


data class LoggedInUser(
    val _id: String,
    val fullname: String,
    val gravatar: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                user       = currentUser,
                artistId   = artistId,
                artistName = artistName,
                onBack     = { navController.popBackStack() }
            )
        }
    }
}
