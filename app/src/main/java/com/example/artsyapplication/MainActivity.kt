package com.example.artsyapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.artsyapplication.screenviews.*
import com.example.artsyapplication.ui.theme.ArtsyApplicationTheme

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

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onArtistSelected = { id, name ->
                    navController.navigate("artistDetails/$id/$name")
                }
            )
        }
        composable("artistDetails/{artistId}/{artistName}") { backStackEntry ->
            val artistId = backStackEntry.arguments?.getString("artistId") ?: ""
            val artistName = backStackEntry.arguments?.getString("artistName") ?: ""
            ArtistDetailsScreen(
                artistId = artistId,
                artistName = artistName,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
