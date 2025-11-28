package com.example.trackspend.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * PackageEntity represents a single tracked package stored in Room database.
 *
 * @Entity tells Room to create a table named "packages".
 * Each field becomes a column in the table.
 */
@Entity(tableName = "packages")
data class PackageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,   // Auto ID
    val trackingNumber: String,                        // UPS/FedEx/Canada Post tracking code
    val carrier: String,                               // Carrier name
    val store: String?,                                // Amazon, eBay, Walmart, etc.
    val itemName: String?,                             // Name of product
    val price: Double?,                                // Optional price
    val orderDate: Long?,                              // Timestamp
    val eta: Long?,                                    // Estimated delivery timestamp
    val status: String,                                // "In Transit", "Delivered", etc.
    val lastUpdate: Long                               // When we last updated this entry
)
