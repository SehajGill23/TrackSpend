package com.example.trackspend.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.trackspend.viewmodel.PackageViewModel

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
                    .padding(top = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    CircularProgressIndicator(
                        progress = { if (hasData) topPercent else 0f },
                        strokeWidth = 12.dp,
                        modifier = Modifier.size(130.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "${(topPercent * 100).toInt()}% from $topName",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Divider()

            // Stats list
            Text("Total spending: $${"%.2f".format(safeTotal)}",
                style = MaterialTheme.typography.titleMedium)

            Text("Orders placed: $totalOrders",
                style = MaterialTheme.typography.bodyLarge)

            Text("Distinct stores: $totalStores",
                style = MaterialTheme.typography.bodyLarge)

            if (!hasData) {
                Spacer(Modifier.height(20.dp))
                Text(
                    "No data yet. Add packages to start seeing insights.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}