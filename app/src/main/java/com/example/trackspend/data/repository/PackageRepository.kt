package com.example.trackspend.data.repository

import com.example.trackspend.data.local.PackageDao
import com.example.trackspend.data.local.PackageEntity

/**
 * Repository = the backend brain.
 *
 * ViewModel talks to Repository.
 * Repository talks to DAO (database).
 *
 * To keep the architecture clean and testable.
 */
class PackageRepository(

    private val dao: PackageDao
) {

    fun getAllPackages() = dao.getAllPackages()

    fun getTotalSpending() = dao.getTotalSpending()

    suspend fun insertPackage(pkg: PackageEntity) {
        dao.insertPackage(pkg)
    }

    suspend fun deletePackage(pkg: PackageEntity) {
        dao.deletePackage(pkg)
    }
}
