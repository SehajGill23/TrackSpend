package com.example.trackspend.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * Renders the bottom navigation bar seen across all main screens.
 *
 * Responsibilities:
 *  - Displays three main destinations (Home, Add, Analytics)
 *  - Highlights the currently active route
 *  - Applies dynamic theming for dark/light mode (colors, glow, icons)
 *  - Handles navigation while preserving back stack rules
 *
 * The bar is visually elevated using a custom glow shadow and rounded
 * corners to match the app’s overall modern glass-UI aesthetic.
 *
 * @param navController Used to navigate between the root screens.
 */
@Composable
fun BottomNavBar(navController: NavController) {

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Add,
        BottomNavItem.Analytics
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // NAV BAR COLORS
    val containerColor =
        if (isDark) Color(0xFF0F0F14).copy(alpha = 0.92f)     // slightly lighter so it pops
        else Color.White.copy(alpha = 0.90f)

    val glow =
        if (isDark) Color(0xFF9B6DFF).copy(alpha = 0.45f)
        else Color(0xFF9B6DFF).copy(alpha = 0.15f)

    val borderColor =
        if (isDark) Color.White.copy(alpha = 0.10f)
        else Color.Black.copy(alpha = 0.08f)

    NavigationBar(
        modifier = Modifier
            .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
            .shadow(
                elevation = 18.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = glow,
                spotColor = glow
            )
            .fillMaxWidth(),
        containerColor = containerColor,
        tonalElevation = 0.dp
    ) {

        items.forEach { item ->

            val selected = currentRoute == item.route

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            popUpTo(Routes.HOME)
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (selected)
                            Color(0xFF9B6DFF)
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(
                            if (selected) 26.dp else 22.dp
                        )
                    )
                },
                label = {
                    Text(
                        item.label,
                        color = if (selected)
                            Color(0xFF9B6DFF)
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = if (isDark)
                        Color(0xFF9B6DFF).copy(alpha = 0.12f)
                    else
                        Color(0xFF9B6DFF).copy(alpha = 0.10f)
                )
            )
        }
    }
}