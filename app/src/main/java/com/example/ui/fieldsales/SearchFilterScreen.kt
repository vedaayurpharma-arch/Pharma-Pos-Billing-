package com.example.ui.fieldsales

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FieldCustomer
import com.example.ui.FieldSalesViewModel
import com.example.ui.theme.FieldSalesColors
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFilterScreen(
    viewModel: FieldSalesViewModel,
    onBack: () -> Unit,
    onCustomerClick: (FieldCustomer) -> Unit
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val filterRep by viewModel.filterSalesRep.collectAsState()
    val filteredCustomers by viewModel.filteredCustomers.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()

    var showFilters by remember { mutableStateOf(true) }

    val states = listOf("All", "Karnataka", "Andhra Pradesh", "Telangana", "Maharashtra", "Tamil Nadu")
    val salesReps = listOf("All", "Ramesh Kumar", "Kiran Reddy", "Dr. Veda Murthy")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Search & Filter Customers",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${filteredCustomers.size} Clinics & Pharmacies Found",
                            fontSize = 11.sp,
                            color = FieldSalesColors.MintSoft
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_search")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Toggle Filters", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FieldSalesColors.DarkGreen)
            )
        },
        containerColor = FieldSalesColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                label = { Text("Search by Customer, Clinic, Phone, City...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = FieldSalesColors.DarkGreen)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_customers"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FieldSalesColors.DarkGreen,
                    focusedLabelColor = FieldSalesColors.DarkGreen,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                singleLine = true
            )

            // State & Rep Filter Chips
            if (showFilters) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Filter by State:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = FieldSalesColors.TextSecondary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(states) { st ->
                            FilterChip(
                                selected = filterState == st,
                                onClick = { viewModel.filterState.value = st },
                                label = { Text(st, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FieldSalesColors.DarkGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Text("Filter by Sales Representative:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = FieldSalesColors.TextSecondary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(salesReps) { rep ->
                            FilterChip(
                                selected = filterRep == rep,
                                onClick = { viewModel.filterSalesRep.value = rep },
                                label = { Text(rep, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FieldSalesColors.EarthyBrown,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Customer List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredCustomers.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 30.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Business, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("No matching customers found", fontWeight = FontWeight.Bold, color = FieldSalesColors.TextPrimary)
                                Text("Try changing your search terms or state filter.", fontSize = 12.sp, color = FieldSalesColors.TextSecondary)
                            }
                        }
                    }
                }

                items(filteredCustomers) { customer ->
                    val distanceKm = viewModel.calculateDistanceKm(
                        currentLocation.first, currentLocation.second,
                        customer.latitude, customer.longitude
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_customer_${customer.id}"),
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(FieldSalesColors.LightMintGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🌿", fontSize = 18.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = customer.clinicOrPharmacyName,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = FieldSalesColors.TextPrimary
                                        )
                                        Text(
                                            text = "Contact: ${customer.customerName}",
                                            fontSize = 12.sp,
                                            color = FieldSalesColors.TextSecondary
                                        )
                                    }
                                }

                                if (distanceKm > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(FieldSalesColors.LimeGreenSoft)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "$distanceKm km away",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = FieldSalesColors.LimeGreen
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = FieldSalesColors.DarkGreen, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "${customer.address}, ${customer.city}, ${customer.state} ${customer.pinCode}",
                                    fontSize = 12.sp,
                                    color = FieldSalesColors.TextSecondary
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = FieldSalesColors.EarthyBrown, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Rep: ${customer.salesRep}", fontSize = 11.sp, color = FieldSalesColors.EarthyBrown, fontWeight = FontWeight.SemiBold)
                                }

                                Text(
                                    text = "Total Purchases: ₹${String.format(Locale.US, "%,.2f", customer.totalPurchasesValue)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FieldSalesColors.DarkGreen
                                )
                            }

                            // Quick Action Buttons (Call, Navigate, Record Purchase)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        try {
                                            val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                                            context.startActivity(callIntent)
                                        } catch (_: Exception) {
                                            Toast.makeText(context, "Dialing ${customer.phone}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = FieldSalesColors.LightMintGreen),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, tint = FieldSalesColors.DarkGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Call", color = FieldSalesColors.DarkGreen, fontSize = 11.sp)
                                }

                                Button(
                                    onClick = {
                                        try {
                                            val navUri = Uri.parse("google.navigation:q=${customer.latitude},${customer.longitude}")
                                            val mapIntent = Intent(Intent.ACTION_VIEW, navUri)
                                            context.startActivity(mapIntent)
                                        } catch (_: Exception) {
                                            Toast.makeText(context, "Navigating to ${customer.clinicOrPharmacyName}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = FieldSalesColors.EarthyBrownLight),
                                    modifier = Modifier.weight(1.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Navigation, contentDescription = null, tint = FieldSalesColors.EarthyBrown, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Navigate", color = FieldSalesColors.EarthyBrown, fontSize = 11.sp)
                                }

                                Button(
                                    onClick = { onCustomerClick(customer) },
                                    colors = ButtonDefaults.buttonColors(containerColor = FieldSalesColors.DarkGreen),
                                    modifier = Modifier.weight(1.3f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Select / Order", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
