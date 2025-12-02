package com.example.trackspend.data.remote

/**
 * ParsedEmailInfo represents the final structured result returned
 * by EmailParser after scanning a raw email.
 *
 * Each field is optional because parsing is best-effort — emails
 * vary heavily across stores and formats.
 *
 * Fields include:
 *  - trackingNumber: extracted tracking code (UPS/FedEx/etc.)
 *  - carrier: detected carrier name, either from tracking or body
 *  - store: store the order came from
 *  - itemName: product name guessed from email content
 *  - price: extracted order total or paid amount
 *  - orderDate: normalized date string (YYYY-MM-DD)
 *  - senderDomain: email domain used for store inference
 *
 * This model is used by the Add Package screen to pre-fill fields.
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
