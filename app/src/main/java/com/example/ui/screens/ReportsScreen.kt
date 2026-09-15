package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.dao.PurchaseWithItems
import com.example.data.model.InvoiceWithItems
import com.example.data.model.Party
import com.example.data.model.Product
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.AlertRed
import com.example.ui.theme.PharmaBg
import com.example.ui.theme.PharmaBorder
import com.example.ui.theme.PharmaTextPrimary
import com.example.ui.theme.PharmaTextSecondary
import com.example.ui.theme.VedaEmerald
import com.example.ui.theme.VedaGreen
import com.example.ui.theme.VedaGreenDark
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    invoices: List<InvoiceWithItems>,
    purchases: List<PurchaseWithItems>,
    products: List<Product>,
    parties: List<Party>,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Sales Analysis, 1: Expiry Tracker, 2: Stock Valuation, 3: Outstanding

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PharmaBg)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Reports & Business Intelligence",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = VedaGreenDark
            )
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = VedaGreen
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Sales") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Expiry") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Stock Val") })
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Outstanding") })
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // Sales Reports
                    val totalSales = invoices.sumOf { it.invoice.grandTotal }
                    val totalQtySold = invoices.flatMap { it.items }.sumOf { it.qty }
                    val cashSales = invoices.filter { it.invoice.paymentMode.equals("CASH", ignoreCase = true) }.sumOf { it.invoice.grandTotal }
                    val creditSales = invoices.filter { it.invoice.paymentMode.equals("CREDIT", ignoreCase = true) }.sumOf { it.invoice.grandTotal }

                    item {
                        ReportSummaryHeader(
                            icon = Icons.Default.TrendingUp,
                            title = "Consolidated Sales Performance",
                            metric = "₹${String.format(Locale.US, "%,.2f", totalSales)}",
                            subtext = "$totalQtySold units sold across ${invoices.size} bills"
                        )
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PharmaBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Payment Mode Split", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Cash Counter Sales:", fontSize = 12.sp, color = PharmaTextSecondary)
                                    Text("₹${String.format(Locale.US, "%,.2f", cashSales)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VedaEmerald)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Credit / Chemist Receivables:", fontSize = 12.sp, color = PharmaTextSecondary)
                                    Text("₹${String.format(Locale.US, "%,.2f", creditSales)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "PARTY-WISE SALES BREAKDOWN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmaTextSecondary
                        )
                    }

                    val partySales = invoices.groupBy { it.invoice.partyName }
                    items(partySales.entries.toList()) { entry ->
                        val pTotal = entry.value.sumOf { it.invoice.grandTotal }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PharmaBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(entry.key, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${entry.value.size} invoices", fontSize = 11.sp, color = PharmaTextSecondary)
                                }
                                Text("₹${String.format(Locale.US, "%,.2f", pTotal)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = VedaGreen)
                            }
                        }
                    }
                }

                1 -> {
                    // Expiry Tracker (Near Expiry & Expired)
                    val nearExp = products.filter { it.expiryDate.contains("26") || it.expiryDate.contains("25") }
                    val safeExp = products.filterNot { it.expiryDate.contains("26") || it.expiryDate.contains("25") }

                    item {
                        ReportSummaryHeader(
                            icon = Icons.Default.Warning,
                            title = "Pharma Expiry & FEFO Status",
                            metric = "${nearExp.size} Batches Alert",
                            subtext = "Products nearing expiration requiring discount schemes or supplier return"
                        )
                    }

                    item {
                        Text(
                            text = "NEAR-EXPIRY / DUMP MEDICINES (< 180 DAYS)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AlertAmber
                        )
                    }

                    items(nearExp) { prod ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AlertAmber.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Batch: ${prod.batchNumber} • Exp: ${prod.expiryDate}", fontSize = 11.sp, color = AlertAmber, fontWeight = FontWeight.SemiBold)
                                    Text("Rack: ${prod.rackNumber} • Stock: ${prod.stockQty} ${prod.unit}", fontSize = 10.sp, color = PharmaTextSecondary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("MRP ₹${prod.mrp}", fontSize = 11.sp, color = PharmaTextSecondary)
                                    Text("Val: ₹${(prod.stockQty * prod.purchaseRate).toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AlertRed)
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Stock Valuation
                    val totalStockVal = products.sumOf { (it.purchaseRate.takeIf { pr -> pr > 0 } ?: (it.saleRate * 0.75)) * it.stockQty }
                    val totalMrpVal = products.sumOf { it.mrp * it.stockQty }

                    item {
                        ReportSummaryHeader(
                            icon = Icons.Default.Inventory2,
                            title = "Total Warehouse Inventory Valuation",
                            metric = "₹${String.format(Locale.US, "%,.2f", totalStockVal)}",
                            subtext = "MRP Value: ₹${String.format(Locale.US, "%,.2f", totalMrpVal)}"
                        )
                    }

                    item {
                        Text(
                            text = "PRODUCT-WISE STOCK POSITION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmaTextSecondary
                        )
                    }

                    items(products) { prod ->
                        val cost = (prod.purchaseRate.takeIf { it > 0 } ?: (prod.saleRate * 0.75)) * prod.stockQty
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PharmaBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Pack: ${prod.pack} | In Stock: ${prod.stockQty} ${prod.unit}", fontSize = 11.sp, color = PharmaTextSecondary)
                                    Text("Purchase: ₹${prod.purchaseRate} | Sale: ₹${prod.saleRate}", fontSize = 10.sp, color = PharmaTextSecondary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("₹${String.format(Locale.US, "%,.2f", cost)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = VedaGreen)
                                    Text("MRP Val: ₹${(prod.stockQty * prod.mrp).toInt()}", fontSize = 10.sp, color = PharmaTextSecondary)
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // Outstanding Receivables & Payables Ageing
                    val customersWithBalance = parties.filter { it.type == "CUSTOMER" && it.currentBalance > 0 }
                    val suppliersWithBalance = parties.filter { it.type == "SUPPLIER" }

                    item {
                        ReportSummaryHeader(
                            icon = Icons.Default.Assessment,
                            title = "Chemist Receivables Ledger",
                            metric = "₹${String.format(Locale.US, "%,.2f", customersWithBalance.sumOf { it.currentBalance })}",
                            subtext = "${customersWithBalance.size} party accounts with pending credit balances"
                        )
                    }

                    items(customersWithBalance) { party ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PharmaBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(party.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("DL: ${party.dlNo} • Ph: ${party.phone}", fontSize = 11.sp, color = PharmaTextSecondary)
                                    Text("Credit Limit: ₹${party.creditLimit.toInt()} (${party.creditDays} days allowed)", fontSize = 10.sp, color = Color(0xFF1565C0))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("₹${String.format(Locale.US, "%,.2f", party.currentBalance)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFD97706))
                                    Text("OVERDUE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AlertRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportSummaryHeader(
    icon: ImageVector,
    title: String,
    metric: String,
    subtext: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, PharmaBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = VedaGreen, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PharmaTextSecondary)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(metric, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = VedaGreenDark)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtext, fontSize = 11.sp, color = PharmaTextSecondary)
        }
    }
}
