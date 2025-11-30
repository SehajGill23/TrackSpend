package com.example.trackspend.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trackspend.data.local.PackageEntity

//@Composable
//fun PackageCard(
//    pkg: PackageEntity,
//    onClick: () -> Unit,
//    onDelete: () -> Unit
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .combinedClickable(
//                onClick = onClick,        // open details
//                onLongClick = onDelete    // delete package
//            ),
//        elevation = CardDefaults.cardElevation(4.dp),
//        shape = RoundedCornerShape(16.dp)
//    ) {
//
//        Column(modifier = Modifier.padding(16.dp)) {
//
//            Text(
//                pkg.itemName ?: pkg.store ?: "Package",
//                style = MaterialTheme.typography.titleMedium,
//                fontWeight = FontWeight.Bold
//            )
//
//            Spacer(Modifier.height(6.dp))
//            Text("Tracking: ${pkg.trackingNumber}")
//            Text("Carrier: ${pkg.carrier}")
//            Text("Order: ${pkg.orderDate ?: "N/A"}")
//            Text(
//                "Status: ${pkg.status}",
//                color = MaterialTheme.colorScheme.primary,
//                fontWeight = FontWeight.SemiBold
//            )
//        }
//    }
//}

@Composable
fun PackageCard(
    pkg: PackageEntity,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(Modifier.fillMaxWidth()) {

            Column(Modifier.padding(16.dp)) {
                Text(pkg.trackingNumber, style = MaterialTheme.typography.titleMedium)
                Text(pkg.carrier)

                Text("Status: ${pkg.status}")
                pkg.orderDate?.let { Text("Ordered: $it") }
            }

            if (pkg.isPinned) {
                Icon(
                    Icons.Default.PushPin,
                    contentDescription = null,
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.TopEnd)
                        .padding(8.dp)
                )
            }
        }
    }
}