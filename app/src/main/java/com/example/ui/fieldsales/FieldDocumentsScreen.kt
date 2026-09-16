package com.example.ui.fieldsales

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FieldDocument
import com.example.ui.FieldSalesViewModel
import com.example.ui.theme.FieldSalesColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldDocumentsScreen(
    viewModel: FieldSalesViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val documents by viewModel.allDocuments.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    var newTitle by remember { mutableStateOf("") }
    var newCustomer by remember { mutableStateOf("") }
    var newType by remember { mutableStateOf("PHYSICAL_BILL") }
    var newNotes by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Documents & Receipts",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Bills, Licenses & Delivery Proofs (${documents.size})",
                            fontSize = 11.sp,
                            color = FieldSalesColors.MintSoft
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_documents")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }, modifier = Modifier.testTag("btn_add_document")) {
                        Icon(Icons.Default.Add, contentDescription = "Add Document", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FieldSalesColors.DarkGreen)
            )
        },
        containerColor = FieldSalesColors.Background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = FieldSalesColors.LightMintGreen),
                    border = BorderStroke(1.dp, FieldSalesColors.DarkGreen)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = FieldSalesColors.DarkGreen)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Store scanned physical bills, Ayush drug licenses (Form 20B/21B), GST certificates and signed customer receipts in the field cloud vault.",
                            fontSize = 11.sp,
                            color = FieldSalesColors.DarkGreen
                        )
                    }
                }
            }

            items(documents) { doc ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, FieldSalesColors.BorderLight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (doc.documentType) {
                                            "PHYSICAL_BILL" -> FieldSalesColors.LightMintGreen
                                            "DRUG_LICENSE" -> Color(0xFFE0F2FE)
                                            else -> FieldSalesColors.EarthyBrownLight
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    when (doc.documentType) {
                                        "PHYSICAL_BILL" -> Icons.Default.Receipt
                                        "DRUG_LICENSE" -> Icons.Default.MedicalServices
                                        else -> Icons.Default.Description
                                    },
                                    contentDescription = null,
                                    tint = when (doc.documentType) {
                                        "PHYSICAL_BILL" -> FieldSalesColors.DarkGreen
                                        "DRUG_LICENSE" -> Color(0xFF0369A1)
                                        else -> FieldSalesColors.EarthyBrown
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = doc.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FieldSalesColors.TextPrimary
                                )
                                Text(
                                    text = "${doc.customerName} • ${doc.dateUploaded}",
                                    fontSize = 11.sp,
                                    color = FieldSalesColors.TextSecondary
                                )
                                if (doc.notes.isNotBlank()) {
                                    Text(
                                        text = doc.notes,
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        Row {
                            IconButton(onClick = {
                                Toast.makeText(context, "Viewing: ${doc.title}", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Visibility, contentDescription = "View", tint = FieldSalesColors.DarkGreen)
                            }
                            IconButton(onClick = { viewModel.deleteDocument(doc) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = FieldSalesColors.RedAccent)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Upload Physical Bill / Document", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Document Title") },
                        placeholder = { Text("e.g. Bill #1002 Scanned Copy") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCustomer,
                        onValueChange = { newCustomer = it },
                        label = { Text("Customer / Clinic Name") },
                        placeholder = { Text("e.g. Dhanvantari Nilayam") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newNotes,
                        onValueChange = { newNotes = it },
                        label = { Text("Notes / Verification Details") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            viewModel.uploadDocument(
                                title = newTitle,
                                customerName = newCustomer.ifBlank { "Direct Chemist" },
                                documentType = newType,
                                fileUri = "scans/doc_${System.currentTimeMillis()}.pdf",
                                notes = newNotes
                            )
                            showAddDialog = false
                            newTitle = ""
                            newCustomer = ""
                            newNotes = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FieldSalesColors.DarkGreen)
                ) {
                    Text("Upload Document")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
