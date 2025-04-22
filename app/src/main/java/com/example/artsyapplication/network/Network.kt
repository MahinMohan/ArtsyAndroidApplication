package com.example.artsyapplication.network

import android.content.Context
import okhttp3.OkHttpClient

object Network {
    lateinit var client: OkHttpClient
    lateinit var cookieJar: ManualCookieJar

    fun init(context: Context) {
        // create the jar once so we can clear it on logout
        cookieJar = ManualCookieJar(context)
        client    = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .build()
    }
}
