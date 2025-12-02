package com.example.trackspend.ui.add

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
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
        "UPS","FedEx","USPS","DHL","DHL eCommerce","Canada Post",
        "Amazon Logistics","China Post","Purolator","Lasership",
        "Cainiao","OnTrac","Royal Mail","Hermes","DPD","GLS","Other"
    )

    // Helpers
    fun formatDate(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        return when {
            digits.length <= 4 -> digits
            digits.length <= 6 -> digits.substring(0,4)+"-"+digits.substring(4)
            digits.length <= 8 -> digits.substring(0,4)+"-"+digits.substring(4,6)+"-"+digits.substring(6)
            else -> digits.substring(0,8).let { it.substring(0,4)+"-"+it.substring(4,6)+"-"+it.substring(6,8) }
        }
    }

    fun isValidDate(f: String): Boolean =
        try { f.length == 10 && LocalDate.parse(f) != null } catch(e:Exception){ false }

    // ------------------------ UI --------------------------
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Package", style = MaterialTheme.typography.titleLarge) }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()   // Only here → avoids weird top padding
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            //-----------------------------------------------------
            // EMAIL PARSING CARD
            //-----------------------------------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Smart Import", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = emailRaw,
                        onValueChange = { emailRaw = it },
                        label = { Text("Email content") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
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
                                parsed.orderDate?.let {
                                    orderDateField = TextFieldValue(it, TextRange(it.length))
                                }

                                isLoading = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        if (isLoading)
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        else
                            Text("Extract Info")
                    }
                }
            }

            //-----------------------------------------------------
            // PACKAGE DETAILS CARD
            //-----------------------------------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    Text("Package Details", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = trackingNumber,
                        onValueChange = {
                            trackingNumber = it.filter { it.isLetterOrDigit() }
                            trackingError = trackingNumber.isEmpty()
                        },
                        label = { Text("Tracking Number") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.barcode),
                                contentDescription = "Barcode",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        isError = trackingError,
                        supportingText = {
                            if (trackingError) Text("Tracking number required")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )

                    //--------------------------------
                    // CARRIER DROPDOWN
                    //--------------------------------
                    var expanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = carrier,
                            readOnly = true,
                            label = { Text("Carrier") },
                            leadingIcon = { Icon(Icons.Default.LocalShipping, null) },
                            onValueChange = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            isError = carrierError,
                            shape = MaterialTheme.shapes.medium
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
                            label = { Text("Specify Carrier") },
                            leadingIcon = { Icon(Icons.Default.Build, null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        )
                    }

                    OutlinedTextField(
                        value = store,
                        onValueChange = { store = it },
                        label = { Text("Store") },
                        leadingIcon = { Icon(Icons.Default.Store, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )

                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("Item Name") },
                        leadingIcon = { Icon(Icons.Default.ShoppingBag, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )

                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it.replace("[^0-9.]".toRegex(),"") },
                        label = { Text("Price") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            //-----------------------------------------------------
            // DATE + SAVE CARD
            //-----------------------------------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
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
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                            )
                        },
                        isError = orderDateError,
                        supportingText = {
                            if (orderDateError)
                                Text("Enter a valid date")
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
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
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text("Add Package")
                    }
                }
            }

            Spacer(Modifier.height(60.dp))
        }
    }
}