package com.example.trackspend.data.tracking.track17

import android.util.Log
import com.example.trackspend.BuildConfig.SEVENTEEN_API_KEY
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Track17Client {

    private const val API_KEY = SEVENTEEN_API_KEY

    private val authInterceptor = Interceptor { chain ->
        val req = chain.request()
            .newBuilder()
            .addHeader("17token", API_KEY)
            .build()

        Log.d("17TRACK", "API KEY USED = $API_KEY")
        chain.proceed(req)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    val api: Track17Api = Retrofit.Builder()
        .baseUrl("https://api.17track.net/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
        .create(Track17Api::class.java)
}