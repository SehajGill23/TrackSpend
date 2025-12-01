package com.example.trackspend.ui.add

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.text.KeyboardOptions
import com.example.trackspend.data.remote.EmailParser
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.example.trackspend.data.local.DatabaseModule
import com.example.trackspend.data.local.PackageEntity
import java.time.LocalDate


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
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    // Scroll + coroutine helpers
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Raw email + extraction loader
    var emailRaw by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Form fields
    var trackingNumber by remember { mutableStateOf("") }
    var carrier by remember { mutableStateOf("") }
    var customCarrier by remember { mutableStateOf("") }
    var store by remember { mutableStateOf("") }
    var itemName by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }

    // Date field using TextFieldValue (lets us control cursor position)
    var orderDateField by remember { mutableStateOf(TextFieldValue("")) }

    // Error states
    var trackingError by remember { mutableStateOf(false) }
    var carrierError by remember { mutableStateOf(false) }

    // Supported carriers
    val carriers = listOf(
        "UPS", "FedEx", "USPS", "DHL", "DHL eCommerce", "Canada Post",
        "Amazon Logistics", "China Post", "Purolator", "Lasership",
        "Cainiao", "OnTrac", "Royal Mail", "Hermes", "DPD", "GLS", "Other"
    )

    // --- DATE FORMATTING HELPERS ---


    //     Auto-format to YYYY-MM-DD
    fun formatDate(raw: String): String {
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

    // Validates final formatted YYYY-MM-DD
    fun isValidDate(formatted: String): Boolean {
        return try {
            if (formatted.length != 10) return false
            LocalDate.parse(formatted)
            true
        } catch (e: Exception) {
            false
        }
    }

    // ---------------- UI -----------------

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    )


    // ----- EMAIL INPUT + EXTRACT BUTTON -----

    {

        Text("Paste Email (Optional)", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = emailRaw,
            onValueChange = { emailRaw = it },
            label = { Text("Email content") },
            placeholder = { Text("Paste confirmation email here...") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 6
        )

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
                    parsed.orderDate?.let { date ->
                        orderDateField = TextFieldValue(
                            text = date.trim(),
                            selection = TextRange(date.length)
                        )
                    }

                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else Text("Extract Info")
        }

        Divider()

        OutlinedTextField(
            value = trackingNumber,
            onValueChange = {
                trackingNumber = it.filter { c -> c.isLetterOrDigit() }
                trackingError = trackingNumber.isEmpty()
            },
            label = { Text("Tracking Number") },
            isError = trackingError,
            supportingText = {
                if (trackingError) Text("Tracking number required")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Carrier", style = MaterialTheme.typography.titleMedium)

        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = carrier,
                readOnly = true,
                onValueChange = {},
                label = { Text("Select Carrier") },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                isError = carrierError
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
                            expanded = false
                            carrierError = false
                        }
                    )
                }
            }
        }

        if (carrier == "Other") {
            OutlinedTextField(
                value = customCarrier,
                onValueChange = { customCarrier = it },
                label = { Text("Specify Carrier") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        OutlinedTextField(
            value = store,
            onValueChange = { store = it },
            label = { Text("Store (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = itemName,
            onValueChange = { itemName = it },
            label = { Text("Item Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = priceText,
            onValueChange = { new ->
                priceText = new.replace("[^0-9.]".toRegex(), "")
            },
            label = { Text("Price (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )


        var orderDateError by remember { mutableStateOf(false) }


        OutlinedTextField(
            value = orderDateField,
            onValueChange = { new ->
                val formatted = formatDate(new.text.trim())

                // Always push cursor to the end
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
                if (orderDateError)
                    Text("Enter a valid date like 2025-02-11")
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                trackingError = trackingNumber.isEmpty()
                carrierError = carrier.isEmpty()

                if (!trackingError && !carrierError) {
                    val priceDouble = priceText.toDoubleOrNull()

                    scope.launch {
                        val entity = PackageEntity(
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

                        DatabaseModule.providePackageDao().insertPackage(entity)
                        navController.popBackStack()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Package")
        }
    }
}