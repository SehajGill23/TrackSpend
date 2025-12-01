package com.example.trackspend.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.trackspend.ui.stats.components.SegmentedRing
import com.example.trackspend.viewmodel.PackageViewModel





@Composable
fun StatRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                modifier = Modifier.size(22.dp)
            )

            Spacer(Modifier.width(12.dp))

            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsSummaryDetailScreen(
    navController: NavController,
    viewModel: PackageViewModel
) {
    val packages by viewModel.allPackages.collectAsState()
    val totalSpent by viewModel.totalSpent.collectAsState()
    val storeCounts by viewModel.ordersByStore.collectAsState()

    val hasData = packages.isNotEmpty()
    val totalOrders = packages.size
    val totalStores = storeCounts.size
    val safeTotal = totalSpent ?: 0.0

// after safeTotal
    val topStoreEntry = storeCounts.maxByOrNull { it.value }
    val topName = topStoreEntry?.key ?: "Unknown"

    val topPercent =
        if (totalOrders > 0 && topStoreEntry != null)
            topStoreEntry.value.toFloat() / totalOrders.toFloat()
        else
            0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Summary Overview") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {

            // Ring placeholder (we will animate later)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {

                        // the ring itself
                        SegmentedRing(
                            segments = storeCounts,
                            total = totalOrders,
                            modifier = Modifier.size(260.dp),
                            showLabels = true
                        )

                        // centered text overlay
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "This month",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            Text(
                                text = "$${"%.2f".format(safeTotal)}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(Modifier.height(50.dp))

                    Text(
                        text = "Top store: $topName",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "${(topPercent * 100).toInt()}% of all orders",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Divider()

// 🔥 Modern Stats Cards (compact + aesthetic)
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                // 💸 Total Spending
                StatRow(
                    icon = Icons.Default.AttachMoney,
                    label = "Total spending",
                    value = "$${"%.2f".format(safeTotal)}"
                )

                // 📦 Orders
                StatRow(
                    icon = Icons.Default.Inventory2,
                    label = "Orders placed",
                    value = totalOrders.toString()
                )

                // 🏬 Stores
                StatRow(
                    icon = Icons.Default.Store,
                    label = "Distinct stores",
                    value = totalStores.toString()
                )

                if (!hasData) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "No data yet. Add packages to start seeing insights.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
    }
}


