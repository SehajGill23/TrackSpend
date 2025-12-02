package com.example.trackspend.data.remote

import android.content.Context
import com.google.gson.Gson

/**
 * StoreLoader reads a predefined list of known online stores
 * from the app's assets directory (stores.json).
 *
 * The EmailParser uses this list to improve store detection
 * when parsing emails and identifying the merchant.
 */
object StoreLoader {

    /**
     * Loads the store list from assets/stores.json.
     *
     * @param context Android context used to access the assets folder.
     * @return A list of store names, or an empty list if reading fails.
     *
     * This prevents crashes and ensures EmailParser always
     * has a safe fallback when store detection is attempted.
     */
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
