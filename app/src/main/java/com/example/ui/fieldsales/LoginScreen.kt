package com.example.ui.fieldsales

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserRole
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
    val isAuthenticating by viewModel.isAuthenticating.collectAsState()
    val authErrorMessage by viewModel.authErrorMessage.collectAsState()
    val firebaseUser by viewModel.firebaseUser.collectAsState()

    val defaultClientId = stringResource(R.string.default_web_client_id)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Veda Ayur ERP Single Sign-On",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_login")) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ayurvedic Brand Header
            item {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(FieldSalesColors.LightMintGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌿", fontSize = 34.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Veda Ayur Pharma ERP",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FieldSalesColors.DarkGreen
                )
                Text(
                    text = "Google Sign-In with Credential Manager & Firebase Auth",
                    fontSize = 12.sp,
                    color = FieldSalesColors.TextSecondary
                )
            }

            // Google Sign-In with Credential Manager Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_google_auth"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, FieldSalesColors.BorderLight)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = FieldSalesColors.DarkGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Google Identity & Firebase Auth",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = FieldSalesColors.TextPrimary
                                )
                            }

                            if (currentUser.isGoogleAccount || firebaseUser != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFE8F5E9))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Authenticated",
                                        color = FieldSalesColors.DarkGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (currentUser.isGoogleAccount || firebaseUser != null) {
                            // Active Google & Firebase Session
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(FieldSalesColors.LightMintGreen)
                                    .border(1.dp, FieldSalesColors.DarkGreen.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(FieldSalesColors.DarkGreen),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = currentUser.name.take(1).uppercase(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = currentUser.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = FieldSalesColors.DarkGreen
                                            )
                                            Text(
                                                text = currentUser.email,
                                                fontSize = 12.sp,
                                                color = FieldSalesColors.TextSecondary
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VerifiedUser,
                                            contentDescription = null,
                                            tint = FieldSalesColors.DarkGreen,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Token Verified • Role: ${currentUser.role.name}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = FieldSalesColors.DarkGreen
                                        )
                                    }

                                    firebaseUser?.let { fbUser ->
                                        Text(
                                            text = "Firebase UID: ${fbUser.uid.take(16)}...",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onLoginSuccess,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("btn_continue_to_erp"),
                                    colors = ButtonDefaults.buttonColors(containerColor = FieldSalesColors.DarkGreen),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Enter ERP", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.signOut {
                                            Toast.makeText(context, "Signed out from Google session", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("btn_sign_out_google"),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFEF5350))
                                ) {
                                    Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sign Out", color = Color(0xFFD32F2F), fontSize = 13.sp)
                                }
                            }
                        } else {
                            // Primary Google Sign-In with Credential Manager Button
                            OutlinedButton(
                                onClick = {
                                    viewModel.performGoogleSignIn(
                                        activityContext = context,
                                        serverClientId = defaultClientId,
                                        onSuccess = {
                                            Toast.makeText(context, "Google Sign-In Successful via Credential Manager!", Toast.LENGTH_SHORT).show()
                                            onLoginSuccess()
                                        },
                                        onFailure = { errorMsg ->
                                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                },
                                enabled = !isAuthenticating,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_google_signin"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFDADCE0))
                            ) {
                                if (isAuthenticating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = FieldSalesColors.DarkGreen,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        "Opening Credential Manager...",
                                        color = FieldSalesColors.TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                } else {
                                    Text(
                                        "G",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 19.sp,
                                        color = Color(0xFFEA4335)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        "Sign in with Google",
                                        color = Color(0xFF3C4043),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Notice if Credential Manager encountered error or fallback needed
                            if (!authErrorMessage.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFFF3E0))
                                        .padding(10.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.ErrorOutline,
                                                contentDescription = null,
                                                tint = Color(0xFFE65100),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Credential Manager Notice",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Color(0xFFE65100)
                                            )
                                        }
                                        Text(
                                            text = authErrorMessage ?: "",
                                            fontSize = 11.sp,
                                            color = Color(0xFFBF360C)
                                        )
                                    }
                                }
                            }

                            // 1-Tap Fast Link Option (vedaayurpharma@gmail.com)
                            OutlinedButton(
                                onClick = {
                                    viewModel.connectGmailAccount("vedaayurpharma@gmail.com")
                                    Toast.makeText(context, "Connected as vedaayurpharma@gmail.com", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .testTag("btn_fast_google_link"),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, FieldSalesColors.MintSoft)
                            ) {
                                Icon(
                                    Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = FieldSalesColors.DarkGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Continue as vedaayurpharma@gmail.com",
                                    color = FieldSalesColors.DarkGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
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
                                        text = "Google Drive / Remote ERP Vault",
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

            // Quick Role Switcher for ERP Personnel
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
                            text = "Or Select ERP User Role & Profile:",
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
                                    .border(
                                        1.dp,
                                        if (isSelected) FieldSalesColors.DarkGreen else Color(0xFFE2E8F0),
                                        RoundedCornerShape(8.dp)
                                    )
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
                                        Text(
                                            user.fullName.take(1),
                                            fontWeight = FontWeight.Bold,
                                            color = FieldSalesColors.DarkGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            "${user.role.name} • ${user.email}",
                                            fontSize = 11.sp,
                                            color = FieldSalesColors.TextSecondary
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        viewModel.setCurrentUser(user)
                                        Toast.makeText(
                                            context,
                                            "Active profile: ${user.fullName} (${user.role.name})",
                                            Toast.LENGTH_SHORT
                                        ).show()
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_trigger_sync"),
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
