@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.artsyapplication.screenviews

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.artsyapplication.Favorite
import com.example.artsyapplication.LoggedInUser
import com.example.artsyapplication.R
import com.example.artsyapplication.network.AddFavouriteRequest
import com.example.artsyapplication.network.ArtistDataClient
import com.example.artsyapplication.network.DeleteFavouritesClient
import com.example.artsyapplication.network.DeleteFavouriteRequest
import com.example.artsyapplication.network.FavouritesClient
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.launch
import java.time.Instant
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

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

data class Href(@SerializedName("href") val href: String?)

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
    onFavoriteAdded    : (Favorite) -> Unit,
    onFavoriteRemoved  : (String) -> Unit
) {
    val topBarBlue        = Color(0xFFbfcdf2)
    val isDarkTheme       = isSystemInDarkTheme()
    val topBarColor       = if (isDarkTheme) Color(0xFF223D6B) else topBarBlue
    val textColor         = if (isDarkTheme) Color.White else Color.Black
    val placeholderColor  = if (isDarkTheme) Color.White else Color.DarkGray

    val searchResults     = remember { mutableStateOf<List<Artist>>(emptyList()) }
    val hasSearched = remember { mutableStateOf(false) }
    val isLoading         = remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope    = rememberCoroutineScope()

    val focusManager      = LocalFocusManager.current
    val keyboardController= LocalSoftwareKeyboardController.current

    LaunchedEffect(searchText) {
        if (searchText.length >= 3) {
            isLoading.value = true
            searchResults.value = try {
                val resp = RetrofitClient.instance.searchArtists(searchText)
                if (resp.isSuccessful) resp.body()!! else emptyList()
            } catch (_: Exception) {
                emptyList()
            }
            isLoading.value = false
            hasSearched.value = true
        } else {
            searchResults.value = emptyList()
            isLoading.value = false
            hasSearched.value = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = topBarColor),
                title  = {
                    val focusRequester = remember { FocusRequester() }
                    LaunchedEffect(Unit) { focusRequester.requestFocus() }
                    TextField(
                        value               = searchText,
                        onValueChange       = onSearchTextChange,
                        placeholder         = { Text("Search artists...", color = placeholderColor) },
                        singleLine          = true,
                        modifier            = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        colors              = TextFieldDefaults.colors(
                            focusedTextColor          = textColor,
                            unfocusedTextColor        = textColor,
                            focusedContainerColor     = topBarColor,
                            unfocusedContainerColor   = topBarColor,
                            cursorColor               = textColor,
                            focusedPlaceholderColor   = placeholderColor,
                            unfocusedPlaceholderColor = placeholderColor,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        keyboardOptions     = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                        keyboardActions     = KeyboardActions(
                            onSearch = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }
                        )
                    )
                },
                navigationIcon = {
                    Icon(
                        imageVector        = Icons.Filled.Search,
                        contentDescription = "Search Icon",
                        tint               = textColor
                    )
                },
                actions = {
                    IconButton(onClick = onCancelSearch) {
                        Icon(
                            imageVector        = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint               = textColor
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading.value) {
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Loading..")
                }
            } else if (hasSearched.value && !isLoading.value && searchText.length >= 3 && searchResults.value.isEmpty()) {
                Column(
                    modifier            = Modifier
                        .fillMaxWidth().padding(vertical = 16.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = topBarBlue, shape = RoundedCornerShape(16.dp))
                            .padding(vertical = 8.dp),
                    ) {
                        Text(
                            text      = "No results found",
                            color     = textColor,
                            modifier  = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(searchResults.value) { artist ->
                        val artistId = artist.links?.self?.href?.substringAfterLast("/") ?: ""
                        val isFav    = user?.favourites?.any { it.artistId == artistId } == true

                        ArtistCard(
                            artist           = artist,
                            onDetailsClick   = {
                                if (artistId.isNotEmpty())
                                    onArtistSelected(artistId, artist.title ?: "Unknown")
                            },
                            user             = user,
                            onAddToFavorites = {
                                if (user != null) {
                                    coroutineScope.launch {
                                        if (isFav) {
                                            try {
                                                DeleteFavouritesClient
                                                    .api
                                                    .deleteFavourite(DeleteFavouriteRequest(id = artistId))
                                                onFavoriteRemoved(artistId)
                                                snackbarHostState.showSnackbar("Removed from Favourites")
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
                                                    image       = artist.links?.thumbnail?.href.orEmpty()
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
                                            snackbarHostState.showSnackbar("Added to Favourites")
                                        }
                                    }
                                }
                            },
                            isDarkTheme      = isDarkTheme
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArtistCard(
    artist           : Artist,
    onDetailsClick   : () -> Unit,
    user             : LoggedInUser?,
    onAddToFavorites : () -> Unit,
    isDarkTheme      : Boolean
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable{ onDetailsClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.height(200.dp)) {
            val painter = rememberAsyncImagePainter(
                model        = artist.links?.thumbnail?.href.orEmpty(),
                error        = painterResource(id = R.drawable.artsy_logo),
                contentScale = ContentScale.Crop
            )

            Image(
                painter            = painter,
                contentDescription = "Artist Image",
                modifier           = Modifier.fillMaxSize(),
                contentScale       = ContentScale.Crop
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
                        .background(
                            color = if (isDarkTheme) Color(0xFF223D6B) else Color(0xFFbfcdf2),
                            shape = CircleShape
                        )
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
                    .background(
                        color = if (isDarkTheme)
                            Color(0xFF223D6B).copy(alpha = 0.85f)
                        else
                            Color(0xFFbfcdf2).copy(alpha = 0.85f)
                    )
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text  = artist.title ?: "",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color.White else Color.Black
                    )
                )
                IconButton(onClick = onDetailsClick) {
                    Icon(
                        imageVector        = Icons.Filled.ArrowForwardIos,
                        contentDescription = "Details",
                        tint               = if (isDarkTheme) Color.White else Color.Black
                    )
                }
            }
        }
    }
}
