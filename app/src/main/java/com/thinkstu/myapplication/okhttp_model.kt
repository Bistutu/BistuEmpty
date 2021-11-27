package com.thinkstu.myapplication

import android.os.Build
import androidx.annotation.RequiresApi
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.Duration

object okhttp_model {
    fun send(url:String): String? {
        val okhttp = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()
        val response = okhttp.newCall(request).execute()
        return response.body?.string()
    }
}