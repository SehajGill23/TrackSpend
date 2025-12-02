package com.example.trackspend.data.tracking.track17

import android.util.Log
import com.example.trackspend.BuildConfig.SEVENTEEN_API_KEY
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Track17Client configures and exposes the Retrofit client used to communicate
 * with the 17Track API.
 *
 * Responsibilities:
 * - Inject API authentication header ("17token")
 * - Build an OkHttp client with interceptors
 * - Provide a ready-to-use Retrofit API instance (Track17Api)
 *
 * This object ensures all parts of the app use the same shared client.
 */
object Track17Client {

    private const val API_KEY = SEVENTEEN_API_KEY

    /**
     * Interceptor that automatically attaches the 17Track API key
     * to every outgoing HTTP request.
     *
     * Adds header:
     *   - "17token": API_KEY
     *
     * Also logs the key for debugging.
     */
    private val authInterceptor = Interceptor { chain ->
        val req = chain.request()
            .newBuilder()
            .addHeader("17token", API_KEY)
            .build()

        Log.d("17TRACK", "Request sent to 17Track API")
        chain.proceed(req)
    }


    /**
     * OkHttpClient configured with authentication interceptor.
     *
     * All network calls for tracking use this client to guarantee
     * consistent headers and network behavior.
     */
    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()


    /**
     * Retrofit instance bound to:
     * - Base URL of the 17Track API
     * - Gson for JSON serialization
     * - OkHttp client with auth interceptor
     *
     * Exposes the `Track17Api` interface for making actual API calls.
     */
    val api: Track17Api = Retrofit.Builder()
        .baseUrl("https://api.17track.net/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
        .create(Track17Api::class.java)
}