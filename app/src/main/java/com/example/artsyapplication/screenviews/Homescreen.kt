package com.example.artsyapplication.screenviews

import androidx.compose.foundation.layout.*                       // added Column, Spacer, fillMaxWidth, weight, height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText         // for clickable link
import androidx.compose.foundation.clickable              // for clickable modifier
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment                        // for horizontalAlignment, Alignment.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler           // to open browser
import androidx.compose.ui.text.font.FontStyle              // for italic text
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate                                  // for current date
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
            // compute formatted current date
            val currentDate = remember {
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1) current date
                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 8.dp)
                )

                // 2) grey bar with "Favorites"
                Surface(
                    color = Color.LightGray,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Favorites",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // 3) centered login button
                Button(onClick = { /* TODO: trigger login */ }) {
                    Text("Log in to see favorites")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4) Powered by Artsy link
                Text(
                    text = "Powered by Artsy",
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                    modifier = Modifier
                        .clickable { uriHandler.openUri("https://www.artsy.net/") }
                        .padding(bottom = 16.dp)
                )

                Spacer(modifier = Modifier.weight(2f))
            }
            // --- END ADDED SECTION ---
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
