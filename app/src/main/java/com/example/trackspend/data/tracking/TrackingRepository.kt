package com.example.trackspend.data.tracking

class TrackingRepository {

    suspend fun track(carrier: String, tracking: String): TrackingResult {

        return when (carrier.lowercase()) {

            "ups" -> trackUPS(tracking)
            "fedex" -> trackFedEx(tracking)
            "usps" -> trackUSPS(tracking)
            "canada post" -> trackCanadaPost(tracking)

            else -> TrackingResult(
                latestStatus = "Carrier not supported",
                events = emptyList()
            )
        }
    }

    // ----- Placeholder methods; filled in next steps -----

    private suspend fun trackUPS(tracking: String): TrackingResult {
        // UPS implementation will go here
        return TrackingResult("Not implemented yet", emptyList())
    }

    private suspend fun trackFedEx(tracking: String): TrackingResult {
        return TrackingResult("Not implemented yet", emptyList())
    }

    private suspend fun trackUSPS(tracking: String): TrackingResult {
        return TrackingResult("Not implemented yet", emptyList())
    }

    private suspend fun trackCanadaPost(tracking: String): TrackingResult {
        return TrackingResult("Not implemented yet", emptyList())
    }
}
