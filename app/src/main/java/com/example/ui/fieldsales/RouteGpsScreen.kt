package com.example.ui.fieldsales

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FieldCustomer
import com.example.ui.FieldSalesViewModel
import com.example.ui.theme.FieldSalesColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteGpsScreen(
    viewModel: FieldSalesViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val customers by viewModel.allCustomers.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val visits by viewModel.allVisits.collectAsState()

    var selectedCustomerForVisit by remember { mutableStateOf<FieldCustomer?>(null) }
    var visitOutcome by remember { mutableStateOf("ORDER_PLACED") }
    var visitNotes by remember { mutableStateOf("") }

    // Proximity sorted customer list
    val sortedCustomers = remember(customers, currentLocation) {
        customers.sortedBy { customer ->
            viewModel.calculateDistanceKm(
                currentLocation.first, currentLocation.second,
                customer.latitude, customer.longitude
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Route & GPS Tracker",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Live Route Proximity & Visit Logging",
                            fontSize = 11.sp,
                            color = FieldSalesColors.MintSoft
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_route_gps")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.fetchLiveGps() }) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Refresh GPS", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FieldSalesColors.DarkGreen)
            )
        },
        containerColor = FieldSalesColors.Background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Live Route Map Canvas
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FieldSalesColors.DarkGreenSurface),
                    border = BorderStroke(1.dp, FieldSalesColors.DarkGreen)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Draw subtle grid lines
                            for (i in 1..4) {
                                drawLine(
                                    color = Color(0xFF1B4332).copy(alpha = 0.5f),
                                    start = Offset(0f, h * i / 5f),
                                    end = Offset(w, h * i / 5f),
                                    strokeWidth = 1f
                                )
                                drawLine(
                                    color = Color(0xFF1B4332).copy(alpha = 0.5f),
                                    start = Offset(w * i / 5f, 0f),
                                    end = Offset(w * i / 5f, h),
                                    strokeWidth = 1f
                                )
                            }

                            // Dynamic Points for Route
                            val points = listOf(
                                Offset(w * 0.15f, h * 0.75f), // My location
                                Offset(w * 0.35f, h * 0.40f), // Customer 1
                                Offset(w * 0.65f, h * 0.30f), // Customer 2
                                Offset(w * 0.85f, h * 0.60f)  // Customer 3
                            )

                            // Draw dashed route path
                            val path = Path().apply {
                                moveTo(points[0].x, points[0].y)
                                for (i in 1 until points.size) {
                                    lineTo(points[i].x, points[i].y)
                                }
                            }

                            drawPath(
                                path = path,
                                color = Color(0xFF65A30D),
                                style = Stroke(
                                    width = 4f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                                )
                            )

                            // Current User Pin
                            drawCircle(
                                color = Color(0xFF38BDF8).copy(alpha = 0.35f),
                                radius = 24f,
                                center = points[0]
                            )
                            drawCircle(
                                color = Color(0xFF0284C7),
                                radius = 10f,
                                center = points[0]
                            )

                            // Customer Pins
                            for (i in 1 until points.size) {
                                drawCircle(
                                    color = Color(0xFF65A30D).copy(alpha = 0.3f),
                                    radius = 18f,
                                    center = points[i]
                                )
                                drawCircle(
                                    color = Color(0xFFE53935),
                                    radius = 8f,
                                    center = points[i]
                                )
                            }
                        }

                        // Overlay labels
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF38BDF8))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "LIVE GPS: ${String.format(Locale.US, "%.4f, %.4f", currentLocation.first, currentLocation.second)}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Optimized Field Route (Proximity Sorted)",
                                color = FieldSalesColors.LimeGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Proximity Section Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nearest Clinics & Pharmacies",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = FieldSalesColors.TextPrimary
                    )
                    Text(
                        text = "${sortedCustomers.size} on Route",
                        fontSize = 12.sp,
                        color = FieldSalesColors.DarkGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Sorted Customer Cards with Navigation and Visit Logging
            items(sortedCustomers) { customer ->
                val distanceKm = viewModel.calculateDistanceKm(
                    currentLocation.first, currentLocation.second,
                    customer.latitude, customer.longitude
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, FieldSalesColors.BorderLight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = customer.clinicOrPharmacyName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = FieldSalesColors.TextPrimary
                                )
                                Text(
                                    text = "Doctor: ${customer.customerName} • ${customer.city}",
                                    fontSize = 12.sp,
                                    color = FieldSalesColors.TextSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(FieldSalesColors.LightMintGreen)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$distanceKm km",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FieldSalesColors.DarkGreen
                                )
                            }
                        }

                        Text(
                            text = customer.address,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        // Action Buttons: Navigate & Record Visit
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    try {
                                        val gmmIntentUri = Uri.parse("google.navigation:q=${customer.latitude},${customer.longitude}")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                        context.startActivity(mapIntent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "Navigating to: ${customer.clinicOrPharmacyName}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FieldSalesColors.DarkGreen),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Navigate", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { selectedCustomerForVisit = customer },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, FieldSalesColors.EarthyBrown)
                            ) {
                                Icon(Icons.Default.PinDrop, contentDescription = null, tint = FieldSalesColors.EarthyBrown, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Record Visit", color = FieldSalesColors.EarthyBrown, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Record Customer Visit Dialog
    if (selectedCustomerForVisit != null) {
        val cust = selectedCustomerForVisit!!
        AlertDialog(
            onDismissRequest = { selectedCustomerForVisit = null },
            title = {
                Text(
                    text = "Record Customer Visit",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Clinic: ${cust.clinicOrPharmacyName}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FieldSalesColors.DarkGreen
                    )
                    Text(
                        text = "GPS Coordinates: ${cust.latitude}, ${cust.longitude}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Text(
                        text = "Visit Outcome:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    val outcomes = listOf("ORDER_PLACED", "PAYMENT_COLLECTED", "FOLLOW_UP", "SAMPLE_GIVEN")
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        outcomes.forEach { out ->
                            FilterChip(
                                selected = visitOutcome == out,
                                onClick = { visitOutcome = out },
                                label = { Text(out.replace("_", " "), fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FieldSalesColors.DarkGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = visitNotes,
                        onValueChange = { visitNotes = it },
                        label = { Text("Visit Notes") },
                        placeholder = { Text("e.g. Doctor showed interest in Chyawanprash 1kg packs.") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.recordVisit(
                            customer = cust,
                            outcome = visitOutcome,
                            notes = visitNotes
                        )
                        Toast.makeText(context, "Visit logged successfully!", Toast.LENGTH_SHORT).show()
                        selectedCustomerForVisit = null
                        visitNotes = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FieldSalesColors.DarkGreen)
                ) {
                    Text("Save Visit")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedCustomerForVisit = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
