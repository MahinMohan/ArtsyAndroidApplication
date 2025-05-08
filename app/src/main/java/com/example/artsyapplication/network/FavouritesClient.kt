package com.example.artsyapplication.network

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.POST


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


data class DeleteFavouriteRequest(
    @SerializedName("id") val id: String
)

interface DeleteFavouritesApiService {
    @HTTP(method = "DELETE", path = "api/deletefavourites", hasBody = true)
    suspend fun deleteFavourite(
        @Body request: DeleteFavouriteRequest
    ): Response<ResponseBody>
}

object DeleteFavouritesClient {
    val api: DeleteFavouritesApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/")
            .client(Network.client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DeleteFavouritesApiService::class.java)
    }
}
