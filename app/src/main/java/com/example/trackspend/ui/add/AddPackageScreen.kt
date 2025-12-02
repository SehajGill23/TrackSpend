package com.example.trackspend.ui.add

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.trackspend.R
import com.example.trackspend.data.local.DatabaseModule
import com.example.trackspend.data.local.PackageEntity
import com.example.trackspend.data.remote.EmailParser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate


// ------------------------------------------------------
// SHARED COLORS (same as HomeScreen UI)
// ------------------------------------------------------
private val PurpleBrand = Color(0xFF9B6DFF)
private val DarkPurple = Color(0xFF110124)
private val LightPurple = Color(0xFFF0E6FC)
private val GlassBorderDark = Color.White.copy(alpha = 0.12f)
private val GlassBorderLight = Color.Black.copy(alpha = 0.10f)
private val ErrorRed = Color(0xFFFF3B3B)


/**
 * Reusable UI component that renders a glowing, glass-style card container.
 *
 * This is used across multiple screens to maintain visual consistency.
 * It:
 *  - applies a purple glow shadow,
 *  - adapts background and border based on light/dark theme,
 *  - wraps any provided Composable content.
 */
@Composable
fun GlowCard(
    modifier: Modifier = Modifier,
    shape: Shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val shadowColor =
        if (isDark) PurpleBrand.copy(alpha = 0.95f)
        else PurpleBrand.copy(alpha = 0.75f)

    val fillColor = if (isDark) DarkPurple else LightPurple
    val borderColor = if (isDark) GlassBorderDark else GlassBorderLight

    Column(
        modifier = modifier
            .shadow(
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
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                content()
            }
        }
    }
}


