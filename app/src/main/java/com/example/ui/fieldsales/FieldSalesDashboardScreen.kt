package com.example.ui.fieldsales

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FieldSalesViewModel
import com.example.ui.theme.FieldSalesColors
import java.util.Locale

enum class FieldSubScreen {
    DASHBOARD,
    NEW_CUSTOMER,
    SEARCH_FILTER,
    ADD_PRODUCT,
    NEW_INVOICE,
    DOCUMENTS,
    SALES_REPORTS,
    DAILY_SALES,
    ROUTE_GPS,
    TOUR_EXPENSES,
    OWNER_PORTAL,
    LOGIN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldSalesMainScreen(
    viewModel: FieldSalesViewModel,
    onExitModule: () -> Unit
) {
    var currentSubScreen by remember { mutableStateOf(FieldSubScreen.DASHBOARD) }

    val currentUser by viewModel.currentUser.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val isCloudConnected by viewModel.isCloudConnected.collectAsState()
    val customers by viewModel.allCustomers.collectAsState()
    val orders by viewModel.allOrders.collectAsState()

    AnimatedContent(
        targetState = currentSubScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "field_sales_screen_transition"
    ) { screen ->
        when (screen) {
            FieldSubScreen.DASHBOARD -> {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = "Field Sales & Customer Mgmt",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Ayurvedic Medical Reps & Quotas",
                                        fontSize = 11.sp,
                                        color = FieldSalesColors.MintSoft
                                    )
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = onExitModule, modifier = Modifier.testTag("btn_exit_field_sales")) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Return to ERP", tint = Color.White)
                                }
                            },
                            actions = {
                                // Cloud Sync Button
                                IconButton(
                                    onClick = { viewModel.syncWithCloud() },
                                    modifier = Modifier.testTag("btn_sync_cloud")
                                ) {
                                    if (isSyncing) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = FieldSalesColors.LimeGreen)
                                    } else {
                                        Icon(
                                            if (isCloudConnected) Icons.Default.CloudDone else Icons.Default.Sync,
                                            contentDescription = "Sync",
                                            tint = if (isCloudConnected) FieldSalesColors.LimeGreen else Color.White
                                        )
                                    }
                                }

                                // Profile / Login Button
                                IconButton(
                                    onClick = { currentSubScreen = FieldSubScreen.LOGIN },
                                    modifier = Modifier.testTag("btn_open_login")
                                ) {
                                    Icon(Icons.Default.AccountCircle, contentDescription = "User Profile", tint = Color.White)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = FieldSalesColors.DarkGreen)
                        )
                    },
                    containerColor = FieldSalesColors.Background
                ) { paddingValues ->
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // User Status & Quick Metric Header (Full Span)
                        item(span = { GridItemSpan(2) }) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = FieldSalesColors.DarkGreenSurface),
                                border = BorderStroke(1.dp, FieldSalesColors.DarkGreen)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(FieldSalesColors.LightMintGreen),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(currentUser.fullName.take(1), fontWeight = FontWeight.Bold, color = FieldSalesColors.DarkGreen)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(currentUser.fullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("Role: ${currentUser.role}", color = FieldSalesColors.MintSoft, fontSize = 10.sp)
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(FieldSalesColors.LimeGreenSoft)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("ONLINE CLOUD", color = FieldSalesColors.LimeGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF2D6A4F)))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Active Clinics", color = Color.LightGray, fontSize = 10.sp)
                                            Text("${customers.size}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        }
                                        Column {
                                            Text("Field Orders", color = Color.LightGray, fontSize = 10.sp)
                                            Text("${orders.size}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Field Sales", color = Color.LightGray, fontSize = 10.sp)
                                            Text(
                                                "₹${String.format(Locale.US, "%,.0f", orders.sumOf { it.finalPurchaseValue })}",
                                                color = FieldSalesColors.LimeGreen,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Section Title
                        item(span = { GridItemSpan(2) }) {
                            Text(
                                text = "FIELD OPERATIONS DASHBOARD",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = FieldSalesColors.TextSecondary,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // 1. New Customer
                        item {
                            DashboardModuleCard(
                                title = "New Customer",
                                subtitle = "Doctor/Clinic & GPS Entry",
                                icon = Icons.Default.PersonAdd,
                                iconBg = FieldSalesColors.LightMintGreen,
                                iconTint = FieldSalesColors.DarkGreen,
                                tag = "btn_nav_new_customer",
                                onClick = { currentSubScreen = FieldSubScreen.NEW_CUSTOMER }
                            )
                        }

                        // 2. Search & Filter
                        item {
                            DashboardModuleCard(
                                title = "Search & Filter",
                                subtitle = "Clinics, Doctors & Reps",
                                icon = Icons.Default.Search,
                                iconBg = Color(0xFFE0F2FE),
                                iconTint = Color(0xFF0284C7),
                                tag = "btn_nav_search_filter",
                                onClick = { currentSubScreen = FieldSubScreen.SEARCH_FILTER }
                            )
                        }

                        // 3. Add Product
                        item {
                            DashboardModuleCard(
                                title = "Add Product",
                                subtitle = "State Allocation Quotas",
                                icon = Icons.Default.Medication,
                                iconBg = FieldSalesColors.LimeGreenSoft,
                                iconTint = FieldSalesColors.LimeGreen,
                                tag = "btn_nav_add_product",
                                onClick = { currentSubScreen = FieldSubScreen.ADD_PRODUCT }
                            )
                        }

                        // 4. New Invoice
                        item {
                            DashboardModuleCard(
                                title = "New Invoice",
                                subtitle = "Digital Bills & Wholesale",
                                icon = Icons.Default.Receipt,
                                iconBg = Color(0xFFFEF3C7),
                                iconTint = Color(0xFFD97706),
                                tag = "btn_nav_new_invoice",
                                onClick = { currentSubScreen = FieldSubScreen.NEW_INVOICE }
                            )
                        }

                        // 5. Documents
                        item {
                            DashboardModuleCard(
                                title = "Documents",
                                subtitle = "Bills, Licenses & Proofs",
                                icon = Icons.Default.Description,
                                iconBg = FieldSalesColors.EarthyBrownLight,
                                iconTint = FieldSalesColors.EarthyBrown,
                                tag = "btn_nav_documents",
                                onClick = { currentSubScreen = FieldSubScreen.DOCUMENTS }
                            )
                        }

                        // 6. Sales Reports
                        item {
                            DashboardModuleCard(
                                title = "Sales Reports",
                                subtitle = "Analytics & Excel Exports",
                                icon = Icons.Default.Assessment,
                                iconBg = Color(0xFFFEE2E2),
                                iconTint = FieldSalesColors.RedAccent,
                                tag = "btn_nav_sales_reports",
                                onClick = { currentSubScreen = FieldSubScreen.SALES_REPORTS }
                            )
                        }

                        // 7. Daily Sales
                        item {
                            DashboardModuleCard(
                                title = "Daily Sales",
                                subtitle = "Real-Time Bookings",
                                icon = Icons.Default.CalendarToday,
                                iconBg = FieldSalesColors.LightMintGreen,
                                iconTint = FieldSalesColors.DarkGreen,
                                tag = "btn_nav_daily_sales",
                                onClick = { currentSubScreen = FieldSubScreen.DAILY_SALES }
                            )
                        }

                        // 8. Route & GPS
                        item {
                            DashboardModuleCard(
                                title = "Route & GPS",
                                subtitle = "Proximity Map & Visits",
                                icon = Icons.Default.Explore,
                                iconBg = Color(0xFFE0E7FF),
                                iconTint = Color(0xFF4338CA),
                                tag = "btn_nav_route_gps",
                                onClick = { currentSubScreen = FieldSubScreen.ROUTE_GPS }
                            )
                        }

                        // 9. Tour Expenses
                        item {
                            DashboardModuleCard(
                                title = "Tour Expenses",
                                subtitle = "Travel, Food & Lodging",
                                icon = Icons.Default.DirectionsCar,
                                iconBg = FieldSalesColors.EarthyBrownLight,
                                iconTint = FieldSalesColors.EarthyBrown,
                                tag = "btn_nav_tour_expenses",
                                onClick = { currentSubScreen = FieldSubScreen.TOUR_EXPENSES }
                            )
                        }

                        // 10. Owner Portal
                        item {
                            DashboardModuleCard(
                                title = "Owner Portal",
                                subtitle = "Executive Audit & Staff",
                                icon = Icons.Default.AdminPanelSettings,
                                iconBg = Color(0xFFFEF9C3),
                                iconTint = Color(0xFFCA8A04),
                                tag = "btn_nav_owner_portal",
                                onClick = { currentSubScreen = FieldSubScreen.OWNER_PORTAL }
                            )
                        }

                        // Bottom spacing
                        item(span = { GridItemSpan(2) }) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            FieldSubScreen.NEW_CUSTOMER -> {
                NewCustomerEntryScreen(
                    viewModel = viewModel,
                    onBack = { currentSubScreen = FieldSubScreen.DASHBOARD }
                )
            }

            FieldSubScreen.SEARCH_FILTER -> {
                SearchFilterScreen(
                    viewModel = viewModel,
                    onBack = { currentSubScreen = FieldSubScreen.DASHBOARD },
                    onCustomerClick = {
                        currentSubScreen = FieldSubScreen.NEW_CUSTOMER
                    }
                )
            }

            FieldSubScreen.ADD_PRODUCT -> {
                ProductAllocationScreen(
                    viewModel = viewModel,
                    onBack = { currentSubScreen = FieldSubScreen.DASHBOARD }
                )
            }

            FieldSubScreen.NEW_INVOICE -> {
                FieldInvoiceScreen(
                    viewModel = viewModel,
                    onBack = { currentSubScreen = FieldSubScreen.DASHBOARD },
                    onCreateNewCustomerClick = { currentSubScreen = FieldSubScreen.NEW_CUSTOMER }
                )
            }

            FieldSubScreen.DOCUMENTS -> {
                FieldDocumentsScreen(
                    viewModel = viewModel,
                    onBack = { currentSubScreen = FieldSubScreen.DASHBOARD }
                )
            }

            FieldSubScreen.SALES_REPORTS -> {
                FieldSalesReportsScreen(
                    viewModel = viewModel,
                    onBack = { currentSubScreen = FieldSubScreen.DASHBOARD }
                )
            }

            FieldSubScreen.DAILY_SALES -> {
                DailySalesScreen(
                    viewModel = viewModel,
                    onBack = { currentSubScreen = FieldSubScreen.DASHBOARD }
                )
            }

            FieldSubScreen.ROUTE_GPS -> {
                RouteGpsScreen(
                    viewModel = viewModel,
                    onBack = { currentSubScreen = FieldSubScreen.DASHBOARD }
                )
            }

            FieldSubScreen.TOUR_EXPENSES -> {
                TourExpensesScreen(
                    viewModel = viewModel,
                    onBack = { currentSubScreen = FieldSubScreen.DASHBOARD }
                )
            }

            FieldSubScreen.OWNER_PORTAL -> {
                OwnerPortalScreen(
                    viewModel = viewModel,
                    onBack = { currentSubScreen = FieldSubScreen.DASHBOARD }
                )
            }

            FieldSubScreen.LOGIN -> {
                LoginScreen(
                    viewModel = viewModel,
                    onBack = { currentSubScreen = FieldSubScreen.DASHBOARD },
                    onLoginSuccess = { currentSubScreen = FieldSubScreen.DASHBOARD }
                )
            }
        }
    }
}

@Composable
fun DashboardModuleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(tag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, FieldSalesColors.BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }

            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = FieldSalesColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = FieldSalesColors.TextSecondary,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
