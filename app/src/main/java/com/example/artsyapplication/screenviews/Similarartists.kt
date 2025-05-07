@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.artsyapplication.screenviews

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.artsyapplication.Favorite
import com.example.artsyapplication.LoggedInUser
import com.example.artsyapplication.R
import com.example.artsyapplication.network.AddFavouriteRequest
import com.example.artsyapplication.network.ArtistDataClient
import com.example.artsyapplication.network.FavouritesClient
import com.example.artsyapplication.screenviews.Links
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.launch
import java.time.Instant
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// ── models for your similar‐artists API ────────────────────────────────
data class SimilarArtist(
    @SerializedName("name")   val name:  String?,
    @SerializedName("_links") val links: Links?
)

data class EmbeddedArtists(
    @SerializedName("artists") val artists: List<SimilarArtist>?
)

data class SimilarArtistsResponse(
    @SerializedName("_embedded") val embedded: EmbeddedArtists?
)

interface SimilarApiService {
    @GET("api/similarartists")
    suspend fun getSimilarArtists(@Query("id") id: String): Response<SimilarArtistsResponse>
}

object SimilarRetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:3000/"
    val instance: SimilarApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SimilarApiService::class.java)
    }
}

@Composable
fun Similarartists(
    artistId: String,
    navController: NavController,
    user: LoggedInUser?,
    onFavoriteAdded: (Favorite) -> Unit
) {
    val similarResults  = remember { mutableStateOf<List<SimilarArtist>>(emptyList()) }
    val coroutineScope  = rememberCoroutineScope()

    LaunchedEffect(artistId) {
        try {
            val resp = SimilarRetrofitClient.instance.getSimilarArtists(artistId)
            similarResults.value = if (resp.isSuccessful) {
                resp.body()?.embedded?.artists ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            similarResults.value = emptyList()
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(similarResults.value) { artist ->
            val href = artist.links?.self?.href.orEmpty()
            val id   = href.substringAfterLast("/")
            val name = artist.name.orEmpty()

            SimilarArtistCard(
                similarArtist    = artist,
                onClick          = {
                    navController.navigate("artistDetails/$id/${Uri.encode(name)}")
                },
                user             = user,
                onAddToFavorites = {
                    if (user != null) {
                        coroutineScope.launch {
                            try {
                                // 1️⃣ fetch full artist data
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

                                // 3️⃣ update global state
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

@Composable
fun SimilarArtistCard(
    similarArtist: SimilarArtist,
    onClick: () -> Unit,
    user: LoggedInUser?,
    onAddToFavorites: () -> Unit
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape     = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.height(200.dp)) {
            val thumbUrl = similarArtist.links?.thumbnail?.href ?: ""
            val painter  = rememberAsyncImagePainter(
                model        = thumbUrl,
                error        = painterResource(id = R.drawable.artsy_logo),
                contentScale = ContentScale.Crop
            )

            Image(
                painter           = painter,
                contentDescription= similarArtist.name ?: "",
                modifier          = Modifier.fillMaxSize(),
                contentScale      = ContentScale.Crop
            )

            Icon(
                imageVector        = Icons.Filled.ArrowForward,
                contentDescription = "View details",
                modifier           = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clickable(onClick = onClick)
            )

            if (user != null) {
                val artistId = similarArtist.links?.self?.href
                    ?.substringAfterLast("/") ?: ""
                val isFav = user.favourites.any { it.artistId == artistId }

                IconButton(
                    onClick  = onAddToFavorites,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .background(Color(0xFFbfcdf2), shape = CircleShape)
                ) {
                    Icon(
                        imageVector        = if (isFav) Icons.Filled.Star else Icons.Filled.StarBorder,
                        tint               = if (isFav) Color.Black else Color.Gray,
                        contentDescription = if (isFav)
                            "Remove from favorites" else "Add to favorites"
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .align(Alignment.BottomStart)
                    .background(Color(0xFFbfcdf2).copy(alpha = 0.85f))
                    .padding(horizontal = 8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text  = similarArtist.name.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black)
                )
            }
        }
    }
}
