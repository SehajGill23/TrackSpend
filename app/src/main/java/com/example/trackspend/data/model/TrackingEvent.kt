package com.example.trackspend.data.model

/**
 * One update in a package's tracking history.
 */
data class TrackingEvent(
    val status: String,
    val location: String? = null,
    val timestamp: Long       // epoch millis
)