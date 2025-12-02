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

// ---------------------------------------------------------
//  Universal Glow Card (matches AddPackage + Home UI)
// ---------------------------------------------------------
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


// ---------------------------------------------------------
//   MAIN SCREEN
// ---------------------------------------------------------
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
                            color = Color.White,
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
                            Text("Top Store: $topStoreName", color = Color.LightGray)
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
                    Text("No Data", color = Color.LightGray)
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
                    Text("No Data", color = Color.LightGray)
                } else {
                    YearlyBarChart(
                        data = viewModel.getYearlyOrders(),
                        maxY = viewModel.getYearlyOrders().maxOrNull() ?: 1f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(top = 30.dp)
                    )
                }
            }

            Spacer(Modifier.height(60.dp))
        }
    }
}