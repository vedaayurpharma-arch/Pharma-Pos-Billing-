package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AccountsScreen
import com.example.ui.screens.CompanySettingsScreen
import com.example.ui.screens.CreateEditInvoiceScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GstCenterScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.InvoiceDesignerScreen
import com.example.ui.screens.InvoiceListScreen
import com.example.ui.screens.LandscapeBillPreviewScreen
import com.example.ui.screens.PartiesScreen
import com.example.ui.screens.PurchaseScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.VedaAiScreen
import com.example.ui.theme.PharmaBorder
import com.example.ui.theme.PharmaTextPrimary
import com.example.ui.theme.PharmaTextSecondary
import com.example.ui.theme.VedaGold
import com.example.ui.theme.VedaGoldLight
import com.example.ui.theme.VedaGreen
import com.example.ui.theme.VedaGreenDark

enum class AppScreen {
    DASHBOARD,
    INVOICE_LIST,
    CREATE_EDIT_INVOICE,
    LANDSCAPE_BILL_PREVIEW,
    PURCHASE,
    INVENTORY,
    ACCOUNTS,
    GST_CENTER,
    INVOICE_DESIGNER,
    REPORTS,
    VEDA_AI,
    PARTIES,
    COMPANY_SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    viewModel: BillingViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }
    var showMoreHubSheet by remember { mutableStateOf(false) }

    val filteredInvoices by viewModel.filteredInvoices.collectAsState()
    val allInvoices by viewModel.allInvoices.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val companyProfile by viewModel.companyProfile.collectAsState()
    val allParties by viewModel.allParties.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val allPurchases by viewModel.allPurchases.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val dashboardMetrics by viewModel.dashboardMetrics.collectAsState()
    val designerConfig by viewModel.designerConfig.collectAsState()
    val aiQueries by viewModel.aiQueries.collectAsState()
    val selectedInvoice by viewModel.selectedInvoice.collectAsState()
    val draftInvoice by viewModel.draftInvoice.collectAsState()

    val primaryNavigationScreens = listOf(
        AppScreen.DASHBOARD,
        AppScreen.INVOICE_LIST,
        AppScreen.INVENTORY,
        AppScreen.PURCHASE
    )

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = currentScreen in primaryNavigationScreens,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                NavigationBar(
                    containerColor = Color.White,
                    contentColor = VedaGreen
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.DASHBOARD,
                        onClick = { currentScreen = AppScreen.DASHBOARD },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Dashboard", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VedaGreen,
                            selectedTextColor = VedaGreen,
                            indicatorColor = Color(0xFFE8F5E9)
                        ),
                        modifier = Modifier.testTag("nav_dashboard")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.INVOICE_LIST,
                        onClick = { currentScreen = AppScreen.INVOICE_LIST },
                        icon = { Icon(Icons.Default.Receipt, contentDescription = "Bills") },
                        label = { Text("Sales", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VedaGreen,
                            selectedTextColor = VedaGreen,
                            indicatorColor = Color(0xFFE8F5E9)
                        ),
                        modifier = Modifier.testTag("nav_bills")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.INVENTORY,
                        onClick = { currentScreen = AppScreen.INVENTORY },
                        icon = { Icon(Icons.Default.Medication, contentDescription = "Stock") },
                        label = { Text("Stock", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VedaGreen,
                            selectedTextColor = VedaGreen,
                            indicatorColor = Color(0xFFE8F5E9)
                        ),
                        modifier = Modifier.testTag("nav_inventory")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.PURCHASE,
                        onClick = { currentScreen = AppScreen.PURCHASE },
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Purchase") },
                        label = { Text("Purchase", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VedaGreen,
                            selectedTextColor = VedaGreen,
                            indicatorColor = Color(0xFFE8F5E9)
                        ),
                        modifier = Modifier.testTag("nav_purchase")
                    )

                    NavigationBarItem(
                        selected = false,
                        onClick = { showMoreHubSheet = true },
                        icon = { Icon(Icons.Default.Apps, contentDescription = "ERP Hub") },
                        label = { Text("ERP Hub", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VedaGreen,
                            selectedTextColor = VedaGreen,
                            indicatorColor = Color(0xFFE8F5E9)
                        ),
                        modifier = Modifier.testTag("nav_erp_hub")
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentScreen) {
                AppScreen.DASHBOARD -> {
                    DashboardScreen(
                        metrics = dashboardMetrics,
                        company = companyProfile,
                        recentInvoices = allInvoices.take(5),
                        onNewSaleClick = {
                            viewModel.initNewDraft()
                            currentScreen = AppScreen.CREATE_EDIT_INVOICE
                        },
                        onNewPurchaseClick = { currentScreen = AppScreen.PURCHASE },
                        onReceiptClick = { currentScreen = AppScreen.ACCOUNTS },
                        onPaymentClick = { currentScreen = AppScreen.ACCOUNTS },
                        onCustomerClick = { currentScreen = AppScreen.PARTIES },
                        onProductClick = { currentScreen = AppScreen.INVENTORY },
                        onStockClick = { currentScreen = AppScreen.INVENTORY },
                        onVedaAiClick = { currentScreen = AppScreen.VEDA_AI },
                        onInvoiceClick = { invoiceWithItems ->
                            viewModel.selectInvoice(invoiceWithItems)
                            currentScreen = AppScreen.LANDSCAPE_BILL_PREVIEW
                        },
                        onViewAllInvoicesClick = { currentScreen = AppScreen.INVOICE_LIST }
                    )
                }

                AppScreen.INVOICE_LIST -> {
                    InvoiceListScreen(
                        invoices = filteredInvoices,
                        searchQuery = searchQuery,
                        statusFilter = statusFilter,
                        companyProfile = companyProfile,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onFilterChange = { viewModel.setStatusFilter(it) },
                        onInvoiceClick = { invoiceWithItems ->
                            viewModel.selectInvoice(invoiceWithItems)
                            currentScreen = AppScreen.LANDSCAPE_BILL_PREVIEW
                        },
                        onEditInvoice = { invoiceWithItems ->
                            viewModel.editInvoice(invoiceWithItems)
                            currentScreen = AppScreen.CREATE_EDIT_INVOICE
                        },
                        onDeleteInvoice = { invoice ->
                            viewModel.deleteInvoice(invoice)
                        },
                        onCreateInvoice = {
                            viewModel.initNewDraft()
                            currentScreen = AppScreen.CREATE_EDIT_INVOICE
                        }
                    )
                }

                AppScreen.CREATE_EDIT_INVOICE -> {
                    CreateEditInvoiceScreen(
                        draftInvoice = draftInvoice,
                        savedParties = allParties,
                        savedProducts = allProducts,
                        onSelectParty = { party ->
                            viewModel.selectPartyForDraft(party)
                        },
                        onUpdateHeader = { invNum, invDate, dueDate, ordNo, ordDate, pName, pAddr, pPhone, pDl, pGst, status, crDr ->
                            viewModel.updateDraftInvoiceHeader(
                                invNum, invDate, dueDate, ordNo, ordDate,
                                "TAX_INVOICE", pName, pAddr, pPhone, pDl, pGst,
                                "CREDIT", status, crDr
                            )
                        },
                        onAddItem = { item ->
                            viewModel.addDraftItem(item)
                        },
                        onUpdateItem = { index, item ->
                            viewModel.updateDraftItem(index, item)
                        },
                        onRemoveItem = { index ->
                            viewModel.removeDraftItem(index)
                        },
                        onSaveAndPreview = {
                            viewModel.saveDraftInvoice {
                                currentScreen = AppScreen.LANDSCAPE_BILL_PREVIEW
                            }
                        },
                        onBack = {
                            currentScreen = AppScreen.INVOICE_LIST
                        }
                    )
                }

                AppScreen.LANDSCAPE_BILL_PREVIEW -> {
                    LandscapeBillPreviewScreen(
                        invoiceWithItems = selectedInvoice,
                        companyProfile = companyProfile,
                        onBack = {
                            currentScreen = AppScreen.INVOICE_LIST
                        }
                    )
                }

                AppScreen.PURCHASE -> {
                    PurchaseScreen(
                        purchases = allPurchases,
                        suppliers = allParties.filter { it.type == "SUPPLIER" },
                        onBack = { currentScreen = AppScreen.DASHBOARD },
                        onSavePurchase = { purchase, items ->
                            viewModel.savePurchaseRecord(purchase, items) {}
                        },
                        onDeletePurchase = { purchase ->
                            viewModel.deletePurchase(purchase)
                        }
                    )
                }

                AppScreen.INVENTORY -> {
                    InventoryScreen(
                        products = allProducts,
                        onSaveProduct = { viewModel.saveProduct(it) },
                        onDeleteProduct = { viewModel.deleteProduct(it) },
                        onAdjustStock = { id, adj, reason ->
                            viewModel.adjustStock(id, adj, reason)
                        }
                    )
                }

                AppScreen.ACCOUNTS -> {
                    AccountsScreen(
                        transactions = allTransactions,
                        onSaveTransaction = { viewModel.saveAccountsTransaction(it) },
                        onDeleteTransaction = { viewModel.deleteAccountsTransaction(it) },
                        onBack = { currentScreen = AppScreen.DASHBOARD }
                    )
                }

                AppScreen.GST_CENTER -> {
                    GstCenterScreen(
                        invoices = allInvoices,
                        company = companyProfile,
                        onBack = { currentScreen = AppScreen.DASHBOARD }
                    )
                }

                AppScreen.INVOICE_DESIGNER -> {
                    InvoiceDesignerScreen(
                        currentConfig = designerConfig,
                        company = companyProfile,
                        sampleInvoice = selectedInvoice ?: allInvoices.firstOrNull(),
                        onBack = { currentScreen = AppScreen.DASHBOARD },
                        onSaveConfig = { viewModel.updateDesignerConfig(it) },
                        onResetDefault = { viewModel.resetDesignerConfigToDefault() },
                        onApplyPreset = { viewModel.applyDesignerPreset(it) }
                    )
                }

                AppScreen.REPORTS -> {
                    ReportsScreen(
                        invoices = allInvoices,
                        purchases = allPurchases,
                        products = allProducts,
                        parties = allParties,
                        onBack = { currentScreen = AppScreen.DASHBOARD }
                    )
                }

                AppScreen.VEDA_AI -> {
                    VedaAiScreen(
                        queries = aiQueries,
                        onAskQuestion = { viewModel.askAiQuestion(it) },
                        onBack = { currentScreen = AppScreen.DASHBOARD },
                        onNavigateAction = { label ->
                            when {
                                label.contains("Bill", ignoreCase = true) -> currentScreen = AppScreen.INVOICE_LIST
                                label.contains("Expiry", ignoreCase = true) -> currentScreen = AppScreen.REPORTS
                                label.contains("Stock", ignoreCase = true) -> currentScreen = AppScreen.INVENTORY
                                label.contains("Reminder", ignoreCase = true) -> currentScreen = AppScreen.PARTIES
                                label.contains("Purchase", ignoreCase = true) -> currentScreen = AppScreen.PURCHASE
                                else -> currentScreen = AppScreen.DASHBOARD
                            }
                        }
                    )
                }

                AppScreen.PARTIES -> {
                    PartiesScreen(
                        parties = allParties,
                        onSaveParty = { viewModel.saveParty(it) },
                        onDeleteParty = { viewModel.deleteParty(it) }
                    )
                }

                AppScreen.COMPANY_SETTINGS -> {
                    CompanySettingsScreen(
                        currentProfile = companyProfile,
                        onSaveProfile = { viewModel.saveCompanyProfile(it) }
                    )
                }
            }
        }
    }

    // ERP Hub Modal Sheet for Quick Access to All Modules
    if (showMoreHubSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreHubSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Veda ERP Navigation Hub",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = VedaGreenDark
                )
                Text(
                    text = "Complete Pharma & Ayurvedic Management Suite",
                    fontSize = 12.sp,
                    color = PharmaTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    item {
                        HubGridItem(
                            icon = Icons.Default.AutoAwesome,
                            title = "Veda AI",
                            iconColor = Color(0xFFB45309),
                            bgColor = VedaGoldLight,
                            onClick = {
                                showMoreHubSheet = false
                                currentScreen = AppScreen.VEDA_AI
                            }
                        )
                    }
                    item {
                        HubGridItem(
                            icon = Icons.Default.Palette,
                            title = "Designer (20+)",
                            iconColor = Color(0xFF7C3AED),
                            bgColor = Color(0xFFF3E8FF),
                            onClick = {
                                showMoreHubSheet = false
                                currentScreen = AppScreen.INVOICE_DESIGNER
                            }
                        )
                    }
                    item {
                        HubGridItem(
                            icon = Icons.Default.AccountBalance,
                            title = "Accounts",
                            iconColor = Color(0xFF1565C0),
                            bgColor = Color(0xFFEFF6FF),
                            onClick = {
                                showMoreHubSheet = false
                                currentScreen = AppScreen.ACCOUNTS
                            }
                        )
                    }
                    item {
                        HubGridItem(
                            icon = Icons.Default.Assessment,
                            title = "GST Center",
                            iconColor = Color(0xFF2E7D32),
                            bgColor = Color(0xFFE8F5E9),
                            onClick = {
                                showMoreHubSheet = false
                                currentScreen = AppScreen.GST_CENTER
                            }
                        )
                    }
                    item {
                        HubGridItem(
                            icon = Icons.Default.Analytics,
                            title = "Reports & BI",
                            iconColor = Color(0xFFD97706),
                            bgColor = Color(0xFFFEF3C7),
                            onClick = {
                                showMoreHubSheet = false
                                currentScreen = AppScreen.REPORTS
                            }
                        )
                    }
                    item {
                        HubGridItem(
                            icon = Icons.Default.Business,
                            title = "Parties / Chemists",
                            iconColor = Color(0xFF0D9488),
                            bgColor = Color(0xFFCCFBF1),
                            onClick = {
                                showMoreHubSheet = false
                                currentScreen = AppScreen.PARTIES
                            }
                        )
                    }
                    item {
                        HubGridItem(
                            icon = Icons.Default.Settings,
                            title = "Settings",
                            iconColor = Color(0xFF475569),
                            bgColor = Color(0xFFF1F5F9),
                            onClick = {
                                showMoreHubSheet = false
                                currentScreen = AppScreen.COMPANY_SETTINGS
                            }
                        )
                    }
                    item {
                        HubGridItem(
                            icon = Icons.Default.ShoppingCart,
                            title = "Purchases",
                            iconColor = Color(0xFF059669),
                            bgColor = Color(0xFFD1FAE5),
                            onClick = {
                                showMoreHubSheet = false
                                currentScreen = AppScreen.PURCHASE
                            }
                        )
                    }
                    item {
                        HubGridItem(
                            icon = Icons.Default.Receipt,
                            title = "New Bill",
                            iconColor = Color(0xFF2563EB),
                            bgColor = Color(0xFFDBEAFE),
                            onClick = {
                                showMoreHubSheet = false
                                viewModel.initNewDraft()
                                currentScreen = AppScreen.CREATE_EDIT_INVOICE
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HubGridItem(
    icon: ImageVector,
    title: String,
    iconColor: Color,
    bgColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, PharmaBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = PharmaTextPrimary
            )
        }
    }
}
