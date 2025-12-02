package com.example.trackspend.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


/**
 * Factory class responsible for creating instances of {@link PackageViewModel}.
 *
 * <p>This factory allows the {@link PackageViewModel} to receive a {@link Context}
 * parameter, which cannot be passed through the default ViewModelProvider.</p>
 *
 * <p><b>How it works:</b></p>
 * <ul>
 *   <li>Checks whether the requested ViewModel class matches {@code PackageViewModel}.</li>
 *   <li>Creates and returns a new instance of {@code PackageViewModel}, providing the
 *       required application context.</li>
 *   <li>Throws an {@link IllegalArgumentException} if the ViewModel type is unknown.</li>
 * </ul>
 *
 * @constructor Creates a new factory with the provided application context.
 * @param context The context used by the ViewModel for database access, resources,
 *                and other application-level operations.
 */
class PackageViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")

    /**
     * Creates an instance of the requested ViewModel class.
     *
     * @param modelClass The ViewModel class requested by the ViewModelProvider.
     * @return A new instance of {@link PackageViewModel} if the type matches.
     * @throws IllegalArgumentException If the requested ViewModel class is not supported.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(PackageViewModel::class.java)) {
            return PackageViewModel(context) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
