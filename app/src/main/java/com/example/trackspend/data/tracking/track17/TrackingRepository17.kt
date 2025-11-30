package com.example.trackspend.data.tracking.track17

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.trackspend.data.model.TrackingEvent
import com.example.trackspend.data.tracking.TrackingResult
import java.time.Instant

class TrackingRepository17 {

    companion object {
        private const val NOT_REGISTERED = -18019902
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun track(number: String): TrackingResult {
        Log.d("TRACK17", "track() CALLED for $number")

        // 1️⃣ Try GetTrackInfo immediately
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

    private suspend fun fetchTracking(number: String): Track17Response {
        return Track17Client.api.getTrackInfo(
            listOf(Track17GetInfoItem(number))
        )
    }

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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseDate(date: String?): Long {
        return try {
            Instant.parse(date).toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}