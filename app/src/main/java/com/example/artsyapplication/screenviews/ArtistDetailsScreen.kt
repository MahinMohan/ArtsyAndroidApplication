package com.example.artsyapplication.screenviews

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.artsyapplication.LoggedInUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailsScreen(
    user: LoggedInUser?,
    artistId: String,
    artistName: String,
    navController: NavController,         // ← new param
    onBack: () -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }

    val baseTabs  = listOf("Details", "Artworks")
    val baseIcons = listOf(Icons.Outlined.Info, Icons.Outlined.AccountBox)

    val tabs  = if (user != null) baseTabs + "Similar" else baseTabs
    val icons = if (user != null) baseIcons + Icons.Filled.PersonSearch else baseIcons

    val tabBar = @Composable {
        TopAppBar(
            title = { Text(text = artistName) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFbfcdf2))
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        tabBar()

        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick  = { selectedTabIndex = index },
                    icon     = { Icon(icons[index], contentDescription = title) },
                    text     = { Text(title) }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> ArtistInfo(artistId)
            1 -> Artworks(artistId)
            2 -> if (user != null) Similarartists(
                artistId      = artistId,
                navController  = navController   // ← passed here
            )
        }
    }
}
