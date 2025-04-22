package com.example.artsyapplication.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.DELETE

interface LogoutApiService {
    @DELETE("api/logout")
    suspend fun logout(): Response<ResponseBody>
}

object LogoutClient {
    val api: LogoutApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/")
            .client(Network.client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LogoutApiService::class.java)
    }
}
