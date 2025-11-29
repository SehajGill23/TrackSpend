package com.example.trackspend.data.local


import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * AppDatabase defines the Room database configuration.
 *
 * - entities = all tables inside the database
 * - version = database version for migrations
 */
@Database(
    entities = [PackageEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun packageDao(): PackageDao
}
