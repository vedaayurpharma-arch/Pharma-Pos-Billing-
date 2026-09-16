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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.FieldSalesViewModel
import com.example.ui.theme.FieldSalesColors
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerPortalScreen(
    viewModel: FieldSalesViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val orders by viewModel.allOrders.collectAsState()
    val customers by viewModel.allCustomers.collectAsState()
    val expenses by viewModel.allExpenses.collectAsState()

    val isAuthorized = currentUser.role == UserRole.ADMIN || currentUser.role == UserRole.MANAGER

    val totalSales = orders.sumOf { it.finalPurchaseValue }
    val totalClaims = expenses.sumOf { it.totalAmount }
    val pendingClaims = expenses.filter { it.status == "PENDING" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Owner & Executive Portal",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Executive Governance & Field Staff Audit",
                            fontSize = 11.sp,
                            color = FieldSalesColors.MintSoft
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_owner_portal")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
            // User Role Status Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FieldSalesColors.DarkGreenSurface),
                    border = BorderStroke(1.dp, FieldSalesColors.DarkGreen)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(FieldSalesColors.LimeGreenSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = FieldSalesColors.LimeGreen, modifier = Modifier.size(22.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(currentUser.fullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(currentUser.email, color = Color.LightGray, fontSize = 11.sp)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(FieldSalesColors.LimeGreen)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = currentUser.role.name,
                                    color = Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Switch Active Persona
                        Text("Switch Active Role / User:", fontSize = 11.sp, color = Color.LightGray)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            allUsers.forEach { user ->
                                FilterChip(
                                    selected = currentUser.id == user.id,
                                    onClick = { viewModel.setCurrentUser(user) },
                                    label = { Text(user.fullName.split(" ").first() + " (${user.role})", fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = FieldSalesColors.LimeGreen,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (!isAuthorized) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        border = BorderStroke(1.dp, FieldSalesColors.RedAccent)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = FieldSalesColors.RedAccent)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Owner / Manager Privilege Required", fontWeight = FontWeight.Bold, color = FieldSalesColors.RedAccent)
                            }
                            Text(
                                "You are currently signed in as a FIELD_SALES user. Tap one of the Owner/Manager pills above to view company executive figures.",
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            } else {
                // Executive High-Level Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = FieldSalesColors.LightMintGreen)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Gross Field Revenue", fontSize = 11.sp, color = FieldSalesColors.DarkGreen, fontWeight = FontWeight.SemiBold)
                                Text("₹${String.format(Locale.US, "%,.0f", totalSales)}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = FieldSalesColors.DarkGreen)
                                Text("${orders.size} booked purchases", fontSize = 10.sp, color = Color.Gray)
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Tour Expense Claims", fontSize = 11.sp, color = Color(0xFF92400E), fontWeight = FontWeight.SemiBold)
                                Text("₹${String.format(Locale.US, "%,.0f", totalClaims)}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF92400E))
                                Text("${pendingClaims.size} pending approval", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                // Pending Tour Claims Approval Queue
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, FieldSalesColors.BorderLight)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AttachMoney, contentDescription = null, tint = FieldSalesColors.EarthyBrown)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pending Tour Approvals (${pendingClaims.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }

                            if (pendingClaims.isEmpty()) {
                                Text("No pending expense claims requiring approval.", fontSize = 12.sp, color = Color.Gray)
                            } else {
                                pendingClaims.forEach { claim ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(claim.tourTitle, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                            Text("Rep: ${claim.salesRep} • ₹${claim.totalAmount}", fontSize = 11.sp, color = Color.Gray)
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.approveTourExpense(claim)
                                                Toast.makeText(context, "Claim of ₹${claim.totalAmount} approved by Owner", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = FieldSalesColors.DarkGreen),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Approve", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Field Sales Staff Performance Leaderboard
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, FieldSalesColors.BorderLight)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Group, contentDescription = null, tint = FieldSalesColors.DarkGreen)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Field Rep Performance & Target Quota", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            val reps = listOf(
                                Triple("Ramesh Kumar", "₹72,400", "96% of target"),
                                Triple("Kiran Reddy", "₹38,900", "78% of target"),
                                Triple("Dr. Veda Murthy", "₹24,500", "Direct Field Visits")
                            )

                            reps.forEach { (rep, sales, status) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(rep, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text(status, fontSize = 11.sp, color = FieldSalesColors.LimeGreen, fontWeight = FontWeight.SemiBold)
                                    }
                                    Text(sales, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = FieldSalesColors.DarkGreen)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
