package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.dao.PurchaseWithItems
import com.example.data.model.Party
import com.example.data.model.PurchaseItem
import com.example.data.model.PurchaseRecord
import com.example.ui.theme.PharmaBg
import com.example.ui.theme.PharmaBorder
import com.example.ui.theme.PharmaTextPrimary
import com.example.ui.theme.PharmaTextSecondary
import com.example.ui.theme.VedaGreen
import com.example.ui.theme.VedaGreenDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseScreen(
    purchases: List<PurchaseWithItems>,
    suppliers: List<Party>,
    onBack: () -> Unit,
    onSavePurchase: (PurchaseRecord, List<PurchaseItem>) -> Unit,
    onDeletePurchase: (PurchaseRecord) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: All Purchases, 1: Purchase Orders
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PharmaBg)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Purchase & Inward Stock",
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
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Inward Invoices (${purchases.size})", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Supplier PO Orders", fontWeight = FontWeight.SemiBold) }
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (purchases.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = PharmaTextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Purchase Invoices Recorded",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharmaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Record supplier inward bills to automatically update batches, expiry, and stock counts.",
                        fontSize = 13.sp,
                        color = PharmaTextSecondary,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(purchases) { item ->
                        PurchaseItemCard(
                            purchaseWithItems = item,
                            onDelete = { onDeletePurchase(item.purchase) }
                        )
                    }
                }
            }

            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = VedaGreen,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Purchase") },
                text = { Text("New Purchase Bill") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("btn_new_purchase")
            )
        }
    }

    if (showAddDialog) {
        AddPurchaseBillDialog(
            suppliers = suppliers,
            onDismiss = { showAddDialog = false },
            onSave = { purchase, items ->
                onSavePurchase(purchase, items)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun PurchaseItemCard(
    purchaseWithItems: PurchaseWithItems,
    onDelete: () -> Unit
) {
    val purchase = purchaseWithItems.purchase
    val items = purchaseWithItems.items

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, PharmaBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE3F2FD))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = purchase.purchaseInvoiceNo,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = purchase.purchaseDate,
                        fontSize = 11.sp,
                        color = PharmaTextSecondary
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = purchase.supplierName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PharmaTextPrimary
            )

            Text(
                text = "GSTIN: ${purchase.supplierGstin.ifEmpty { "Unregistered" }} • Mode: ${purchase.paymentMode}",
                fontSize = 11.sp,
                color = PharmaTextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Items mini table
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(8.dp)
            ) {
                items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${item.productName} (x${item.qty})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = PharmaTextPrimary
                        )
                        Text(
                            text = "₹${String.format(Locale.US, "%.2f", item.amount)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PharmaTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Taxable: ₹${String.format(Locale.US, "%.2f", purchase.subTotal)} | GST: ₹${String.format(Locale.US, "%.2f", purchase.taxAmount)}",
                    fontSize = 11.sp,
                    color = PharmaTextSecondary
                )
                Text(
                    text = "Total: ₹${String.format(Locale.US, "%,.2f", purchase.grandTotal)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = VedaGreen
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPurchaseBillDialog(
    suppliers: List<Party>,
    onDismiss: () -> Unit,
    onSave: (PurchaseRecord, List<PurchaseItem>) -> Unit
) {
    val today = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()) }
    var invoiceNo by remember { mutableStateOf("PUR-${(1000..9999).random()}") }
    var selectedSupplier by remember { mutableStateOf(suppliers.firstOrNull()?.name ?: "Veda Ayurvedic Labs Ltd") }
    var supplierGstin by remember { mutableStateOf(suppliers.firstOrNull()?.gstin ?: "27AAACV9876E1Z5") }
    var paymentMode by remember { mutableStateOf("CREDIT") }

    // First line item
    var prodName by remember { mutableStateOf("VEDA ASHWAGANDHA CHURNA") }
    var batch by remember { mutableStateOf("ASH2405") }
    var exp by remember { mutableStateOf("05/27") }
    var pack by remember { mutableStateOf("100g Jar") }
    var qtyStr by remember { mutableStateOf("25") }
    var freeQtyStr by remember { mutableStateOf("2") }
    var purchaseRateStr by remember { mutableStateOf("80.00") }
    var mrpStr by remember { mutableStateOf("180.00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Record Supplier Purchase Bill",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = invoiceNo,
                        onValueChange = { invoiceNo = it },
                        label = { Text("Supplier Bill No.") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = selectedSupplier,
                        onValueChange = { selectedSupplier = it },
                        label = { Text("Supplier / Manufacturer Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = supplierGstin,
                        onValueChange = { supplierGstin = it },
                        label = { Text("Supplier GSTIN") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = paymentMode == "CREDIT",
                            onClick = { paymentMode = "CREDIT" },
                            label = { Text("Credit") }
                        )
                        FilterChip(
                            selected = paymentMode == "CASH",
                            onClick = { paymentMode = "CASH" },
                            label = { Text("Cash") }
                        )
                        FilterChip(
                            selected = paymentMode == "BANK",
                            onClick = { paymentMode = "BANK" },
                            label = { Text("Bank Transfer") }
                        )
                    }
                }

                item {
                    Text(
                        text = "Medicine Item Inward Details:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = VedaGreen
                    )
                }

                item {
                    OutlinedTextField(
                        value = prodName,
                        onValueChange = { prodName = it },
                        label = { Text("Product / Formulation Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = batch,
                            onValueChange = { batch = it },
                            label = { Text("Batch") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = exp,
                            onValueChange = { exp = it },
                            label = { Text("Expiry (MM/YY)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = qtyStr,
                            onValueChange = { qtyStr = it },
                            label = { Text("Inward Qty") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = freeQtyStr,
                            onValueChange = { freeQtyStr = it },
                            label = { Text("Free Qty") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = purchaseRateStr,
                            onValueChange = { purchaseRateStr = it },
                            label = { Text("Purchase Rate ₹") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = mrpStr,
                            onValueChange = { mrpStr = it },
                            label = { Text("MRP ₹") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = qtyStr.toIntOrNull() ?: 1
                    val freeQty = freeQtyStr.toIntOrNull() ?: 0
                    val pRate = purchaseRateStr.toDoubleOrNull() ?: 50.0
                    val mrp = mrpStr.toDoubleOrNull() ?: 100.0

                    val taxable = qty * pRate
                    val gstVal = taxable * 0.12
                    val grand = taxable + gstVal

                    val purchaseRecord = PurchaseRecord(
                        purchaseInvoiceNo = invoiceNo,
                        purchaseDate = today,
                        supplierName = selectedSupplier,
                        supplierGstin = supplierGstin,
                        totalTaxable = taxable,
                        totalGst = gstVal,
                        grandTotal = grand,
                        paymentMode = paymentMode
                    )

                    val item = PurchaseItem(
                        productName = prodName,
                        batchNumber = batch,
                        expiryDate = exp,
                        pack = pack,
                        qty = qty,
                        freeQty = freeQty,
                        purchaseRate = pRate,
                        mrp = mrp,
                        gstPercent = 12.0,
                        amount = grand
                    )

                    onSave(purchaseRecord, listOf(item))
                },
                colors = ButtonDefaults.buttonColors(containerColor = VedaGreen)
            ) {
                Text("Save Bill & Inward Stock")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
