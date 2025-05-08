package com.example.artsyapplication.screenviews

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class ArtistDetails(
    val name: String,
    val birthday: String,
    val deathday: String,
    val nationality: String,
    val biography: String
)

interface ArtistApi {
    @GET("api/artistdata")
    suspend fun getArtistDetails(@Query("id") artistId: String): Response<ArtistDetails>
}

object ArtistApiClient {
    private const val BASE_URL = "http://10.0.2.2:3000/"
    val instance: ArtistApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ArtistApi::class.java)
    }
}

@Composable
fun ArtistInfo(artistId: String) {
    val isDarkTheme = isSystemInDarkTheme()
    var artist by remember { mutableStateOf<ArtistDetails?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(artistId) {
        try {
            val response = ArtistApiClient.instance.getArtistDetails(artistId)
            if (response.isSuccessful) {
                artist = response.body()
            }
        } finally {
            isLoading = false
        }
    }


    Box(
        Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) Color.Black else Color.Transparent)
    ) {
        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 48.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.height(8.dp))
                Text(
                    "Loading...",
                    color = if (isDarkTheme) Color.White else Color.Black
                )
            }
        } else {
            artist?.let {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = it.name,
                        color = if (isDarkTheme) Color.White else Color.Black,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${it.nationality}, ${it.birthday} - ${it.deathday}",
                        color = if (isDarkTheme) Color.White else Color.Black,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = it.biography.replace("-\\s+".toRegex(),"").replace("\\s*\\n+".toRegex(),"\n\n"),
                        color = if (isDarkTheme) Color.White else Color.Black,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Justify
                    )
                    Spacer(Modifier.height(32.dp))
                }
            } ?: Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No artist info found.",
                    color = if (isDarkTheme) Color.White else Color.Black
                )
            }
        }
    }
}
