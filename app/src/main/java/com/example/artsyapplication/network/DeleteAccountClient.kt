package com.example.artsyapplication.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.DELETE

interface DeleteAccountApiService {
    @DELETE("api/deleteaccount")
    suspend fun deleteAccount(): Response<ResponseBody>
}

object DeleteAccountClient {
    val api: DeleteAccountApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://mahinartsyappassignment3.wl.r.appspot.com/")
            .client(Network.client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DeleteAccountApiService::class.java)
    }
}
