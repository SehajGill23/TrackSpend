package com.example.trackspend.ui.details

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.trackspend.R
import com.example.trackspend.data.local.PackageEntity
import com.example.trackspend.data.model.TrackingEvent
import com.example.trackspend.ui.components.TrackingTimeline
import com.example.trackspend.viewmodel.PackageViewModel
import com.google.gson.Gson
import kotlinx.coroutines.delay
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
    val lazyListState = rememberLazyListState()
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
        state = lazyListState,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .fillMaxSize()
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
            .padding(horizontal = 18.dp )
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        // 🔥 Top Row: Edit + Delete
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = {
                    navController.navigate("edit/${item.id}")
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF9B6DFF))
                }

                IconButton(onClick = {
                    showDeleteDialog = true
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
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
                        showTimeline = false // hide timeline until loader finishes

                        scope.launch {
                            // scroll to top immediately
                            lazyListState.animateScrollToItem(6)

                            // show loader for 1 seconds
                            delay(1000)

                            // fetch new data
                            viewModel.refreshTracking(item)

                            loading = false
                            showTimeline = true
                        }
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocalShipping,
                            contentDescription = "Tracking Shipment",  // Always add accessibility descriptions
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp)) // Add space between
                        Text("Track Package")
                    }

                }

            }
        }

        // Timeline section
        // Timeline / Loader Section
        item {
            when {
                loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),   // big empty area
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(12.dp))
                            Text("Tracking…", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                showTimeline -> {
                    // 1. State to track if the delay has finished
                    var showMessage by remember { mutableStateOf(false) }

                    // 2. Start a timer when this block enters composition
                    LaunchedEffect(Unit) {
                        // Only wait if events are actually empty
                        if (events.isEmpty()) {
                            delay(2000) // Wait 2 seconds
                            showMessage = true
                        }
                    }

                    if (events.isNotEmpty()) {
                        // If we have events, show them immediately
                        TrackingTimeline(events)
                    } else if (showMessage) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No tracking found.")
                        }
                    }
                }

            }
        }

        item { Spacer(modifier = Modifier.height(120.dp)) }
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

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val purple = Color(0xFF9B6DFF)

    val bg = if (isDark) Color(0xFF140028) else Color(0xFFF3E9FF)
    val border = if (isDark) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.12f)
    val labelColor = if (isDark) Color(0xFFDBC8FF) else Color(0xFF4C2A8A)
    val textColor = if (isDark) Color.White else Color(0xFF1A1A1A)

    // Whole outer glow container
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                22.dp,
                RoundedCornerShape(20.dp),
                ambientColor = purple.copy(alpha = if (isDark) 0.9f else 0.55f),
                spotColor = purple.copy(alpha = if (isDark) 0.9f else 0.55f)
            ),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, border),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(26.dp)
        ) {

            // ---------------- PACKAGE INFO SECTION ----------------
            SectionHeader("Package Info", purple)

            InfoRow(
                R.drawable.barcode,
                "Tracking Number",
                pkg.trackingNumber,
                labelColor,
                textColor
            )
            InfoRow(Icons.Default.LocalShipping, "Carrier", pkg.carrier, labelColor, textColor)
            InfoRow(Icons.Default.Info, "Status", pkg.status, labelColor, textColor)

            // ---------------- PURCHASE INFO SECTION ----------------
            SectionHeader("Purchase Details", purple)

            InfoRow(Icons.Default.ShoppingBag, "Item", pkg.itemName ?: "N/A", labelColor, textColor)
            InfoRow(Icons.Default.Store, "Store", pkg.store ?: "N/A", labelColor, textColor)
            InfoRow(Icons.Default.AttachMoney, "Price", pkg.price?.let { "$$it" } ?: "N/A", labelColor, textColor)
            InfoRow(Icons.Default.CalendarMonth, "Order Date", pkg.orderDate ?: "N/A", labelColor, textColor)
        }
    }
}


@Composable
private fun SectionHeader(title: String, color: Color) {
    Text(
        text = title,
        color = color,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 2.dp)
    )
}



@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    labelColor: Color,
    textColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = labelColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(label, fontWeight = FontWeight.SemiBold, color = labelColor)
        }

        Text(
            value,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 30.dp)
        )
    }
}


@Composable
fun InfoRow(
    iconRes: Int,
    label: String,
    value: String,
    labelColor: Color,
    textColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = labelColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(label, fontWeight = FontWeight.SemiBold, color = labelColor)
        }

        Text(
            value,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 30.dp)
        )
    }
}

