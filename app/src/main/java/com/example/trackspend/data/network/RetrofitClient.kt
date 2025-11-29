package com.example.trackspend.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitClient {

    fun gson(base: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(base)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    fun scalars(base: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(base)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
}