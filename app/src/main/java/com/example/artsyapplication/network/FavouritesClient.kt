package com.example.artsyapplication.network

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// ── exactly these fields, in this order ────────────────────────────────
data class AddFavouriteRequest(
    @SerializedName("artistId")    val artistId:    String,
    @SerializedName("title")       val title:       String,
    @SerializedName("birthyear")   val birthyear:   String,
    @SerializedName("deathyear")   val deathyear:   String,
    @SerializedName("nationality") val nationality: String,
    @SerializedName("image")       val image:       String
)

interface FavouritesApiService {
    @POST("api/addtofavourites")
    suspend fun addToFavorites(
        @Body request: AddFavouriteRequest
    ): Response<ResponseBody>
}

object FavouritesClient {
    val api: FavouritesApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/")
            .client(Network.client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FavouritesApiService::class.java)
    }
}
