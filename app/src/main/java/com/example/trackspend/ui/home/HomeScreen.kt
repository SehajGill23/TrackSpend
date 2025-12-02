@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.trackspend.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.trackspend.data.local.PackageEntity
import com.example.trackspend.navigation.Routes
import com.example.trackspend.viewmodel.PackageViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PackageViewModel,
    navController: NavController
) {
    val packages by viewModel.allPackages.collectAsState()

    var search by remember { mutableStateOf("") }
    var selectedPkg by remember { mutableStateOf<PackageEntity?>(null) }
    var showSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Filtered & sorted
    val filtered = packages
        .filter {
            val q = search.lowercase()
            it.trackingNumber.lowercase().contains(q) ||
                    it.carrier.lowercase().contains(q) ||
                    (it.itemName ?: "").lowercase().contains(q) ||
                    (it.store ?: "").lowercase().contains(q)
        }
        .sortedWith(
            compareByDescending<PackageEntity> { it.isPinned }
                .thenByDescending { it.lastUpdate }
        )

    // Bottom Sheet
    if (showSheet && selectedPkg != null) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    showSheet = false
                }
            },
            sheetState = sheetState
        ) {
            BottomSheetActions(
                pkg = selectedPkg!!,
                onEdit = {
                    navController.navigate("edit/${selectedPkg!!.id}")
                },
                onDelete = {
                    showDeleteDialog = true
                },
                onTogglePin = {
                    viewModel.updatePinned(selectedPkg!!.id, !selectedPkg!!.isPinned)
                }
            )
        }
    }

    // Delete Dialog
    if (showDeleteDialog && selectedPkg != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePackage(selectedPkg!!)
                    showDeleteDialog = false
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Delete Package") },
            text = { Text("Are you sure you want to delete this package?") }
        )
    }

    Scaffold(
        topBar = { TrackSpendTopBar() }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(12.dp)
        ) {

            SearchBar(query = search, onQueryChange = { search = it })

            Spacer(Modifier.height(12.dp))

            if (filtered.isEmpty()) {
                EmptyState()
                return@Column
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered) { pkg ->

                    ModernPackageCard(
                        pkg = pkg,
                        onClick = {
                            navController.navigate("${Routes.DETAILS}/${pkg.id}")
                        },
                        onLongPress = {
                            selectedPkg = pkg
                            showSheet = true
                            scope.launch { sheetState.show() }
                        }
                    )
                }

                item { Spacer(Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
fun TrackSpendTopBar() {
    TopAppBar(
        title = {
            Text(
                "TrackSpend",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    )
}

/* ---------------------------------------------------------
   MODERN PACKAGE CARD — CLEAN, PROFESSIONAL, AESTHETIC
 --------------------------------------------------------- */
@Composable
fun ModernPackageCard(
    pkg: PackageEntity,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    Card(
        Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocalShipping,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        pkg.carrier,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (pkg.isPinned) {
                    Icon(
                        Icons.Filled.PushPin,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                pkg.itemName ?: "Unnamed item",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                pkg.trackingNumber,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )

                Text(
                    pkg.orderDate ?: "No date",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.weight(1f))

                Icon(
                    Icons.Outlined.AttachMoney,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )

                Text(
                    pkg.price?.let { "$${"%.2f".format(it)}" } ?: "--",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/* ---------------------------------------------------------
   BOTTOM SHEET ACTIONS — CLEANER & MODERN
 --------------------------------------------------------- */
@Composable
fun BottomSheetActions(
    pkg: PackageEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit
) {
    Column(
        Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        SheetRow(Icons.Default.Edit, "Edit", onEdit)
        SheetRow(Icons.Default.Delete, "Delete", onDelete)

        SheetRow(
            if (pkg.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
            if (pkg.isPinned) "Unpin" else "Pin",
            onTogglePin
        )
    }
}

@Composable
fun SheetRow(icon: ImageVector, text: String, action: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { action() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

/* ---------------------------------------------------------
   SEARCH BAR — Cleaner & more modern
 --------------------------------------------------------- */
@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search packages…") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(18.dp),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "No packages added yet.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}