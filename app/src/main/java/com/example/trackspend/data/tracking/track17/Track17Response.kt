package com.example.trackspend.data.tracking.track17

data class Track17Response(
    val code: Int?,
    val data: Track17Data?
)

data class Track17Data(
    val accepted: List<Track17Accepted>?,
    val rejected: List<Track17Rejected>?
)

data class Track17Accepted(
    val number: String?,
    val carrier: Int?,
    val track_info: Track17TrackInfo?
)

data class Track17Rejected(
    val number: String?,
    val carrier: Int?,
    val error: Track17Error?
)

data class Track17Error(
    val code: Int?,
    val message: String?
)

data class Track17TrackInfo(
    val latest_event: Track17Event?,
    val latest_status: Track17LatestStatus?,
    val tracking: Track17Tracking?
)

data class Track17LatestStatus(
    val status: String?,
    val sub_status: String?,
    val sub_status_descr: String?
)

data class Track17Tracking(
    val providers: List<Track17ProviderWrapper>?
)

data class Track17ProviderWrapper(
    val provider: Track17Provider?,
    val events: List<Track17Event>?
)

data class Track17Provider(
    val key: Int?,
    val name: String?,
    val alias: String?,
    val tel: String?,
    val homepage: String?,
    val country: String?
)

data class Track17Event(
    val time_iso: String?,
    val description: String?,
    val location: String?, // may be null or empty
    val stage: String?
)