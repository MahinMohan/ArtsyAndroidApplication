@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.artsyapplication.screenviews

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.artsyapplication.R
import com.example.artsyapplication.screenviews.Links
import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response

// 1. Data model for “similar artists”
data class SimilarArtist(
    @SerializedName("name") val name: String?,
    @SerializedName("_links") val links: Links?
)

// 2. Retrofit interface
interface SimilarApiService {
    @GET("api/similarartists")
    suspend fun getSimilarArtists(@Query("id") id: String): Response<List<SimilarArtist>>
}

// 3. Retrofit client instance
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
fun Similarartists(artistId: String) {
    // hold our API results
    val similarResults = remember { mutableStateOf<List<SimilarArtist>>(emptyList()) }

    // fetch on first composition or when artistId changes
    LaunchedEffect(artistId) {
        try {
            val resp = SimilarRetrofitClient.instance.getSimilarArtists(artistId)
            similarResults.value = if (resp.isSuccessful) resp.body()!! else emptyList()
        } catch (_: Exception) {
            similarResults.value = emptyList()
        }
    }

    // display them in a lazy column of cards
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(similarResults.value) { artist ->
            SimilarArtistCard(artist)
        }
    }
}

/** Card UI, identical styling & layout to SearchArtistsScreen’s ArtistCard */
@Composable
fun SimilarArtistCard(similarArtist: SimilarArtist) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape     = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.height(200.dp)) {
            // thumbnail
            val thumbUrl = similarArtist.links?.thumbnail?.href ?: ""
            val painter  = rememberAsyncImagePainter(
                model        = thumbUrl,
                error        = painterResource(id = R.drawable.artsy_logo),
                contentScale = ContentScale.Crop
            )

            Image(
                painter           = painter,
                contentDescription = similarArtist.name ?: "",
                modifier          = Modifier.fillMaxSize(),
                contentScale      = ContentScale.Crop
            )

            // overlay title bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .align(Alignment.BottomStart)
                    .background(Color(0xFFbfcdf2).copy(alpha = 0.85f))
                    .padding(horizontal = 8.dp),
                verticalAlignment   = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text  = similarArtist.name ?: "",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black)
                )
            }
        }
    }
}
