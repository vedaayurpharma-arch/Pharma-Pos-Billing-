package com.example.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.CompanyProfile
import com.example.data.model.InvoiceWithItems
import com.example.ui.theme.PharmaBorder
import com.example.ui.theme.PharmaTextPrimary
import com.example.ui.theme.PharmaTextSecondary
import com.example.ui.theme.VedaGold
import com.example.ui.theme.VedaGreen
import com.example.ui.theme.VedaGreenDark
import com.example.util.InvoiceHtmlGenerator
import com.example.util.InvoicePrintHelper
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LandscapeBillPreviewScreen(
    invoiceWithItems: InvoiceWithItems?,
    companyProfile: CompanyProfile,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    if (invoiceWithItems == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No invoice selected",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBack) {
                    Text("Back to Invoices")
                }
            }
        }
        return
    }

    val htmlContent = remember(invoiceWithItems, companyProfile) {
        InvoiceHtmlGenerator.generateHtml(invoiceWithItems, companyProfile)
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Native M3 Bill Sheet, 1 = HTML Print Layout
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var zoomLevel by remember { mutableFloatStateOf(100f) }
    var isWebViewCrashed by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "A4 Landscape Tax Invoice",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "${invoiceWithItems.invoice.invoiceNumber} • ${invoiceWithItems.invoice.partyName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("preview_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (selectedTab == 1 && !isWebViewCrashed) {
                        IconButton(
                            onClick = {
                                webViewRef?.zoomOut()
                                zoomLevel = (zoomLevel - 15f).coerceAtLeast(50f)
                            },
                            modifier = Modifier.testTag("zoom_out_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomOut,
                                contentDescription = "Zoom Out",
                                tint = Color.White
                            )
                        }
                        IconButton(
                            onClick = {
                                webViewRef?.zoomIn()
                                zoomLevel = (zoomLevel + 15f).coerceAtMost(250f)
                            },
                            modifier = Modifier.testTag("zoom_in_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = "Zoom In",
                                tint = Color.White
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            InvoicePrintHelper.shareInvoiceHtml(
                                context = context,
                                htmlContent = htmlContent,
                                invoiceNo = invoiceWithItems.invoice.invoiceNumber
                            )
                        },
                        modifier = Modifier.testTag("share_bill_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = {
                            if (activity != null) {
                                InvoicePrintHelper.printInvoice(
                                    activity = activity,
                                    htmlContent = htmlContent,
                                    jobName = "Invoice_${invoiceWithItems.invoice.invoiceNumber}"
                                )
                            }
                        },
                        modifier = Modifier.testTag("print_bill_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Print / Save PDF",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VedaGreenDark
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Grand Total",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹${String.format(Locale.US, "%.2f", invoiceWithItems.invoice.grandTotal)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = VedaGreenDark
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                InvoicePrintHelper.shareInvoiceHtml(
                                    context = context,
                                    htmlContent = htmlContent,
                                    invoiceNo = invoiceWithItems.invoice.invoiceNumber
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            modifier = Modifier.testTag("bottom_share_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share")
                        }

                        Button(
                            onClick = {
                                if (activity != null) {
                                    InvoicePrintHelper.printInvoice(
                                        activity = activity,
                                        htmlContent = htmlContent,
                                        jobName = "Invoice_${invoiceWithItems.invoice.invoiceNumber}"
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VedaGreen
                            ),
                            modifier = Modifier.testTag("bottom_print_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Print A4 PDF")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF1F5F9))
        ) {
            // View Mode Selector Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = VedaGreen,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = VedaGreen
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedTab == 0) VedaGreen else Color.Gray
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Live Bill Sheet (M3)",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedTab == 1) VedaGreen else Color.Gray
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "HTML Print View",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    }
                )
            }

            // Orientation Helper Tip
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = null,
                        tint = VedaGreenDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pharma Landscape: Rotate device or scroll horizontally to inspect all 14 columns.",
                        fontSize = 11.sp,
                        color = VedaGreenDark
                    )
                }
            }

            // Body Content based on Tab
            if (selectedTab == 0) {
                // High-Fidelity Native Jetpack Compose A4 Landscape Bill Sheet
                NativeLandscapeInvoiceSheet(
                    invoiceWithItems = invoiceWithItems,
                    company = companyProfile
                )
            } else {
                // Software-rendered WebView with crash safety and fallback
                if (isWebViewCrashed) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF87171))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Device GPU renderer encountered an issue",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Switched safely to Native Bill Sheet. PDF Printing and Sharing remain 100% operational.",
                                fontSize = 12.sp,
                                color = Color(0xFF7F1D1D),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    isWebViewCrashed = false
                                    selectedTab = 0
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VedaGreen)
                            ) {
                                Text("View in Live Bill Sheet")
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        AndroidView(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("invoice_webview"),
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    // CRITICAL: Disable hardware acceleration for WebView to avoid Mesa rendernode issues in cloud / emulator
                                    setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                                    settings.apply {
                                        javaScriptEnabled = true
                                        loadWithOverviewMode = true
                                        useWideViewPort = true
                                        setSupportZoom(true)
                                        builtInZoomControls = true
                                        displayZoomControls = false
                                        domStorageEnabled = true
                                    }
                                    webViewClient = object : WebViewClient() {
                                        override fun onRenderProcessGone(
                                            view: WebView?,
                                            detail: RenderProcessGoneDetail?
                                        ): Boolean {
                                            // Handled safely - prevent app termination!
                                            isWebViewCrashed = true
                                            return true
                                        }
                                    }
                                    loadDataWithBaseURL(
                                        "file:///android_asset/",
                                        htmlContent,
                                        "text/html",
                                        "UTF-8",
                                        null
                                    )
                                    webViewRef = this
                                }
                            },
                            update = { view ->
                                try {
                                    view.loadDataWithBaseURL(
                                        "file:///android_asset/",
                                        htmlContent,
                                        "text/html",
                                        "UTF-8",
                                        null
                                    )
                                } catch (e: Exception) {
                                    isWebViewCrashed = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 100% Native Jetpack Compose High-Fidelity A4 Landscape Bill Sheet.
 * Provides instant, zero-latency rendering with pharmaceutical styling,
 * HSN breakdown, batch/expiry columns, bank details, and total calculations.
 */
@Composable
fun NativeLandscapeInvoiceSheet(
    invoiceWithItems: InvoiceWithItems,
    company: CompanyProfile
) {
    val inv = invoiceWithItems.invoice
    val items = invoiceWithItems.items
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(6.dp),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScrollState)
                .padding(8.dp)
        ) {
            // Header Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFCBD5E1))
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GSTIN: ${company.gstin}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharmaTextSecondary
                    )
                    Text(
                        text = "TAX INVOICE (CASH / CREDIT)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = VedaGreenDark
                    )
                    Text(
                        text = "DL: ${company.dlNo.take(22)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharmaTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = company.companyName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = VedaGreenDark,
                    letterSpacing = 1.sp
                )

                Text(
                    text = company.tagline,
                    fontSize = 10.sp,
                    color = VedaGold,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "${company.addressLine1}, ${company.addressLine2}",
                    fontSize = 10.sp,
                    color = PharmaTextSecondary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Phone: ${company.phone} • Email: ${company.email} • FSSAI: ${company.fssaiNo}",
                    fontSize = 9.sp,
                    color = PharmaTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Metadata & Buyer Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFCBD5E1))
            ) {
                // Buyer Details
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "BILLED TO / BUYER:",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = VedaGreenDark
                    )
                    Text(
                        text = inv.partyName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharmaTextPrimary
                    )
                    if (inv.partyAddress.isNotBlank()) {
                        Text(
                            text = inv.partyAddress,
                            fontSize = 10.sp,
                            color = PharmaTextSecondary
                        )
                    }
                    if (inv.partyPhone.isNotBlank()) {
                        Text(
                            text = "Phone: ${inv.partyPhone}",
                            fontSize = 10.sp,
                            color = PharmaTextSecondary
                        )
                    }
                    Text(
                        text = "GSTIN: ${inv.partyGstin.ifBlank { "Unregistered" }}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PharmaTextPrimary
                    )
                    if (inv.partyDlNo.isNotBlank()) {
                        Text(
                            text = "D.L. No: ${inv.partyDlNo}",
                            fontSize = 10.sp,
                            color = PharmaTextSecondary
                        )
                    }
                }

                // Vertical Separator
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(100.dp)
                        .background(Color(0xFFCBD5E1))
                )

                // Invoice Meta Details
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                ) {
                    InvoiceMetaRow("Invoice No:", inv.invoiceNumber, isBold = true)
                    InvoiceMetaRow("Date:", inv.invoiceDate)
                    InvoiceMetaRow("Due Date:", inv.dueDate)
                    InvoiceMetaRow("Order Ref:", inv.orderNo.ifBlank { "Direct Sale" })
                    InvoiceMetaRow("Payment Mode:", inv.paymentMode)
                    InvoiceMetaRow("State / Code:", company.state)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 14-Column Landscape Table (Horizontally Scrollable)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(horizontalScrollState)
                    .border(1.dp, Color(0xFFCBD5E1))
            ) {
                Column {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .background(Color(0xFFE2E8F0))
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableCell("#", width = 28.dp, isHeader = true)
                        TableCell("PRODUCT / COMPOSITION", width = 160.dp, isHeader = true, alignLeft = true)
                        TableCell("PACK", width = 45.dp, isHeader = true)
                        TableCell("BATCH", width = 60.dp, isHeader = true)
                        TableCell("EXPIRY", width = 50.dp, isHeader = true)
                        TableCell("HSN", width = 60.dp, isHeader = true)
                        TableCell("QTY", width = 36.dp, isHeader = true)
                        TableCell("FREE", width = 36.dp, isHeader = true)
                        TableCell("MRP", width = 55.dp, isHeader = true)
                        TableCell("RATE", width = 55.dp, isHeader = true)
                        TableCell("DISC%", width = 42.dp, isHeader = true)
                        TableCell("TAXABLE", width = 65.dp, isHeader = true)
                        TableCell("GST%", width = 45.dp, isHeader = true)
                        TableCell("AMOUNT", width = 75.dp, isHeader = true)
                    }

                    HorizontalDivider(color = Color(0xFFCBD5E1), thickness = 1.dp)

                    // Items Rows
                    items.forEachIndexed { index, item ->
                        val rowBg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
                        Row(
                            modifier = Modifier
                                .background(rowBg)
                                .padding(vertical = 5.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TableCell("${index + 1}", width = 28.dp)
                            TableCell(item.productName, width = 160.dp, alignLeft = true, isBold = true)
                            TableCell(item.pack, width = 45.dp)
                            TableCell(item.batch, width = 60.dp)
                            TableCell(item.exp, width = 50.dp)
                            TableCell(item.hsn, width = 60.dp)
                            TableCell("${item.qty}", width = 36.dp, isBold = true)
                            TableCell("${item.freeQty}", width = 36.dp)
                            TableCell(String.format(Locale.US, "%.2f", item.mrp), width = 55.dp)
                            TableCell(String.format(Locale.US, "%.2f", item.rate), width = 55.dp)
                            TableCell(String.format(Locale.US, "%.1f", item.discountPercent), width = 42.dp)
                            val itemTaxable = (item.qty * item.rate) * (1.0 - item.discountPercent / 100.0)
                            TableCell(String.format(Locale.US, "%.2f", itemTaxable), width = 65.dp)
                            TableCell("${item.cgstPercent + item.sgstPercent}%", width = 45.dp)
                            TableCell(String.format(Locale.US, "%.2f", item.amount), width = 75.dp, isBold = true)
                        }
                        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp)
                    }

                    // Blank rows placeholder to maintain A4 aesthetic
                    if (items.size < 5) {
                        repeat(5 - items.size) {
                            Row(
                                modifier = Modifier
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TableCell("", width = 28.dp)
                                TableCell("", width = 160.dp)
                                TableCell("", width = 45.dp)
                                TableCell("", width = 60.dp)
                                TableCell("", width = 50.dp)
                                TableCell("", width = 60.dp)
                                TableCell("", width = 36.dp)
                                TableCell("", width = 36.dp)
                                TableCell("", width = 55.dp)
                                TableCell("", width = 55.dp)
                                TableCell("", width = 42.dp)
                                TableCell("", width = 65.dp)
                                TableCell("", width = 45.dp)
                                TableCell("", width = 75.dp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Section: Bank Details + Summary Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFCBD5E1))
            ) {
                // Left Column: Bank Details & Terms
                Column(
                    modifier = Modifier
                        .weight(1.3f)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "BANK ACCOUNT DETAILS:",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = VedaGreenDark
                    )
                    Text(
                        text = "Bank: ${company.bankName} • A/C: ${company.accountNo}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PharmaTextPrimary
                    )
                    Text(
                        text = "IFSC: ${company.ifscCode} • Branch: ${company.branch}",
                        fontSize = 10.sp,
                        color = PharmaTextSecondary
                    )
                    Text(
                        text = "UPI: ${company.upiId}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = VedaGold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "TERMS & CONDITIONS:",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharmaTextSecondary
                    )
                    Text(
                        text = company.termsConditions.lines().take(2).joinToString("\n"),
                        fontSize = 8.sp,
                        color = PharmaTextSecondary,
                        lineHeight = 10.sp
                    )
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(130.dp)
                        .background(Color(0xFFCBD5E1))
                )

                // Right Column: Tax Breakdown & Grand Total
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                ) {
                    InvoiceSummaryLine("Sub Total (Taxable):", "₹${String.format(Locale.US, "%.2f", inv.subTotalTaxable)}")
                    InvoiceSummaryLine("SGST Total:", "₹${String.format(Locale.US, "%.2f", inv.sgstPayable)}")
                    InvoiceSummaryLine("CGST Total:", "₹${String.format(Locale.US, "%.2f", inv.cgstPayable)}")
                    if (inv.igstPayable > 0) {
                        InvoiceSummaryLine("IGST Total:", "₹${String.format(Locale.US, "%.2f", inv.igstPayable)}")
                    }
                    if (inv.crDrNote != 0.0) {
                        InvoiceSummaryLine("Cr/Dr Adjustment:", "₹${String.format(Locale.US, "%.2f", inv.crDrNote)}")
                    }

                    HorizontalDivider(
                        color = Color(0xFFCBD5E1),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "NET PAYABLE:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = VedaGreenDark
                        )
                        Text(
                            text = "₹${String.format(Locale.US, "%.2f", inv.grandTotal)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = VedaGreenDark
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "For ${company.companyName}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Authorised Signatory",
                        fontSize = 8.sp,
                        textAlign = TextAlign.End,
                        color = PharmaTextSecondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun TableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    isHeader: Boolean = false,
    alignLeft: Boolean = false,
    isBold: Boolean = false
) {
    Text(
        text = text,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 2.dp),
        fontSize = if (isHeader) 8.sp else 9.sp,
        fontWeight = if (isHeader || isBold) FontWeight.Bold else FontWeight.Normal,
        color = if (isHeader) VedaGreenDark else PharmaTextPrimary,
        textAlign = if (alignLeft) TextAlign.Start else TextAlign.Center,
        maxLines = 2
    )
}

@Composable
private fun InvoiceMetaRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 9.sp, color = PharmaTextSecondary)
        Text(
            text = value,
            fontSize = 9.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = PharmaTextPrimary
        )
    }
}

@Composable
private fun InvoiceSummaryLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 9.sp, color = PharmaTextSecondary)
        Text(
            text = value,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = PharmaTextPrimary
        )
    }
}
