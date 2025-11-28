package com.example.trackspend.viewmodel

import com.example.trackspend.data.local.PackageEntity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackspend.data.local.DatabaseModule
import com.example.trackspend.data.repository.PackageRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.content.Context

/**
 * PackageViewModel connects the backend (Room/Repository)
 * with the UI (Compose screens).
 *
 * It exposes Flows as State so UI updates automatically.
 */
class PackageViewModel(context: Context) : ViewModel() {

    // Build database + DAO + repository
    private val db = DatabaseModule.provideDatabase(context)
    private val dao = DatabaseModule.providePackageDao(db)
    private val repo = PackageRepository(dao)

    // Live list of all tracked packages
    val allPackages = repo.getAllPackages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // Live total spending amount
    val totalSpent = repo.getTotalSpending()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    // Add a package to database
    fun addPackage(pkg: PackageEntity) {
        viewModelScope.launch {
            repo.insertPackage(pkg)
        }
    }

    // Delete a package
    fun deletePackage(pkg: PackageEntity) {
        viewModelScope.launch {
            repo.deletePackage(pkg)
        }
    }
}
