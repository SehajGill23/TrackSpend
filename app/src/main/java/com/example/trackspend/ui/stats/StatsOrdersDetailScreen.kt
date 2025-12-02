package com.example.trackspend.ui.stats

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.trackspend.ui.stats.components.ThinToggleSwitch
import com.example.trackspend.ui.stats.components.YearlyBarChart
import com.example.trackspend.ui.stats.components.YearlyLineChart
import com.example.trackspend.viewmodel.PackageViewModel


/**
 * Detailed analytics screen showing the user's total orders across all
 * twelve months of the year. This screen allows switching between a bar
 * chart and a line chart visualization using a thin toggle switch.
 *
 * <p>Features:</p>
 * <ul>
 *   <li><b>Bar / Line Toggle:</b> A custom ThinToggleSwitch lets the user
 *       dynamically switch between two chart types.</li>
 *   <li><b>YearlyBarChart:</b> Displays monthly order counts as bars with
 *       rounded corners and axis labels.</li>
 *   <li><b>YearlyLineChart:</b> Smooth line graph with gradient fill and
 *       data points representing each month.</li>
 *   <li><b>Auto-Scaling Y-Axis:</b> The maximum Y value is increased by 30%
 *       for visual spacing above the highest data point.</li>
 *   <li><b>Consistent Material You styling:</b> Uses the app theme for
 *       colors, surface backgrounds, and typography.</li>
 *   <li><b>Back Navigation:</b> TopAppBar includes a back arrow that pops
 *       the NavController stack.</li>
 * </ul>
 *
 * <p>State & Behavior:</p>
 * <ul>
 *   <li>The chart type is controlled via a boolean state (showBar).</li>
 *   <li>Data is retrieved using {@link PackageViewModel#getYearlyOrders()}.</li>
 *   <li>The chart container uses rounded corners and a dimmed
 *       surfaceVariant background for clarity.</li>
 * </ul>
 *
 * <p>UI Structure:</p>
 * <ol>
 *   <li>TopAppBar with back button</li>
 *   <li>Header text explaining the date range</li>
 *   <li>Bar/Line toggle row</li>
 *   <li>Chart container (Box) that renders either chart</li>
 * </ol>
 *
 * @param navController NavController used for back navigation.
 * @param viewModel PackageViewModel providing yearly aggregated order data.
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsOrdersDetailScreen(
    navController: NavController,
    viewModel: PackageViewModel
) {
    val yearlyOrders = viewModel.getYearlyOrders()

    val maxY = (yearlyOrders.maxOrNull() ?: 1f) * 1.3f
    var showBar by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yearly Orders") },
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
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ---------------- HEADER ----------------
            Text(
                text = "Orders placed from Jan–Dec",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            // ---------------- TOGGLE ----------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Bar", style = MaterialTheme.typography.bodySmall)

                ThinToggleSwitch(
                    checked = showBar,
                    onCheckedChange = { showBar = it },
                    modifier = Modifier.padding(horizontal = 10.dp)
                )

                Text("Line", style = MaterialTheme.typography.bodySmall)
            }

            // ---------------- CHART CARD ----------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (showBar) {
                    YearlyBarChart(
                        data = yearlyOrders,
                        maxY = maxY,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    YearlyLineChart(
                        data = yearlyOrders,
                        maxY = maxY,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}