package com.example.artsyapplication.screenviews

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.artsyapplication.Favorite
import com.example.artsyapplication.LoggedInUser
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: LoggedInUser?,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    onArtistSelected: (artistId: String, artistName: String) -> Unit,
    onFavoriteAdded: (Favorite) -> Unit,
    onFavoriteRemoved: (String) -> Unit
) {
    val topBarBlue     = Color(0xFFbfcdf2)
    val isDarkTheme    = isSystemInDarkTheme()
    val topBarColor    = if (isDarkTheme) Color(0xFF223D6B) else topBarBlue
    val titleTextColor = if (isDarkTheme) Color.White else Color.Black


    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()

    var isSearching by rememberSaveable { mutableStateOf(false) }
    var searchText  by rememberSaveable { mutableStateOf("") }

    if (isSearching) {
        SearchArtistsScreen(
            searchText         = searchText,
            onSearchTextChange = { searchText = it },
            onCancelSearch     = {
                isSearching = false
                searchText  = ""
            },
            onArtistSelected   = onArtistSelected,
            user               = user,
            onFavoriteAdded    = onFavoriteAdded,
            onFavoriteRemoved  = onFavoriteRemoved
        )
    } else {
        Scaffold(
            modifier     = Modifier.fillMaxSize(),
            topBar       = {
                TopAppBar(
                    colors         = topAppBarColors(containerColor = topBarColor),
                    title          = { Text("Artist Search", color = titleTextColor) },
                    navigationIcon = {},
                    actions        = {
                        IconButton(onClick = { isSearching = true }) {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        }
                        if (user == null) {
                            IconButton(onClick = onLogin) {
                                Icon(Icons.Filled.Person, contentDescription = "User")
                            }
                        } else {
                            var menuExpanded by remember { mutableStateOf(false) }
                            Box {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Image(
                                        painter            = rememberAsyncImagePainter(user.gravatar),
                                        contentDescription = user.fullname,
                                        modifier           = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                    )
                                }
                                DropdownMenu(
                                    expanded         = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Log out", color = Color.Blue) },
                                        onClick = {
                                            onLogout()
                                            menuExpanded = false
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Logged out successfully")
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete account", color = Color.Red) },
                                        onClick = {
                                            onDeleteAccount()
                                            menuExpanded = false
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Deleted user successfully")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { innerPadding ->
            val uriHandler  = LocalUriHandler.current
            val currentDate = remember {
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
            }

            var now by remember { mutableStateOf(Instant.now()) }
            LaunchedEffect(Unit) {
                while (true) {
                    delay(1000L)
                    now = Instant.now()
                }
            }

            Column(
                modifier            = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color    = MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                ) {
                    Text(
                        text      = currentDate,
                        style     = MaterialTheme.typography.bodySmall,
                        modifier  = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        textAlign = TextAlign.Start
                    )
                }

                Surface(
                    color    = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                ) {
                    Text(
                        text      = "Favorites",
                        style     = MaterialTheme.typography.bodyMedium,
                        modifier  = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                if (user == null) {
                    Button(
                        onClick        = onLogin,
                        shape          = RoundedCornerShape(24.dp),
                        colors         = if (isDarkTheme)
                            ButtonDefaults.buttonColors(containerColor = Color(0xFF223D6B))
                        else
                            ButtonDefaults.buttonColors(),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            "Log in to see favorites",
                            color = if (isDarkTheme) Color.White else Color.Unspecified
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }


                if (user != null) {
                    val sortedFavs = user
                        .favourites
                        .sortedByDescending { Instant.parse(it.addedAt) }

                    if (sortedFavs.isEmpty()) {
                        Surface(
                            color    = topBarBlue,
                            shape    = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(48.dp)
                        ) {
                            Box(
                                Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No favorites")
                            }
                        }
                    } else {
                        sortedFavs.forEach { fav ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onArtistSelected(fav.artistId, fav.title) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text  = fav.title,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = listOfNotNull(
                                            fav.nationality.takeIf(String::isNotBlank),
                                            fav.birthyear.takeIf(String::isNotBlank)
                                        ).joinToString(", "),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Text(
                                    text  = timeAgo(fav.addedAt, now),
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Icon(
                                    imageVector        = Icons.Filled.ArrowForward,
                                    contentDescription = "Go to details",
                                    modifier           = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }


                Text(
                    text     = "Powered by Artsy",
                    style    = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                    modifier = Modifier.clickable {
                        uriHandler.openUri("https://www.artsy.net/")
                    }
                )
            }
        }
    }
}

private fun timeAgo(iso: String, now: Instant): String {
    val then = Instant.parse(iso)
    val timediff = Duration.between(then, now).seconds
    return when {
        timediff < 60     -> "$timediff seconds ago"
        timediff < 3_600  -> "${timediff / 60} minutes ago"
        timediff < 86_400 -> "${timediff / 3_600} hours ago"
        else          -> "${timediff / 86_400} days ago"
    }
}
