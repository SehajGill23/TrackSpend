package com.example.trackspend.viewmodel


import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackspend.data.local.DatabaseModule
import com.example.trackspend.data.local.PackageEntity
import com.example.trackspend.data.repository.PackageRepository
import com.example.trackspend.data.tracking.track17.TrackingRepository17
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

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

    fun updatePackage(pkg: PackageEntity) = viewModelScope.launch {
        repo.updatePackage(pkg)
    }

    fun updatePinned(id: Int, value: Boolean) {
        viewModelScope.launch {
            repo.updatePinned(id, value)
        }
    }

    val ordersByStore = allPackages.map { list ->
        list
            .mapNotNull { it.store?.takeIf { s -> s.isNotBlank() } }
            .groupingBy { it }
            .eachCount()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyMap())


    // -------------------------------Monthly Parsing methods------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseOrderDate(raw: String?): LocalDate? {
        if (raw.isNullOrBlank()) return null
        if (raw.equals("N/A", ignoreCase = true)) return null

        val cleaned = raw.trim()

        return try {
            // Case 1: directly parse yyyy-MM-dd
            LocalDate.parse(cleaned)
        } catch (_: Exception) {
            try {
                // Case 2: parse yyyyMMdd (8 digits)
                if (cleaned.length == 8 && cleaned.all { it.isDigit() }) {
                    val yyyy = cleaned.substring(0, 4)
                    val mm = cleaned.substring(4, 6)
                    val dd = cleaned.substring(6, 8)
                    LocalDate.parse("$yyyy-$mm-$dd")
                } else null
            } catch (_: Exception) {
                null
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    val monthlyPackages = allPackages
        .map { list ->
            val today = LocalDate.now()

            val start = today.withDayOfMonth(1)
            val end = today.withDayOfMonth(today.lengthOfMonth())

            list.filter { pkg ->
                val date = parseOrderDate(pkg.orderDate)
                date != null && date >= start && date <= end
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    @RequiresApi(Build.VERSION_CODES.O)
    val monthlySpent = monthlyPackages
        .map { list -> list.sumOf { it.price ?: 0.0 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    @RequiresApi(Build.VERSION_CODES.O)
    val monthlyOrders = monthlyPackages
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    @RequiresApi(Build.VERSION_CODES.O)
    val monthlyOrdersByStore = monthlyPackages
        .map { list ->
            list.mapNotNull { it.store?.takeIf { s -> s.isNotBlank() } }
                .groupingBy { it }
                .eachCount()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyMap())

    // -------------------------------Monthly Parsing methods------------------------------------


    @RequiresApi(Build.VERSION_CODES.O)
    fun getDailySpending(): List<Float> {
        val days = LocalDate.now().lengthOfMonth()
        val spending = MutableList(days) { 0f }

        monthlyPackages.value.forEach { pkg ->
            val date = LocalDate.parse(pkg.orderDate)
            val day = date.dayOfMonth - 1
            spending[day] += (pkg.price ?: 0.0).toFloat()
        }

        return spending
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getYearlySpending(): List<Float> {
        val list = allPackages.value
        val currentYear = LocalDate.now().year
        val totals = MutableList(12) { 0f }

        list.forEach { pkg ->
            val raw = pkg.orderDate ?: return@forEach
            try {
                val date = LocalDate.parse(raw)
                if (date.year == currentYear) {
                    val index = date.monthValue - 1
                    totals[index] += (pkg.price ?: 0.0).toFloat()
                }
            } catch (_: Exception) { }
        }

        return totals
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun getYearlyOrders(): List<Float> {
        val list = allPackages.value
        val currentYear = LocalDate.now().year
        val totals = MutableList(12) { 0f }

        list.forEach { pkg ->
            val raw = pkg.orderDate ?: return@forEach
            try {
                val date = LocalDate.parse(raw)
                if (date.year == currentYear) {
                    val index = date.monthValue - 1
                    totals[index] += 1f
                }
            } catch (_: Exception) { }
        }

        return totals
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