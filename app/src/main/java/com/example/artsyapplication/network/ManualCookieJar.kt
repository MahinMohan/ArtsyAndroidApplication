package com.example.artsyapplication.network

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class ManualCookieJar(context: Context) : CookieJar {
    private val prefs = context
        .getSharedPreferences("cookie_prefs", Context.MODE_PRIVATE)
    // key = cookie.name@cookie.domain, value = cookie.toString()
    private val cache = mutableMapOf<String, String>()

    init {

        prefs.getStringSet("cookies", emptySet())!!
            .forEach { line ->
                val parts = line.split("=", limit = 2)
                if (parts.size == 2) cache[parts[0]] = parts[1]
            }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookies.forEach { c ->
            val key = "${c.name}@${c.domain}"
            cache[key] = c.toString()
        }
        // persist
        val set = cache.entries.map { "${it.key}=${it.value}" }.toSet()
        prefs.edit().putStringSet("cookies", set).apply()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        val out = mutableListOf<Cookie>()

        cache.values.forEach { cookieString ->
            Cookie.parse(url, cookieString)?.let { c ->
                if (c.expiresAt >= now &&
                    (c.hostOnly && c.domain == url.host ||
                            !c.hostOnly && url.host.endsWith(c.domain)) &&
                    url.encodedPath.startsWith(c.path)
                ) {
                    out += c
                }
            }
        }
        return out
    }


    fun clear() {
        cache.clear()
        prefs.edit().remove("cookies").apply()
    }
}
