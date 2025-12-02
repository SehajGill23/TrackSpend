package com.example.trackspend.data.local

import android.content.Context
import androidx.room.Room

/**
 * Provides global access to the application's Room database.
 *
 * This singleton is responsible for:
 * - Initializing the database once at app startup.
 * - Exposing the database instance.
 * - Exposing DAO instances used throughout the app.
 *
 * This prevents multiple database instances from being created
 * and keeps database access centralized.
 */
object DatabaseModule {

    private lateinit var db: AppDatabase

    /**
     * Initializes the Room database.
     *
     * Must be called once—typically inside Application.onCreate().
     *
     * @param context Application context used to build the database.
     */
    fun init(context: Context) {
        db = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "TrackSpend.db"
        )
            .fallbackToDestructiveMigration()
            .build()

    }

    /**
     * Returns the singleton Room database instance.
     *
     * @return The application's AppDatabase instance.
     */
    fun provideDatabase(): AppDatabase = db

    /**
     * Returns the DAO responsible for package-related operations.
     *
     * @return PackageDao instance for accessing package data.
     */
    fun providePackageDao(): PackageDao = db.packageDao()
}
