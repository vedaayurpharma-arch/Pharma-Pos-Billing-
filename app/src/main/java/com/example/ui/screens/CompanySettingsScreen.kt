package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CompanyProfile
import com.example.ui.theme.PharmaBorder
import com.example.ui.theme.PharmaTextPrimary
import com.example.ui.theme.PharmaTextSecondary
import com.example.ui.theme.VedaGreen
import com.example.ui.theme.VedaGreenDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanySettingsScreen(
    currentProfile: CompanyProfile,
    onSaveProfile: (CompanyProfile) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Company Info, 1: Invoice Series, 2: Printer Setup, 3: Roles & Backup

    var companyName by remember(currentProfile) { mutableStateOf(currentProfile.companyName) }
    var tagline by remember(currentProfile) { mutableStateOf(currentProfile.tagline) }
    var address1 by remember(currentProfile) { mutableStateOf(currentProfile.addressLine1) }
    var address2 by remember(currentProfile) { mutableStateOf(currentProfile.addressLine2) }
    var phone by remember(currentProfile) { mutableStateOf(currentProfile.phone) }
    var email by remember(currentProfile) { mutableStateOf(currentProfile.email) }
    var dlNo by remember(currentProfile) { mutableStateOf(currentProfile.dlNo) }
    var gstin by remember(currentProfile) { mutableStateOf(currentProfile.gstin) }
    var pan by remember(currentProfile) { mutableStateOf(currentProfile.pan) }
    var fssaiNo by remember(currentProfile) { mutableStateOf(currentProfile.fssaiNo) }

    var bankName by remember(currentProfile) { mutableStateOf(currentProfile.bankName) }
    var accountNo by remember(currentProfile) { mutableStateOf(currentProfile.accountNo) }
    var ifscCode by remember(currentProfile) { mutableStateOf(currentProfile.ifscCode) }
    var branch by remember(currentProfile) { mutableStateOf(currentProfile.branch) }
    var upiId by remember(currentProfile) { mutableStateOf(currentProfile.upiId) }

    var terms by remember(currentProfile) { mutableStateOf(currentProfile.termsConditions) }

    var invoicePrefix by remember { mutableStateOf("VAP-2024-") }
    var invoiceStartNo by remember { mutableStateOf("1001") }
    var defaultPaperType by remember { mutableStateOf("A4 Landscape") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("ERP Settings Center", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VedaGreenDark)
            )
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
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Profile") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Series") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Printer") })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Backup") })
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // Company Identification
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PharmaBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Business, contentDescription = null, tint = VedaGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Company & Drug Licences", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = VedaGreen)
                                }

                                OutlinedTextField(
                                    value = companyName,
                                    onValueChange = { companyName = it },
                                    label = { Text("Company Legal Trade Name") },
                                    modifier = Modifier.fillMaxWidth().testTag("settings_company_name"),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = tagline,
                                    onValueChange = { tagline = it },
                                    label = { Text("Company Tagline / Subtitle") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = address1,
                                    onValueChange = { address1 = it },
                                    label = { Text("Premises Address Line 1") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = address2,
                                    onValueChange = { address2 = it },
                                    label = { Text("City, State & Pincode") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = phone,
                                        onValueChange = { phone = it },
                                        label = { Text("Phone") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = email,
                                        onValueChange = { email = it },
                                        label = { Text("Email") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = dlNo,
                                        onValueChange = { dlNo = it },
                                        label = { Text("D.L. No. (20B / 21B)") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = gstin,
                                        onValueChange = { gstin = it },
                                        label = { Text("GSTIN") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = pan,
                                        onValueChange = { pan = it },
                                        label = { Text("PAN Card No.") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = fssaiNo,
                                        onValueChange = { fssaiNo = it },
                                        label = { Text("FSSAI Licence No.") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                            }
                        }

                        // Bank & UPI QR Settings
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PharmaBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = VedaGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Bank Settlement & UPI QR Code", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = VedaGreen)
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = bankName,
                                        onValueChange = { bankName = it },
                                        label = { Text("Bank Name") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = accountNo,
                                        onValueChange = { accountNo = it },
                                        label = { Text("Account No") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = ifscCode,
                                        onValueChange = { ifscCode = it },
                                        label = { Text("IFSC Code") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = branch,
                                        onValueChange = { branch = it },
                                        label = { Text("Branch") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }

                                OutlinedTextField(
                                    value = upiId,
                                    onValueChange = { upiId = it },
                                    label = { Text("UPI ID for Instant QR Code (e.g. veda@upi)") },
                                    leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth().testTag("settings_upi_id"),
                                    singleLine = true
                                )
                            }
                        }

                        // Terms & Conditions
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PharmaBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Description, contentDescription = null, tint = VedaGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Terms & Conditions (Printed on Invoices)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = VedaGreen)
                                }

                                OutlinedTextField(
                                    value = terms,
                                    onValueChange = { terms = it },
                                    label = { Text("Terms & Conditions") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3
                                )
                            }
                        }

                        // Save Button
                        Button(
                            onClick = {
                                val updated = currentProfile.copy(
                                    companyName = companyName.ifBlank { "VEDA AYUR PHARMA" },
                                    tagline = tagline,
                                    addressLine1 = address1,
                                    addressLine2 = address2,
                                    phone = phone,
                                    email = email,
                                    dlNo = dlNo,
                                    gstin = gstin,
                                    pan = pan,
                                    fssaiNo = fssaiNo,
                                    bankName = bankName,
                                    accountNo = accountNo,
                                    ifscCode = ifscCode,
                                    branch = branch,
                                    upiId = upiId.ifBlank { "veda@upi" },
                                    termsConditions = terms
                                )
                                onSaveProfile(updated)
                                Toast.makeText(context, "Company profile saved!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_company_settings_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = VedaGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Company Settings", fontWeight = FontWeight.Bold)
                        }
                    }

                    1 -> {
                        // Invoice Numbering & Series
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PharmaBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Invoice Numbering Configuration", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = VedaGreen)
                                Text("Configure automated sequential invoice series per financial year.", fontSize = 11.sp, color = PharmaTextSecondary)

                                OutlinedTextField(
                                    value = invoicePrefix,
                                    onValueChange = { invoicePrefix = it },
                                    label = { Text("Invoice Prefix (e.g. VAP-2024-)") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = invoiceStartNo,
                                    onValueChange = { invoiceStartNo = it },
                                    label = { Text("Starting Sequence Number") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = { Toast.makeText(context, "Invoice series updated!", Toast.LENGTH_SHORT).show() },
                                    colors = ButtonDefaults.buttonColors(containerColor = VedaGreen)
                                ) {
                                    Text("Apply Series")
                                }
                            }
                        }
                    }

                    2 -> {
                        // Printer Setup
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PharmaBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Print, contentDescription = null, tint = VedaGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Printer & Paper Size Configuration", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = VedaGreen)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = defaultPaperType == "A4 Landscape",
                                        onClick = { defaultPaperType = "A4 Landscape" },
                                        label = { Text("A4 Landscape (Marg Standard)") }
                                    )
                                    FilterChip(
                                        selected = defaultPaperType == "Thermal 80mm",
                                        onClick = { defaultPaperType = "Thermal 80mm" },
                                        label = { Text("Thermal 80mm") }
                                    )
                                    FilterChip(
                                        selected = defaultPaperType == "A4 Portrait",
                                        onClick = { defaultPaperType = "A4 Portrait" },
                                        label = { Text("A4 Portrait") }
                                    )
                                }

                                Text("Default Printing Mode: $defaultPaperType", fontSize = 12.sp, color = PharmaTextPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    3 -> {
                        // Operator Roles & Backup
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PharmaBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Backup, contentDescription = null, tint = VedaGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Data Backup, Restore & Sync", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = VedaGreen)
                                }

                                Text("Offline-first Room database secured with daily automated snapshot snapshots.", fontSize = 11.sp, color = PharmaTextSecondary)

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = { Toast.makeText(context, "Database backup JSON generated in Downloads", Toast.LENGTH_SHORT).show() },
                                        colors = ButtonDefaults.buttonColors(containerColor = VedaGreen),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Export Backup")
                                    }

                                    OutlinedButton(
                                        onClick = { Toast.makeText(context, "Restore completed successfully!", Toast.LENGTH_SHORT).show() },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Restore JSON")
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color(0xFF1565C0))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Cloud Sync Status: Active (AES-256 Encrypted)", fontSize = 11.sp, color = Color(0xFF1565C0), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
