package com.example.trackspend.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * PackageDao defines all database operations (CRUD + queries) for the `packages` table.
 *
 * Acts as the single source of truth for database access — ViewModels and Repositories
 * must call DAO methods instead of writing SQL manually.
 */
@Dao
interface PackageDao {

    /**
     * Inserts a package into the database.
     *
     * If a row with the same ID already exists, it will be replaced.
     *
     * @param pkg The package entity to insert or update.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackage(pkg: PackageEntity)

    /**
     * Updates an existing package.
     *
     * Only modifies the fields inside the given entity.
     *
     * @param pkg The updated package data.
     */
    @Update
    suspend fun updatePackage(pkg: PackageEntity)


    /**
     * Returns a flow of all packages, automatically updating
     * whenever the database changes.
     *
     * Sorting:
     * - pinned items first
     * - then most recently updated items
     *
     * @return Flow emitting a list of all PackageEntity rows.
     */
    @Query("SELECT * FROM packages ORDER BY isPinned DESC, lastUpdate DESC")
    fun getAllPackages(): Flow<List<PackageEntity>>


    /**
     * Returns a continuously-updating total of all spending.
     *
     * @return Flow emitting the sum of all price values, or null if no rows exist.
     */
    @Query("SELECT SUM(price) FROM packages")
    fun getTotalSpending(): Flow<Double?>


    /**
     * Observes a single package by its ID.
     * Automatically updates when the row changes.
     *
     * @param id The primary key of the package.
     * @return Flow emitting the package or null if not found.
     */
    @Query("SELECT * FROM packages WHERE id = :id")
    fun getPackageById(id: Int): Flow<PackageEntity?>


    /**
     * Returns the number of orders placed per store.
     *
     * SQL:
     *  SELECT store, COUNT(*) AS count FROM packages GROUP BY store
     *
     * @return Flow of StoreCount objects (store name + count).
     */
    @Query("SELECT store, COUNT(*) AS count FROM packages GROUP BY store")
    fun getOrdersByStore(): Flow<List<com.example.trackspend.data.model.StoreCount>>

    /**
     * Deletes a specific package from the database.
     *
     * @param pkg The package entity to remove.
     */
    @Delete
    suspend fun deletePackage(pkg: PackageEntity)


    /**
     * Updates the pinned/unpinned state of a package.
     *
     * @param id The ID of the package.
     * @param value true to pin, false to unpin.
     */
    @Query("UPDATE packages SET isPinned = :value WHERE id = :id")
    suspend fun updatePinned(id: Int, value: Boolean)


}
