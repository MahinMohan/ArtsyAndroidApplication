package com.example.artsyapplication.network

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


data class ArtistDataResponse(
    val id:          String,
    val name:        String,
    val birthday:    String,
    val deathday:    String,
    val nationality: String
)

interface ArtistDataApiService {
    @GET("api/artistdata")
    suspend fun getArtistData(
        @Query("id") artistId: String
    ): Response<ArtistDataResponse>
}

object ArtistDataClient {
    val api: ArtistDataApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://mahinartsyappassignment3.wl.r.appspot.com/")
            .client(Network.client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ArtistDataApiService::class.java)
    }
}
