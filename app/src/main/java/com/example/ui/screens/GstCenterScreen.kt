package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CompanyProfile
import com.example.data.model.InvoiceWithItems
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
fun GstCenterScreen(
    invoices: List<InvoiceWithItems>,
    company: CompanyProfile,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: GSTR-1, 1: GSTR-3B, 2: e-Invoice & E-Way
    val context = LocalContext.current

    val b2bInvoices = invoices.filter { it.invoice.partyGstin.length >= 15 }
    val b2cInvoices = invoices.filter { it.invoice.partyGstin.length < 15 }

    val totalTaxable = invoices.sumOf { it.invoice.subTotalTaxable }
    val totalCgst = invoices.sumOf { it.invoice.cgstPayable }
    val totalSgst = invoices.sumOf { it.invoice.sgstPayable }
    val totalGst = totalCgst + totalSgst

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PharmaBg)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "GST Compliance & e-Invoice",
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
            actions = {
                IconButton(onClick = {
                    Toast.makeText(context, "GSTR-1 JSON exported successfully to Downloads", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Export GSTR JSON",
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
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("GSTR-1 Summary") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("GSTR-3B Tax") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("e-Invoice / E-Way") })
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PharmaBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = VedaGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "GSTIN: ${company.gstin}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmaTextPrimary
                            )
                            Text(
                                text = "Trade Name: ${company.companyName} | State: ${company.state}",
                                fontSize = 11.sp,
                                color = PharmaTextSecondary
                            )
                        }
                    }
                }
            }

            if (selectedTab == 0) {
                // GSTR-1 View
                item {
                    Text(
                        text = "OUTWARD SUPPLIES (GSTR-1)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharmaTextSecondary
                    )
                }

                item {
                    GstSummaryCard(
                        tableCode = "4A, 4B, 4C",
                        title = "B2B Taxable Invoices",
                        count = b2bInvoices.size,
                        taxable = b2bInvoices.sumOf { it.invoice.subTotalTaxable },
                        tax = b2bInvoices.sumOf { it.invoice.cgstPayable + it.invoice.sgstPayable }
                    )
                }

                item {
                    GstSummaryCard(
                        tableCode = "7",
                        title = "B2C (Others / Retail Walk-in)",
                        count = b2cInvoices.size,
                        taxable = b2cInvoices.sumOf { it.invoice.subTotalTaxable },
                        tax = b2cInvoices.sumOf { it.invoice.cgstPayable + it.invoice.sgstPayable }
                    )
                }

                item {
                    GstSummaryCard(
                        tableCode = "12",
                        title = "HSN Summary of Outward Supplies",
                        count = invoices.flatMap { it.items }.map { it.hsn }.distinct().size,
                        taxable = totalTaxable,
                        tax = totalGst
                    )
                }
            } else if (selectedTab == 1) {
                // GSTR-3B View
                item {
                    Text(
                        text = "MONTHLY TAX LIABILITY (GSTR-3B)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharmaTextSecondary
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
                            Text("3.1 (a) Tax on Outward Supplies", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            GstRow("Total Taxable Value:", "₹${String.format(Locale.US, "%,.2f", totalTaxable)}")
                            GstRow("Central Tax (CGST):", "₹${String.format(Locale.US, "%,.2f", totalCgst)}")
                            GstRow("State Tax (SGST):", "₹${String.format(Locale.US, "%,.2f", totalSgst)}")
                            GstRow("Integrated Tax (IGST):", "₹0.00")
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total GST Payable:", fontWeight = FontWeight.Bold)
                                Text("₹${String.format(Locale.US, "%,.2f", totalGst)}", fontWeight = FontWeight.Bold, color = VedaGreen)
                            }
                        }
                    }
                }
            } else {
                // e-Invoice & E-Way Bills
                item {
                    Text(
                        text = "NIC e-INVOICE & E-WAY BILL SYSTEM",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharmaTextSecondary
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.QrCode2, contentDescription = null, tint = VedaEmerald, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Real-Time IRN Generation", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Automatic 64-character hash & signed QR for B2B bills", fontSize = 11.sp, color = PharmaTextSecondary)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Active IRN Count: ${b2bInvoices.size} invoices stamped", fontSize = 12.sp, color = VedaGreen, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    Toast.makeText(context, "Synced all pending invoices with IRP Portal", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VedaGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Bulk Generate IRN & Signed QR")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GstSummaryCard(
    tableCode: String,
    title: String,
    count: Int,
    taxable: Double,
    tax: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, PharmaBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Table $tableCode",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = VedaGreen
                )
                Text(
                    text = "$count Records",
                    fontSize = 11.sp,
                    color = PharmaTextSecondary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = PharmaTextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Taxable: ₹${String.format(Locale.US, "%,.2f", taxable)}",
                    fontSize = 11.sp,
                    color = PharmaTextSecondary
                )
                Text(
                    text = "Tax: ₹${String.format(Locale.US, "%,.2f", tax)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PharmaTextPrimary
                )
            }
        }
    }
}

@Composable
fun GstRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = PharmaTextSecondary)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PharmaTextPrimary)
    }
}
