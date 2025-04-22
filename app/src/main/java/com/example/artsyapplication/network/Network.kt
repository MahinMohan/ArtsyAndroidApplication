// Network.kt
package com.example.artsyapplication.network

import android.content.Context
import okhttp3.OkHttpClient

object Network {
    lateinit var client: OkHttpClient

    fun init(context: Context) {
        client = OkHttpClient.Builder()
            .cookieJar(ManualCookieJar(context))
            .build()
    }
}
