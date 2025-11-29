package com.example.trackspend.data.remote

/**
 * Output model from EmailParser.
 */
data class ParsedEmailInfo(
    val trackingNumber: String? = null,
    val carrier: String? = null,
    val store: String? = null,
    val itemName: String? = null,
    val price: Double? = null,
    val orderDate: String? = null,
    val senderDomain: String? = null
)
