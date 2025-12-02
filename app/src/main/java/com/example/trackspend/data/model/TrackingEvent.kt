package com.example.trackspend.data.model

/**
 * TrackingEvent represents a single checkpoint in a package's tracking history.
 *
 * Each event contains:
 * - status: the description of the tracking update (e.g., "Out for Delivery")
 * - location: optional details where the update occurred
 * - timestamp: epoch time in milliseconds for sorting & display
 *
 * These objects are deserialized from the carrier’s API response and
 * shown in the tracking timeline UI.
 */
data class TrackingEvent(
    val status: String,
    val location: String? = null,
    val timestamp: Long       // epoch millis
)