package com.example.trackspend.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * PackageDao contains all SQL queries for the packages table.
 * DAO = Data Access Object.
 *
 * ViewModel never touches SQL directly — it always passes through DAO.
 */
@Dao
interface PackageDao {

    // Insert or update package
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackage(pkg: PackageEntity)

    @Update
    suspend fun updatePackage(pkg: PackageEntity)


    // Read all tracked packages, sorted by latest update
    @Query("SELECT * FROM packages ORDER BY isPinned DESC, lastUpdate DESC")
    fun getAllPackages(): Flow<List<PackageEntity>>

    // Calculate total spending (nullable)
    @Query("SELECT SUM(price) FROM packages")
    fun getTotalSpending(): Flow<Double?>

    @Query("SELECT * FROM packages WHERE id = :id")
    fun getPackageById(id: Int): Flow<PackageEntity?>

    @Query("SELECT store, COUNT(*) AS count FROM packages GROUP BY store")
    fun getOrdersByStore(): Flow<List<com.example.trackspend.data.model.StoreCount>>

    // Delete a package
    @Delete
    suspend fun deletePackage(pkg: PackageEntity)

    @Query("UPDATE packages SET isPinned = :value WHERE id = :id")
    suspend fun updatePinned(id: Int, value: Boolean)


}
