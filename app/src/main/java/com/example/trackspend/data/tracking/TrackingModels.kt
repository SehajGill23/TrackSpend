package com.example.trackspend.data.tracking

/**
 * One update in a package's tracking history.
 */
data class TrackingEvent(
    val status: String,       // "In transit", "Delivered", etc.
    val location: String? = null,
    val timestamp: Long       // epoch millis
)

/**
 * Unified result returned after tracking ANY carrier.
 */
data class TrackingResult(
    val latestStatus: String,
    val events: List<TrackingEvent>
)
