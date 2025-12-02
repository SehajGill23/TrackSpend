package com.example.trackspend.data.model

/**
 * StoreCount represents the result of an aggregated SQL query:
 * each row contains:
 * - the store name (nullable if missing in the database)
 * - the total number of orders associated with that store
 *
 * This model is used by DAO queries that group packages by store.
 */
data class StoreCount(
    val store: String?, // Store name (nullable)
    val count: Int      // Number of orders from this store
)