package com.example.trackspend.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.ui.Modifier
import com.example.trackspend.ui.add.AddPackageScreen

/**
 * AppNavHost defines ALL navigation routes for your app.
 *
 * Each composable() is one screen.
 */
@RequiresApi(Build.VERSION_CODES.O)
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
            AddPackageScreen(
                navController = navController,
                modifier = modifier,
                onSave = { tracking, carrier, store, itemName, price, date ->
                    // TODO: save to DB using ViewModel
                }
            )
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
