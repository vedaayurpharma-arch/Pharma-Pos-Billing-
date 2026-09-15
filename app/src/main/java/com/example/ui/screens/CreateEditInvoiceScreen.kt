package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Party
import com.example.data.model.Product
import com.example.ui.DraftInvoice
import com.example.ui.DraftInvoiceItem
import com.example.ui.theme.PharmaBlue
import com.example.ui.theme.PharmaGreen
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditInvoiceScreen(
    draftInvoice: DraftInvoice,
    savedParties: List<Party>,
    savedProducts: List<Product>,
    onSelectParty: (Party) -> Unit,
    onUpdateHeader: (
        invoiceNumber: String,
        invoiceDate: String,
        dueDate: String,
        orderNo: String,
        orderDate: String,
        partyName: String,
        partyAddress: String,
        partyPhone: String,
        partyDlNo: String,
        partyGstin: String,
        status: String,
        crDrNote: Double
    ) -> Unit,
    onAddItem: (DraftInvoiceItem) -> Unit,
    onUpdateItem: (Int, DraftInvoiceItem) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onSaveAndPreview: () -> Unit,
    onBack: () -> Unit
) {
    var showItemDialog by remember { mutableStateOf(false) }
    var editingItemIndex by remember { mutableStateOf<Int?>(null) }
    var partyDropdownExpanded by remember { mutableStateOf(false) }

    var invoiceNumber by remember(draftInvoice.invoiceNumber) { mutableStateOf(draftInvoice.invoiceNumber) }
    var invoiceDate by remember(draftInvoice.invoiceDate) { mutableStateOf(draftInvoice.invoiceDate) }
    var dueDate by remember(draftInvoice.dueDate) { mutableStateOf(draftInvoice.dueDate) }
    var orderNo by remember(draftInvoice.orderNo) { mutableStateOf(draftInvoice.orderNo) }
    var orderDate by remember(draftInvoice.orderDate) { mutableStateOf(draftInvoice.orderDate) }

    var partyName by remember(draftInvoice.partyName) { mutableStateOf(draftInvoice.partyName) }
    var partyAddress by remember(draftInvoice.partyAddress) { mutableStateOf(draftInvoice.partyAddress) }
    var partyPhone by remember(draftInvoice.partyPhone) { mutableStateOf(draftInvoice.partyPhone) }
    var partyDlNo by remember(draftInvoice.partyDlNo) { mutableStateOf(draftInvoice.partyDlNo) }
    var partyGstin by remember(draftInvoice.partyGstin) { mutableStateOf(draftInvoice.partyGstin) }
    var status by remember(draftInvoice.status) { mutableStateOf(draftInvoice.status) }
    var crDrNoteStr by remember(draftInvoice.crDrNote) { mutableStateOf(draftInvoice.crDrNote.toString()) }

    fun syncHeader() {
        val crDr = crDrNoteStr.toDoubleOrNull() ?: 0.0
        onUpdateHeader(
            invoiceNumber, invoiceDate, dueDate, orderNo, orderDate,
            partyName, partyAddress, partyPhone, partyDlNo, partyGstin, status, crDr
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (draftInvoice.id == 0L) "New Pharma Bill" else "Edit Bill ${draftInvoice.invoiceNumber}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("create_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            syncHeader()
                            onSaveAndPreview()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = PharmaBlue
                        ),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("save_and_preview_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save & Preview", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PharmaBlue
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8FAFC))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Invoice Metadata Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = PharmaBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Invoice Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = PharmaBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = invoiceNumber,
                            onValueChange = { invoiceNumber = it; syncHeader() },
                            label = { Text("Invoice No") },
                            modifier = Modifier.weight(1f).testTag("input_invoice_number"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = invoiceDate,
                            onValueChange = { invoiceDate = it; syncHeader() },
                            label = { Text("Invoice Date") },
                            modifier = Modifier.weight(1f).testTag("input_invoice_date"),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = dueDate,
                            onValueChange = { dueDate = it; syncHeader() },
                            label = { Text("Due Date") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = orderNo,
                            onValueChange = { orderNo = it; syncHeader() },
                            label = { Text("Order No") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = orderDate,
                            onValueChange = { orderDate = it; syncHeader() },
                            label = { Text("Order Date") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = status,
                            onValueChange = { status = it; syncHeader() },
                            label = { Text("Status (PAID/PENDING)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }

            // Party / Buyer Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = PharmaBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Party / Chemist Details",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = PharmaBlue
                            )
                        }

                        if (savedParties.isNotEmpty()) {
                            Box {
                                OutlinedButton(
                                    onClick = { partyDropdownExpanded = true },
                                    modifier = Modifier.testTag("select_party_dropdown_btn")
                                ) {
                                    Text("Pick Party", fontSize = 12.sp)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                                DropdownMenu(
                                    expanded = partyDropdownExpanded,
                                    onDismissRequest = { partyDropdownExpanded = false }
                                ) {
                                    savedParties.forEach { p ->
                                        DropdownMenuItem(
                                            text = { Text(p.name) },
                                            onClick = {
                                                onSelectParty(p)
                                                partyName = p.name
                                                partyAddress = p.address
                                                partyPhone = p.phone
                                                partyDlNo = p.dlNo
                                                partyGstin = p.gstin
                                                partyDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = partyName,
                        onValueChange = { partyName = it; syncHeader() },
                        label = { Text("Party Name (e.g. ASIA TICO PHARMA)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_party_name"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = partyAddress,
                        onValueChange = { partyAddress = it; syncHeader() },
                        label = { Text("Address & City (e.g. BTM LAYOUT, BANGALORE-68)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_party_address"),
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = partyDlNo,
                            onValueChange = { partyDlNo = it; syncHeader() },
                            label = { Text("D.L. No (20B, 21B)") },
                            modifier = Modifier.weight(1f).testTag("input_party_dl"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = partyGstin,
                            onValueChange = { partyGstin = it; syncHeader() },
                            label = { Text("GSTIN") },
                            modifier = Modifier.weight(1f).testTag("input_party_gstin"),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = partyPhone,
                        onValueChange = { partyPhone = it; syncHeader() },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Line Items Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalPharmacy, contentDescription = null, tint = PharmaBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Medicine Items (${draftInvoice.items.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = PharmaBlue
                            )
                        }

                        Button(
                            onClick = {
                                editingItemIndex = null
                                showItemDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PharmaBlue),
                            modifier = Modifier.testTag("add_item_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Item", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (draftInvoice.items.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No items added yet. Click 'Add Item' above.",
                                color = Color(0xFF64748B),
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        // Horizontal scrollable table summary
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            draftInvoice.items.forEachIndexed { index, item ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "${index + 1}. ${item.productName}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF0F172A)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Surface(
                                                    color = Color(0xFFE0F2FE),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "Pack: ${item.pack}",
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF0369A1),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = "Batch: ${item.batch} | Exp: ${item.exp} | HSN: ${item.hsn}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                            Text(
                                                text = "Qty: ${item.qty} + Free: ${item.freeQty} • Rate: ₹${item.rate} • Dis: ${item.discountPercent}% • GST: ${(item.sgstPercent + item.cgstPercent).toInt()}%",
                                                fontSize = 11.sp,
                                                color = Color(0xFF475569)
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "₹${String.format(Locale.US, "%.2f", item.amount)}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = PharmaBlue
                                            )
                                            Text(
                                                text = "GST: ₹${String.format(Locale.US, "%.2f", item.gstVal)}",
                                                fontSize = 10.sp,
                                                color = Color(0xFF64748B)
                                            )

                                            Row {
                                                IconButton(
                                                    onClick = {
                                                        editingItemIndex = index
                                                        showItemDialog = true
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Edit,
                                                        contentDescription = "Edit",
                                                        tint = Color(0xFF0284C7),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { onRemoveItem(index) },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = "Delete",
                                                        tint = Color(0xFFEF4444),
                                                        modifier = Modifier.size(16.dp)
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
            }

            // Calculation & Totals Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Bill Summary (Pharma GST)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PharmaBlue
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SummaryRow(label = "Total Items", value = "${draftInvoice.items.size}")
                    SummaryRow(label = "Total Quantity", value = "${draftInvoice.items.sumOf { it.qty }} Units")
                    SummaryRow(label = "Taxable Subtotal", value = "₹${String.format(Locale.US, "%.2f", draftInvoice.subTotalTaxable)}")
                    SummaryRow(label = "Total Discount", value = "₹${String.format(Locale.US, "%.2f", draftInvoice.totalDiscount)}")
                    SummaryRow(label = "SGST Payable (6%)", value = "₹${String.format(Locale.US, "%.2f", draftInvoice.sgstPayable)}")
                    SummaryRow(label = "CGST Payable (6%)", value = "₹${String.format(Locale.US, "%.2f", draftInvoice.cgstPayable)}")

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "CR/DR Note Adj.", fontSize = 13.sp, color = Color(0xFF475569))
                        OutlinedTextField(
                            value = crDrNoteStr,
                            onValueChange = {
                                crDrNoteStr = it
                                syncHeader()
                            },
                            modifier = Modifier.width(100.dp).height(50.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFCBD5E1)))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Grand Total",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "₹${String.format(Locale.US, "%.2f", draftInvoice.grandTotal)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmaBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = draftInvoice.amountInWords,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PharmaGreen
                    )
                }
            }

            // Save & Preview Button
            Button(
                onClick = {
                    syncHeader()
                    onSaveAndPreview()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_invoice_button"),
                colors = ButtonDefaults.buttonColors(containerColor = PharmaBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Generate & View A4 Landscape Bill", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Add / Edit Item Dialog
    if (showItemDialog) {
        val initialItem = editingItemIndex?.let { draftInvoice.items.getOrNull(it) }
        AddEditItemDialog(
            initialItem = initialItem,
            savedProducts = savedProducts,
            onDismiss = { showItemDialog = false },
            onConfirm = { item ->
                if (editingItemIndex != null) {
                    onUpdateItem(editingItemIndex!!, item)
                } else {
                    onAddItem(item)
                }
                showItemDialog = false
            }
        )
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = Color(0xFF475569))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditItemDialog(
    initialItem: DraftInvoiceItem?,
    savedProducts: List<Product>,
    onDismiss: () -> Unit,
    onConfirm: (DraftInvoiceItem) -> Unit
) {
    var productName by remember { mutableStateOf(initialItem?.productName ?: "") }
    var pack by remember { mutableStateOf(initialItem?.pack ?: "1*10") }
    var batch by remember { mutableStateOf(initialItem?.batch ?: "") }
    var exp by remember { mutableStateOf(initialItem?.exp ?: "") }
    var hsn by remember { mutableStateOf(initialItem?.hsn ?: "") }
    var mrpStr by remember { mutableStateOf(initialItem?.mrp?.toString() ?: "0.0") }
    var rateStr by remember { mutableStateOf(initialItem?.rate?.toString() ?: "0.0") }
    var qtyStr by remember { mutableStateOf(initialItem?.qty?.toString() ?: "1") }
    var freeQtyStr by remember { mutableStateOf(initialItem?.freeQty?.toString() ?: "0") }
    var discountStr by remember { mutableStateOf(initialItem?.discountPercent?.toString() ?: "0.0") }
    var sgstStr by remember { mutableStateOf(initialItem?.sgstPercent?.toString() ?: "6.0") }
    var cgstStr by remember { mutableStateOf(initialItem?.cgstPercent?.toString() ?: "6.0") }

    var productPickerExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialItem == null) "Add Medicine Item" else "Edit Medicine Item",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PharmaBlue
                    )

                    if (savedProducts.isNotEmpty()) {
                        Box {
                            OutlinedButton(
                                onClick = { productPickerExpanded = true },
                                modifier = Modifier.testTag("pick_product_btn")
                            ) {
                                Text("From Catalog", fontSize = 11.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = productPickerExpanded,
                                onDismissRequest = { productPickerExpanded = false }
                            ) {
                                savedProducts.forEach { p ->
                                    DropdownMenuItem(
                                        text = { Text("${p.name} (${p.batchNumber})") },
                                        onClick = {
                                            productName = p.name
                                            pack = p.pack
                                            batch = p.batchNumber
                                            exp = p.expiryDate
                                            hsn = p.hsnCode
                                            mrpStr = p.mrp.toString()
                                            rateStr = p.saleRate.toString()
                                            discountStr = p.defaultDiscount.toString()
                                            sgstStr = p.sgstPercent.toString()
                                            cgstStr = p.cgstPercent.toString()
                                            productPickerExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Product / Medicine Name") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_item_name"),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pack,
                        onValueChange = { pack = it },
                        label = { Text("Pack (e.g. 1*10)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = batch,
                        onValueChange = { batch = it },
                        label = { Text("Batch No") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = exp,
                        onValueChange = { exp = it },
                        label = { Text("Exp (MM/YY)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = hsn,
                        onValueChange = { hsn = it },
                        label = { Text("HSN Code") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = mrpStr,
                        onValueChange = { mrpStr = it },
                        label = { Text("M.R.P") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = rateStr,
                        onValueChange = { rateStr = it },
                        label = { Text("Sale Rate") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = qtyStr,
                        onValueChange = { qtyStr = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f).testTag("dialog_item_qty"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = freeQtyStr,
                        onValueChange = { freeQtyStr = it },
                        label = { Text("Free Qty") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = discountStr,
                        onValueChange = { discountStr = it },
                        label = { Text("Discount %") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = sgstStr,
                        onValueChange = { sgstStr = it },
                        label = { Text("SGST %") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = cgstStr,
                        onValueChange = { cgstStr = it },
                        label = { Text("CGST %") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Live Item Calculation Preview
                val curRate = rateStr.toDoubleOrNull() ?: 0.0
                val curQty = qtyStr.toIntOrNull() ?: 0
                val curDisc = discountStr.toDoubleOrNull() ?: 0.0
                val curSgst = sgstStr.toDoubleOrNull() ?: 0.0
                val curCgst = cgstStr.toDoubleOrNull() ?: 0.0
                val base = curRate * curQty
                val discAmt = base * (curDisc / 100.0)
                val taxable = base - discAmt
                val tax = taxable * ((curSgst + curCgst) / 100.0)
                val lineTotal = taxable + tax

                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Taxable: ₹${String.format(Locale.US, "%.2f", taxable)}", fontSize = 11.sp, color = Color(0xFF1E40AF))
                            Text("GST Val: ₹${String.format(Locale.US, "%.2f", tax)}", fontSize = 11.sp, color = Color(0xFF1E40AF))
                        }
                        Text(
                            text = "Total: ₹${String.format(Locale.US, "%.2f", lineTotal)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PharmaBlue
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val item = DraftInvoiceItem(
                                id = initialItem?.id ?: 0,
                                serialNo = initialItem?.serialNo ?: 1,
                                productName = productName.ifBlank { "MEDICINE" },
                                pack = pack.ifBlank { "1*10" },
                                batch = batch.ifBlank { "BATCH01" },
                                exp = exp.ifBlank { "12/28" },
                                hsn = hsn.ifBlank { "30049099" },
                                mrp = mrpStr.toDoubleOrNull() ?: 0.0,
                                rate = curRate,
                                qty = curQty.coerceAtLeast(1),
                                freeQty = freeQtyStr.toIntOrNull() ?: 0,
                                discountPercent = curDisc,
                                sgstPercent = curSgst,
                                cgstPercent = curCgst
                            )
                            onConfirm(item)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PharmaBlue),
                        modifier = Modifier.testTag("dialog_confirm_item")
                    ) {
                        Text(if (initialItem == null) "Add Item" else "Update Item")
                    }
                }
            }
        }
    }
}
