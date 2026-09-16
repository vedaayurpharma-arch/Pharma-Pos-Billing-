package com.example.ui.fieldsales

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.ui.FieldSalesViewModel
import com.example.ui.SelectedOrderItem
import com.example.ui.theme.FieldSalesColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCustomerEntryScreen(
    viewModel: FieldSalesViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val erpProducts by viewModel.erpProducts.collectAsState()
    val productAllocations by viewModel.productAllocations.collectAsState()
    val currentGps by viewModel.currentLocation.collectAsState()

    val todayDate = remember {
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
    }

    // Form States
    var clinicOrPharmacyName by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("Karnataka") }
    var city by remember { mutableStateOf("Bangalore") }
    var pinCode by remember { mutableStateOf("") }
    var dateOfPurchase by remember { mutableStateOf(todayDate) }

    // GPS States
    var latitude by remember { mutableDoubleStateOf(currentGps.first) }
    var longitude by remember { mutableDoubleStateOf(currentGps.second) }
    var latText by remember { mutableStateOf(latitude.toString()) }
    var lngText by remember { mutableStateOf(longitude.toString()) }

    // Selected Products for Purchase
    val selectedItems = remember { mutableStateListOf<SelectedOrderItem>() }

    // Financial calculations
    var extraDiscountPercent by remember { mutableDoubleStateOf(5.0) }
    var gstPercent by remember { mutableDoubleStateOf(5.0) }

    // Digital Invoice & Physical Receipt
    var digitalInvoiceUrl by remember { mutableStateOf("https://vedaayurpharma.com/inv/FLD-" + (System.currentTimeMillis() % 10000)) }
    var physicalReceiptUri by remember { mutableStateOf("") }
    var isReceiptUploaded by remember { mutableStateOf(false) }

    // Order Notes
    var orderNotes by remember { mutableStateOf("") }

    // Validation error state
    var validationError by remember { mutableStateOf<String?>(null) }

    // Permission launcher for GPS
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.fetchLiveGps()
            latitude = viewModel.currentLocation.value.first
            longitude = viewModel.currentLocation.value.second
            latText = latitude.toString()
            lngText = longitude.toString()
            Toast.makeText(context, "GPS Location Updated!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Location permission denied. You may enter coordinates manually.", Toast.LENGTH_LONG).show()
        }
    }

    // Available products filtered for selected State
    val stateAllocations = productAllocations.filter { it.state.equals(state, ignoreCase = true) && it.isAvailable }
    val allocatedProductIds = stateAllocations.map { it.productId }.toSet()
    val availableProductsForState = erpProducts.filter {
        allocatedProductIds.isEmpty() || allocatedProductIds.contains(it.id)
    }

    // Calculate live financial summary
    val totalMrp = selectedItems.sumOf { it.totalMrp }
    val netWholesaleRate = selectedItems.sumOf { it.discountedSubtotal }
    val discountAmount = netWholesaleRate * (extraDiscountPercent / 100.0)
    val taxableValue = netWholesaleRate - discountAmount
    val gstAmount = taxableValue * (gstPercent / 100.0)
    val finalPurchaseValue = taxableValue + gstAmount

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "New Customer Entry",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Ayurvedic Field Sales & Purchase Allocation",
                            fontSize = 11.sp,
                            color = FieldSalesColors.MintSoft
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_new_customer")) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FieldSalesColors.DarkGreen
                )
            )
        },
        containerColor = FieldSalesColors.Background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error banner if any
            if (validationError != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = FieldSalesColors.RedAccentSoft),
                        border = BorderStroke(1.dp, FieldSalesColors.RedAccent)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = FieldSalesColors.RedAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = validationError ?: "",
                                color = FieldSalesColors.RedAccent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 1. Customer & Location Details
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FieldSalesColors.CardBackground),
                    border = BorderStroke(1.dp, FieldSalesColors.BorderLight)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(FieldSalesColors.LightMintGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Business,
                                    contentDescription = null,
                                    tint = FieldSalesColors.DarkGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Customer & Clinic Details",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = FieldSalesColors.TextPrimary
                            )
                        }

                        OutlinedTextField(
                            value = clinicOrPharmacyName,
                            onValueChange = { clinicOrPharmacyName = it; validationError = null },
                            label = { Text("Customer / Clinic / Pharmacy Name *") },
                            placeholder = { Text("e.g. Dhanvantari Ayurvedic Nilayam") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_clinic_name"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FieldSalesColors.DarkGreen,
                                focusedLabelColor = FieldSalesColors.DarkGreen
                            ),
                            singleLine = true
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = customerName,
                                onValueChange = { customerName = it },
                                label = { Text("Doctor / Contact Person") },
                                placeholder = { Text("e.g. Dr. S. K. Rao") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_customer_name"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FieldSalesColors.DarkGreen,
                                    focusedLabelColor = FieldSalesColors.DarkGreen
                                ),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Phone / Mobile") },
                                placeholder = { Text("9845112233") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_phone"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FieldSalesColors.DarkGreen,
                                    focusedLabelColor = FieldSalesColors.DarkGreen
                                ),
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = FieldSalesColors.TextSecondary, modifier = Modifier.size(16.dp))
                                },
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it; validationError = null },
                            label = { Text("Address / Street / Landmark *") },
                            placeholder = { Text("e.g. #44, Temple Road, Malleshwaram 8th Cross") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_address"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FieldSalesColors.DarkGreen,
                                focusedLabelColor = FieldSalesColors.DarkGreen
                            )
                        )

                        // State Selection Chips
                        Text(
                            text = "State * (Controls Product Allocation)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FieldSalesColors.TextSecondary
                        )

                        val states = listOf("Karnataka", "Andhra Pradesh", "Telangana", "Maharashtra", "Tamil Nadu", "Kerala")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            states.take(3).forEach { st ->
                                FilterChip(
                                    selected = state == st,
                                    onClick = { state = st },
                                    label = { Text(st, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = FieldSalesColors.DarkGreen,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            states.drop(3).forEach { st ->
                                FilterChip(
                                    selected = state == st,
                                    onClick = { state = st },
                                    label = { Text(st, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = FieldSalesColors.DarkGreen,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it; validationError = null },
                                label = { Text("City *") },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("input_city"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FieldSalesColors.DarkGreen,
                                    focusedLabelColor = FieldSalesColors.DarkGreen
                                ),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = pinCode,
                                onValueChange = { pinCode = it },
                                label = { Text("PIN Code") },
                                modifier = Modifier
                                    .weight(0.8f)
                                    .testTag("input_pincode"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FieldSalesColors.DarkGreen,
                                    focusedLabelColor = FieldSalesColors.DarkGreen
                                ),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = dateOfPurchase,
                            onValueChange = { dateOfPurchase = it },
                            label = { Text("Date of Purchase *") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = FieldSalesColors.DarkGreen, modifier = Modifier.size(16.dp))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FieldSalesColors.DarkGreen,
                                focusedLabelColor = FieldSalesColors.DarkGreen
                            ),
                            singleLine = true
                        )
                    }
                }
            }

            // 2. Map Live Location & GPS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FieldSalesColors.CardBackground),
                    border = BorderStroke(1.dp, FieldSalesColors.BorderLight)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                    .background(Color(0xFFE0F2FE)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = Color(0xFF0369A1),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Map Live Location & GPS",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FieldSalesColors.TextPrimary
                                )
                            }

                            // Current GPS status pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(FieldSalesColors.LightMintGreen)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "GPS READY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FieldSalesColors.DarkGreen
                                )
                            }
                        }

                        // Fetch Live Location Button
                        Button(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FieldSalesColors.DarkGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_fetch_gps")
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Fetch Current GPS Live Location", fontWeight = FontWeight.SemiBold)
                        }

                        // Manual Editable Coordinates
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = latText,
                                onValueChange = {
                                    latText = it
                                    latitude = it.toDoubleOrNull() ?: latitude
                                },
                                label = { Text("Latitude") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_latitude"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FieldSalesColors.DarkGreen,
                                    focusedLabelColor = FieldSalesColors.DarkGreen
                                ),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = lngText,
                                onValueChange = {
                                    lngText = it
                                    longitude = it.toDoubleOrNull() ?: longitude
                                },
                                label = { Text("Longitude") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_longitude"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FieldSalesColors.DarkGreen,
                                    focusedLabelColor = FieldSalesColors.DarkGreen
                                ),
                                singleLine = true
                            )
                        }

                        // Open in Navigation Button
                        OutlinedButton(
                            onClick = {
                                try {
                                    val geoUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(clinicOrPharmacyName.ifBlank { "Customer Location" })})")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
                                    context.startActivity(mapIntent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Opening Map coordinates: $latitude, $longitude", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_open_maps"),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, FieldSalesColors.EarthyBrown)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = FieldSalesColors.EarthyBrown, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Customer Location in Maps / Navigation", color = FieldSalesColors.EarthyBrown, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // 3. Ayurvedic Product Allocation based on State
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FieldSalesColors.CardBackground),
                    border = BorderStroke(1.dp, FieldSalesColors.BorderLight)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(FieldSalesColors.LimeGreenSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Medication,
                                        contentDescription = null,
                                        tint = FieldSalesColors.LimeGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Ayurvedic Product Allocation",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FieldSalesColors.TextPrimary
                                    )
                                    Text(
                                        text = "Allocated for: $state",
                                        fontSize = 11.sp,
                                        color = FieldSalesColors.LimeGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Tap to add Ayurvedic remedies to this purchase. MRP & Wholesale rates are calculated automatically.",
                            fontSize = 12.sp,
                            color = FieldSalesColors.TextSecondary
                        )

                        // Available Products List
                        availableProductsForState.forEach { product ->
                            val alreadySelected = selectedItems.find { it.product.id == product.id }
                            val stateAlloc = stateAllocations.find { it.productId == product.id }
                            val discountPct = stateAlloc?.specialStateDiscount ?: 5.0
                            val wholesale = if (product.wholesaleRate > 0) product.wholesaleRate else product.saleRate * 0.85

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (alreadySelected != null) FieldSalesColors.LightMintGreen else Color(0xFFF8FAFC))
                                    .border(
                                        1.dp,
                                        if (alreadySelected != null) FieldSalesColors.DarkGreen else Color(0xFFE2E8F0),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FieldSalesColors.TextPrimary
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "MRP: ₹${product.mrp}",
                                            fontSize = 11.sp,
                                            color = FieldSalesColors.TextSecondary
                                        )
                                        Text(
                                            text = "Wholesale: ₹$wholesale",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = FieldSalesColors.DarkGreen
                                        )
                                        Text(
                                            text = "Batch: ${product.batchNumber}",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                if (alreadySelected != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                val idx = selectedItems.indexOfFirst { it.product.id == product.id }
                                                if (idx >= 0) {
                                                    val current = selectedItems[idx]
                                                    if (current.quantity > 1) {
                                                        selectedItems[idx] = current.copy(quantity = current.quantity - 1)
                                                    } else {
                                                        selectedItems.removeAt(idx)
                                                    }
                                                }
                                            },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = FieldSalesColors.RedAccent)
                                        }

                                        Text(
                                            text = "${alreadySelected.quantity}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp)
                                        )

                                        IconButton(
                                            onClick = {
                                                val idx = selectedItems.indexOfFirst { it.product.id == product.id }
                                                if (idx >= 0) {
                                                    val current = selectedItems[idx]
                                                    selectedItems[idx] = current.copy(quantity = current.quantity + 1)
                                                }
                                            },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = FieldSalesColors.DarkGreen)
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            selectedItems.add(
                                                SelectedOrderItem(
                                                    product = product,
                                                    quantity = 1,
                                                    stateDiscountPercent = discountPct
                                                )
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = FieldSalesColors.DarkGreen),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Add", fontSize = 11.sp)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }

            // 4. Financial Purchase Values
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FieldSalesColors.DarkGreenSurface),
                    border = BorderStroke(1.dp, FieldSalesColors.DarkGreen)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = FieldSalesColors.LimeGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Financial Purchase Values",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total MRP Value:", color = Color.LightGray, fontSize = 13.sp)
                            Text("₹${String.format(Locale.US, "%,.2f", totalMrp)}", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Net Wholesale Base Rate:", color = Color.LightGray, fontSize = 13.sp)
                            Text("₹${String.format(Locale.US, "%,.2f", netWholesaleRate)}", color = FieldSalesColors.MintSoft, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Additional Field Discount (%):", color = Color.LightGray, fontSize = 13.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = extraDiscountPercent.toString(),
                                    onValueChange = { extraDiscountPercent = it.toDoubleOrNull() ?: 0.0 },
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(48.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = FieldSalesColors.LimeGreen,
                                        unfocusedBorderColor = Color.Gray
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("- ₹${String.format(Locale.US, "%.2f", discountAmount)}", color = FieldSalesColors.LimeGreen, fontSize = 12.sp)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Ayurvedic GST Rate (%):", color = Color.LightGray, fontSize = 13.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                listOf(5.0, 12.0).forEach { gst ->
                                    FilterChip(
                                        selected = gstPercent == gst,
                                        onClick = { gstPercent = gst },
                                        label = { Text("${gst.toInt()}%", fontSize = 11.sp) },
                                        modifier = Modifier.padding(horizontal = 2.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = FieldSalesColors.LimeGreen,
                                            selectedLabelColor = Color.Black
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("+ ₹${String.format(Locale.US, "%.2f", gstAmount)}", color = Color.LightGray, fontSize = 12.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color.DarkGray)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Final Purchase Value:",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "₹${String.format(Locale.US, "%,.2f", finalPurchaseValue)}",
                                color = FieldSalesColors.LimeGreen,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // 5. Digital Invoice & Physical Receipt
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FieldSalesColors.CardBackground),
                    border = BorderStroke(1.dp, FieldSalesColors.BorderLight)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(FieldSalesColors.EarthyBrownLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = FieldSalesColors.EarthyBrown,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Digital Invoice / Physical Receipt",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = FieldSalesColors.TextPrimary
                            )
                        }

                        OutlinedTextField(
                            value = digitalInvoiceUrl,
                            onValueChange = { digitalInvoiceUrl = it },
                            label = { Text("Digital Invoice URL / Cloud Link") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FieldSalesColors.DarkGreen,
                                focusedLabelColor = FieldSalesColors.DarkGreen
                            ),
                            singleLine = true
                        )

                        // Upload / Scan physical bill
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    isReceiptUploaded = true
                                    physicalReceiptUri = "receipt_phys_${System.currentTimeMillis()}.jpg"
                                    Toast.makeText(context, "Physical bill scanned & attached successfully!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FieldSalesColors.EarthyBrown),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_scan_bill")
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Scan Physical Bill", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    isReceiptUploaded = true
                                    physicalReceiptUri = "doc_attach_${System.currentTimeMillis()}.pdf"
                                    Toast.makeText(context, "Document attached from device storage!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_attach_receipt")
                            ) {
                                Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Attach Receipt", fontSize = 12.sp)
                            }
                        }

                        if (isReceiptUploaded) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = FieldSalesColors.LightMintGreen),
                                border = BorderStroke(1.dp, FieldSalesColors.DarkGreen)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = FieldSalesColors.DarkGreen)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Physical Receipt Stored",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = FieldSalesColors.DarkGreen
                                            )
                                            Text(text = physicalReceiptUri, fontSize = 10.sp, color = FieldSalesColors.TextSecondary)
                                        }
                                    }
                                    IconButton(onClick = { isReceiptUploaded = false; physicalReceiptUri = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = FieldSalesColors.RedAccent)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. Order Notes
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FieldSalesColors.CardBackground),
                    border = BorderStroke(1.dp, FieldSalesColors.BorderLight)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Note, contentDescription = null, tint = FieldSalesColors.EarthyBrown)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Special Medical Instructions & Order Notes",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = FieldSalesColors.TextPrimary
                            )
                        }

                        OutlinedTextField(
                            value = orderNotes,
                            onValueChange = { orderNotes = it },
                            placeholder = { Text("e.g. Doctor requires dispatch before Monday for arthritis camp. Prefer freshest mfg batch.") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .testTag("input_order_notes"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FieldSalesColors.DarkGreen,
                                focusedLabelColor = FieldSalesColors.DarkGreen
                            )
                        )
                    }
                }
            }

            // 7. Save Customer & Record Purchase Button
            item {
                Button(
                    onClick = {
                        if (clinicOrPharmacyName.isBlank()) {
                            validationError = "Please enter Customer / Clinic / Pharmacy Name."
                            return@Button
                        }
                        if (address.isBlank()) {
                            validationError = "Please enter Address / Street / Landmark."
                            return@Button
                        }
                        if (city.isBlank()) {
                            validationError = "Please enter City."
                            return@Button
                        }

                        viewModel.saveCustomerAndRecordPurchase(
                            customerName = customerName.ifBlank { clinicOrPharmacyName },
                            clinicOrPharmacyName = clinicOrPharmacyName,
                            phone = phone,
                            address = address,
                            state = state,
                            city = city,
                            pinCode = pinCode,
                            orderDate = dateOfPurchase,
                            latitude = latitude,
                            longitude = longitude,
                            selectedItems = selectedItems.toList(),
                            extraDiscountPercent = extraDiscountPercent,
                            gstPercent = gstPercent,
                            digitalInvoiceUrl = digitalInvoiceUrl,
                            physicalReceiptUri = physicalReceiptUri,
                            orderNotes = orderNotes,
                            onSuccess = {
                                Toast.makeText(context, "Saved Customer & Recorded Purchase Successfully!", Toast.LENGTH_LONG).show()
                                onBack()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FieldSalesColors.DarkGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("btn_save_customer_purchase")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Save Customer & Record Purchase",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
