package com.example.ui.screens

import android.view.View
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.CompanyProfile
import com.example.data.model.InvoiceDesignerConfig
import com.example.data.model.InvoiceWithItems
import com.example.ui.theme.PharmaBg
import com.example.ui.theme.PharmaBorder
import com.example.ui.theme.PharmaTextPrimary
import com.example.ui.theme.PharmaTextSecondary
import com.example.ui.theme.VedaEmerald
import com.example.ui.theme.VedaGold
import com.example.ui.theme.VedaGreen
import com.example.ui.theme.VedaGreenDark
import com.example.util.InvoiceHtmlGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDesignerScreen(
    currentConfig: InvoiceDesignerConfig,
    company: CompanyProfile,
    sampleInvoice: InvoiceWithItems?,
    onBack: () -> Unit,
    onSaveConfig: (InvoiceDesignerConfig) -> Unit,
    onApplyPreset: (String) -> Unit,
    onResetDefault: () -> Unit
) {
    var config by remember(currentConfig) { mutableStateOf(currentConfig) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Live Preview, 1: Field Toggles, 2: 20+ Template Gallery

    val templates = listOf(
        "Veda Pharma A4 Landscape",
        "Veda Classic A4 Portrait",
        "Veda Premium Gold",
        "Veda Wholesale Distributor",
        "Veda Retail Tax Invoice",
        "Veda Ayurvedic Heritage",
        "Veda Modern Teal",
        "Thermal 80mm POS",
        "Thermal 58mm Mini",
        "Veda Minimal Clean",
        "Veda Compact GST",
        "Veda Proforma",
        "Veda Delivery Challan",
        "Veda Credit Note / Return",
        "Veda Export Format",
        "Veda Hospital / Clinic Dispensing",
        "Veda State Tax Form",
        "Veda Double Column",
        "Veda Borderless Modern",
        "Veda Dark Accents"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PharmaBg)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Configurable Invoice Designer",
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
                    config = InvoiceDesignerConfig()
                    onResetDefault()
                }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Defaults",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = VedaGreenDark
            )
        )

        // Presets Chips Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Presets:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PharmaTextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            listOf("Default", "Retail", "Wholesale", "Thermal").forEach { preset ->
                item {
                    FilterChip(
                        selected = (preset == "Default" && config.selectedTemplate.contains("Landscape")) ||
                                (preset == "Retail" && config.selectedTemplate.contains("Retail")) ||
                                (preset == "Wholesale" && config.selectedTemplate.contains("Wholesale")) ||
                                (preset == "Thermal" && config.selectedTemplate.contains("Thermal")),
                        onClick = { onApplyPreset(preset) },
                        label = { Text(preset) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VedaGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = VedaGreen
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Live Preview") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Show / Hide Fields") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Template Gallery (20)") })
        }

        when (selectedTab) {
            0 -> {
                // Live HTML Preview
                if (sampleInvoice != null) {
                    val htmlContent = remember(config, company, sampleInvoice) {
                        InvoiceHtmlGenerator.generateHtml(sampleInvoice, company, config)
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE8F5E9))
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Active Template: ${config.selectedTemplate}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = VedaGreen
                            )
                            Button(
                                onClick = { onSaveConfig(config) },
                                colors = ButtonDefaults.buttonColors(containerColor = VedaGreen),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Save Configuration", fontSize = 11.sp)
                            }
                        }

                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                                    settings.apply {
                                        javaScriptEnabled = true
                                        useWideViewPort = true
                                        loadWithOverviewMode = true
                                        builtInZoomControls = true
                                        displayZoomControls = false
                                        defaultFontSize = 10
                                    }
                                    webViewClient = object : WebViewClient() {
                                        override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                                            return true
                                        }
                                    }
                                }
                            },
                            update = { webView ->
                                webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("designer_live_preview_webview")
                        )
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No sample invoice available for preview.")
                    }
                }
            }

            1 -> {
                // Show / Hide Field Toggles
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "CUSTOMIZE BILL COLUMNS & SECTIONS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmaTextSecondary
                        )
                    }

                    item {
                        DesignerToggleCard(
                            title = "Customer GSTIN",
                            subtitle = "Display buyer 15-digit GST identification number",
                            checked = config.showCustomerGstin,
                            onCheckedChange = {
                                config = config.copy(showCustomerGstin = it)
                                onSaveConfig(config)
                            }
                        )
                    }

                    item {
                        DesignerToggleCard(
                            title = "Customer Drug Licence (D.L. No.)",
                            subtitle = "Show Chemist 20B/21B retail/wholesale licence",
                            checked = config.showCustomerDl,
                            onCheckedChange = {
                                config = config.copy(showCustomerDl = it)
                                onSaveConfig(config)
                            }
                        )
                    }

                    item {
                        DesignerToggleCard(
                            title = "HSN Code Column",
                            subtitle = "Display 8-digit pharma HSN code for each medicine",
                            checked = config.showHsn,
                            onCheckedChange = {
                                config = config.copy(showHsn = it)
                                onSaveConfig(config)
                            }
                        )
                    }

                    item {
                        DesignerToggleCard(
                            title = "Batch Number Column",
                            subtitle = "Essential for drug batch tracking and recalls",
                            checked = config.showBatch,
                            onCheckedChange = {
                                config = config.copy(showBatch = it)
                                onSaveConfig(config)
                            }
                        )
                    }

                    item {
                        DesignerToggleCard(
                            title = "Expiry Date Column",
                            subtitle = "Shows MM/YY expiration on bill printouts",
                            checked = config.showExpiry,
                            onCheckedChange = {
                                config = config.copy(showExpiry = it)
                                onSaveConfig(config)
                            }
                        )
                    }

                    item {
                        DesignerToggleCard(
                            title = "M.R.P Column",
                            subtitle = "Display Maximum Retail Price alongside billing rate",
                            checked = config.showMrp,
                            onCheckedChange = {
                                config = config.copy(showMrp = it)
                                onSaveConfig(config)
                            }
                        )
                    }

                    item {
                        DesignerToggleCard(
                            title = "Discount % Column",
                            subtitle = "Display item level trade/scheme discount percentage",
                            checked = config.showDiscount,
                            onCheckedChange = {
                                config = config.copy(showDiscount = it)
                                onSaveConfig(config)
                            }
                        )
                    }

                    item {
                        DesignerToggleCard(
                            title = "CGST / SGST Columns",
                            subtitle = "Detailed break-up of state and central tax rates",
                            checked = config.showCgstSgst,
                            onCheckedChange = {
                                config = config.copy(showCgstSgst = it)
                                onSaveConfig(config)
                            }
                        )
                    }

                    item {
                        DesignerToggleCard(
                            title = "UPI Dynamic QR Code",
                            subtitle = "Print instant scan-and-pay UPI code with exact bill amount",
                            checked = config.showUpiQr,
                            onCheckedChange = {
                                config = config.copy(showUpiQr = it)
                                onSaveConfig(config)
                            }
                        )
                    }

                    item {
                        DesignerToggleCard(
                            title = "GST e-Invoice IRN & Signed QR",
                            subtitle = "Print mandatory government B2B e-invoice barcode",
                            checked = config.showEInvoiceQr,
                            onCheckedChange = {
                                config = config.copy(showEInvoiceQr = it)
                                onSaveConfig(config)
                            }
                        )
                    }

                    item {
                        DesignerToggleCard(
                            title = "Bank Account & IFSC Details",
                            subtitle = "Bank details at the footer for RTGS / NEFT settlement",
                            checked = config.showBankDetails,
                            onCheckedChange = {
                                config = config.copy(showBankDetails = it)
                                onSaveConfig(config)
                            }
                        )
                    }

                    item {
                        DesignerToggleCard(
                            title = "Terms & Conditions",
                            subtitle = "Pharma credit return policy, interest rules & jurisdiction",
                            checked = config.showTerms,
                            onCheckedChange = {
                                config = config.copy(showTerms = it)
                                onSaveConfig(config)
                            }
                        )
                    }

                    item {
                        DesignerToggleCard(
                            title = "Authorised Signatory Section",
                            subtitle = "Show signature endorsement box for cashier/proprietor",
                            checked = config.showSignature,
                            onCheckedChange = {
                                config = config.copy(showSignature = it)
                                onSaveConfig(config)
                            }
                        )
                    }
                }
            }

            2 -> {
                // 20+ Template Gallery
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "20+ PROFESSIONAL PHARMA INVOICE TEMPLATES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmaTextSecondary
                        )
                        Text(
                            text = "Choose landscape, portrait, gold heritage, distributor, or thermal POS formats.",
                            fontSize = 11.sp,
                            color = PharmaTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    items(templates) { template ->
                        val isSelected = config.selectedTemplate.equals(template, ignoreCase = true)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val isThermal = template.contains("Thermal", ignoreCase = true)
                                    val isPortrait = template.contains("Portrait", ignoreCase = true)
                                    val colorHex = when {
                                        template.contains("Gold", ignoreCase = true) -> "#996515"
                                        template.contains("Teal", ignoreCase = true) -> "#00695c"
                                        template.contains("Wholesale", ignoreCase = true) -> "#1565c0"
                                        template.contains("Ayurvedic", ignoreCase = true) -> "#1e5128"
                                        else -> "#0d5c3a"
                                    }
                                    config = config.copy(
                                        selectedTemplate = template,
                                        paperOrientation = if (isThermal) "THERMAL" else if (isPortrait) "PORTRAIT" else "LANDSCAPE",
                                        primaryColorHex = colorHex
                                    )
                                    onSaveConfig(config)
                                    selectedTab = 0 // Switch to preview
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFE8F5E9) else Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) VedaGreen else PharmaBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = template,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) VedaGreenDark else PharmaTextPrimary
                                    )
                                    Text(
                                        text = when {
                                            template.contains("Landscape") -> "A4 Landscape • Marg ERP Standard Format"
                                            template.contains("Thermal") -> "POS Roll Print • Fast Counter Billing"
                                            template.contains("Wholesale") -> "Full B2B with DL, IRN & Delivery Details"
                                            template.contains("Ayurvedic") -> "Ayurvedic Heritage with Herbal Border Accents"
                                            else -> "High-contrast Clean GST Tax Invoice"
                                        },
                                        fontSize = 11.sp,
                                        color = PharmaTextSecondary
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = VedaGreen
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

@Composable
fun DesignerToggleCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, PharmaBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PharmaTextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 10.5.sp,
                    color = PharmaTextSecondary
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = VedaGreen
                )
            )
        }
    }
}
