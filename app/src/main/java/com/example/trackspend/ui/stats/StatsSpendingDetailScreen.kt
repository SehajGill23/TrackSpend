package com.example.trackspend.ui.stats

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.trackspend.ui.stats.components.ThinToggleSwitch
import com.example.trackspend.ui.stats.components.YearlyBarChart
import com.example.trackspend.ui.stats.components.YearlyLineChart
import com.example.trackspend.viewmodel.PackageViewModel


/**
 * Screen displaying a detailed breakdown of the user's yearly spending
 * (January through December). The user can switch between a line chart and
 * a bar chart representation using a thin custom toggle switch.
 *
 * <p><b>Features:</b></p>
 * <ul>
 *   <li><b>Interactive Chart Toggle:</b> A ThinToggleSwitch lets the user
 *       instantly switch between a smooth line graph or bar visualization.</li>
 *   <li><b>YearlyLineChart:</b> Displays spending trends with a gradient
 *       under-fill, grid lines, and data point markers.</li>
 *   <li><b>YearlyBarChart:</b> Simple month-by-month bar visualization with
 *       rounded bars and axis labels.</li>
 *   <li><b>Auto-Adjusted Scaling:</b> The maximum Y-axis is increased by 30%
 *       to give breathing room above the highest value.</li>
 *   <li><b>Material You integration:</b> Uses theme-driven colors, modern
 *       padding, elevations, and rounded 22dp card styling.</li>
 *   <li><b>Back Navigation:</b> TopAppBar back arrow pops the navigation stack.</li>
 * </ul>
 *
 * <p><b>Data sources:</b></p>
 * <ul>
 *   <li>{@link PackageViewModel#getYearlySpending()} — provides 12 values
 *       representing the total spending for each month.</li>
 *   <li>The chart entirely recomposes when toggle state changes.</li>
 * </ul>
 *
 * <p><b>UI layout:</b></p>
 * <ol>
 *   <li>TopAppBar with back arrow</li>
 *   <li>Large rounded card container</li>
 *   <li>Header row with title + chart type toggle</li>
 *   <li>Chart box that fills the card space (line or bar)</li>
 * </ol>
 *
 * @param navController Used to navigate back to the main stats screen.
 * @param viewModel Provides monthly aggregated spending data for the chart.
 */
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatsSpendingDetailScreen(
    navController: NavController,
    viewModel: PackageViewModel
) {
    val yearlyData = viewModel.getYearlySpending()

    val maxY = (yearlyData.maxOrNull() ?: 1f) * 1.3f
    var isLine by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yearly Spending") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { pad ->

        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // --------------------------------------------------------
            // 🔥 BEAUTIFUL ROUNDED CHART CARD
            // --------------------------------------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // ---------- TITLE + TOGGLE ----------
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            "Spending (Jan–Dec)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                        )

                        // Toggle inside card, clean + modern
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Line", style = MaterialTheme.typography.bodySmall)

                            ThinToggleSwitch(
                                checked = isLine,
                                onCheckedChange = { isLine = it },
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            Text("Bar", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    // ---------- THE GRAPH ----------
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .padding(top = 10.dp)
                    ) {
                        if (isLine)
                            YearlyLineChart(
                                data = yearlyData,
                                maxY = maxY,
                                modifier = Modifier.fillMaxSize()
                            )
                        else
                            YearlyBarChart(
                                data = yearlyData,
                                maxY = maxY,
                                modifier = Modifier.fillMaxSize()
                            )
                    }
                }
            }
        }
    }
}