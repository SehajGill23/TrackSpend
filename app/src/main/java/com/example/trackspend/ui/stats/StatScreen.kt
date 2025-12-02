@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.trackspend.ui.stats

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.trackspend.navigation.Routes
import com.example.trackspend.ui.stats.components.SegmentedRing
import com.example.trackspend.ui.stats.components.YearlyBarChart
import com.example.trackspend.ui.stats.components.YearlyLineChart
import com.example.trackspend.viewmodel.PackageViewModel


// 🔥 Same colors as AddPackageScreen / Home
private val PurpleBrand = Color(0xFF9B6DFF)
private val DarkPurple = Color(0xFF110124)
private val LightPurple = Color(0xFFF0E6FC)
private val BorderLight = Color.Black.copy(alpha = 0.10f)
private val BorderDark = Color.White.copy(alpha = 0.12f)


/**
 * A reusable glowing card component used throughout the Stats screen.
 *
 * This composable wraps content inside a branded TrackSpend-style glow box,
 * using a soft purple ambient shadow, rounded corners, theme-aware background
 * colors, and a subtle glass-like border.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Adaptive colors for dark and light mode</li>
 *   <li>Purple glow shadow using custom ambient/spot colors</li>
 *   <li>Rounded-corner shape with 20.dp radius</li>
 *   <li>Border stroke to create a soft glass rim appearance</li>
 *   <li>Fully flexible content slot for inner UI</li>
 * </ul>
 *
 * @param modifier Optional modifier used to adjust layout, padding, or size.
 * @param content Composable lambda defining the card's inner UI content.
 */
@Composable
fun StatsGlowCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val glowColor = if (isDark) PurpleBrand.copy(alpha = 0.9f) else PurpleBrand.copy(alpha = 0.7f)
    val fill = if (isDark) DarkPurple else LightPurple
    val border = if (isDark) BorderDark else BorderLight

    Column(
        modifier = modifier.shadow(
            elevation = 18.dp,
            shape = RoundedCornerShape(20.dp),
            ambientColor = glowColor,
            spotColor = glowColor,
            clip = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = fill),
            border = BorderStroke(1.dp, border),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                content()
            }
        }
    }
}


/**
 * Main Statistics Dashboard screen for the TrackSpend app.
 *
 * Displays the user's annual analytics including:
 * total spending, total orders, monthly spending patterns, order distribution
 * by store, and top-store percentage. Uses custom-built Canvas charts and glow
 * cards to present data visually and interactively.
 *
 * <p>Key UI Elements:</p>
 * <ul>
 *   <li><b>Summary (Ring) Card</b> — Shows total spending, total orders, store count,
 *       and top store share of monthly orders.</li>
 *   <li><b>YearlyLineChart</b> — Visualizes monthly spending trends across the year.</li>
 *   <li><b>YearlyBarChart</b> — Displays monthly order counts using animated bars.</li>
 *   <li><b>SegmentedRing</b> — Circular distribution of orders by store.</li>
 *   <li><b>Glow Cards</b> — Consistent branded container UI for charts.</li>
 *   <li><b>Navigation</b> — Each chart card navigates to a more detailed analytics screen.</li>
 * </ul>
 *
 * <p>State & Data:</p>
 * Values are obtained from {@link PackageViewModel} using StateFlow:
 * <ul>
 *   <li>allPackages – raw list of packages</li>
 *   <li>monthlySpent – aggregated monthly spending</li>
 *   <li>monthlyOrders – number of orders per month</li>
 *   <li>monthlyOrdersByStore – store-based order distribution map</li>
 * </ul>
 *
 * <p>Theme Support:</p>
 * Colors and glow effects adapt automatically to dark or light mode.
 *
 * @param viewModel The PackageViewModel providing aggregated analytics data.
 * @param navController Used to navigate to Stats Summary, Spending, or Orders detail screens.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatsScreen(
    viewModel: PackageViewModel,
    navController: NavController
) {
    val packages by viewModel.allPackages.collectAsState()
    val monthlySpent by viewModel.monthlySpent.collectAsState()
    val monthlyOrders by viewModel.monthlyOrders.collectAsState()
    val storeCounts by viewModel.monthlyOrdersByStore.collectAsState()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val hasData = packages.isNotEmpty()
    val totalSpent = monthlySpent
    val totalOrders = monthlyOrders
    val totalStores = storeCounts.size

    val topEntry = storeCounts.maxByOrNull { it.value }
    val topStoreName = topEntry?.key ?: "Unknown"
    val topStorePercent =
        if (topEntry != null && totalOrders > 0)
            (topEntry.value.toFloat() / totalOrders.toFloat())
        else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Stats",
                        color = PurpleBrand,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {

            // ---------------------------------------------------------
            // SUMMARY Stats Card (Glow)
            // ---------------------------------------------------------
            StatsGlowCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { if (hasData) navController.navigate(Routes.STATS_SUMMARY) }
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    // Ring Chart
                    Box(
                        modifier = Modifier.size(95.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SegmentedRing(
                            segments = storeCounts,
                            total = totalOrders,
                            showLabels = false
                        )
                        Text(
                            text = if (hasData) "${(topStorePercent * 100).toInt()}%" else "0%",
                            color =  if (isDark) Color.White else PurpleBrand,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f).padding(start = 14.dp)
                    ) {
                        Text("This Month", color = PurpleBrand)

                        Text(
                            "Total Spending: \$${"%.2f".format(totalSpent)}",
                            fontWeight = FontWeight.Bold
                        )
                        Text("Orders: $totalOrders")
                        Text("Stores: $totalStores")

                        if (hasData)
                            Text("Top Store: $topStoreName", color = if (isDark) Color.LightGray else PurpleBrand)
                    }
                }
            }


            // ---------------------------------------------------------
            // LINE CHART (Glow Card)
            // ---------------------------------------------------------
            StatsGlowCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { if (hasData) navController.navigate(Routes.STATS_SPENDING) }
            ) {

                Text("Monthly Spending", color = PurpleBrand, fontWeight = FontWeight.Bold)

                if (!hasData) {
                    Text("No Data", color = if (isDark) Color.LightGray else Color.Gray)
                } else {
                    YearlyLineChart(
                        data = viewModel.getYearlySpending(),
                        maxY = viewModel.getYearlySpending().maxOrNull() ?: 1f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(top = 20.dp)
                    )
                }
            }


            // ---------------------------------------------------------
            // BAR CHART (Glow Card)
            // ---------------------------------------------------------
            StatsGlowCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { if (hasData) navController.navigate(Routes.STATS_ORDERS) }
            ) {
                Text("Orders by Month", color = PurpleBrand, fontWeight = FontWeight.Bold)

                if (!hasData) {
                    Text("No Data", color = if (isDark) Color.LightGray else Color.Gray)
                } else {
                    YearlyBarChart(
                        data = viewModel.getYearlyOrders(),
                        maxY = viewModel.getYearlyOrders().maxOrNull() ?: 1f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(top = 60.dp)
                    )
                }
            }

            Spacer(Modifier.height(60.dp))
        }
    }
}