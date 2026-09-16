package com.example.ui.fieldsales

import android.content.Intent
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FieldOrder
import com.example.ui.FieldSalesViewModel
import com.example.ui.theme.FieldSalesColors
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldInvoiceScreen(
    viewModel: FieldSalesViewModel,
    onBack: () -> Unit,
    onCreateNewCustomerClick: () -> Unit
) {
    val context = LocalContext.current
    val orders by viewModel.allOrders.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Field Invoices",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Digital & Physical Purchase Bills (${orders.size})",
                            fontSize = 11.sp,
                            color = FieldSalesColors.MintSoft
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_invoice")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onCreateNewCustomerClick, modifier = Modifier.testTag("btn_add_invoice")) {
                        Icon(Icons.Default.Add, contentDescription = "New Customer & Purchase", tint = Color.White)
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Field Invoices & Bills",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = FieldSalesColors.TextPrimary
                    )

                    Button(
                        onClick = onCreateNewCustomerClick,
                        colors = ButtonDefaults.buttonColors(containerColor = FieldSalesColors.DarkGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Bill", fontSize = 12.sp)
                    }
                }
            }

            if (orders.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No Field Invoices recorded yet", fontWeight = FontWeight.Bold)
                            Text("Record a new customer purchase to generate digital bills.", fontSize = 12.sp, color = FieldSalesColors.TextSecondary)
                        }
                    }
                }
            }

            items(orders) { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_invoice_${order.orderNumber}"),
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
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(FieldSalesColors.LightMintGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Receipt, contentDescription = null, tint = FieldSalesColors.DarkGreen, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = order.orderNumber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = FieldSalesColors.DarkGreen
                                    )
                                    Text(
                                        text = order.orderDate,
                                        fontSize = 11.sp,
                                        color = FieldSalesColors.TextSecondary
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(FieldSalesColors.LimeGreenSoft)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = order.paymentStatus,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FieldSalesColors.LimeGreen
                                )
                            }
                        }

                        Text(
                            text = order.clinicOrPharmacyName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = FieldSalesColors.TextPrimary
                        )

                        Text(
                            text = "Customer: ${order.customerName} | ${order.city}, ${order.state}",
                            fontSize = 12.sp,
                            color = FieldSalesColors.TextSecondary
                        )

                        if (order.orderNotes.isNotBlank()) {
                            Text(
                                text = "Notes: \"${order.orderNotes}\"",
                                fontSize = 11.sp,
                                color = FieldSalesColors.EarthyBrown
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(FieldSalesColors.BorderLight)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total MRP: ₹${String.format(Locale.US, "%,.2f", order.totalMrp)}",
                                    fontSize = 11.sp,
                                    color = FieldSalesColors.TextSecondary
                                )
                                Text(
                                    text = "Wholesale: ₹${String.format(Locale.US, "%,.2f", order.netWholesaleRate)} (-${order.discountPercent}%)",
                                    fontSize = 11.sp,
                                    color = FieldSalesColors.TextSecondary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Final Value (incl GST)",
                                    fontSize = 10.sp,
                                    color = FieldSalesColors.TextSecondary
                                )
                                Text(
                                    text = "₹${String.format(Locale.US, "%,.2f", order.finalPurchaseValue)}",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FieldSalesColors.DarkGreen
                                )
                            }
                        }

                        // Share / Print Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "Veda Ayur Pharma Invoice ${order.orderNumber} for ${order.clinicOrPharmacyName}: Amount ₹${order.finalPurchaseValue}. View bill at: ${order.digitalInvoiceUrl}"
                                        )
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Invoice"))
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, FieldSalesColors.DarkGreen)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = FieldSalesColors.DarkGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share", color = FieldSalesColors.DarkGreen, fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    Toast.makeText(context, "Printing Invoice ${order.orderNumber} via wireless POS...", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FieldSalesColors.DarkGreen),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Print Bill", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
