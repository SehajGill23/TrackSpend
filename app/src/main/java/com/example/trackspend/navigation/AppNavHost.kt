package com.example.trackspend.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.ui.Modifier

/**
 * AppNavHost defines ALL navigation routes for your app.
 *
 * Each composable() is one screen.
 */
@Composable
fun AppNavHost(navController: NavHostController,
               modifier: Modifier = Modifier) {

    NavHost(
        navController = navController,
        startDestination = Routes.HOME   // Default screen
    ) {
        // Home screen
        composable(Routes.HOME) {
            PlaceholderScreen("Home Screen")
        }

        // Add-package screen
        composable(Routes.ADD) {
            PlaceholderScreen("Add Package Screen")
        }

        // Analytics screen
        composable(Routes.ANALYTICS) {
            PlaceholderScreen("Analytics Screen")
        }

        // Details screen with a parameter (packageId)
        composable("${Routes.DETAILS}/{packageId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("packageId")
            PlaceholderScreen("Details Screen - id: $id")
        }
    }
}
