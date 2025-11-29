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
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    val trackingNumber: String,
    val carrier: String,
    val store: String?,
    val itemName: String?,
    val price: Double?,

    val orderDate: String?,
    val eta: String?,
    val status: String,
    val lastUpdate: Long
)