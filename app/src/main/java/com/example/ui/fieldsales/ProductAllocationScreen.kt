package com.example.ui.fieldsales

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Percent
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.ui.FieldSalesViewModel
import com.example.ui.theme.FieldSalesColors
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductAllocationScreen(
    viewModel: FieldSalesViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val allocations by viewModel.productAllocations.collectAsState()
    val erpProducts by viewModel.erpProducts.collectAsState()

    var selectedState by remember { mutableStateOf("Karnataka") }
    val states = listOf("Karnataka", "Andhra Pradesh", "Telangana", "Maharashtra", "Tamil Nadu")

    val stateAllocations = allocations.filter { it.state.equals(selectedState, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "State Product Allocation",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "ERP Product Master Mapping & State Quotas",
                            fontSize = 11.sp,
                            color = FieldSalesColors.MintSoft
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_allocation")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // State Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, FieldSalesColors.BorderLight)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select State Territory:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FieldSalesColors.TextSecondary
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(states) { st ->
                            FilterChip(
                                selected = selectedState == st,
                                onClick = { selectedState = st },
                                label = { Text(st, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FieldSalesColors.DarkGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Overview Notice
            Card(
                colors = CardDefaults.cardColors(containerColor = FieldSalesColors.LightMintGreen),
                border = BorderStroke(1.dp, FieldSalesColors.DarkGreen)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Medication, contentDescription = null, tint = FieldSalesColors.DarkGreen)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Allocated products are pulled directly from existing ERP Product Master. Enable or disable availability per state and assign special state discounts.",
                        fontSize = 11.sp,
                        color = FieldSalesColors.DarkGreen
                    )
                }
            }

            // Allocations List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(stateAllocations) { alloc ->
                    val product = erpProducts.find { it.id == alloc.productId }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, if (alloc.isAvailable) FieldSalesColors.BorderLight else Color(0xFFFED7D7))
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
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (alloc.isAvailable) FieldSalesColors.LightMintGreen else Color(0xFFFEE2E2)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            if (alloc.isAvailable) Icons.Default.CheckCircle else Icons.Default.DoNotDisturb,
                                            contentDescription = null,
                                            tint = if (alloc.isAvailable) FieldSalesColors.DarkGreen else FieldSalesColors.RedAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = alloc.productName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = FieldSalesColors.TextPrimary
                                        )
                                        Text(
                                            text = "Pack: ${product?.pack ?: "Standard"} | HSN: ${product?.hsnCode ?: "3004"}",
                                            fontSize = 11.sp,
                                            color = FieldSalesColors.TextSecondary
                                        )
                                    }
                                }

                                Switch(
                                    checked = alloc.isAvailable,
                                    onCheckedChange = { isChecked ->
                                        viewModel.updateAllocationStatus(alloc, isChecked)
                                        Toast.makeText(context, "${alloc.productName} updated for $selectedState", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = FieldSalesColors.DarkGreen,
                                        checkedTrackColor = FieldSalesColors.MintSoft
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "MRP: ₹${product?.mrp ?: 0.0} | Wholesale: ₹${product?.wholesaleRate ?: 0.0}",
                                    fontSize = 12.sp,
                                    color = FieldSalesColors.TextSecondary
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Percent, contentDescription = null, tint = FieldSalesColors.LimeGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "State Discount: ${alloc.specialStateDiscount}%",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FieldSalesColors.LimeGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
