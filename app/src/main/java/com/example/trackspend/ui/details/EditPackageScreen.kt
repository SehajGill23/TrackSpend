@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.trackspend.ui.details

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.trackspend.R
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

    val scope = rememberCoroutineScope()
    val item = pkg!!

    // Editable
    val scrollState = rememberScrollState()
    var tracking by remember { mutableStateOf(item.trackingNumber) }
    var carrier by remember { mutableStateOf(item.carrier) }
    var itemName by remember { mutableStateOf(item.itemName ?: "") }
    var store by remember { mutableStateOf(item.store ?: "") }
    var price by remember { mutableStateOf(item.price?.toString() ?: "") }

    // DATE with TextFieldValue (like Add screen)
    var orderDateField by remember {
        mutableStateOf(TextFieldValue(item.orderDate ?: ""))
    }

    // ---- Date formatter ----
    fun formatDate(raw: String): String {
        val d = raw.filter { it.isDigit() }
        return when {
            d.length <= 4 -> d
            d.length <= 6 -> "${d.substring(0, 4)}-${d.substring(4)}"
            d.length <= 8 -> "${d.substring(0, 4)}-${d.substring(4, 6)}-${d.substring(6)}"
            else -> "${d.substring(0, 4)}-${d.substring(4, 6)}-${d.substring(6, 8)}"
        }
    }

    fun isValidDate(s: String): Boolean =
        try { s.length == 10 && LocalDate.parse(s) != null }
        catch(_: Exception) { false }

    // ---- Change detection ----
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
                title = { Text("Edit Package", style = MaterialTheme.typography.titleLarge) },
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
                .verticalScroll(scrollState)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {

            // ---------------------------------------------------
            //   CARD 1 — Package Details (consistent with Add UI)
            // ---------------------------------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Text("Package Details", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = tracking,
                        onValueChange = { tracking = it },
                        label = { Text("Tracking Number") },
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.barcode),
                                contentDescription = null,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )

                    OutlinedTextField(
                        value = carrier,
                        onValueChange = { carrier = it },
                        label = { Text("Carrier") },
                        leadingIcon = {
                            Icon(Icons.Default.LocalShipping, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )

                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("Item Name") },
                        leadingIcon = {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )

                    OutlinedTextField(
                        value = store,
                        onValueChange = { store = it },
                        label = { Text("Store") },
                        leadingIcon = {
                            Icon(Icons.Default.Store, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )

                    OutlinedTextField(
                        value = price,
                        onValueChange = {
                            if (it.matches(Regex("^\\d*\\.?\\d*\$"))) price = it
                        },
                        label = { Text("Price") },
                        leadingIcon = {
                            Icon(Icons.Default.AttachMoney, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }

            // ---------------------------------------------------
            //   CARD 2 — DATE FIELD + SAVE BUTTON
            // ---------------------------------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {

                var orderDateError by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    OutlinedTextField(
                        value = orderDateField,
                        onValueChange = { new ->
                            val formatted = formatDate(new.text.trim())

                            orderDateField = TextFieldValue(
                                text = formatted,
                                selection = TextRange(formatted.length)
                            )

                            val today = LocalDate.now()

                            orderDateError =
                                formatted.isNotEmpty() &&
                                        (!isValidDate(formatted) ||
                                                LocalDate.parse(formatted).isAfter(today))
                        },
                        label = { Text("Order date (YYYY-MM-DD)") },
                        leadingIcon = {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null)
                        },
                        isError = orderDateError,
                        supportingText = {
                            if (orderDateError)
                                Text("Enter a valid date (cannot be in the future)")
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = MaterialTheme.shapes.large,
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
            Spacer(Modifier.height(60.dp))
        }
    }
}