@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.trackspend.ui.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.trackspend.viewmodel.PackageViewModel
import kotlinx.coroutines.launch


@Composable
fun EditPackageScreen(
    id: Int,
    viewModel: PackageViewModel,
    navController: NavController
) {
    val pkg by viewModel.packageById(id).collectAsState(null)

    if (pkg == null) {
        Text("Loading...", modifier = Modifier.padding(16.dp))
        return
    }

    val item = pkg!!
    val scope = rememberCoroutineScope()

    // editable fields
    var tracking by remember { mutableStateOf(item.trackingNumber) }
    var carrier by remember { mutableStateOf(item.carrier) }
    var itemName by remember { mutableStateOf(item.itemName ?: "") }
    var store by remember { mutableStateOf(item.store ?: "") }
    var price by remember { mutableStateOf(item.price?.toString() ?: "") }

    // detect unsaved changes
    val hasChanges =
        tracking != item.trackingNumber ||
                carrier != item.carrier ||
                itemName != (item.itemName ?: "") ||
                store != (item.store ?: "") ||
                price != (item.price?.toString() ?: "")

    var showExitDialog by remember { mutableStateOf(false) }

    // Leave screen alert
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    navController.popBackStack()
                }) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("Cancel") }
            },
            title = { Text("Discard changes?") },
            text = { Text("You have unsaved changes. Leave without saving?") }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Package") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasChanges) showExitDialog = true
                        else navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            // Tracking ID
            OutlinedTextField(
                value = tracking,
                onValueChange = { tracking = it },
                label = { Text("Tracking Number") },
                modifier = Modifier.fillMaxWidth()
            )

            // Carrier
            OutlinedTextField(
                value = carrier,
                onValueChange = { carrier = it },
                label = { Text("Carrier") },
                modifier = Modifier.fillMaxWidth()
            )

            // Item name
            OutlinedTextField(
                value = itemName,
                onValueChange = { itemName = it },
                label = { Text("Item Name") },
                modifier = Modifier.fillMaxWidth()
            )

            // Store
            OutlinedTextField(
                value = store,
                onValueChange = { store = it },
                label = { Text("Store") },
                modifier = Modifier.fillMaxWidth()
            )

            // Price – ONLY NUMBERS NOW
            OutlinedTextField(
                value = price,
                onValueChange = { input ->
                    // allow only digits and decimals
                    if (input.matches(Regex("^\\d*\\.?\\d*\$"))) {
                        price = input
                    }
                },
                label = { Text("Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            // SAVE BUTTON RIGHT UNDER LAST FIELD
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                onClick = {
                    scope.launch {
                        viewModel.updatePackage(
                            item.copy(
                                trackingNumber = tracking,
                                carrier = carrier,
                                itemName = itemName,
                                store = store,
                                price = price.toDoubleOrNull()
                            )
                        )
                        navController.popBackStack()
                    }
                }
            ) {
                Text("Save")
            }
        }
    }
}