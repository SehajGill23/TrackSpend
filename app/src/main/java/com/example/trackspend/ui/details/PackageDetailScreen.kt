//package com.example.trackspend.ui.details
//
//import android.os.Build
//import androidx.annotation.RequiresApi
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Check
//import androidx.compose.material.icons.filled.Edit
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
//import com.example.trackspend.viewmodel.PackageViewModel
//import com.example.trackspend.data.local.PackageEntity
//import com.google.gson.Gson
//import com.example.trackspend.data.model.TrackingEvent
//import com.example.trackspend.ui.components.TrackingTimeline
//import kotlinx.coroutines.launch
//import androidx.compose.material.icons.filled.Delete
//
//@RequiresApi(Build.VERSION_CODES.O)
//@Composable
//fun PackageDetailScreen(
//    id: Int,
//    viewModel: PackageViewModel,
//    navController: NavController
//) {
//    val pkg by viewModel.packageById(id).collectAsState(initial = null)
//
//    if (pkg == null) {
//        Text("Loading...", Modifier.padding(16.dp))
//        return
//    }
//
//    val item = pkg!!
//    var showTimeline by remember { mutableStateOf(false) }
//    var loading by remember { mutableStateOf(false) }
//    var events by remember { mutableStateOf<List<TrackingEvent>>(emptyList()) }
//    var showDeleteDialog by remember { mutableStateOf(false) }
//
//    val scope = rememberCoroutineScope()
//
//    // Timeline decode
//    LaunchedEffect(item.trackingHistoryJson) {
//        events = if (!item.trackingHistoryJson.isNullOrEmpty()) {
//            Gson().fromJson(item.trackingHistoryJson, Array<TrackingEvent>::class.java).toList()
//        } else emptyList()
//    }
//
//    // Layout
//    LazyColumn(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(18.dp),
//        verticalArrangement = Arrangement.spacedBy(20.dp)
//    ) {
//
//        // Delete button
//        item {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.End
//            ) {
//                IconButton(onClick = { showDeleteDialog = true }) {
//                    Icon(Icons.Default.Delete, contentDescription = "Delete")
//                }
//            }
//        }
//
//        item {
//            Text("Package Details", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
//        }
//
//        // Editable fields
//        item {
//            EditableDetailRow("Tracking Number", item.trackingNumber) {
//                viewModel.updateTrackingNumber(item.id, it)
//            }
//            EditableDetailRow("Carrier", item.carrier) {
//                viewModel.updateCarrier(item.id, it)
//            }
//            EditableDetailRow("Item", item.itemName ?: "") {
//                viewModel.updateItemName(item.id, it)
//            }
//            EditableDetailRow("Store", item.store ?: "") {
//                viewModel.updateStore(item.id, it)
//            }
//            EditableDetailRow("Price", item.price?.toString() ?: "") {
//                viewModel.updatePrice(item.id, it.toDoubleOrNull() ?: 0.0)
//            }
//        }
//
//        // Track button
//        item {
//            Button(
//                modifier = Modifier.fillMaxWidth(),
//                onClick = {
//                    loading = true
//                    showTimeline = true
//                    scope.launch {
//                        try {
//                            viewModel.refreshTracking(item)
//                        } finally {
//                            loading = false
//                        }
//                    }
//                }
//            ) {
//                Text("Track Package")
//            }
//
//            if (loading) Text("Tracking…")
//        }
//
//        // Timeline
//        item {
//            if (showTimeline) {
//                if (events.isEmpty()) {
//                    Text("No tracking found.")
//                } else {
//                    TrackingTimeline(events)
//                }
//            }
//        }
//    }
//
//    // Delete dialog
//    if (showDeleteDialog) {
//        AlertDialog(
//            onDismissRequest = { showDeleteDialog = false },
//            confirmButton = {
//                TextButton(onClick = {
//                    scope.launch {
//                        viewModel.deletePackage(item)
//                        navController.popBackStack()
//                    }
//                }) { Text("Delete") }
//            },
//            dismissButton = {
//                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
//            },
//            text = { Text("Are you sure you want to delete this package?") }
//        )
//    }
//}
//
//
//
//@Composable
//fun EditableDetailRow(
//    label: String,
//    value: String,
//    onEdit: (String) -> Unit
//) {
//    var editing by remember { mutableStateOf(false) }
//    var text by remember { mutableStateOf(value) }
//
//    Row(modifier = Modifier.fillMaxWidth()) {
//
//        Column(modifier = Modifier.weight(1f)) {
//            Text(label, fontWeight = FontWeight.SemiBold)
//
//            if (editing) {
//                TextField(
//                    value = text,
//                    onValueChange = { text = it },
//                    modifier = Modifier.fillMaxWidth()
//                )
//            } else {
//                Text(text)
//            }
//        }
//
//        IconButton(onClick = {
//            if (editing) onEdit(text)
//            editing = !editing
//        }) {
//            Icon(
//                imageVector = if (editing) Icons.Default.Check else Icons.Default.Edit,
//                contentDescription = null
//            )
//        }
//    }
//
//    Spacer(Modifier.height(8.dp))
//}
//
//
//@Composable
//fun MainInfoCard(pkg: PackageEntity) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(16.dp),
//        elevation = CardDefaults.cardElevation(4.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(18.dp),
//            verticalArrangement = Arrangement.spacedBy(10.dp)
//        ) {
//            DetailRow("Tracking Number", pkg.trackingNumber)
//            DetailRow("Carrier", pkg.carrier)
//            DetailRow("Store", pkg.store ?: "N/A")
//            DetailRow("Item", pkg.itemName ?: "N/A")
//            DetailRow("Price", pkg.price?.let { "$$it" } ?: "N/A")
//            DetailRow("Order Date", pkg.orderDate?.toString() ?: "N/A")
//            DetailRow("Status", pkg.status)
//        }
//    }
//}
//
//@Composable
//fun TrackingTimelineSection(pkg: PackageEntity, show: Boolean) {
//    if (!show) return
//
//    var events by remember { mutableStateOf<List<TrackingEvent>>(emptyList()) }
//
//    LaunchedEffect(pkg.trackingHistoryJson) {
//        events = try {
//            if (pkg.trackingHistoryJson.isNullOrEmpty()) {
//                emptyList()
//            } else {
//                Gson().fromJson(
//                    pkg.trackingHistoryJson,
//                    Array<TrackingEvent>::class.java
//                ).toList()
//            }
//        } catch (e: Exception) {
//            emptyList()
//        }
//    }
//
//    if (events.isEmpty()) {
//        Text(
//            text = "No tracking found.",
//            style = MaterialTheme.typography.bodyMedium
//        )
//    } else {
//        TrackingTimeline(events = events)
//    }
//}
//
//@Composable
//fun DetailRow(label: String, value: String) {
//    Column {
//        Text(label, fontWeight = FontWeight.SemiBold)
//        Text(value)
//        Spacer(modifier = Modifier.height(8.dp))
//    }
//}


