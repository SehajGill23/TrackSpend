package com.example.trackspend.navigation


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Defines the full set of items shown in the bottom navigation bar.
 *
 * This sealed class allows each destination to have:
 *  - a unique route for navigation,
 *  - a user-visible label,
 *  - an icon for UI display.
 *
 * Using a sealed class ensures all nav items are strongly typed,
 * discoverable at compile time, and prevents invalid items from being created.
 */
sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(
        route = Routes.HOME,
        label = "Home",
        icon = Icons.Default.Home
    )

    object Add : BottomNavItem(
        route = Routes.ADD,
        label = "Add",
        icon = Icons.Default.Add
    )

    object Analytics : BottomNavItem(
        route = Routes.ANALYTICS,
        label = "Stats",
        icon = Icons.Default.PieChart
    )
}
