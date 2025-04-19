@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.artsyapplication.screenviews

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.artsyapplication.R
import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response



data class Artist(
    @SerializedName("type") val type: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("og_type") val ogType: String?,
    @SerializedName("_links") val links: Links?
)

data class Links(
    @SerializedName("self") val self: Href?,
    @SerializedName("permalink") val permalink: Href?,
    @SerializedName("thumbnail") val thumbnail: Href?
)

data class Href(
    @SerializedName("href") val href: String?
)

interface ApiService {
    @GET("api/searchdata")
    suspend fun searchArtists(
        @Query("q") query: String,
        @Query("size") size: Int = 10,
        @Query("type") type: String = "artist"
    ): Response<List<Artist>>
}

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:3000/"
    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}



@Composable
fun SearchArtistsScreen(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onCancelSearch: () -> Unit,
    onArtistSelected: (String, String) -> Unit
) {
    val topBarBlue = Color(0xFFbfcdf2)
    val searchResults = remember { mutableStateOf<List<Artist>>(emptyList()) }

    LaunchedEffect(searchText) {
        if (searchText.length >= 3) {
            try {
                val response = RetrofitClient.instance.searchArtists(searchText)
                searchResults.value = if (response.isSuccessful) {
                    response.body() ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                searchResults.value = emptyList()
            }
        } else {
            searchResults.value = emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = topBarBlue),
                title = {
                    val focusRequester = remember { FocusRequester() }
                    LaunchedEffect(Unit) { focusRequester.requestFocus() }
                    TextField(
                        value = searchText,
                        onValueChange = onSearchTextChange,
                        placeholder = { Text("Search artists...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = topBarBlue,
                            unfocusedContainerColor = topBarBlue,
                            cursorColor = Color.Black,
                            focusedPlaceholderColor = Color.DarkGray,
                            unfocusedPlaceholderColor = Color.DarkGray
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {})
                    )
                },
                navigationIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = "Search Icon")
                },
                actions = {
                    IconButton(onClick = onCancelSearch) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(searchResults.value) { artist ->
                ArtistCard(artist = artist, onDetailsClick = {
                    val id = artist.links?.self?.href?.substringAfterLast("/") ?: return@ArtistCard
                    val name = artist.title ?: "Unknown"
                    onArtistSelected(id, name)
                })
            }
        }
    }
}

@Composable
fun ArtistCard(artist: Artist, onDetailsClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.height(200.dp)) {
            val thumbnailUrl = artist.links?.thumbnail?.href
            val painter = rememberAsyncImagePainter(
                model = thumbnailUrl ?: "",
                error = painterResource(id = R.drawable.artsy_logo),
                contentScale = ContentScale.Crop
            )

            Image(
                painter = painter,
                contentDescription = "Artist Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .align(Alignment.BottomStart)
                    .background(Color(0xFFbfcdf2).copy(alpha = 0.85f))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = artist.title ?: "",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black)
                )
                IconButton(onClick = onDetailsClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowForwardIos,
                        contentDescription = "Details",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}
