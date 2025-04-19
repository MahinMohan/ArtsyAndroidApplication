package com.example.artsyapplication.screenviews

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onArtistSelected: (artistId: String, artistName: String) -> Unit
) {
    val topBarBlue = Color(0xFFbfcdf2)
    var isSearching by rememberSaveable { mutableStateOf(false) }
    var searchText by rememberSaveable { mutableStateOf("") }

    if (isSearching) {
        SearchArtistsScreen(
            searchText = searchText,
            onSearchTextChange = { searchText = it },
            onCancelSearch = {
                isSearching = false
                searchText = ""
            },
            onArtistSelected = onArtistSelected
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = topBarBlue),
                    title = { Text("Artist Search", color = Color.Black) },
                    navigationIcon = {},
                    actions = {
                        IconButton(onClick = { isSearching = true }) {
                            Icon(imageVector = Icons.Filled.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { /* handle user icon */ }) {
                            Icon(imageVector = Icons.Filled.Person, contentDescription = "User")
                        }
                    }
                )
            }
        ) { innerPadding ->
            // --- START ADDED SECTION ---
            val uriHandler = LocalUriHandler.current
            val currentDate = remember {
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1) Date row
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                ) {
                    Text(
                        text = currentDate,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        textAlign = TextAlign.Start
                    )
                }

                // 2) Favorites bar
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                ) {
                    Text(
                        text = "Favorites",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // 3) Login button (pill shape)
                Button(
                    onClick = { /* TODO: trigger login */ },
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text("Log in to see favorites")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4) Powered by Artsy link
                Text(
                    text = "Powered by Artsy",
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                    modifier = Modifier
                        .clickable { uriHandler.openUri("https://www.artsy.net/") }
                )
            }

        }
    }
}



//package com.example.artsyapplication.screenviews
//
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.*
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Person
//import androidx.compose.material.icons.filled.Search
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.runtime.saveable.rememberSaveable
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun HomeScreen(
//    onArtistSelected: (artistId: String, artistName: String) -> Unit
//) {
//    val topBarBlue = Color(0xFFbfcdf2)
//    var isSearching by rememberSaveable { mutableStateOf(false) }
//    var searchText by rememberSaveable { mutableStateOf("") }
//
//    if (isSearching) {
//        SearchArtistsScreen(
//            searchText = searchText,
//            onSearchTextChange = { searchText = it },
//            onCancelSearch = {
//                isSearching = false
//                searchText = ""
//            },
//            onArtistSelected = onArtistSelected
//        )
//    } else {
//        Scaffold(
//            modifier = Modifier.fillMaxSize(),
//            topBar = {
//                TopAppBar(
//                    colors = TopAppBarDefaults.topAppBarColors(containerColor = topBarBlue),
//                    title = { Text("Artist Search", color = Color.Black) },
//                    navigationIcon = {},
//                    actions = {
//                        IconButton(onClick = { isSearching = true }) {
//                            Icon(imageVector = Icons.Filled.Search, contentDescription = "Search")
//                        }
//                        IconButton(onClick = { /* handle user icon */ }) {
//                            Icon(imageVector = Icons.Filled.Person, contentDescription = "User")
//                        }
//                    }
//                )
//            }
//        ) { innerPadding ->
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(innerPadding)
//            ) {
//                items(items = emptyList<Any>()) { }
//            }
//        }
//    }
//}
