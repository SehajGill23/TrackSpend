package com.example.trackspend.navigation


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents each bottom navigation item.
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
