package com.example.ui.fieldsales

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FieldSalesViewModel
import com.example.ui.theme.FieldSalesColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: FieldSalesViewModel,
    onBack: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val isCloudConnected by viewModel.isCloudConnected.collectAsState()

    var email by remember { mutableStateOf(currentUser.email) }
    var password by remember { mutableStateOf("••••••••") }
    var showPassword by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Veda Ayur ERP & Field Cloud Access",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_login")) {
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ayurvedic Brand Header
            item {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(FieldSalesColors.LightMintGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌿", fontSize = 36.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Veda Ayur Pharma ERP",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FieldSalesColors.DarkGreen
                )
                Text(
                    text = "Single Sign-On, Cloud Access & Field Sync",
                    fontSize = 12.sp,
                    color = FieldSalesColors.TextSecondary
                )
            }

            // Gmail Sign-In / Fast Access
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, FieldSalesColors.BorderLight)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Google / Gmail Cloud Access",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = FieldSalesColors.TextPrimary
                        )

                        OutlinedButton(
                            onClick = {
                                viewModel.connectGmailAccount("vedaayurpharma@gmail.com")
                                Toast.makeText(context, "Authenticated via Google Account (vedaayurpharma@gmail.com)", Toast.LENGTH_SHORT).show()
                                onLoginSuccess()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_google_signin"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFDADCE0))
                        ) {
                            Text("G", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFFEA4335))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Sign in with Google (vedaayurpharma@gmail.com)", color = Color(0xFF3C4043), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // Cloud Sync Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(FieldSalesColors.LightMintGreen)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (isCloudConnected) Icons.Default.CloudDone else Icons.Default.Cloud,
                                    contentDescription = null,
                                    tint = FieldSalesColors.DarkGreen
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isCloudConnected) "Cloud Access Enabled" else "Cloud Access Offline",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = FieldSalesColors.DarkGreen
                                    )
                                    Text(
                                        text = "Google Drive / Remote ERP Database",
                                        fontSize = 10.sp,
                                        color = FieldSalesColors.TextSecondary
                                    )
                                }
                            }

                            Switch(
                                checked = isCloudConnected,
                                onCheckedChange = { viewModel.toggleCloudConnection(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = FieldSalesColors.DarkGreen,
                                    checkedTrackColor = FieldSalesColors.MintSoft
                                )
                            )
                        }
                    }
                }
            }

            // Quick Role Switcher
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, FieldSalesColors.BorderLight)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Or Select ERP User Role:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = FieldSalesColors.TextPrimary
                        )

                        allUsers.forEach { user ->
                            val isSelected = currentUser.id == user.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) FieldSalesColors.LightMintGreen else Color(0xFFF8FAFC))
                                    .border(1.dp, if (isSelected) FieldSalesColors.DarkGreen else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(FieldSalesColors.MintSoft),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(user.fullName.take(1), fontWeight = FontWeight.Bold, color = FieldSalesColors.DarkGreen)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${user.role} • ${user.email}", fontSize = 11.sp, color = FieldSalesColors.TextSecondary)
                                    }
                                }

                                Button(
                                    onClick = {
                                        viewModel.setCurrentUser(user)
                                        email = user.email
                                        Toast.makeText(context, "Active profile: ${user.fullName} (${user.role})", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = FieldSalesColors.DarkGreen),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Switch", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Sync Button
            item {
                OutlinedButton(
                    onClick = { viewModel.syncWithCloud() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, FieldSalesColors.DarkGreen)
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = FieldSalesColors.DarkGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Syncing with Cloud Vault...", color = FieldSalesColors.DarkGreen)
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = null, tint = FieldSalesColors.DarkGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Trigger Immediate Cloud Sync", color = FieldSalesColors.DarkGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
