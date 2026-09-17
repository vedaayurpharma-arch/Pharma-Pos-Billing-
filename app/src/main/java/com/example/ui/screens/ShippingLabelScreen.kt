package com.example.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.CompanyProfile
import com.example.data.model.ShippingLabel
import com.example.ui.ShippingLabelViewModel
import com.example.ui.theme.PharmaBorder
import com.example.ui.theme.PharmaTextPrimary
import com.example.ui.theme.PharmaTextSecondary
import com.example.ui.theme.VedaGreen
import com.example.ui.theme.VedaGreenDark
import com.example.util.ShippingLabelHtmlGenerator
import com.example.util.ShippingLabelPrintHelper
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ShippingLabelScreen(
    viewModel: ShippingLabelViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val labels by viewModel.allShippingLabels.collectAsState()
    val invoices by viewModel.allInvoices.collectAsState()
    val fieldOrders by viewModel.allFieldOrders.collectAsState()
    val profileState by viewModel.companyProfile.collectAsState()
    val statusMsg by viewModel.statusMessage.collectAsState()

    val company = profileState ?: CompanyProfile(
        companyName = "VEDA AYUR PHARMA",
        addressLine1 = "H.No. 4-22/A, Ayurveda Bhavan, Herbal Complex",
        addressLine2 = "Kurnool - 518002 (A.P.)",
        phone = "+91 94401 23456",
        email = "dispatch@vedapharma.com",
        gstin = "37AAKFV1234F1Z8",
        dlNo = "20B/21B-AP/KNL/2023"
    )

    var selectedTab by remember { mutableStateOf(0) } // 0: All Labels, 1: Quick Generate from Invoices, 2: Preview / Editor
    var activeLabelForPreview by remember { mutableStateOf<ShippingLabel?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var labelToEdit by remember { mutableStateOf<ShippingLabel?>(null) }

    LaunchedEffect(statusMsg) {
        statusMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Shipping Label Creator",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Veda Ayur Pharma Logistics & Dispatch",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_shipping_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (activeLabelForPreview != null) {
                        val activeLabel = activeLabelForPreview!!
                        val html = remember(activeLabel, company) {
                            ShippingLabelHtmlGenerator.generateShippingLabelHtml(activeLabel, company)
                        }

                        // WhatsApp Share Button
                        IconButton(
                            onClick = {
                                if (activity != null) {
                                    ShippingLabelPrintHelper.shareToWhatsApp(
                                        activity = activity,
                                        htmlContent = html,
                                        labelNumber = activeLabel.shippingNumber,
                                        customerName = activeLabel.customerName,
                                        phoneNumber = activeLabel.mobileNumber
                                    )
                                }
                            },
                            modifier = Modifier.testTag("btn_share_label_whatsapp")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Share via WhatsApp",
                                tint = Color(0xFF25D366)
                            )
                        }

                        // Native Share
                        IconButton(
                            onClick = {
                                ShippingLabelPrintHelper.shareShippingLabel(
                                    context = context,
                                    htmlContent = html,
                                    labelNumber = activeLabel.shippingNumber,
                                    customerName = activeLabel.customerName
                                )
                            },
                            modifier = Modifier.testTag("btn_share_label")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Label",
                                tint = Color.White
                            )
                        }

                        // Direct Print (Thermal Sticker / A4)
                        IconButton(
                            onClick = {
                                if (activity != null) {
                                    ShippingLabelPrintHelper.printShippingLabel(
                                        activity = activity,
                                        htmlContent = html,
                                        labelNumber = activeLabel.shippingNumber,
                                        labelSize = activeLabel.labelSize
                                    )
                                }
                            },
                            modifier = Modifier.testTag("btn_print_label")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = "Print Label",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VedaGreenDark)
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = {
                        labelToEdit = ShippingLabel(
                            shippingNumber = "VEDA-SHP-AUTO",
                            customerName = "",
                            address = "",
                            cityOrDistrict = "Kurnool",
                            state = "Andhra Pradesh",
                            pinCode = "518002",
                            shippingDate = "Today"
                        )
                        showEditDialog = true
                    },
                    containerColor = VedaGreen,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_create_shipping_label")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Shipping Label")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8FAFC))
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = VedaGreen
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Labels (${labels.size})", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("From Invoices (${invoices.size})", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
                if (activeLabelForPreview != null) {
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Live Label", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // All Created Shipping Labels
                    if (labels.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No Shipping Labels generated yet", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Pick an existing invoice or tap '+' to instantly create a barcode shipping sticker.", color = PharmaTextSecondary, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { selectedTab = 1 },
                                    colors = ButtonDefaults.buttonColors(containerColor = VedaGreen)
                                ) {
                                    Text("Pick from Invoices")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(labels, key = { it.id }) { label ->
                                ShippingLabelCard(
                                    label = label,
                                    company = company,
                                    onPreview = {
                                        activeLabelForPreview = label
                                        selectedTab = 2
                                    },
                                    onEdit = {
                                        labelToEdit = label
                                        showEditDialog = true
                                    },
                                    onDelete = {
                                        viewModel.deleteShippingLabel(label)
                                    },
                                    onShareWhatsApp = {
                                        if (activity != null) {
                                            val html = ShippingLabelHtmlGenerator.generateShippingLabelHtml(label, company)
                                            ShippingLabelPrintHelper.shareToWhatsApp(
                                                activity = activity,
                                                htmlContent = html,
                                                labelNumber = label.shippingNumber,
                                                customerName = label.customerName,
                                                phoneNumber = label.mobileNumber
                                            )
                                        }
                                    },
                                    onPrint = {
                                        if (activity != null) {
                                            val html = ShippingLabelHtmlGenerator.generateShippingLabelHtml(label, company)
                                            ShippingLabelPrintHelper.printShippingLabel(
                                                activity = activity,
                                                htmlContent = html,
                                                labelNumber = label.shippingNumber,
                                                labelSize = label.labelSize
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Select an existing Invoice or Order to auto-generate label
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                "Tax Invoices Ready for Dispatch",
                                fontWeight = FontWeight.Bold,
                                color = VedaGreenDark,
                                fontSize = 14.sp
                            )
                        }

                        if (invoices.isEmpty()) {
                            item {
                                Text("No invoices available in database.", color = Color.Gray, fontSize = 12.sp)
                            }
                        } else {
                            items(invoices) { inv ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, PharmaBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(inv.partyName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Inv: ${inv.invoiceNumber} • Date: ${inv.invoiceDate}", fontSize = 11.sp, color = PharmaTextSecondary)
                                            Text("Total: ₹${String.format(Locale.US, "%.2f", inv.grandTotal)} • Mode: ${inv.paymentMode}", fontSize = 11.sp, color = VedaGreen)
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.createLabelFromInvoice(inv) { newId ->
                                                    selectedTab = 0
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = VedaGreen)
                                        ) {
                                            Text("Generate Label", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }

                        if (fieldOrders.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "Field Sales Orders Ready for Dispatch",
                                    fontWeight = FontWeight.Bold,
                                    color = VedaGreenDark,
                                    fontSize = 14.sp
                                )
                            }

                            items(fieldOrders) { order ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, PharmaBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(order.customerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Order: ${order.orderNumber} • ${order.clinicOrPharmacyName}", fontSize = 11.sp, color = PharmaTextSecondary)
                                            Text("Value: ₹${String.format(Locale.US, "%.2f", order.finalPurchaseValue)} • ${order.paymentStatus}", fontSize = 11.sp, color = VedaGreen)
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.createLabelFromFieldOrder(order) {
                                                    selectedTab = 0
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                                        ) {
                                            Text("Generate Label", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Live Shipping Label Preview
                    if (activeLabelForPreview != null) {
                        val activeLabel = activeLabelForPreview!!
                        val html = remember(activeLabel, company) {
                            ShippingLabelHtmlGenerator.generateShippingLabelHtml(activeLabel, company)
                        }

                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "AWB: ${activeLabel.shippingNumber}",
                                    fontWeight = FontWeight.Bold,
                                    color = VedaGreenDark
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            if (activity != null) {
                                                ShippingLabelPrintHelper.shareToWhatsApp(
                                                    activity = activity,
                                                    htmlContent = html,
                                                    labelNumber = activeLabel.shippingNumber,
                                                    customerName = activeLabel.customerName,
                                                    phoneNumber = activeLabel.mobileNumber
                                                )
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("WhatsApp", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            if (activity != null) {
                                                ShippingLabelPrintHelper.printShippingLabel(
                                                    activity = activity,
                                                    htmlContent = html,
                                                    labelNumber = activeLabel.shippingNumber,
                                                    labelSize = activeLabel.labelSize
                                                )
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = VedaGreen)
                                    ) {
                                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Print 4x6", fontSize = 12.sp)
                                    }
                                }
                            }

                            // Software-rendered WebView to guarantee no crash in emulator
                            AndroidView(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("shipping_label_webview"),
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                                        settings.apply {
                                            javaScriptEnabled = true
                                            loadWithOverviewMode = true
                                            useWideViewPort = true
                                            setSupportZoom(true)
                                            builtInZoomControls = true
                                            displayZoomControls = false
                                        }
                                        webViewClient = object : WebViewClient() {
                                            override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean = true
                                        }
                                        loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null)
                                    }
                                },
                                update = { view ->
                                    view.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Shipping Label Editor / Customizer Dialog
    if (showEditDialog && labelToEdit != null) {
        val label = labelToEdit!!
        var custName by remember { mutableStateOf(label.customerName) }
        var clinicName by remember { mutableStateOf(label.clinicOrPharmacyName) }
        var addr by remember { mutableStateOf(label.address) }
        var city by remember { mutableStateOf(label.cityOrDistrict) }
        var state by remember { mutableStateOf(label.state) }
        var pin by remember { mutableStateOf(label.pinCode) }
        var phone by remember { mutableStateOf(label.mobileNumber) }
        var codAmt by remember { mutableStateOf(label.codAmount.toString()) }
        var isCod by remember { mutableStateOf(label.isCod) }
        var courier by remember { mutableStateOf(label.courierPartner) }
        var weight by remember { mutableStateOf(label.packageWeightKg.toString()) }
        var boxes by remember { mutableStateOf(label.numberOfBoxes.toString()) }
        var size by remember { mutableStateOf(label.labelSize) }
        var handling by remember { mutableStateOf(label.handlingInstructions) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = if (label.id == 0L) "New Shipping Label" else "Edit Shipping Label ${label.shippingNumber}",
                    fontWeight = FontWeight.Bold,
                    color = VedaGreenDark
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = custName,
                        onValueChange = { custName = it },
                        label = { Text("Customer Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = clinicName,
                        onValueChange = { clinicName = it },
                        label = { Text("Clinic / Pharmacy Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = addr,
                        onValueChange = { addr = it },
                        label = { Text("Complete Delivery Address *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City / District") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = state,
                            onValueChange = { state = it },
                            label = { Text("State") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = pin,
                            onValueChange = { pin = it },
                            label = { Text("PIN Code *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Mobile Number *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !isCod,
                            onClick = { isCod = false },
                            label = { Text("Prepaid / Billed") }
                        )
                        FilterChip(
                            selected = isCod,
                            onClick = { isCod = true },
                            label = { Text("Cash on Delivery (COD)") }
                        )
                    }
                    if (isCod) {
                        OutlinedTextField(
                            value = codAmt,
                            onValueChange = { codAmt = it },
                            label = { Text("COD Amount (₹)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    OutlinedTextField(
                        value = courier,
                        onValueChange = { courier = it },
                        label = { Text("Courier / Transport Partner") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            label = { Text("Weight (KG)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = boxes,
                            onValueChange = { boxes = it },
                            label = { Text("Boxes / Pcs") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Text("Label Size / Format", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PharmaTextPrimary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = size == "4x6_INCH",
                            onClick = { size = "4x6_INCH" },
                            label = { Text("4x6\" Sticker") }
                        )
                        FilterChip(
                            selected = size == "100x150_MM",
                            onClick = { size = "100x150_MM" },
                            label = { Text("100x150mm") }
                        )
                        FilterChip(
                            selected = size == "A4_SHEET",
                            onClick = { size = "A4_SHEET" },
                            label = { Text("A4 Sheet") }
                        )
                    }

                    OutlinedTextField(
                        value = handling,
                        onValueChange = { handling = it },
                        label = { Text("Handling Instructions") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalShpNum = if (label.shippingNumber == "VEDA-SHP-AUTO" || label.shippingNumber.isBlank()) {
                            "VEDA-SHP-2024-${System.currentTimeMillis() % 1000000}"
                        } else label.shippingNumber

                        val updated = label.copy(
                            shippingNumber = finalShpNum,
                            customerName = custName,
                            clinicOrPharmacyName = clinicName,
                            address = addr,
                            cityOrDistrict = city,
                            state = state,
                            pinCode = pin,
                            mobileNumber = phone,
                            isCod = isCod,
                            codAmount = codAmt.toDoubleOrNull() ?: 0.0,
                            courierPartner = courier,
                            packageWeightKg = weight.toDoubleOrNull() ?: 1.0,
                            numberOfBoxes = boxes.toIntOrNull() ?: 1,
                            labelSize = size,
                            handlingInstructions = handling
                        )
                        viewModel.saveShippingLabel(updated) {
                            showEditDialog = false
                            activeLabelForPreview = updated
                            selectedTab = 2
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VedaGreen)
                ) {
                    Text("Save & Render")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ShippingLabelCard(
    label: ShippingLabel,
    company: CompanyProfile,
    onPreview: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShareWhatsApp: () -> Unit,
    onPrint: () -> Unit
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = null,
                        tint = VedaGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(label.shippingNumber, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = VedaGreenDark)
                }

                if (label.isCod || label.codAmount > 0.0) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFEF2F2), RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("COD: ₹${String.format(Locale.US, "%.2f", label.codAmount)}", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFECFDF5), RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFF6EE7B7), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("PREPAID", color = Color(0xFF059669), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(label.customerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            if (label.clinicOrPharmacyName.isNotBlank() && label.clinicOrPharmacyName != label.customerName) {
                Text(label.clinicOrPharmacyName, color = Color(0xFF1E3A8A), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Text("${label.address}, ${label.cityOrDistrict}, ${label.state} - PIN: ${label.pinCode}", fontSize = 12.sp, color = PharmaTextSecondary)
            Text("Phone: ${label.mobileNumber} • Courier: ${label.courierPartner}", fontSize = 11.sp, color = PharmaTextPrimary)

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onShareWhatsApp,
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", fontSize = 11.sp, color = Color(0xFF25D366))
                    }

                    OutlinedButton(
                        onClick = onPrint,
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Print", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onPreview,
                        colors = ButtonDefaults.buttonColors(containerColor = VedaGreen),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Preview", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
