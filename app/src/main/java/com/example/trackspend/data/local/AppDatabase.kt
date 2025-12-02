package com.example.trackspend.data.local


import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * The main Room database for the TrackSpend application.
 *
 * This class serves as the entry point for all local data storage.
 * It defines:
 * - The list of entities (tables) used in the database.
 * - The database schema version for migration handling.
 * - The DAOs (Data Access Objects) that expose queries and operations.
 *
 * Room automatically generates the database implementation at build time.
 */
@Database(
    entities = [PackageEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Provides access to all package-related database operations.
     *
     * @return A DAO containing CRUD functions for `PackageEntity`.
     */
    abstract fun packageDao(): PackageDao
}
