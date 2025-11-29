package com.example.trackspend.ui.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.trackspend.viewmodel.PackageViewModel
import com.example.trackspend.data.local.PackageEntity

@Composable
fun PackageDetailScreen(
    id: Int,
    viewModel: PackageViewModel,
    navController: NavController
) {
    // Look for THIS package in the list
    val packages by viewModel.allPackages.collectAsState()
    val pkg = packages.find { it.id == id }

    if (pkg == null) {
        Text("Package not found", modifier = Modifier.padding(16.dp))
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Package Details",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // MAIN CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailRow("Tracking Number", pkg.trackingNumber)
                DetailRow("Carrier", pkg.carrier)
                DetailRow("Store", pkg.store ?: "N/A")
                DetailRow("Item", pkg.itemName ?: "N/A")
                DetailRow("Price", pkg.price?.let { "$$it" } ?: "N/A")
                DetailRow("Order Date", pkg.orderDate?.toString() ?: "N/A")
                DetailRow("Status", pkg.status)
            }
        }

        // TIMELINE PLACEHOLDER (YOU CAN STYLE LATER)
        Text(
            "Tracking Timeline",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("• Label Created")
                Text("• In Transit")
                Text("• Out for Delivery")
                Text("• Delivered")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column {
        Text(label, fontWeight = FontWeight.SemiBold)
        Text(value)
        Spacer(modifier = Modifier.height(8.dp))
    }
}
