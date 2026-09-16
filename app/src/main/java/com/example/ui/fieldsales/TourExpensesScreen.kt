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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FieldSalesViewModel
import com.example.ui.theme.FieldSalesColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TourExpensesScreen(
    viewModel: FieldSalesViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val expenses by viewModel.allExpenses.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val todayDate = remember {
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
    }

    // New Expense State
    var tourTitle by remember { mutableStateOf("Bangalore - Mysore Field Tour") }
    var expenseDate by remember { mutableStateOf(todayDate) }
    var travelAmount by remember { mutableDoubleStateOf(450.0) }
    var foodAmount by remember { mutableDoubleStateOf(220.0) }
    var lodgingAmount by remember { mutableDoubleStateOf(0.0) }
    var otherAmount by remember { mutableDoubleStateOf(50.0) }
    var expenseNotes by remember { mutableStateOf("") }
    var receiptAttached by remember { mutableStateOf(false) }

    val totalAmount = travelAmount + foodAmount + lodgingAmount + otherAmount
    val grandTotalExpenses = expenses.sumOf { it.totalAmount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Tour Expenses",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Field Travel, Lodging & Food Claims",
                            fontSize = 11.sp,
                            color = FieldSalesColors.MintSoft
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_expenses")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }, modifier = Modifier.testTag("btn_add_expense")) {
                        Icon(Icons.Default.Add, contentDescription = "Add Expense", tint = Color.White)
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Expense Summary Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FieldSalesColors.DarkGreenSurface),
                    border = BorderStroke(1.dp, FieldSalesColors.DarkGreen)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Tour Claims", color = Color.White, fontSize = 14.sp)
                            Text(
                                "₹${String.format(Locale.US, "%,.2f", grandTotalExpenses)}",
                                color = FieldSalesColors.LimeGreen,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Claims: ${expenses.size}", color = Color.LightGray, fontSize = 11.sp)
                            Text("Approved: ${expenses.count { it.status == "APPROVED" }} | Pending: ${expenses.count { it.status == "PENDING" }}", color = Color.LightGray, fontSize = 11.sp)
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Claim History",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = FieldSalesColors.TextPrimary
                    )

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = FieldSalesColors.DarkGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Claim", fontSize = 12.sp)
                    }
                }
            }

            items(expenses) { exp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, FieldSalesColors.BorderLight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(FieldSalesColors.EarthyBrownLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = FieldSalesColors.EarthyBrown, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = exp.tourTitle,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = FieldSalesColors.TextPrimary
                                    )
                                    Text(
                                        text = "${exp.date} • Rep: ${exp.salesRep}",
                                        fontSize = 11.sp,
                                        color = FieldSalesColors.TextSecondary
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when (exp.status) {
                                            "APPROVED" -> FieldSalesColors.LightMintGreen
                                            "REIMBURSED" -> Color(0xFFE0F2FE)
                                            else -> Color(0xFFFEF3C7)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = exp.status,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (exp.status) {
                                        "APPROVED" -> FieldSalesColors.DarkGreen
                                        "REIMBURSED" -> Color(0xFF0369A1)
                                        else -> Color(0xFFB45309)
                                    }
                                )
                            }
                        }

                        // Categories breakdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            CategoryBadge(icon = Icons.Default.DirectionsCar, label = "Travel", amount = exp.travelAmount)
                            CategoryBadge(icon = Icons.Default.Fastfood, label = "Food", amount = exp.foodAmount)
                            CategoryBadge(icon = Icons.Default.Hotel, label = "Lodging", amount = exp.lodgingAmount)
                            CategoryBadge(icon = Icons.Default.Receipt, label = "Other", amount = exp.otherAmount)
                        }

                        if (exp.notes.isNotBlank()) {
                            Text(
                                text = "Notes: \"${exp.notes}\"",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(FieldSalesColors.BorderLight)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (exp.receiptUri.isNotBlank()) "📎 Receipt Attached" else "No receipt attached",
                                fontSize = 11.sp,
                                color = if (exp.receiptUri.isNotBlank()) FieldSalesColors.DarkGreen else Color.Gray,
                                fontWeight = if (exp.receiptUri.isNotBlank()) FontWeight.Bold else FontWeight.Normal
                            )

                            Text(
                                text = "Total: ₹${String.format(Locale.US, "%,.2f", exp.totalAmount)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = FieldSalesColors.DarkGreen
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Expense Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("New Tour Expense Claim", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tourTitle,
                        onValueChange = { tourTitle = it },
                        label = { Text("Tour / Destination Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = travelAmount.toString(),
                            onValueChange = { travelAmount = it.toDoubleOrNull() ?: 0.0 },
                            label = { Text("Travel (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = foodAmount.toString(),
                            onValueChange = { foodAmount = it.toDoubleOrNull() ?: 0.0 },
                            label = { Text("Food (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = lodgingAmount.toString(),
                            onValueChange = { lodgingAmount = it.toDoubleOrNull() ?: 0.0 },
                            label = { Text("Lodging (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = otherAmount.toString(),
                            onValueChange = { otherAmount = it.toDoubleOrNull() ?: 0.0 },
                            label = { Text("Other (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = expenseNotes,
                        onValueChange = { expenseNotes = it },
                        label = { Text("Notes / Purpose") },
                        placeholder = { Text("e.g. Bus fare and toll receipt for Mysore route") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            receiptAttached = true
                            Toast.makeText(context, "Receipt captured via camera!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FieldSalesColors.EarthyBrown),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (receiptAttached) "Receipt Attached (Click to Change)" else "Attach Receipt Photo")
                    }

                    Text(
                        text = "Total Claim: ₹${String.format(Locale.US, "%,.2f", totalAmount)}",
                        fontWeight = FontWeight.Bold,
                        color = FieldSalesColors.DarkGreen,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addTourExpense(
                            tourTitle = tourTitle,
                            date = expenseDate,
                            travel = travelAmount,
                            food = foodAmount,
                            lodging = lodgingAmount,
                            other = otherAmount,
                            receiptUri = if (receiptAttached) "receipts/exp_${System.currentTimeMillis()}.jpg" else "",
                            notes = expenseNotes
                        )
                        showAddDialog = false
                        Toast.makeText(context, "Tour expense submitted for manager approval!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FieldSalesColors.DarkGreen)
                ) {
                    Text("Submit Claim")
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

@Composable
fun CategoryBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, amount: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = FieldSalesColors.DarkGreen, modifier = Modifier.size(16.dp))
        Text(label, fontSize = 10.sp, color = Color.Gray)
        Text("₹${amount.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FieldSalesColors.TextPrimary)
    }
}
