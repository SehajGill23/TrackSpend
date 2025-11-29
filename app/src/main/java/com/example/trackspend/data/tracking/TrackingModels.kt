package com.example.trackspend.data.tracking
import com.example.trackspend.data.model.TrackingEvent


/**
 * Unified result returned after tracking ANY carrier.
 */
data class TrackingResult(
    val latestStatus: String,
    val events: List<TrackingEvent>
)
