package com.example.trackspend.data.repository

import com.example.trackspend.data.local.PackageDao
import com.example.trackspend.data.local.PackageEntity
import kotlinx.coroutines.flow.Flow

/**
 * PackageRepository acts as the data mediator between ViewModel and the DAO.
 *
 * Responsibilities:
 * - Exposes clean functions for accessing and modifying package data.
 * - Hides Room/SQL implementation details from the UI layer.
 * - Keeps architecture modular, scalable, and test-friendly.
 */
class PackageRepository(

    private val dao: PackageDao
) {

    /**
     * Returns a stream of all packages, sorted by pinned and last update.
     *
     * Used by HomeScreen to reactively update the UI whenever the DB changes.
     */
    fun getAllPackages() = dao.getAllPackages()

    /**
     * Returns a Flow representing the total spending across all packages.
     *
     * Useful for analytics dashboards or spending summaries.
     */
    fun getTotalSpending() = dao.getTotalSpending()


    /**
     * Fetches a single package by its ID.
     *
     * @param id The primary key of the package.
     * @return A Flow that emits updates to this package over time.
     */
    fun getPackageById(id: Int): Flow<PackageEntity?> =
        dao.getPackageById(id)


    /**
     * Inserts a new package or replaces an existing one with the same ID.
     *
     * @param pkg The package entity to be stored in the database.
     */
    suspend fun insertPackage(pkg: PackageEntity) {
        dao.insertPackage(pkg)
    }

    /**
     * Deletes a package from the database.
     *
     * @param pkg The package entity to remove.
     */
    suspend fun deletePackage(pkg: PackageEntity) {
        dao.deletePackage(pkg)
    }

    /**
     * Updates the pinned state of a package.
     *
     * @param id The package ID.
     * @param value True to pin, false to unpin.
     */
    suspend fun updatePinned(id: Int, value: Boolean) {
        dao.updatePinned(id, value)
    }

    /**
     * Updates all fields of an existing package.
     *
     * @param pkg The updated package object.
     */
    suspend fun updatePackage(pkg: PackageEntity) {
        dao.updatePackage(pkg)
    }
}
