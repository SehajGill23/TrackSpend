package com.example.trackspend.data.tracking
import com.example.trackspend.data.model.TrackingEvent


/**
 * Represents a unified tracking response used by the app regardless of which
 * tracking provider (17TRACK, custom API, etc.) produced the data.
 *
 * This model is what the UI and ViewModel consume, ensuring the rest of the
 * application never depends on provider-specific formats.
 *
 * @property latestStatus A human-readable summary of the most recent tracking update.
 * @property events A chronological list of tracking events (location, timestamp, status).
 */
data class TrackingResult(
    val latestStatus: String,
    val events: List<TrackingEvent>
)
