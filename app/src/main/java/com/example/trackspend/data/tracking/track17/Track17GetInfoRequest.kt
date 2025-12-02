package com.example.trackspend.data.tracking.track17

/**
 * Represents a single tracking request item for the 17TRACK API.
 *
 * The API expects a list of objects, each containing:
 *  - number: the tracking number to query
 */
data class Track17GetInfoItem(
    val number: String,
)


/**
 * API request payload for `gettrackinfo`.
 *
 * 17TRACK requires the request body to be:
 *  [
 *      { "number": "TRACKING_NUMBER_1" },
 *      { "number": "TRACKING_NUMBER_2" }
 *  ]
 *
 * So we simply alias it to a List of items.
 */
typealias Track17GetInfoRequest = List<Track17GetInfoItem>