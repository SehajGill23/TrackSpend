package com.example.trackspend.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlin.jvm.java

/**
 * A ViewModelProvider.Factory is required because
 * PackageViewModel takes a Context parameter.
 *
 * Compose + ViewModel only works automatically when
 * the ViewModel has NO arguments in its constructor.
 *
 * Since we need Context (for Room database), we need this factory.
 */
class PackageViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if asking for the correct ViewModel
        if (modelClass.isAssignableFrom(PackageViewModel::class.java)) {
            return PackageViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
