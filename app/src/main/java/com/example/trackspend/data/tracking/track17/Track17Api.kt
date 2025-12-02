package com.example.trackspend.data.tracking.track17

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Retrofit API interface for interacting with the 17Track tracking service.
 *
 * Defines the HTTP endpoints used to:
 * - register a tracking number with 17Track
 * - retrieve detailed tracking updates
 *
 * All requests use JSON bodies and expect JSON responses.
 */
interface Track17Api {

    /**
     * Registers a tracking number with the 17Track API.
     *
     * @param body The registration request payload containing tracking number + options.
     * @return The API response containing registration status or error details.
     */
    @Headers("Content-Type: application/json")
    @POST("track/v2.4/register")
    suspend fun register(
        @Body body: Track17RegisterRequest
    ): Track17Response


    /**
     * Requests tracking information for one or more registered tracking numbers.
     *
     * @param body The request payload specifying the tracking number(s) to query.
     * @return The API response containing detailed tracking progress updates.
     */
    @Headers("Content-Type: application/json")
    @POST("track/v2.4/gettrackinfo")
    suspend fun getTrackInfo(
        @Body body: Track17GetInfoRequest
    ): Track17Response
}