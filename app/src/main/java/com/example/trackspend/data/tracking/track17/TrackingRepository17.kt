package com.example.trackspend.data.tracking.track17

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.trackspend.data.model.TrackingEvent
import com.example.trackspend.data.tracking.TrackingResult
import java.time.Instant


/**
 * Repository for interacting with the 17TRACK API.
 *
 * Responsibilities:
 *  - Send tracking requests to 17TRACK
 *  - Auto-register tracking numbers when required
 *  - Convert raw 17TRACK API responses into internal TrackingResult models
 *
 * This class provides a clean interface used by your ViewModel.
 */
class TrackingRepository17 {

    companion object {

        /** Error code returned by 17TRACK when a number must be registered first. */
        private const val NOT_REGISTERED = -18019902
    }


    /**
     * Tracks a shipment using the 17TRACK API.
     *
     * Workflow:
     *  1. Attempt to call `getTrackInfo()` immediately.
     *  2. If the API reports NOT_REGISTERED, automatically register the number.
     *  3. Retry the GET request after registration.
     *  4. Parse the final API response into a TrackingResult containing:
     *      - latest status
     *      - list of TrackingEvent items
     *
     * @param number The tracking number (UPS, FedEx, DHL, etc.)
     * @return Parsed tracking result for UI usage.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun track(number: String): TrackingResult {
        Log.d("TRACK17", "track() CALLED for $number")

        // 1. Try GetTrackInfo immediately
        val getResult = fetchTracking(number)

        // If error = not registered → auto-register then retry
        val rejected = getResult.data?.rejected?.firstOrNull()
        val errCode = rejected?.error?.code

        if (errCode == NOT_REGISTERED) {
            Log.w("TRACK17", "Number not registered. Registering now...")

            Track17Client.api.register(
                listOf(Track17RegisterItem(number))
            )

            // Retry GET
            Log.d("TRACK17", "Retrying getTrackInfo() after register")
            return parseTracking(fetchTracking(number))
        }

        // Normal case
        return parseTracking(getResult)
    }



    /**
     * Calls the 17TRACK `gettrackinfo` endpoint.
     *
     * @param number Tracking number to query.
     * @return Raw Track17Response returned by Retrofit.
     */
    private suspend fun fetchTracking(number: String): Track17Response {
        return Track17Client.api.getTrackInfo(
            listOf(Track17GetInfoItem(number))
        )
    }


    /**
     * Converts a Track17Response into your internal TrackingResult model.
     *
     * Extracts:
     *  - latest event
     *  - list of tracking events (status, location, timestamp)
     *
     * If no accepted entry exists, returns an "Unknown" empty TrackingResult.
     *
     * @param response Raw API response from 17TRACK.
     * @return Clean TrackingResult used by the UI.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseTracking(response: Track17Response): TrackingResult {
        Log.d("TRACK17", "Response code = ${response.code}")

        val accepted = response.data?.accepted?.firstOrNull()
            ?: return TrackingResult("Unknown", emptyList())

        val info = accepted.track_info ?: return TrackingResult("Unknown", emptyList())

        val events = info.tracking
            ?.providers
            ?.firstOrNull()
            ?.events
            ?.map { e ->
                TrackingEvent(
                    status = e.description ?: "Unknown",
                    location = e.location ?: "",
                    timestamp = parseDate(e.time_iso)
                )
            }
            ?: emptyList()

        val latest = info.latest_event?.description ?: "Unknown"

        Log.d("TRACK17", "Parsed ${events.size} events. Latest=$latest")

        return TrackingResult(latest, events)
    }



    /**
     * Safely converts an ISO timestamp string (e.g., "2024-01-03T12:45:00Z")
     * into epoch milliseconds.
     *
     * If parsing fails, returns the current system time instead.
     *
     * @param date ISO8601 timestamp from the 17TRACK API.
     * @return Epoch millis representation of the date.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseDate(date: String?): Long {
        return try {
            Instant.parse(date).toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}