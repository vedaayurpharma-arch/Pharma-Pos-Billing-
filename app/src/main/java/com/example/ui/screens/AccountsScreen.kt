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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
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
import com.example.data.model.AccountsTransaction
import com.example.ui.theme.AlertRed
import com.example.ui.theme.PharmaBg
import com.example.ui.theme.PharmaBorder
import com.example.ui.theme.PharmaTextPrimary
import com.example.ui.theme.PharmaTextSecondary
import com.example.ui.theme.VedaEmerald
import com.example.ui.theme.VedaGreen
import com.example.ui.theme.VedaGreenDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    transactions: List<AccountsTransaction>,
    onBack: () -> Unit,
    onSaveTransaction: (AccountsTransaction) -> Unit,
    onDeleteTransaction: (AccountsTransaction) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: All Transactions, 1: Receipts, 2: Payments, 3: Expenses
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredList = when (selectedTab) {
        1 -> transactions.filter { it.type == "RECEIPT" }
        2 -> transactions.filter { it.type == "PAYMENT" }
        3 -> transactions.filter { it.type == "EXPENSE" }
        else -> transactions
    }

    val totalReceipts = transactions.filter { it.type == "RECEIPT" }.sumOf { it.amount }
    val totalPayments = transactions.filter { it.type == "PAYMENT" }.sumOf { it.amount }
    val totalExpenses = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val netCashFlow = totalReceipts - (totalPayments + totalExpenses)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PharmaBg)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Accounting & Cash Books",
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

        // Summary Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PharmaBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Collections", fontSize = 10.sp, color = PharmaTextSecondary)
                    Text("+₹${totalReceipts.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VedaEmerald)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Payments", fontSize = 10.sp, color = PharmaTextSecondary)
                    Text("-₹${totalPayments.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AlertRed)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Expenses", fontSize = 10.sp, color = PharmaTextSecondary)
                    Text("-₹${totalExpenses.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2185B))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Net Cash", fontSize = 10.sp, color = PharmaTextSecondary)
                    Text("₹${netCashFlow.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (netCashFlow >= 0) VedaGreen else AlertRed)
                }
            }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = VedaGreen
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Day Book") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Receipts") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Payments") })
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Expenses") })
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (filteredList.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = PharmaTextSecondary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Transactions Found",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharmaTextPrimary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList) { tx ->
                        TransactionCard(
                            tx = tx,
                            onDelete = { onDeleteTransaction(tx) }
                        )
                    }
                }
            }

            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = VedaGreen,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Entry") },
                text = { Text("Record Entry") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("btn_record_entry")
            )
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            onDismiss = { showAddDialog = false },
            onSave = { tx ->
                onSaveTransaction(tx)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun TransactionCard(
    tx: AccountsTransaction,
    onDelete: () -> Unit
) {
    val isCredit = tx.type == "RECEIPT"
    val iconColor = if (isCredit) VedaEmerald else AlertRed
    val iconBg = if (isCredit) Color(0xFFE8F5E9) else Color(0xFFFEE2E2)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, PharmaBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = tx.type,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.partyName.ifEmpty { tx.category },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PharmaTextPrimary
                )
                Text(
                    text = "${tx.category} • ${tx.paymentMode} • ${tx.date}",
                    fontSize = 11.sp,
                    color = PharmaTextSecondary
                )
                if (tx.notes.isNotEmpty()) {
                    Text(
                        text = tx.notes,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isCredit) "+" else "-"}₹${String.format(Locale.US, "%,.2f", tx.amount)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = iconColor
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onSave: (AccountsTransaction) -> Unit
) {
    val today = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()) }
    var type by remember { mutableStateOf("RECEIPT") } // RECEIPT, PAYMENT, EXPENSE
    var amountStr by remember { mutableStateOf("") }
    var partyName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Customer Collection") }
    var paymentMode by remember { mutableStateOf("CASH") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Record Cash / Bank Entry",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = type == "RECEIPT",
                        onClick = {
                            type = "RECEIPT"
                            category = "Customer Collection"
                        },
                        label = { Text("Receipt") }
                    )
                    FilterChip(
                        selected = type == "PAYMENT",
                        onClick = {
                            type = "PAYMENT"
                            category = "Supplier Payment"
                        },
                        label = { Text("Payment") }
                    )
                    FilterChip(
                        selected = type == "EXPENSE",
                        onClick = {
                            type = "EXPENSE"
                            category = "Store Electricity & Rent"
                        },
                        label = { Text("Expense") }
                    )
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount ₹") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = partyName,
                    onValueChange = { partyName = it },
                    label = { Text(if (type == "EXPENSE") "Payee / Vendor" else "Party / Customer Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Account Category") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = paymentMode == "CASH",
                        onClick = { paymentMode = "CASH" },
                        label = { Text("Cash") }
                    )
                    FilterChip(
                        selected = paymentMode == "UPI",
                        onClick = { paymentMode = "UPI" },
                        label = { Text("UPI QR") }
                    )
                    FilterChip(
                        selected = paymentMode == "BANK",
                        onClick = { paymentMode = "BANK" },
                        label = { Text("Bank") }
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Reference No.") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onSave(
                            AccountsTransaction(
                                date = today,
                                type = type,
                                category = category,
                                amount = amt,
                                partyName = partyName,
                                paymentMode = paymentMode,
                                notes = notes
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VedaGreen)
            ) {
                Text("Save Entry")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
