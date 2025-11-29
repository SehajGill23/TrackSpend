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

    private lateinit var db: AppDatabase

    fun init(context: Context) {
        db = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "TrackSpend.db"
        )
            .fallbackToDestructiveMigration()
            .build()

    }

    fun provideDatabase(): AppDatabase = db

    fun providePackageDao(): PackageDao = db.packageDao()
}
