package com.example.trackspend.data.tracking.track17


/**
 * Root response model returned from the 17TRACK API.
 *
 * Contains:
 *  - code: overall API result code
 *  - data: nested tracking data (accepted + rejected items)
 */
data class Track17Response(
    val code: Int?,
    val data: Track17Data?
)



/**
 * Container for successfully accepted tracking numbers and any rejected ones.
 *
 * - accepted: items 17TRACK accepted for processing
 * - rejected: items rejected with an error message/code
 */
data class Track17Data(
    val accepted: List<Track17Accepted>?,
    val rejected: List<Track17Rejected>?
)


/**
 * Represents one successfully accepted tracking number.
 *
 * Contains:
 *  - number: tracking number
 *  - carrier: numeric carrier ID used by 17TRACK
 *  - track_info: aggregated tracking status/events
 */
data class Track17Accepted(
    val number: String?,
    val carrier: Int?,
    val track_info: Track17TrackInfo?
)



/**
 * Represents one rejected tracking number.
 *
 * Contains:
 *  - number: tracking number
 *  - carrier: numeric carrier ID if recognized
 *  - error: error details explaining why it was rejected
 */
data class Track17Rejected(
    val number: String?,
    val carrier: Int?,
    val error: Track17Error?
)


/**
 * Error information returned for rejected tracking items.
 */
data class Track17Error(
    val code: Int?,
    val message: String?
)


/**
 * High-level object containing:
 *  - latest_event (last known activity)
 *  - latest_status (summary of delivery state)
 *  - tracking (full provider + events list)
 */
data class Track17TrackInfo(
    val latest_event: Track17Event?,
    val latest_status: Track17LatestStatus?,
    val tracking: Track17Tracking?
)



/**
 * Mathematical delivery summary used by 17TRACK.
 *
 * Includes:
 *  - status: main delivery state
 *  - sub_status: detailed sub-state
 *  - sub_status_descr: human-friendly text
 */
data class Track17LatestStatus(
    val status: String?,
    val sub_status: String?,
    val sub_status_descr: String?
)



/**
 * Full list of provider tracking info.
 *
 * A shipment may have multiple providers (origin → transit → destination).
 */
data class Track17Tracking(
    val providers: List<Track17ProviderWrapper>?
)



/**
 * Wraps a logistics provider together with all its tracking events.
 */
data class Track17ProviderWrapper(
    val provider: Track17Provider?,
    val events: List<Track17Event>?
)


/**
 * Details about a logistics provider/carrier used by 17TRACK.
 */
data class Track17Provider(
    val key: Int?,
    val name: String?,
    val alias: String?,
    val tel: String?,
    val homepage: String?,
    val country: String?
)


/**
 * Represents one tracking event from the provider.
 *
 * - time_iso: timestamp in ISO format
 * - description: event summary (e.g., "Arrived at facility")
 * - location: optional location text
 * - stage: 17TRACK internal event stage identifier
 */
data class Track17Event(
    val time_iso: String?,
    val description: String?,
    val location: String?, // may be null or empty
    val stage: String?
)