package com.example.trackspend.data.local

import android.content.Context
import androidx.room.Room

/**
 * DatabaseModule is a singleton provider for:
 * - AppDatabase instance (Room)
 * - DAO instance
 *
 * We avoid creating Room database manually anywhere else.
 */
object DatabaseModule {

    // Create the Room database
    fun provideDatabase(context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "TrackSpend.db"          // The actual database file stored on device
        ).build()

    // Provide DAO instance
    fun providePackageDao(db: AppDatabase) = db.packageDao()
}