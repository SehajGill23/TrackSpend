package com.example.trackspend.data.tracking.track17

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface Track17Api {

    @Headers("Content-Type: application/json")
    @POST("track/v2.4/register")
    suspend fun register(
        @Body body: Track17RegisterRequest
    ): Track17Response

    @Headers("Content-Type: application/json")
    @POST("track/v2.4/gettrackinfo")
    suspend fun getTrackInfo(
        @Body body: Track17GetInfoRequest
    ): Track17Response
}