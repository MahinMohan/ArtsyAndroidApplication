package com.example.artsyapplication.screenviews

import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.artsyapplication.R
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// --- models ---
data class GenesResponse(
    @SerializedName("_embedded") val embedded: EmbeddedGenes
)
data class EmbeddedGenes(
    @SerializedName("genes") val genes: List<CategoryData>
)
data class CategoryData(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("_links") val links: Links?
)

// --- retrofit ---
private interface GenesApiService {
    @GET("api/genesdata")
    suspend fun getGenes(@Query("id") id: String): Response<GenesResponse>
}
private val genesService: GenesApiService by lazy {
    Retrofit.Builder()
        .baseUrl("http://10.0.2.2:3000/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GenesApiService::class.java)
}

// --- Composable ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Categories(
    artworkId: String,
    onDismiss: () -> Unit
) {
    // 1) Loading flag
    var isLoading by remember { mutableStateOf(true) }
    var index by remember { mutableStateOf(0) }

    // 2) Fetch categories
    val genes by produceState<List<CategoryData>>(initialValue = emptyList(), artworkId) {
        isLoading = true
        try {
            val resp = genesService.getGenes(artworkId)
            if (resp.isSuccessful) {
                value = resp.body()?.embedded?.genes.orEmpty()
            }
        } catch (_: Exception) { }
        finally {
            isLoading = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Categories", style = MaterialTheme.typography.headlineSmall)

                when {
                    // 3) Show loading while fetching
                    isLoading -> {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Loading…", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    // 4) After load, if empty, show no categories
                    genes.isEmpty() -> {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No categories available", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    // 5) Otherwise show your pager
                    else -> {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(520.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = { if (index > 0) index-- },
                                enabled = index > 0,
                                modifier = Modifier.align(Alignment.CenterStart)
                            ) {
                                Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous")
                            }

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth(0.80f)
                                    .fillMaxHeight()
                            ) {
                                Column {
                                    val thumbUrl = genes[index].links?.thumbnail?.href
                                    Image(
                                        painter = rememberAsyncImagePainter(
                                            model = thumbUrl,
                                            error = painterResource(R.drawable.artsy_logo)
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        genes[index].name,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        genes[index].description.orEmpty(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { if (index < genes.lastIndex) index++ },
                                enabled = index < genes.lastIndex,
                                modifier = Modifier.align(Alignment.CenterEnd)
                            ) {
                                Icon(Icons.Filled.ChevronRight, contentDescription = "Next")
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}
