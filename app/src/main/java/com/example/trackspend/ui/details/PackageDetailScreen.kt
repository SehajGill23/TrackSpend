package com.example.trackspend.ui.details

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.trackspend.viewmodel.PackageViewModel
import com.example.trackspend.data.local.PackageEntity
import com.google.gson.Gson
import com.example.trackspend.data.model.TrackingEvent
import com.example.trackspend.ui.components.TrackingTimeline
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PackageDetailScreen(
    id: Int,
    viewModel: PackageViewModel,
    navController: NavController
) {
    val pkgFlow = viewModel.packageById(id)
    val pkg by pkgFlow.collectAsState(initial = null)

    var showTimeline by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    if (pkg == null) {
        Text("Loading...", modifier = Modifier.padding(16.dp))
        return
    }

    val item = pkg!!
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        // TITLE
        item {
            Text(
                text = "Package Details",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // MAIN INFO CARD
        item {
            MainInfoCard(item)
        }

        // TRACK BUTTON
        item {
            Column {
                Button(
                    onClick = {
                        loading = true
                        showTimeline = true
                        scope.launch {
                            try {
                                viewModel.refreshTracking(item)
                            } finally {
                                loading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Track Package")
                }

                if (loading) {
                    Text("Tracking… Please wait.")
                }
            }
        }

        // TIMELINE SECTION
        item {
            TrackingTimelineSection(pkg = item, show = showTimeline)
        }

        // EMPTY SPACE
        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}

@Composable
fun MainInfoCard(pkg: PackageEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DetailRow("Tracking Number", pkg.trackingNumber)
            DetailRow("Carrier", pkg.carrier)
            DetailRow("Store", pkg.store ?: "N/A")
            DetailRow("Item", pkg.itemName ?: "N/A")
            DetailRow("Price", pkg.price?.let { "$$it" } ?: "N/A")
            DetailRow("Order Date", pkg.orderDate?.toString() ?: "N/A")
            DetailRow("Status", pkg.status)
        }
    }
}

@Composable
fun TrackingTimelineSection(pkg: PackageEntity, show: Boolean) {
    if (!show) return

    var events by remember { mutableStateOf<List<TrackingEvent>>(emptyList()) }

    LaunchedEffect(pkg.trackingHistoryJson) {
        events = try {
            if (pkg.trackingHistoryJson.isNullOrEmpty()) {
                emptyList()
            } else {
                Gson().fromJson(
                    pkg.trackingHistoryJson,
                    Array<TrackingEvent>::class.java
                ).toList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    if (events.isEmpty()) {
        Text(
            text = "No tracking found.",
            style = MaterialTheme.typography.bodyMedium
        )
    } else {
        TrackingTimeline(events = events)
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column {
        Text(label, fontWeight = FontWeight.SemiBold)
        Text(value)
        Spacer(modifier = Modifier.height(8.dp))
    }
}