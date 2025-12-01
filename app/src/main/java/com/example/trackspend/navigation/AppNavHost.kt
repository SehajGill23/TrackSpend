package com.example.trackspend.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.trackspend.data.local.PackageEntity
import com.example.trackspend.ui.add.AddPackageScreen
import com.example.trackspend.ui.details.EditPackageScreen
import com.example.trackspend.ui.details.PackageDetailScreen
import com.example.trackspend.ui.home.HomeScreen
import com.example.trackspend.ui.stats.StatsOrdersDetailScreen
import com.example.trackspend.ui.stats.StatsScreen
import com.example.trackspend.ui.stats.StatsSpendingDetailScreen
import com.example.trackspend.viewmodel.PackageViewModel
import com.example.trackspend.viewmodel.PackageViewModelFactory

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val factory = PackageViewModelFactory(context)
    val vm: PackageViewModel = viewModel(factory = factory)

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {

        // 🏡 HOME SCREEN
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = vm,
                navController = navController
            )
        }

        // ➕ ADD PACKAGE
        composable(Routes.ADD) {
            AddPackageScreen(
                navController = navController,
                modifier = modifier,
                onSave = { tracking, carrier, store, itemName, price, orderDate ->

                    vm.addPackage(
                        PackageEntity(
                            trackingNumber = tracking.trim(),
                            carrier = carrier.trim(),
                            store = store.trim().ifEmpty { null },
                            itemName = itemName.trim().ifEmpty { null },
                            price = price,
                            orderDate = orderDate.ifEmpty { null },  // ← FIXED
                            eta = null,
                            status = "N/A",
                            lastUpdate = System.currentTimeMillis(),
                            trackingHistoryJson = null
                        )
                    )
                }
            )
        }

        // 📦 PACKAGE DETAILS
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

        // ✏️ EDIT PACKAGE
        composable("edit/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toInt()

            if (id != null) {
                EditPackageScreen(
                    id = id,
                    viewModel = vm,
                    navController = navController
                )
            }
        }

        // 📊 ANALYTICS MAIN PAGE
        composable(Routes.ANALYTICS) {
            StatsScreen(
                viewModel = vm,
                navController = navController
            )
        }

        // 📈 MONTHLY SPENDING DETAIL PAGE
        composable(Routes.STATS_SPENDING) {
            StatsSpendingDetailScreen(
                navController = navController,
                viewModel = vm
            )
        }

        // 🏪 ORDERS BY STORE CHART PAGE
        composable(Routes.STATS_ORDERS) {
            StatsOrdersDetailScreen(
                navController = navController,
                viewModel = vm
            )
        }
    }
}

