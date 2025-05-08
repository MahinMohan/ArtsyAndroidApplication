package com.example.artsyapplication.screenviews

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import com.example.artsyapplication.R
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class GenesResponse(
    @SerializedName("_embedded") val embedded: EmbeddedGenes
)
data class EmbeddedGenes(
    @SerializedName("genes") val genes: List<CategoryData>
)
data class CategoryData(
    @SerializedName("name")        val name:        String,
    @SerializedName("description") val description: String?,
    @SerializedName("_links")      val links:       Links?
)

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun Categories(
    artworkId: String,
    onDismiss: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var index     by remember { mutableStateOf(0) }

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
            shape          = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp,
            modifier       = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Categories", style = MaterialTheme.typography.headlineSmall)

                when {
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
                    else -> {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(520.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val pagestate = rememberPagerState(initialPage = 0) { genes.size }
                            val coroutineScope = rememberCoroutineScope()
                            IconButton(
                                onClick = { if(pagestate.currentPage==0){
                                    coroutineScope.launch {
                                        pagestate.animateScrollToPage(pagestate.currentPage+genes.size-1)
                                    }
                                }
                                    else if(pagestate.currentPage > 0){
                                    coroutineScope.launch {
                                        pagestate.animateScrollToPage(pagestate.currentPage-1)
                                    }
                                } },

                                modifier = Modifier.align(Alignment.CenterStart).zIndex(2F)
                            ) {
                                Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous")
                            }

                            HorizontalPager(state = pagestate,
                                contentPadding= PaddingValues(horizontal = 18.dp),
                                pageSpacing = 2.dp) {
                                page ->
                                val gene = genes[page]
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()

                                ) {
                                    Column {
                                        val thumbUrl = gene.links?.thumbnail?.href
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
                                            gene.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        val scroll = rememberScrollState()
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                                .verticalScroll(scroll)
                                                .padding(horizontal = 12.dp)
                                        ) {
                                            Text(
                                                gene.description.orEmpty(),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            }
                            IconButton(
                                onClick = {
                                    if(pagestate.currentPage==genes.size-1){
                                        coroutineScope.launch {
                                            pagestate.animateScrollToPage(pagestate.currentPage-genes.size+1)
                                        }
                                    }

                                    else if(pagestate.currentPage < genes.lastIndex){
                                    coroutineScope.launch {
                                        pagestate.animateScrollToPage(pagestate.currentPage+1)
                                    }
                                } },
                                modifier = Modifier.align(Alignment.CenterEnd).zIndex(2F)
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
