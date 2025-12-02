package com.example.trackspend.ui.stats

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.trackspend.ui.stats.components.SegmentedRing
import com.example.trackspend.viewmodel.PackageViewModel


/**
 * A compact, reusable row used for displaying a labeled statistic in the
 * summary screen. Shows an icon, a text label, and the value aligned to the
 * right.
 *
 * <p><b>UI Structure:</b></p>
 * <ul>
 *   <li>Left section: icon + label</li>
 *   <li>Right section: bold statistic value</li>
 * </ul>
 *
 * <p><b>Used in:</b></p>
 * <ul>
 *   <li>{@link StatsSummaryDetailScreen} — to display total spending,
 *       total orders, and number of stores.</li>
 * </ul>
 *
 * @param icon  The leading icon representing the stat.
 * @param label The descriptive label for the statistic (e.g., "Orders").
 * @param value The formatted value to display on the right side.
 */
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


/**
 * Full summary view for the user's monthly activity across spending,
 * orders, and store distribution. This includes a large segmented ring
 * chart, top-store insights, and compact statistics rows.
 *
 * <p><b>Features:</b></p>
 * <ul>
 *   <li><b>SegmentedRing chart:</b> Visualizes how orders are distributed
 *       across different stores for the current month.</li>
 *   <li><b>Dynamic theme-aware labels:</b> Ring labels automatically adjust
 *       their brightness depending on dark/light mode.</li>
 *   <li><b>Top Store Insight:</b> Displays the store with the highest number
 *       of orders and its percentage contribution.</li>
 *   <li><b>Monthly financial summary:</b> Shows total spending, total orders,
 *       and number of unique stores used.</li>
 *   <li><b>Back navigation:</b> Uses a TopAppBar with a back icon.</li>
 * </ul>
 *
 * <p><b>Data sources (from PackageViewModel):</b></p>
 * <ul>
 *   <li>{@code monthlySpent}: total amount spent this month</li>
 *   <li>{@code monthlyOrders}: number of orders this month</li>
 *   <li>{@code monthlyOrdersByStore}: map of store → # of orders</li>
 *   <li>{@code allPackages}: used to determine if any data exists</li>
 * </ul>
 *
 * <p><b>UI Layout Overview:</b></p>
 * <ol>
 *   <li>TopAppBar with back arrow</li>
 *   <li>Large center-aligned ring chart with spending overlay text</li>
 *   <li>Top store name + percentage section</li>
 *   <li>Divider separator</li>
 *   <li>StatRow cards for the numerical metrics</li>
 * </ol>
 *
 * @param navController Handles navigation back to the Stats screen.
 * @param viewModel Provides all monthly and per-store data needed to build the summary.
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsSummaryDetailScreen(
    navController: NavController,
    viewModel: PackageViewModel
) {
    val packages by viewModel.allPackages.collectAsState()

    val monthlySpent by viewModel.monthlySpent.collectAsState()
    val totalOrders by viewModel.monthlyOrders.collectAsState()
    val storeCounts by viewModel.monthlyOrdersByStore.collectAsState()

    val hasData = packages.isNotEmpty()
    val totalStores =  storeCounts.size
    val safeTotal = monthlySpent ?: 0.0

    // after safeTotal
    val topStoreEntry = storeCounts.maxByOrNull { it.value }
    val topName = topStoreEntry?.key ?: "Unknown"

    val topPercent =
        if (totalOrders > 0 && topStoreEntry != null)
            topStoreEntry.value.toFloat() / totalOrders.toFloat()
        else
            0f

    val isLightTheme = MaterialTheme.colorScheme.background.luminance() > 0.5f

    val ringLabelColor: Int =
        if (isLightTheme) {
            // soft dark text for light background
            android.graphics.Color.argb(190, 0, 0, 0)
        } else {
            // soft white for dark background
            android.graphics.Color.argb(190, 255, 255, 255)
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Summary Overview") },
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
                            showLabels = true,
                            labelColor = ringLabelColor

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

        //  Modern Stats Cards (compact + aesthetic)
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


