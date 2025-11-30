package com.example.trackspend.ui.details

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.trackspend.viewmodel.PackageViewModel
import com.example.trackspend.data.local.PackageEntity
import com.google.gson.Gson
import com.example.trackspend.data.model.TrackingEvent
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PackageDetailScreen(
    id: Int,
    viewModel: PackageViewModel,
    navController: NavController
) {
    val pkgFlow = viewModel.packageById(id)
    val pkg by pkgFlow.collectAsState(initial = null)

    if (pkg == null) {
        Text("Loading...", modifier = Modifier.padding(16.dp))
        return
    }

    val item = pkg!!

    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Package Details",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        MainInfoCard(item)


        Button(
            onClick = {
                loading = true
                scope.launch {
                    try {
                        viewModel.refreshTracking(item)
                        delay(400)
                    } finally {
                        loading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Track Package")
        }

        if (loading) Text("Tracking… Please wait.")

        Text(
            "Tracking Timeline",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        TrackingTimelineCard(pkg!!)

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
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
fun TrackingTimelineCard(pkg: PackageEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            var events by remember { mutableStateOf<List<TrackingEvent>>(emptyList()) }

            LaunchedEffect(pkg.trackingHistoryJson) {
                val parsed = try {
                    if (pkg.trackingHistoryJson.isNullOrEmpty()) emptyList()
                    else Gson().fromJson(
                        pkg.trackingHistoryJson,
                        Array<TrackingEvent>::class.java
                    ).toList()
                } catch (e: Exception) {
                    emptyList()
                }

                events = parsed
            }

            if (events.isEmpty()) {
                Text("No tracking updates yet.")
            } else {
                events.forEach { event ->
                    Text("• ${event.status} – ${event.location ?: ""}")
                }
            }
        }
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

