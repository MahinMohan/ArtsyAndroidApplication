@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.artsyapplication.screenviews

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.artsyapplication.Favorite
import com.example.artsyapplication.LoggedInUser
import com.example.artsyapplication.network.AddFavouriteRequest
import com.example.artsyapplication.network.ArtistDataClient
import com.example.artsyapplication.network.DeleteFavouritesClient
import com.example.artsyapplication.network.DeleteFavouriteRequest
import com.example.artsyapplication.network.FavouritesClient
import kotlinx.coroutines.launch
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailsScreen(
    user               : LoggedInUser?,
    artistId           : String,
    artistName         : String,
    navController      : NavController,
    onBack             : () -> Unit,
    onFavoriteAdded    : (Favorite) -> Unit,
    onFavoriteRemoved  : (String) -> Unit
) {

    val snackbar = remember { SnackbarHostState() }


    val SystemDarkCheck = isSystemInDarkTheme()
    val topBarColor = if (SystemDarkCheck) Color(0xFF223D6B) else Color(0xFFbfcdf2)


    val coroutineScope   = rememberCoroutineScope()
    var TabChoice by remember { mutableStateOf(0) }

    val Tabs  = listOf("Details", "Artworks")
    val TabIcons = listOf(Icons.Outlined.Info, Icons.Outlined.AccountBox)
    val tabs  = if (user != null) Tabs + "Similar" else Tabs
    val icons = if (user != null) TabIcons + Icons.Filled.PersonSearch else TabIcons

    val tabBar = @Composable {
        TopAppBar(
            title          = { Text(text = artistName) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions        = {
                if (user != null) {
                    val isFav = user.favourites.any { it.artistId == artistId }
                    IconButton(onClick = {
                        coroutineScope.launch {
                            if (isFav) {
                                try {
                                    DeleteFavouritesClient
                                        .api
                                        .deleteFavourite(DeleteFavouriteRequest(id = artistId))
                                    onFavoriteRemoved(artistId)
                                    snackbar.showSnackbar("Removed from Favourites")
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            } else {
                                val resp = ArtistDataClient.api.getArtistData(artistId)
                                if (!resp.isSuccessful) return@launch
                                val data = resp.body()!!
                                FavouritesClient.api.addToFavorites(
                                    AddFavouriteRequest(
                                        artistId    = data.id,
                                        title       = data.name,
                                        birthyear   = data.birthday,
                                        deathyear   = data.deathday,
                                        nationality = data.nationality,
                                        image       = ""
                                    )
                                )
                                onFavoriteAdded(
                                    Favorite(
                                        artistId    = data.id,
                                        title       = data.name,
                                        birthyear   = data.birthday,
                                        nationality = data.nationality,
                                        addedAt     = Instant.now().toString()
                                    )
                                )
                                snackbar.showSnackbar("Added to Favourites")
                            }
                        }
                    }) {
                        Icon(
                            imageVector        = if (isFav) Icons.Filled.Star else Icons.Filled.StarBorder,
                            tint               = if (isFav) Color.Black else Color.Gray,
                            contentDescription = null
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = topBarColor)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            tabBar()
            TabRow(selectedTabIndex = TabChoice) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = TabChoice == index,
                        onClick  = { TabChoice = index },
                        icon     = { Icon(icons[index], contentDescription = title) },
                        text     = { Text(title) }
                    )
                }
            }
            when (TabChoice) {
                0 -> ArtistInfo(artistId)
                1 -> Artworks(artistId)
                2 -> if (user != null) Similarartists(
                    artistId           = artistId,
                    navController      = navController,
                    user               = user,
                    onFavoriteAdded    = onFavoriteAdded,
                    onFavoriteRemoved  = onFavoriteRemoved
                )
            }
        }

        SnackbarHost(
            hostState = snackbar,
            modifier  = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}
