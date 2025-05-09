package com.example.artsyapplication.screenviews

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.artsyapplication.R
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class ArtworksResponse(
    @SerializedName("_embedded") val embedded: EmbeddedArtworks
)
data class EmbeddedArtworks(
    @SerializedName("artworks") val artworks: List<ArtworkData>
)
data class ArtworkData(
    @SerializedName("id") val id: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("date") val date: String?,
    @SerializedName("_links") val links: Links?
)

private interface ArtworksApiService {
    @GET("api/artworksdata")
    suspend fun getArtworks(@Query("id") id: String): Response<ArtworksResponse>
}
private val artworksService: ArtworksApiService by lazy {
    Retrofit.Builder()
        .baseUrl("https://mahinartsyappassignment3.wl.r.appspot.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ArtworksApiService::class.java)
}

@Composable
fun Artworks(artistId: String) {
    val isDarkTheme = isSystemInDarkTheme()

    var showCategories by remember { mutableStateOf(false) }
    var selectedArtworkId by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }


    var hasDataLoaded by remember { mutableStateOf(false) }

    val artworks by produceState<List<ArtworkData>>(initialValue = emptyList(), artistId) {
        isLoading = true
        errorMsg = null
        try {
            val resp = artworksService.getArtworks(artistId)
            if (resp.isSuccessful) {
                value = resp.body()?.embedded?.artworks.orEmpty()
            } else {
                errorMsg = "Error ${resp.code()}"
            }
        } catch (e: Exception) {
            errorMsg = e.localizedMessage
        } finally {
            isLoading = false
            hasDataLoaded = true
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) Color.Black else Color.Transparent)
    ) {
        when {

            isLoading -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Loading…", color = if (isDarkTheme) Color.White else Color.Black)
            }


            errorMsg != null -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = errorMsg!!)
            }


            artworks.isNotEmpty() -> LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(artworks) { artwork ->
                    ArtworkCard(
                        artwork = artwork,
                        onViewCategories = { id ->
                            selectedArtworkId = id
                            showCategories = true
                        },
                        isDarkTheme = isDarkTheme
                    )
                }
            }


            hasDataLoaded -> Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 0.dp
            ) {
                Text(
                    text = "No Artworks",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )
            }
        }

        if (showCategories && selectedArtworkId != null) {
            Categories(
                artworkId = selectedArtworkId!!,
                onDismiss = { showCategories = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArtworkCard(
    artwork: ArtworkData,
    onViewCategories: (String) -> Unit,
    isDarkTheme: Boolean
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            val painter = rememberAsyncImagePainter(
                model = artwork.links?.thumbnail?.href.orEmpty(),
                error = painterResource(id = R.drawable.artsy_logo)
            )
            Image(
                painter = painter,
                contentDescription = artwork.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(if (isDarkTheme) Color(0xFF303031) else MaterialTheme.colorScheme.surface)
                    .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "${artwork.title.orEmpty()}, ${artwork.date.orEmpty()}",
                    color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { artwork.id?.let(onViewCategories) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("View categories")
                }
            }
        }
    }
}
