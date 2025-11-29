package com.example.trackspend.data.remote

import android.content.Context
import com.google.gson.Gson

/**
 * Loads the list of known e-commerce stores from assets/stores.json
 */
object StoreLoader {
    fun loadStoreList(context: Context): List<String> {
        return try {
            val json = context.assets.open("stores.json")
                .bufferedReader()
                .use { it.readText() }

            Gson().fromJson(json, Array<String>::class.java).toList()
        } catch (e: Exception) {
            emptyList() // fail safe
        }
    }
}
