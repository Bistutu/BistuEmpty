package com.thinkstu.myapplication

import okhttp3.OkHttpClient
import okhttp3.Request

object okhttp_model {
    fun send(url:String): String? {
        val okhttp = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()
        val response = okhttp.newCall(request).execute()
        return response.body()?.string()
    }
}