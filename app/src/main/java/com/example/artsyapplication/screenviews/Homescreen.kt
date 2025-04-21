package com.example.artsyapplication.screenviews

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import com.example.artsyapplication.LoggedInUser
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: LoggedInUser?,
    onLogin: () -> Unit,
    onLogout: () -> Unit,                                     // ← new
    onArtistSelected: (artistId: String, artistName: String) -> Unit
) {
    val topBarBlue = Color(0xFFbfcdf2)
    var isSearching by rememberSaveable { mutableStateOf(false) }
    var searchText by rememberSaveable { mutableStateOf("") }

    if (isSearching) {
        SearchArtistsScreen(
            searchText         = searchText,
            onSearchTextChange = { searchText = it },
            onCancelSearch     = {
                isSearching = false
                searchText  = ""
            },
            onArtistSelected   = onArtistSelected
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar   = {
                TopAppBar(
                    colors         = topAppBarColors(containerColor = topBarBlue),
                    title          = { Text("Artist Search", color = Color.Black) },
                    navigationIcon = {},
                    actions        = {
                        IconButton(onClick = { isSearching = true }) {
                            Icon(imageVector = Icons.Filled.Search, contentDescription = "Search")
                        }
                        if (user == null) {
                            IconButton(onClick = onLogin) {
                                Icon(imageVector = Icons.Filled.Person, contentDescription = "User")
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
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text("Log out", color = Color.Blue)
                                        },
                                        onClick = {
                                            onLogout()               // ← clear user & reset UI
                                            menuExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text("Delete account", color = Color.Red)
                                        },
                                        onClick = {
                                            // TODO: wire up delete‑account logic
                                            menuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            val uriHandler  = LocalUriHandler.current
            val currentDate = remember {
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
            }

            Column(
                modifier            = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Date row
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

                // Favorites bar
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
                        onClick        = { onLogin() },
                        shape          = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text("Log in to see favorites")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
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
