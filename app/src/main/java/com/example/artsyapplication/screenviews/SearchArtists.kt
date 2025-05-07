@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.artsyapplication.screenviews

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.artsyapplication.Favorite
import com.example.artsyapplication.LoggedInUser
import com.example.artsyapplication.R
import com.example.artsyapplication.network.AddFavouriteRequest
import com.example.artsyapplication.network.ArtistDataClient
import com.example.artsyapplication.network.FavouritesClient
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.launch
import java.time.Instant
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// ── extend your model to include the extra fields ────────────────
data class Artist(
    @SerializedName("type")        val type:        String?,
    @SerializedName("title")       val title:       String?,
    @SerializedName("description") val description: String?,
    @SerializedName("og_type")     val ogType:      String?,
    @SerializedName("_links")      val links:       Links?,
    @SerializedName("birthday")    val birthyear:   String?,
    @SerializedName("deathday")    val deathday:    String?,
    @SerializedName("nationality") val nationality: String?
)

data class Links(
    @SerializedName("self")      val self:      Href?,
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
    searchText         : String,
    onSearchTextChange : (String) -> Unit,
    onCancelSearch     : () -> Unit,
    onArtistSelected   : (String, String) -> Unit,
    user               : LoggedInUser?,
    onFavoriteAdded    : (Favorite) -> Unit
) {
    val topBarBlue     = Color(0xFFbfcdf2)
    val searchResults  = remember { mutableStateOf<List<Artist>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

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
                title  = {
                    val focusRequester = remember { FocusRequester() }
                    LaunchedEffect(Unit) { focusRequester.requestFocus() }
                    TextField(
                        value               = searchText,
                        onValueChange       = onSearchTextChange,
                        placeholder         = { Text("Search artists...") },
                        singleLine          = true,
                        modifier            = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        colors              = TextFieldDefaults.colors(
                            focusedTextColor          = Color.Black,
                            unfocusedTextColor        = Color.Black,
                            focusedContainerColor     = topBarBlue,
                            unfocusedContainerColor   = topBarBlue,
                            cursorColor               = Color.Black,
                            focusedPlaceholderColor   = Color.DarkGray,
                            unfocusedPlaceholderColor = Color.DarkGray
                        ),
                        keyboardOptions     = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                        keyboardActions     = KeyboardActions(onSearch = {})
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
                ArtistCard(
                    artist           = artist,
                    onDetailsClick   = {
                        val id   = artist.links?.self?.href?.substringAfterLast("/") ?: return@ArtistCard
                        val name = artist.title ?: "Unknown"
                        onArtistSelected(id, name)
                    },
                    user              = user,
                    onAddToFavorites = {
                        if (user != null) {
                            coroutineScope.launch {
                                try {
                                    // 1️⃣ fetch full artist data
                                    val id   = artist.links?.self?.href
                                        ?.substringAfterLast("/") ?: return@launch
                                    val resp = ArtistDataClient.api.getArtistData(id)
                                    if (!resp.isSuccessful) return@launch
                                    val data = resp.body()!!

                                    // 2️⃣ POST to favourites
                                    FavouritesClient.api.addToFavorites(
                                        AddFavouriteRequest(
                                            artistId    = data.id,
                                            title       = data.name,
                                            birthyear   = data.birthday,
                                            deathyear   = data.deathday,
                                            nationality = data.nationality,
                                            image       = artist.links?.thumbnail?.href.orEmpty()
                                        )
                                    )

                                    // 3️⃣ notify global state
                                    val newFav = Favorite(
                                        artistId    = data.id,
                                        title       = data.name,
                                        birthyear   = data.birthday,
                                        nationality = data.nationality,
                                        addedAt     = Instant.now().toString()
                                    )
                                    onFavoriteAdded(newFav)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ArtistCard(
    artist           : Artist,
    onDetailsClick   : () -> Unit,
    user             : LoggedInUser?,
    onAddToFavorites : () -> Unit
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.height(200.dp)) {
            val thumbnailUrl = artist.links?.thumbnail?.href
            val painter = rememberAsyncImagePainter(
                model        = thumbnailUrl ?: "",
                error        = painterResource(id = R.drawable.artsy_logo),
                contentScale = ContentScale.Crop
            )

            Image(
                painter           = painter,
                contentDescription= "Artist Image",
                modifier          = Modifier.fillMaxSize(),
                contentScale      = ContentScale.Crop
            )

            if (user != null) {
                val artistId = artist.links?.self?.href?.substringAfterLast("/") ?: ""
                val isFav    = user.favourites.any { it.artistId == artistId }

                IconButton(
                    onClick  = onAddToFavorites,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .background(Color.White, shape = CircleShape)
                ) {
                    Icon(
                        imageVector        = if (isFav) Icons.Filled.Star else Icons.Filled.StarBorder,
                        tint               = if (isFav) Color.Black else Color.Gray,
                        contentDescription = if (isFav) "Remove from favorites" else "Add to favorites"
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .align(Alignment.BottomStart)
                    .background(Color(0xFFbfcdf2).copy(alpha = 0.85f))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text  = artist.title ?: "",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black)
                )
                IconButton(onClick = onDetailsClick) {
                    Icon(
                        imageVector        = Icons.Filled.ArrowForwardIos,
                        contentDescription = "Details"
                    )
                }
            }
        }
    }
}
