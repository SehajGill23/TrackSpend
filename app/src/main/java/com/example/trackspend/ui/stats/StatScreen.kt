package com.example.trackspend.ui.stats

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.trackspend.ui.stats.components.SegmentedRing
import com.example.trackspend.ui.stats.components.YearlyBarChart
import com.example.trackspend.ui.stats.components.YearlyLineChart
import com.example.trackspend.viewmodel.PackageViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: PackageViewModel,
    navController: NavController
) {
    val packages by viewModel.allPackages.collectAsState()
    val monthlySpent by viewModel.monthlySpent.collectAsState()
    val monthlyOrders by viewModel.monthlyOrders.collectAsState()
    val monthlyStoreCounts by viewModel.monthlyOrdersByStore.collectAsState()

    val safeTotal = monthlySpent
    val totalOrders = monthlyOrders
    val totalStores = monthlyStoreCounts.size


    val hasData = packages.isNotEmpty()


    // 🟦 FIXED store counts logic
    val topEntry = monthlyStoreCounts.maxByOrNull { it.value }
    val topStoreName = topEntry?.key ?: "Unknown"

    val topStorePercent =
        if (topEntry != null && totalOrders > 0)
            (topEntry.value.toFloat() / totalOrders.toFloat()).coerceIn(0f, 1f)
        else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Stats",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                },
                modifier = Modifier.height(65.dp)  // thinner top bar
            )
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            SummaryStatsCard(
                hasData = hasData,
                totalSpent = safeTotal,
                totalOrders = totalOrders,
                totalStores = totalStores,
                topStoreName = topStoreName,
                topStorePercent = topStorePercent,
                storeCounts = monthlyStoreCounts,
                onClick = { navController.navigate(Routes.STATS_SUMMARY) }
            )

            StatsMiniCard(
                title = "Monthly spending",
                subtitle = if (hasData) "Tap to view chart" else "No data",
                enabled = hasData,
                onClick = { navController.navigate(Routes.STATS_SPENDING) },
                chart = {
                    YearlyLineChart(
                        data = viewModel.getYearlySpending(),
                        maxY = (viewModel.getYearlySpending().maxOrNull() ?: 1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 30.dp, start = 15.dp, end = 5.dp)
                            .height(160.dp)
                    )
                }
            )

            StatsMiniCard(
                title = "Orders by month",
                subtitle = if (hasData) "Tap to view chart" else "No data",
                enabled = hasData,
                onClick = { navController.navigate(Routes.STATS_ORDERS) },
                chart = {
                    Spacer(Modifier.height(20.dp))
                    YearlyBarChart(
                        data = viewModel.getYearlyOrders(),
                        maxY = (viewModel.getYearlyOrders().maxOrNull() ?: 1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp, start = 5.dp, end = 5.dp)
                            .height(110.dp)
                    )
                }
            )
            Spacer(modifier = Modifier.height(100.dp))
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
    storeCounts: Map<String, Int>,
    onClick: () -> Unit,

) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },   // <-- THIS makes the card clickable
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            //  Radial ring
            Box(
                modifier = Modifier.size(90.dp),
                contentAlignment = Alignment.Center
            ) {
                SegmentedRing(
                    segments = storeCounts,
                    total = totalOrders,
                    showLabels = false
                )
                Text(
                    text = if (hasData) "${(topStorePercent * 100).toInt()}%" else "0%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // 📊 Summary text
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
fun StatsMiniCard(
    title: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit,
    chart: @Composable (() -> Unit)
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f)
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .padding(bottom = 12.dp)  // extra breathing room
        ) {

            // Text content
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Add vertical space before graph
            Spacer(Modifier.height(0.3.dp))

            // Chart goes here
            chart()
        }
    }
}