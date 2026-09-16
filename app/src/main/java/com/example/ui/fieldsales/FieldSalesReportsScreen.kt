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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PieChart
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
fun FieldSalesReportsScreen(
    viewModel: FieldSalesViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val customers by viewModel.allCustomers.collectAsState()
    val orders by viewModel.allOrders.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Overview, 1: State-wise, 2: Customer-wise, 3: Rep-wise

    val totalMrpValue = orders.sumOf { it.totalMrp }
    val netSalesValue = orders.sumOf { it.finalPurchaseValue }
    val totalOrdersCount = orders.size
    val totalCustomersCount = customers.size

    val stateGrouping = orders.groupBy { it.state }
    val repGrouping = orders.groupBy { it.salesRep }
    val customerGrouping = orders.groupBy { it.clinicOrPharmacyName }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Field Sales Reports",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Territory Analytics, Quotas & Exports",
                            fontSize = 11.sp,
                            color = FieldSalesColors.MintSoft
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_reports")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val csv = viewModel.exportDataToCsv()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, csv)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Field Sales Report"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
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
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = FieldSalesColors.DarkGreen,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = FieldSalesColors.DarkGreen
                    )
                }
            ) {
                listOf("Overview", "State-Wise", "Customer-Wise", "Rep-Wise").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Export Buttons Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val csv = viewModel.exportDataToCsv()
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, csv)
                                    type = "text/csv"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Export to Excel / CSV"))
                                Toast.makeText(context, "Field Sales data exported in Excel/CSV format", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FieldSalesColors.DarkGreen),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_export_excel"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export Excel (CSV)", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val json = viewModel.exportDataToJson()
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, json)
                                    type = "application/json"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Export to JSON"))
                                Toast.makeText(context, "Field Sales data exported in JSON format", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_export_json"),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, FieldSalesColors.DarkGreen)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = FieldSalesColors.DarkGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export JSON", color = FieldSalesColors.DarkGreen, fontSize = 12.sp)
                        }
                    }
                }

                when (selectedTab) {
                    0 -> {
                        // 4 Key KPI Metrics Cards (2x2 grid)
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    ReportKpiCard(
                                        title = "Net Field Sales",
                                        value = "₹${String.format(Locale.US, "%,.2f", netSalesValue)}",
                                        subtitle = "Realized Wholesale Value",
                                        bgColor = FieldSalesColors.LightMintGreen,
                                        textColor = FieldSalesColors.DarkGreen,
                                        modifier = Modifier.weight(1f)
                                    )
                                    ReportKpiCard(
                                        title = "Total MRP Value",
                                        value = "₹${String.format(Locale.US, "%,.2f", totalMrpValue)}",
                                        subtitle = "Catalog Retail Sum",
                                        bgColor = Color(0xFFFEF3C7),
                                        textColor = Color(0xFF92400E),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    ReportKpiCard(
                                        title = "Clinics / Pharmacies",
                                        value = "$totalCustomersCount",
                                        subtitle = "Acquired Customer Accounts",
                                        bgColor = Color(0xFFE0F2FE),
                                        textColor = Color(0xFF0369A1),
                                        modifier = Modifier.weight(1f)
                                    )
                                    ReportKpiCard(
                                        title = "Total Orders",
                                        value = "$totalOrdersCount",
                                        subtitle = "Booked Field Invoices",
                                        bgColor = FieldSalesColors.LimeGreenSoft,
                                        textColor = FieldSalesColors.LimeGreen,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Top Ayurvedic Lines Summary
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, FieldSalesColors.BorderLight)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Assessment, contentDescription = null, tint = FieldSalesColors.DarkGreen)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Product-Wise Sales Contribution", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }

                                    listOf(
                                        Pair("VEDA CHYAWANPRASH SPECIAL", 45000.0),
                                        Pair("VEDA ASHWAGANDHA CHURNA", 28500.0),
                                        Pair("VEDA BRAHMI TAILA", 18200.0),
                                        Pair("VEDA TRIPHALA GUGGULU", 14600.0),
                                        Pair("VEDA KASAMRIT HERBAL SYRUP", 12100.0)
                                    ).forEach { (name, amount) ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(name, fontSize = 12.sp, color = FieldSalesColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                                            Text("₹${String.format(Locale.US, "%,.2f", amount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FieldSalesColors.DarkGreen)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // State-Wise breakdown
                        stateGrouping.forEach { (state, stateOrders) ->
                            item {
                                val stateTotal = stateOrders.sumOf { it.finalPurchaseValue }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, FieldSalesColors.BorderLight)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(state, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = FieldSalesColors.DarkGreen)
                                            Text("₹${String.format(Locale.US, "%,.2f", stateTotal)}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = FieldSalesColors.DarkGreen)
                                        }
                                        Text("Orders: ${stateOrders.size} | Avg Ticket: ₹${String.format(Locale.US, "%,.0f", stateTotal / stateOrders.size)}", fontSize = 11.sp, color = FieldSalesColors.TextSecondary)
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // Customer-Wise breakdown
                        customerGrouping.forEach { (clinic, clinicOrders) ->
                            item {
                                val clinicTotal = clinicOrders.sumOf { it.finalPurchaseValue }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, FieldSalesColors.BorderLight)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(clinic, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("₹${String.format(Locale.US, "%,.2f", clinicTotal)}", fontWeight = FontWeight.Bold, color = FieldSalesColors.DarkGreen)
                                        }
                                        Text("Purchases: ${clinicOrders.size} bills | Rep: ${clinicOrders.firstOrNull()?.salesRep ?: ""}", fontSize = 11.sp, color = FieldSalesColors.TextSecondary)
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // Sales rep-wise breakdown
                        repGrouping.forEach { (rep, repOrders) ->
                            item {
                                val repTotal = repOrders.sumOf { it.finalPurchaseValue }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, FieldSalesColors.BorderLight)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(rep, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = FieldSalesColors.DarkGreen)
                                            Text("₹${String.format(Locale.US, "%,.2f", repTotal)}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = FieldSalesColors.DarkGreen)
                                        }
                                        Text("Orders Booked: ${repOrders.size} | Territory: South Zone", fontSize = 11.sp, color = FieldSalesColors.TextSecondary)
                                    }
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
fun ReportKpiCard(
    title: String,
    value: String,
    subtitle: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = textColor)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
            Text(subtitle, fontSize = 10.sp, color = Color.Gray)
        }
    }
}
