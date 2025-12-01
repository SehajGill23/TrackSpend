package com.example.trackspend.ui.stats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.trackspend.navigation.Routes
import com.example.trackspend.viewmodel.PackageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: PackageViewModel,
    navController: NavController
) {
    val packages by viewModel.allPackages.collectAsState()
    val totalSpent by viewModel.totalSpent.collectAsState()

    val hasData = packages.isNotEmpty()
    val safeTotal = totalSpent ?: 0.0

    // 🔹 Store counts ("Amazon" = 3, "Nike" = 1, etc.)
    val storeCounts = packages
        .groupBy { it.store ?: "Unknown" }
        .mapValues { it.value.size }

    val totalOrders = packages.size

    // 🔹 Top store % for radial ring
    val topStorePercent: Float =
        if (totalOrders > 0 && storeCounts.isNotEmpty()) {
            val maxStoreOrders = storeCounts.maxOf { it.value }
            (maxStoreOrders.toFloat() / totalOrders.toFloat()).coerceIn(0f, 1f)
        } else 0f

    // 🔹 Top store name
    val topStoreName: String =
        if (storeCounts.isNotEmpty())
            storeCounts.maxByOrNull { it.value }!!.key
        else "Unknown"

    // 🔹 Distinct stores
    val totalStores = storeCounts.size

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Stats", style = MaterialTheme.typography.titleLarge) })
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 🔵 UPDATED SUMMARY CARD (now store-based ring)
            SummaryStatsCard(
                hasData = hasData,
                totalSpent = safeTotal,
                totalOrders = totalOrders,
                totalStores = totalStores,
                topStoreName = topStoreName,
                topStorePercent = topStorePercent,
                modifier = Modifier.clickable {
                    // later we'll open the full summary detail
                }
            )

            StatsMiniCard(
                title = "Monthly spending",
                subtitle = if (hasData) "Tap to view chart" else "No packages yet",
                enabled = hasData,
                onClick = { navController.navigate(Routes.STATS_SPENDING) }
            )

            StatsMiniCard(
                title = "Orders by store",
                subtitle = if (hasData) "Tap to view chart" else "No packages yet",
                enabled = hasData,
                onClick = { navController.navigate(Routes.STATS_ORDERS) }
            )
        }
    }
}

@Composable
private fun SummaryStatsCard(
    hasData: Boolean,
    totalSpent: Double,
    totalOrders: Int,
    totalStores: Int,
    topStoreName: String,
    topStorePercent: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ⭐ LEFT SIDE: store distribution radial ring
            Box(
                modifier = Modifier.size(90.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { if (hasData) topStorePercent else 0f },
                    strokeWidth = 10.dp
                )
                Text(
                    text = if (hasData) "${(topStorePercent * 100).toInt()}%" else "0%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // ⭐ RIGHT SIDE: summary info
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("This month", style = MaterialTheme.typography.labelMedium)

                Text(
                    text = "Total spending: \$${"%.2f".format(totalSpent)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text("Orders: $totalOrders", style = MaterialTheme.typography.bodyMedium)
                Text("Stores: $totalStores", style = MaterialTheme.typography.bodyMedium)

                if (hasData) {
                    Text(
                        "Top store: $topStoreName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        "No packages tracked yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsMiniCard(
    title: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val alpha = if (enabled) 1f else 0.5f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}