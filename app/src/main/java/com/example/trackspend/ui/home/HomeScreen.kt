package com.example.trackspend.ui.home
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.trackspend.navigation.Routes
import com.example.trackspend.viewmodel.PackageViewModel


@Composable
fun HomeScreen(
    viewModel: PackageViewModel,
    navController: NavController
) {
    val packages by viewModel.allPackages.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(packages) { pkg ->
            PackageCard(pkg = pkg) {
                navController.navigate("${Routes.DETAILS}/${pkg.id}")
            }
        }
    }
}
