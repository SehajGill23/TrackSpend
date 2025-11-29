package com.example.trackspend.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.trackspend.data.model.TrackingEvent

class TrackingConverters {

    private val gson = Gson()

    @TypeConverter
    fun fromHistory(list: List<TrackingEvent>?): String? {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toHistory(json: String?): List<TrackingEvent>? {
        if (json.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<TrackingEvent>>() {}.type
        return gson.fromJson(json, type)
    }
}
