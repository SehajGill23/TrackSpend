@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.trackspend.ui.details

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.trackspend.viewmodel.PackageViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
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

    // Editable fields
    var tracking by remember { mutableStateOf(item.trackingNumber) }
    var carrier by remember { mutableStateOf(item.carrier) }
    var itemName by remember { mutableStateOf(item.itemName ?: "") }
    var store by remember { mutableStateOf(item.store ?: "") }
    var price by remember { mutableStateOf(item.price?.toString() ?: "") }

    // DATE field (TextFieldValue like Add screen)
    var orderDateField by remember {
        mutableStateOf(TextFieldValue(item.orderDate ?: ""))
    }

    // ---- Formatting helpers ----
    fun formatDate(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        return when {
            digits.length <= 4 -> digits
            digits.length <= 6 ->
                digits.substring(0, 4) + "-" + digits.substring(4)
            digits.length <= 8 ->
                digits.substring(0, 4) + "-" +
                        digits.substring(4, 6) + "-" +
                        digits.substring(6)
            else ->
                digits.substring(0, 8).let {
                    it.substring(0, 4) + "-" + it.substring(4, 6) + "-" + it.substring(6, 8)
                }
        }
    }

    fun isValidDate(formatted: String): Boolean {
        return try {
            if (formatted.length != 10) return false
            LocalDate.parse(formatted)
            true
        } catch (_: Exception) {
            false
        }
    }

    // ❗ detect unsaved changes
    val hasChanges =
        tracking != item.trackingNumber ||
                carrier != item.carrier ||
                itemName != (item.itemName ?: "") ||
                store != (item.store ?: "") ||
                price != (item.price?.toString() ?: "") ||
                orderDateField.text != (item.orderDate ?: "")

    var showExitDialog by remember { mutableStateOf(false) }

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

            OutlinedTextField(
                value = tracking,
                onValueChange = { tracking = it },
                label = { Text("Tracking Number") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = carrier,
                onValueChange = { carrier = it },
                label = { Text("Carrier") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = itemName,
                onValueChange = { itemName = it },
                label = { Text("Item Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = store,
                onValueChange = { store = it },
                label = { Text("Store") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = price,
                onValueChange = { input ->
                    if (input.matches(Regex("^\\d*\\.?\\d*\$"))) {
                        price = input
                    }
                },
                label = { Text("Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            // ---- DATE FIELD (same as Add screen) ----
            var orderDateError by remember { mutableStateOf(false) }

            OutlinedTextField(
                value = orderDateField,
                onValueChange = { new ->
                    val formatted = formatDate(new.text.trim())

                    orderDateField = TextFieldValue(
                        text = formatted,
                        selection = TextRange(formatted.length)
                    )

                    orderDateError =
                        formatted.isNotEmpty() && !isValidDate(formatted)
                },
                label = { Text("Order Date (YYYY-MM-DD)") },
                isError = orderDateError,
                supportingText = {
                    if (orderDateError) Text("Enter a valid date like 2025-02-11")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // ---- SAVE ----
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
                                price = price.toDoubleOrNull(),
                                orderDate = orderDateField.text.ifEmpty { null }
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