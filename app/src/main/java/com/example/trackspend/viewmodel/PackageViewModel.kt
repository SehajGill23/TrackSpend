package com.example.trackspend.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackspend.data.local.DatabaseModule
import com.example.trackspend.data.local.PackageEntity
import com.example.trackspend.data.repository.PackageRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PackageViewModel(context: Context) : ViewModel() {

    // Database must be initialized globally in MainActivity BEFORE creating this ViewModel
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
}
