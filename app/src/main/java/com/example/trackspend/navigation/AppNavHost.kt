package com.example.trackspend.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trackspend.data.local.PackageEntity
import com.example.trackspend.ui.add.AddPackageScreen
import com.example.trackspend.ui.details.PackageDetailScreen
import com.example.trackspend.ui.home.HomeScreen
import com.example.trackspend.viewmodel.PackageViewModel
import com.example.trackspend.viewmodel.PackageViewModelFactory

/**
 * AppNavHost defines ALL navigation routes for your app.
 *
 * Each composable() is one screen.
 */
//fun AppNavHost(navController: NavHostController,
//               modifier: Modifier = Modifier) {
//
//    NavHost(
//        navController = navController,
//        startDestination = Routes.HOME   // Default screen
//    ) {
//        // Home screen
//        composable(Routes.HOME) {
//            PlaceholderScreen("Home Screen")
//        }
//
//        // Add-package screen
//        composable(Routes.ADD) {
//            AddPackageScreen(
//                navController = navController,
//                modifier = modifier,
//                onSave = { tracking, carrier, store, itemName, price, date ->
//                    // TODO: save to DB using ViewModel
//                }
//            )
//        }
//
//        // Analytics screen
//        composable(Routes.ANALYTICS) {
//            PlaceholderScreen("Analytics Screen")
//        }
//
//        // Details screen with a parameter (packageId)
//        composable("${Routes.DETAILS}/{packageId}") { backStackEntry ->
//            val id = backStackEntry.arguments?.getString("packageId")
//            PlaceholderScreen("Details Screen - id: $id")
//        }
//    }
//}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // FIXED ViewModel creation
    val context = LocalContext.current
    val factory = PackageViewModelFactory(context)
    val vm: PackageViewModel = viewModel(factory = factory)

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {

        // HOME LIST SCREEN
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = vm,
                navController = navController
            )
        }

        // ADD PACKAGE
        composable(Routes.ADD) {
            AddPackageScreen(
                navController = navController,
                modifier = modifier,
                onSave = { tracking, carrier, store, itemName, price, date ->

                    vm.addPackage(
                        PackageEntity(
                            trackingNumber = tracking,
                            carrier = carrier,
                            store = store,
                            itemName = itemName,
                            price = price,
                            orderDate = null,
                            eta = null,
                            status = "In Transit",
                            lastUpdate = System.currentTimeMillis()
                        )
                    )
                }
            )
        }

        // DETAILS SCREEN
        composable("${Routes.DETAILS}/{packageId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("packageId")?.toInt()

            if (id != null) {
                PackageDetailScreen(
                    id = id,
                    viewModel = vm,
                    navController = navController
                )
            }
        }

        // ANALYTICS SCREEN
        composable(Routes.ANALYTICS) {
            PlaceholderScreen("Analytics Screen")
        }
    }
}
