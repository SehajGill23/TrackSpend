package com.example.trackspend.ui.home
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    // --- UI States ---
    var selectedPkg by remember { mutableStateOf<PackageEntity?>(null) }
    var showSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // --- FILTERED + SORTED (Pinned goes top) ---
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

    // --- BOTTOM SHEET ---
    if (showSheet && selectedPkg != null) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    showSheet = false
                }
            },
            sheetState = sheetState
        ) {
            val pkg = selectedPkg!!

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // EDIT option
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch { sheetState.hide() }
                                .invokeOnCompletion { showSheet = false }

                            navController.navigate("${Routes.DETAILS}/${pkg.id}")
                        }
                        .padding(12.dp)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text("Edit")
                }

                // DELETE option
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch { sheetState.hide() }
                                .invokeOnCompletion {
                                    showSheet = false
                                    showDeleteDialog = true
                                }
                        }
                        .padding(12.dp)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text("Delete")
                }

                // PIN / UNPIN option
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch { sheetState.hide() }
                                .invokeOnCompletion {
                                    showSheet = false
                                    viewModel.updatePinned(pkg.id, !pkg.isPinned)
                                }
                        }
                        .padding(12.dp)
                ) {
                    Icon(
                        if (pkg.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(if (pkg.isPinned) "Unpin" else "Pin")
                }
            }
        }
    }

    // --- DELETE CONFIRMATION ---
    if (showDeleteDialog && selectedPkg != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePackage(selectedPkg!!)
                    showDeleteDialog = false
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
            title = { Text("Delete Package") },
            text = { Text("Are you sure you want to delete this package?") }
        )
    }

    // --- SCREEN CONTENT ---
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        SearchBar(
            query = search,
            onQueryChange = { search = it }
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filtered) { pkg ->

                PackageCard(
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
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search packages…") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
        singleLine = true,
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        }
    )
}