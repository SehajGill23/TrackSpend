package com.example.trackspend.viewmodel

import com.example.trackspend.data.tracking.track17.TrackingRepository17


import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackspend.data.local.DatabaseModule
import com.example.trackspend.data.local.PackageEntity
import com.example.trackspend.data.repository.PackageRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PackageViewModel(context: Context) : ViewModel() {

    private val db = DatabaseModule.provideDatabase()
    private val dao = DatabaseModule.providePackageDao()
    private val repo = PackageRepository(dao)

    val allPackages = repo.getAllPackages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val totalSpent = repo.getTotalSpending()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    fun addPackage(pkg: PackageEntity) {
        viewModelScope.launch {
            repo.insertPackage(pkg)
        }
    }

    fun deletePackage(pkg: PackageEntity) {
        viewModelScope.launch {
            repo.deletePackage(pkg)
        }
    }

    fun packageById(id: Int): Flow<PackageEntity?> {
        return repo.getPackageById(id)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshTracking(pkg: PackageEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("TRACK17", "refreshTracking() START for ${pkg.trackingNumber}")

                val result = TrackingRepository17().track(pkg.trackingNumber)

                Log.d("TRACK17", "Result: latest='${result.latestStatus}', events=${result.events.size}")

                val json = Gson().toJson(result.events)

                dao.updatePackage(
                    pkg.copy(
                        status = result.latestStatus,
                        trackingHistoryJson = json,
                        lastUpdate = System.currentTimeMillis()
                    )
                )

                Log.d("TRACK17", "DB updated successfully")
            } catch (e: Exception) {
                Log.e("TRACK17", "refreshTracking() FAILED", e)

                val debugEvents = listOf(
                    com.example.trackspend.data.model.TrackingEvent(
                        status = "DEBUG ERROR: ${e.message}",
                        location = "App",
                        timestamp = System.currentTimeMillis()
                    )
                )
                val json = Gson().toJson(debugEvents)

                dao.updatePackage(
                    pkg.copy(
                        status = "Tracking failed",
                        trackingHistoryJson = json,
                        lastUpdate = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}