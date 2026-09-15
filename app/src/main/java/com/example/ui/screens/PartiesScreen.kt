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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.example.ui.theme.PharmaBorder
import com.example.ui.theme.PharmaTextPrimary
import com.example.ui.theme.PharmaTextSecondary
import com.example.ui.theme.VedaEmerald
import com.example.ui.theme.VedaGreen
import com.example.ui.theme.VedaGreenDark
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartiesScreen(
    parties: List<Party>,
    onSaveParty: (Party) -> Unit,
    onDeleteParty: (Party) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: All, 1: Customers/Chemists, 2: Suppliers
    var showDialog by remember { mutableStateOf(false) }
    var editingParty by remember { mutableStateOf<Party?>(null) }

    val filteredParties = when (selectedTab) {
        1 -> parties.filter { it.type == "CUSTOMER" }
        2 -> parties.filter { it.type == "SUPPLIER" }
        else -> parties
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Parties & Chemist Ledger (${parties.size})",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VedaGreenDark)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingParty = null
                    showDialog = true
                },
                containerColor = VedaGreen,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_party_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Party")
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
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("All Parties") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Customers / Chemists") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Suppliers / Pharma Mfg") })
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (filteredParties.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No parties recorded yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = PharmaTextSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredParties) { party ->
                            PartyCard(
                                party = party,
                                onEdit = {
                                    editingParty = party
                                    showDialog = true
                                },
                                onDelete = { onDeleteParty(party) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        PartyDialog(
            party = editingParty,
            onDismiss = { showDialog = false },
            onSave = {
                onSaveParty(it)
                showDialog = false
            }
        )
    }
}

@Composable
fun PartyCard(
    party: Party,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isCustomer = party.type == "CUSTOMER"

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
                        imageVector = if (isCustomer) Icons.Default.LocalPharmacy else Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = if (isCustomer) VedaGreen else Color(0xFF1565C0),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = party.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharmaTextPrimary
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = VedaGreen, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = party.address,
                fontSize = 11.sp,
                color = PharmaTextSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "DL: ${party.dlNo.ifEmpty { "-" }} | Ph: ${party.phone.ifEmpty { "-" }}",
                    fontSize = 11.sp,
                    color = PharmaTextSecondary
                )
                Text(
                    text = "GSTIN: ${party.gstin.ifEmpty { "URP" }}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = PharmaTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Credit Limit: ₹${party.creditLimit.toInt()} (${party.creditDays}d)",
                    fontSize = 11.sp,
                    color = Color(0xFF1565C0),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Bal: ₹${String.format(Locale.US, "%,.2f", party.currentBalance)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (party.currentBalance > 0) Color(0xFFD97706) else VedaEmerald
                )
            }
        }
    }
}

@Composable
fun PartyDialog(
    party: Party?,
    onDismiss: () -> Unit,
    onSave: (Party) -> Unit
) {
    var name by remember { mutableStateOf(party?.name ?: "") }
    var address by remember { mutableStateOf(party?.address ?: "") }
    var phone by remember { mutableStateOf(party?.phone ?: "") }
    var dlNo by remember { mutableStateOf(party?.dlNo ?: "") }
    var gstin by remember { mutableStateOf(party?.gstin ?: "") }
    var type by remember { mutableStateOf(party?.type ?: "CUSTOMER") }
    var creditLimitStr by remember { mutableStateOf(party?.creditLimit?.toString() ?: "50000.0") }
    var creditDaysStr by remember { mutableStateOf(party?.creditDays?.toString() ?: "30") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (party == null) "Add Chemist / Party" else "Edit Party",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = VedaGreen
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == "CUSTOMER",
                        onClick = { type = "CUSTOMER" },
                        label = { Text("Customer / Chemist") }
                    )
                    FilterChip(
                        selected = type == "SUPPLIER",
                        onClick = { type = "SUPPLIER" },
                        label = { Text("Supplier / Mfg") }
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Chemist / Party Legal Name") },
                    modifier = Modifier.fillMaxWidth().testTag("party_dialog_name"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dlNo,
                        onValueChange = { dlNo = it },
                        label = { Text("D.L. No.") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = gstin,
                    onValueChange = { gstin = it },
                    label = { Text("GSTIN") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = creditLimitStr,
                        onValueChange = { creditLimitStr = it },
                        label = { Text("Credit Limit ₹") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = creditDaysStr,
                        onValueChange = { creditDaysStr = it },
                        label = { Text("Credit Days") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(
                                    party?.copy(
                                        name = name,
                                        address = address,
                                        phone = phone,
                                        dlNo = dlNo,
                                        gstin = gstin,
                                        type = type,
                                        creditLimit = creditLimitStr.toDoubleOrNull() ?: 50000.0,
                                        creditDays = creditDaysStr.toIntOrNull() ?: 30
                                    ) ?: Party(
                                        name = name,
                                        address = address,
                                        phone = phone,
                                        dlNo = dlNo,
                                        gstin = gstin,
                                        type = type,
                                        creditLimit = creditLimitStr.toDoubleOrNull() ?: 50000.0,
                                        creditDays = creditDaysStr.toIntOrNull() ?: 30
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VedaGreen)
                    ) {
                        Text("Save Party")
                    }
                }
            }
        }
    }
}