package com.example.trackspend.ui.details

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.trackspend.viewmodel.PackageViewModel
import com.example.trackspend.data.local.PackageEntity
import com.example.trackspend.data.model.TrackingEvent
import com.example.trackspend.ui.components.TrackingTimeline
import com.google.gson.Gson
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PackageDetailScreen(
    id: Int,
    viewModel: PackageViewModel,
    navController: NavController
) {
    val pkg by viewModel.packageById(id).collectAsState(initial = null)

    if (pkg == null) {
        Text("Loading...", Modifier.padding(16.dp))
        return
    }

    val item = pkg!!
    var showTimeline by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var events by remember { mutableStateOf<List<TrackingEvent>>(emptyList()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Decode timeline JSON
    LaunchedEffect(item.trackingHistoryJson) {
        events = if (!item.trackingHistoryJson.isNullOrEmpty()) {
            Gson()
                .fromJson(item.trackingHistoryJson, Array<TrackingEvent>::class.java)
                .toList()
        } else emptyList()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        // 🔥 Top Row: Edit + Delete
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = {
                    // go to edit screen
                    navController.navigate("edit/${item.id}")
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }

                IconButton(onClick = {
                    showDeleteDialog = true
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }

        // Title
        item {
            Text(
                "Package Details",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Main info card
        item {
            MainInfoCard(item)
        }

        // Track button
        item {
            Column {
                Button(
                    modifier = Modifier.fillMaxWidth(),
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
                    }
                ) {
                    Text("Track Package")
                }

                if (loading) {
                    Text("Tracking… Please wait.")
                }
            }
        }

        // Timeline section
        item {
            if (showTimeline) {
                if (events.isEmpty()) {
                    Text("No tracking found.")
                } else {
                    TrackingTimeline(events)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }

    // Delete confirm dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        viewModel.deletePackage(item)
                        navController.popBackStack()
                    }
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Delete Package?") },
            text = { Text("Are you sure you want to delete this package?") }
        )
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
            DetailRow("Item", pkg.itemName ?: "N/A")
            DetailRow("Store", pkg.store ?: "N/A")
            DetailRow("Price", pkg.price?.let { "$$it" } ?: "N/A")
            DetailRow("Order Date", pkg.orderDate?.toString() ?: "N/A")
            DetailRow("Status", pkg.status)
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