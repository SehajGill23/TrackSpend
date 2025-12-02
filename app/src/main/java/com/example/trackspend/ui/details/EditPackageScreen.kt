@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.trackspend.ui.details

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
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

// ------------------------------------------------------
// COLORS
// ------------------------------------------------------
private val PurpleBrand = Color(0xFF9B6DFF)
private val DarkPurple = Color(0xFF110124)
private val LightPurple = Color(0xFFF0E6FC)
private val GlassBorderDark = Color.White.copy(alpha = 0.12f)
private val GlassBorderLight = Color.Black.copy(alpha = 0.10f)
private val ErrorRed = Color(0xFFFF3B3B)

// ------------------------------------------------------
// HELPER FUNCTIONS ⬇️ (THE FIX TO ALL YOUR ERRORS)
// ------------------------------------------------------

private fun formatDate(raw: String): String {
    val digits = raw.filter { it.isDigit() }
    return when {
        digits.length <= 4 -> digits
        digits.length <= 6 -> digits.substring(0, 4) + "-" + digits.substring(4)
        digits.length <= 8 -> digits.substring(0, 4) + "-" +
                digits.substring(4, 6) + "-" +
                digits.substring(6)
        else -> digits.substring(0, 8).let {
            it.substring(0, 4) + "-" + it.substring(4, 6) + "-" + it.substring(6, 8)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun isValidDate(f: String): Boolean =
    try { f.length == 10 && LocalDate.parse(f) != null }
    catch (_: Exception) { false }

// ------------------------------------------------------
// GLOW CARD
// ------------------------------------------------------
@Composable
private fun EditGlowCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val shadowColor =
        if (isDark) PurpleBrand.copy(alpha = 0.95f)
        else PurpleBrand.copy(alpha = 0.75f)

    val fillColor = if (isDark) DarkPurple else LightPurple
    val borderColor = if (isDark) GlassBorderDark else GlassBorderLight

    Column(
        modifier = modifier.shadow(
            elevation = 16.dp,
            shape = shape,
            ambientColor = shadowColor,
            spotColor = shadowColor,
            clip = false
        )
    ) {
        Card(
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = fillColor),
            border = BorderStroke(1.dp, borderColor),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                Modifier
                    .padding(18.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                content()
            }
        }
    }
}

// ------------------------------------------------------
// MAIN SCREEN
// ------------------------------------------------------
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditPackageScreen(
    id: Int,
    viewModel: PackageViewModel,
    navController: NavController
) {
    val pkg by viewModel.packageById(id).collectAsState(null)

    if (pkg == null) {
        Text("Loading…", Modifier.padding(16.dp))
        return
    }

    val item = pkg!!
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // -------------------------------------
    // State
    // -------------------------------------
    val carriers = listOf(
        "UPS", "FedEx", "DHL", "DHL eCommerce", "USPS", "Amazon Logistics",
        "Canada Post", "Purolator", "OnTrac", "LaserShip", "Intelcom",
        "Royal Mail", "Evri (Hermes)", "DPD", "GLS", "Deutsche Post",
        "La Poste", "Colissimo", "PostNL",
        "China Post", "Cainiao", "Japan Post", "Yamato", "Australia Post",
        "Aramex", "Asendia", "Blue Dart",
        "Other"
    )

    val initialCarrierIsKnown = carriers.contains(item.carrier)
    val initialCarrier = if (initialCarrierIsKnown) item.carrier else "Other"
    val initialCustomCarrier = if (initialCarrierIsKnown) "" else item.carrier

    var trackingNumber by remember { mutableStateOf(item.trackingNumber) }
    var carrier by remember { mutableStateOf(initialCarrier) }
    var customCarrier by remember { mutableStateOf(initialCustomCarrier) }
    var store by remember { mutableStateOf(item.store ?: "") }
    var itemName by remember { mutableStateOf(item.itemName ?: "") }
    var priceText by remember { mutableStateOf(item.price?.toString() ?: "") }
    var orderDateField by remember { mutableStateOf(TextFieldValue(item.orderDate ?: "")) }

    var trackingError by remember { mutableStateOf(false) }
    var carrierError by remember { mutableStateOf(false) }
    var orderDateError by remember { mutableStateOf(false) }

    val effectiveCarrier = if (carrier == "Other") customCarrier else carrier

    // -------------------------------------
    // Change protection
    // -------------------------------------
    val hasChanges =
        trackingNumber != item.trackingNumber ||
                effectiveCarrier.trim() != item.carrier ||
                itemName != (item.itemName ?: "") ||
                store != (item.store ?: "") ||
                priceText != (item.price?.toString() ?: "") ||
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
            text = { Text("You have unsaved changes.") }
        )
    }

    // -------------------------------------
    // UI
    // -------------------------------------
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Edit Package", style = MaterialTheme.typography.titleLarge, color = PurpleBrand)
                },
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
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ------------------------------------------------------
            // CARD 1 — PACKAGE DETAILS
            // ------------------------------------------------------
            EditGlowCard {

                Text("Package Details", style = MaterialTheme.typography.titleMedium)

                // TRACKING NUMBER
                OutlinedTextField(
                    value = trackingNumber,
                    onValueChange = { newText ->
                        val filtered = newText.filter { it.isLetterOrDigit() }
                        if (filtered.length <= 50) {
                            trackingNumber = filtered
                            trackingError = trackingNumber.isEmpty()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    label = { Text("Tracking Number") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.barcode),
                            contentDescription = null,
                            tint = if (trackingError) ErrorRed else PurpleBrand
                        )
                    },
                    isError = trackingError,
                    supportingText = {
                        if (trackingError) Text("Tracking number required", color = ErrorRed)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        errorBorderColor = ErrorRed,
                        errorLeadingIconColor = ErrorRed,
                        errorLabelColor = ErrorRed
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // CARRIER DROPDOWN
                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = carrier,
                        readOnly = true,
                        shape = RoundedCornerShape(16.dp),
                        label = { Text("Carrier") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = if (carrierError) ErrorRed else PurpleBrand
                            )
                        },
                        isError = carrierError,
                        supportingText = {
                            if (carrierError) Text("Carrier is required", color = ErrorRed)
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                        },
                        onValueChange = {},
                        colors = OutlinedTextFieldDefaults.colors(
                            errorBorderColor = ErrorRed,
                            errorLabelColor = ErrorRed,
                            errorLeadingIconColor = ErrorRed
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        carriers.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = {
                                    carrier = c
                                    carrierError = false
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                if (carrier == "Other") {
                    OutlinedTextField(
                        value = customCarrier,
                        onValueChange = { customCarrier = it },
                        shape = RoundedCornerShape(16.dp),
                        label = { Text("Specify Carrier") },
                        leadingIcon = { Icon(Icons.Default.Build, null, tint = PurpleBrand) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = store,
                    onValueChange = { store = it },
                    shape = RoundedCornerShape(16.dp),
                    label = { Text("Store") },
                    leadingIcon = { Icon(Icons.Default.Store, null, tint = PurpleBrand) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    shape = RoundedCornerShape(16.dp),
                    label = { Text("Item Name") },
                    leadingIcon = { Icon(Icons.Default.ShoppingBag, null, tint = PurpleBrand) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.replace("[^0-9.]".toRegex(), "") },
                    shape = RoundedCornerShape(16.dp),
                    label = { Text("Price") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null, tint = PurpleBrand) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ------------------------------------------------------
            // CARD 2 — DATE + SAVE
            // ------------------------------------------------------
            EditGlowCard {

                OutlinedTextField(
                    value = orderDateField,
                    onValueChange = { new ->
                        val formatted = formatDate(new.text)
                        orderDateField = TextFieldValue(formatted, TextRange(formatted.length))

                        val parsed = try { LocalDate.parse(formatted) } catch (_: Exception) { null }

                        orderDateError =
                            formatted.isNotEmpty() &&
                                    (!isValidDate(formatted) || (parsed != null && parsed.isAfter(LocalDate.now())))
                    },
                    shape = RoundedCornerShape(16.dp),
                    label = { Text("Order date (YYYY-MM-DD)") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.CalendarMonth,
                            null,
                            tint = if (orderDateError) ErrorRed else PurpleBrand
                        )
                    },
                    isError = orderDateError,
                    supportingText = {
                        if (orderDateError) Text("Enter a valid past date", color = ErrorRed)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        errorBorderColor = ErrorRed,
                        errorLabelColor = ErrorRed,
                        errorCursorColor = ErrorRed
                    )
                )

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    onClick = {
                        trackingError = trackingNumber.isEmpty()
                        carrierError =
                            carrier.isEmpty() || (carrier == "Other" && customCarrier.isBlank())

                        if (!trackingError && !carrierError && !orderDateError) {
                            val priceDouble = priceText.toDoubleOrNull()

                            scope.launch {
                                viewModel.updatePackage(
                                    item.copy(
                                        trackingNumber = trackingNumber.trim(),
                                        carrier = if (carrier == "Other")
                                            customCarrier.trim()
                                        else carrier.trim(),
                                        store = store.trim().ifEmpty { null },
                                        itemName = itemName.trim().ifEmpty { null },
                                        price = priceDouble,
                                        orderDate = orderDateField.text.trim().ifEmpty { null }
                                    )
                                )
                                navController.popBackStack()
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
            }

            Spacer(Modifier.height(60.dp))
        }
    }
}