/**
 * Main screen for adding a new package manually or via Smart Email Import.
 *
 * Features:
 *  - Smart Import section that auto-extracts fields using EmailParser
 *  - Manual input fields for all package data (tracking, carrier, store, etc.)
 *  - Real-time validation for tracking number, carrier, date format
 *  - Saves package to Room database using the provided onSave callback
 *
 * @param navController Navigation controller for screen transitions
 * @param onSave Callback executed after user submits valid package data
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPackageScreen(
    navController: NavController,
    onSave: (
        trackingNumber: String,
        carrier: String,
        store: String,
        itemName: String,
        price: Double?,
        orderDate: String
    ) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // States
    var emailRaw by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var trackingNumber by remember { mutableStateOf("") }
    var carrier by remember { mutableStateOf("") }
    var customCarrier by remember { mutableStateOf("") }
    var store by remember { mutableStateOf("") }
    var itemName by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var orderDateField by remember { mutableStateOf(TextFieldValue("")) }

    var trackingError by remember { mutableStateOf(false) }
    var carrierError by remember { mutableStateOf(false) }
    var orderDateError by remember { mutableStateOf(false) }

    val carriers = listOf(
        // Major Global
        "UPS", "FedEx", "DHL", "DHL eCommerce", "USPS", "Amazon Logistics",

        // North America
        "Canada Post", "Purolator", "OnTrac", "LaserShip", "Intelcom",

        // Europe
        "Royal Mail", "Evri (Hermes)", "DPD", "GLS", "Deutsche Post",
        "La Poste", "Colissimo", "PostNL",

        // Asia / Pacific
        "China Post", "Cainiao", "Japan Post", "Yamato", "Australia Post",

        // Other International
        "Aramex", "Asendia", "Blue Dart",

        // Fallback
        "Other"
    )


    /**
     * Formats raw numeric input into YYYY-MM-DD as the user types.
     *
     * Accepts digits only and gradually inserts dashes.
     * Example: 202412 -> "2024-12"
     */
    fun formatDate(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        return when {
            digits.length <= 4 -> digits
            digits.length <= 6 -> digits.substring(0,4)+"-"+digits.substring(4)
            digits.length <= 8 -> digits.substring(0,4)+"-"+digits.substring(4,6)+"-"+digits.substring(6)
            else -> digits.substring(0,8).let { it.substring(0,4)+"-"+it.substring(4,6)+"-"+it.substring(6,8) }
        }
    }

    /**
     * Validates whether the formatted date string follows YYYY-MM-DD
     * and represents an actual valid calendar date.
     */
    fun isValidDate(f: String): Boolean =
        try { f.length == 10 && LocalDate.parse(f) != null } catch(e:Exception){ false }

    // ------------------------------------------------------
    // UI LAYOUT
    // ------------------------------------------------------
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add Package",
                        style = MaterialTheme.typography.titleLarge,
                        color = PurpleBrand
                    )
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ------------------------------------------------------
            // SMART IMPORT (Glow card)
            // ------------------------------------------------------
            GlowCard {
                Text("Smart Import", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = emailRaw,
                    shape = RoundedCornerShape(16.dp),
                    onValueChange = { emailRaw = it },
                    label = { Text("Email content") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, null, tint = PurpleBrand) },
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )

                /**
                 * Triggers Smart Email Import:
                 *  - Shows loading indicator
                 *  - Parses email text using EmailParser
                 *  - Autofills recognized fields (tracking, carrier, store, price, date)
                 */
                Button(
                    onClick = {
                        isLoading = true
                        scope.launch {
                            delay(600)
                            val parsed = EmailParser.parse(context, emailRaw)

                            parsed.trackingNumber?.let { trackingNumber = it; trackingError = false }
                            parsed.carrier?.let { carrier = it; carrierError = false }
                            parsed.store?.let { store = it }
                            parsed.itemName?.let { itemName = it }
                            parsed.price?.let { priceText = it.toString() }
                            parsed.orderDate?.let {
                                orderDateField = TextFieldValue(it, TextRange(it.length))
                            }

                            isLoading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading)
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    else Text("Extract Info")
                }
            }

            // ------------------------------------------------------
            // PACKAGE DETAILS
            // ------------------------------------------------------
            GlowCard {
                Text("Package Details", style = MaterialTheme.typography.titleMedium)

                /**
                 * Tracking number field logic:
                 *  - Allows alphanumeric only
                 *  - Max length enforced (50 chars)
                 *  - Automatically clears error if user types valid content
                 */
                OutlinedTextField(
                    value = trackingNumber,
                    shape = RoundedCornerShape(16.dp),
                    onValueChange = { newText ->
                        // 1. Filter for alphanumeric only
                        val filtered = newText.filter { it.isLetterOrDigit() }

                        // 2. Enforce max length (40 characters)
                        if (filtered.length <= 50) {
                            trackingNumber = filtered

                            // 3. Reset error immediately if user types valid input
                            if (filtered.isNotEmpty()) {
                                trackingError = false
                            }
                        }
                    },
                    label = { Text("Tracking Number") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.barcode),
                            contentDescription = null,
                            // Icon turns red if error is true
                            tint = if (trackingError) ErrorRed else PurpleBrand
                        )
                    },
                    isError = trackingError,
                    supportingText = {
                        if (trackingError) {
                            Text(
                                text = if (trackingNumber.isEmpty()) "Tracking number required" else "Max 40 characters allowed",
                                color = ErrorRed
                            )
                        }
                    },
                    // Explicitly link your custom ErrorRed to the border colors
                    colors = OutlinedTextFieldDefaults.colors(
                        errorBorderColor = ErrorRed,
                        errorLabelColor = ErrorRed,
                        errorLeadingIconColor = ErrorRed,
                        errorCursorColor = ErrorRed
                    ),
                    modifier = Modifier.fillMaxWidth()
                )


                var expanded by remember { mutableStateOf(false) }


                /**
                 * Carrier selection dropdown using Material3 ExposedDropdownMenu.
                 *
                 * Error styling is applied if user tries to submit an empty carrier.
                 * If "Other" is selected, a custom carrier field appears.
                 */
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {

                    /**
                     * Text field displayed only when user selects "Other" carrier.
                     * Allows custom carrier name input.
                     */
                    OutlinedTextField(
                        value = carrier,
                        readOnly = true,
                        shape = RoundedCornerShape(16.dp),
                        label = { Text("Carrier") },
                        // Logic to switch icon color based on error
                        leadingIcon = {
                            Icon(
                                Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = if (carrierError) ErrorRed else PurpleBrand
                            )
                        },
                        onValueChange = {},
                        isError = carrierError,
                        // Add specific color mapping for the error state
                        colors = OutlinedTextFieldDefaults.colors(
                            errorBorderColor = ErrorRed,
                            errorLabelColor = ErrorRed,
                            errorLeadingIconColor = ErrorRed,
                            errorTrailingIconColor = ErrorRed,
                            // Ensure the trailing arrow icon also turns red
                            errorCursorColor = ErrorRed
                        ),
                        // Add trailing icon for better UX (arrow)
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        supportingText = {
                            if (carrierError) {
                                Text("Carrier is required", color = ErrorRed)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        carriers.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = {
                                    carrier = c
                                    carrierError = false // Clear error on selection
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                if (carrier == "Other") {
                    OutlinedTextField(
                        value = customCarrier,
                        shape = RoundedCornerShape(16.dp),
                        onValueChange = { customCarrier = it },
                        label = { Text("Specify Carrier") },
                        leadingIcon = {
                            Icon(Icons.Default.Build, null, tint = PurpleBrand)
                        },
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
                    shape = RoundedCornerShape(16.dp),
                    onValueChange = {
                        priceText = it.replace("[^0-9.]".toRegex(), "")
                    },
                    label = { Text("Price") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null, tint = PurpleBrand) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ------------------------------------------------------
            // DATE + SAVE
            // ------------------------------------------------------
            GlowCard {

                OutlinedTextField(
                    value = orderDateField,
                    shape = RoundedCornerShape(16.dp),
                    onValueChange = { new ->

                        val formatted = formatDate(new.text.trim())

                        // LOGIC IMPROVEMENT 2: Preserve cursor position if possible, otherwise jump to end
                        orderDateField = TextFieldValue(
                            text = formatted,
                            selection = TextRange(formatted.length)
                        )

                        val today = LocalDate.now()
                        // LOGIC IMPROVEMENT 3: Move the 'try-catch' safety into isValidDate to prevent crashes
                        // during the LocalDate.parse(formatted) check
                        val parsedDate = try { LocalDate.parse(formatted) } catch(e: Exception) { null }

                        orderDateError = formatted.isNotEmpty() && (
                                !isValidDate(formatted) || (parsedDate != null && parsedDate.isAfter(today))
                                )
                    },
                    label = { Text("Order date (YYYY-MM-DD)") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null, // Accessibility fix
                            tint = if (orderDateError) ErrorRed else PurpleBrand
                        )
                    },
                    isError = orderDateError,
                    supportingText = {
                        if (orderDateError) Text("Enter a valid past date", color = ErrorRed)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),

                    // --- THE FIX FOR THE RED BORDER ---
                    colors = OutlinedTextFieldDefaults.colors(
                        errorBorderColor = ErrorRed,
                        errorLabelColor = ErrorRed,
                        errorCursorColor = ErrorRed,
                        // You can also set focused/unfocused colors here if needed
                        // focusedBorderColor = PurpleBrand,
                    )
                )

                Button(
                    onClick = {
                        trackingError = trackingNumber.isEmpty()
                        carrierError = carrier.isEmpty()

                        if (!trackingError && !carrierError) {
                            val priceDouble = priceText.toDoubleOrNull()

                            scope.launch {
                                DatabaseModule.providePackageDao().insertPackage(
                                    PackageEntity(
                                        trackingNumber = trackingNumber.trim(),
                                        carrier = if (carrier == "Other") customCarrier.trim() else carrier.trim(),
                                        store = store.trim().ifEmpty { null },
                                        itemName = itemName.trim().ifEmpty { null },
                                        price = priceDouble,
                                        orderDate = orderDateField.text.trim(),
                                        eta = null,
                                        status = "N/A",
                                        lastUpdate = System.currentTimeMillis(),
                                        trackingHistoryJson = null
                                    )
                                )
                                navController.popBackStack()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Package")
                }
            }

            Spacer(Modifier.height(60.dp))
        }
    }
}