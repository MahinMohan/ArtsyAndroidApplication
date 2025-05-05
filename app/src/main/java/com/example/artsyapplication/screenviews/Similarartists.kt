@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.artsyapplication.screenviews

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.artsyapplication.R
import com.example.artsyapplication.screenviews.Links
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class SimilarArtist(
    @SerializedName("name") val name: String?,
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
    navController: NavController         // ← new param
) {
    val similarResults = remember { mutableStateOf<List<SimilarArtist>>(emptyList()) }

    LaunchedEffect(artistId) {
        try {
            val resp = SimilarRetrofitClient.instance.getSimilarArtists(artistId)
            if (resp.isSuccessful) {
                similarResults.value = resp.body()?.embedded?.artists ?: emptyList()
            } else {
                similarResults.value = emptyList()
            }
        } catch (e: Exception) {
            similarResults.value = emptyList()
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(similarResults.value) { artist ->
            SimilarArtistCard(
                similarArtist = artist,
                onClick = {
                    val href = artist.links?.self?.href.orEmpty()
                    val id   = href.substringAfterLast("/")
                    val name = Uri.encode(artist.name.orEmpty())
                    navController.navigate("artistDetails/$id/$name")
                }
            )
        }
    }
}

@Composable
fun SimilarArtistCard(
    similarArtist: SimilarArtist,
    onClick: () -> Unit              // ← new param
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),   // ← make the card tappable
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
                painter            = painter,
                contentDescription = similarArtist.name ?: "",
                modifier           = Modifier.fillMaxSize(),
                contentScale       = ContentScale.Crop
            )

            // ← the right-arrow chevron
            Icon(
                imageVector        = Icons.Filled.ArrowForward,
                contentDescription = "View details",
                modifier           = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clickable(onClick = onClick)
            )

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
