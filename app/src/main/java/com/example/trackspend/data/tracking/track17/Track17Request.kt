package com.example.trackspend.data.tracking.track17

/**
 * Represents a single shipment to register with the 17TRACK API.
 *
 * The API requires each object to contain only:
 *  - number: the tracking number being registered
 */
data class Track17RegisterItem(
    val number: String
)


/**
 * API request payload for `register`.
 *
 * 17TRACK expects a JSON array:
 *  [
 *      { "number": "TRACKING_NUMBER_1" },
 *      { "number": "TRACKING_NUMBER_2" }
 *  ]
 *
 * This alias makes the request type clearer and easier to use.
 */
typealias Track17RegisterRequest = List<Track17RegisterItem>