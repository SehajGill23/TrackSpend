package com.example.trackspend.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.trackspend.data.local.PackageEntity

@Composable
fun PackageCard(
    pkg: PackageEntity,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },      // ← Go to detail screen
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(pkg.trackingNumber, style = MaterialTheme.typography.titleMedium)
            Text(pkg.carrier, style = MaterialTheme.typography.bodyMedium)

            // Expandable section
            if (expanded) {
                Spacer(Modifier.height(8.dp))

                pkg.itemName?.let {
                    Text("Item: $it", style = MaterialTheme.typography.bodySmall)
                }

                pkg.store?.let {
                    Text("Store: $it", style = MaterialTheme.typography.bodySmall)
                }

                pkg.price?.let {
                    Text("Price: $it", style = MaterialTheme.typography.bodySmall)
                }

                Text("Status: ${pkg.status}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
