package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CompanyProfile
import com.example.data.model.InvoiceWithItems
import com.example.ui.ErpDashboardMetrics
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.AlertAmberBg
import com.example.ui.theme.AlertBlue
import com.example.ui.theme.AlertBlueBg
import com.example.ui.theme.AlertPurple
import com.example.ui.theme.AlertPurpleBg
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedBg
import com.example.ui.theme.PharmaBg
import com.example.ui.theme.PharmaBorder
import com.example.ui.theme.PharmaTextPrimary
import com.example.ui.theme.PharmaTextSecondary
import com.example.ui.theme.VedaEmerald
import com.example.ui.theme.VedaGold
import com.example.ui.theme.VedaGreen
import com.example.ui.theme.VedaGreenDark
import com.example.ui.theme.VedaGreenLight
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    metrics: ErpDashboardMetrics,
    company: CompanyProfile,
    recentInvoices: List<InvoiceWithItems>,
    onNewSaleClick: () -> Unit,
    onNewPurchaseClick: () -> Unit,
    onReceiptClick: () -> Unit,
    onPaymentClick: () -> Unit,
    onCustomerClick: () -> Unit,
    onProductClick: () -> Unit,
    onStockClick: () -> Unit,
    onVedaAiClick: () -> Unit,
    onInvoiceClick: (InvoiceWithItems) -> Unit,
    onViewAllInvoicesClick: () -> Unit,
    onFieldSalesClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PharmaBg)
    ) {
        // App Bar
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🌿",
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = company.companyName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = company.tagline,
                            fontSize = 11.sp,
                            color = Color(0xFFC8E6C9)
                        )
                    }
                }
            },
            actions = {
                IconButton(
                    onClick = onVedaAiClick,
                    modifier = Modifier.testTag("btn_veda_ai_top")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Veda AI Assistant",
                        tint = VedaGold
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = VedaGreenDark
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Live Alerts Section
            item {
                Text(
                    text = "CRITICAL PHARMA ALERTS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PharmaTextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        AlertBadge(
                            icon = Icons.Default.Warning,
                            label = "Near Expiry",
                            count = "${metrics.nearExpiryProductsCount} items",
                            color = AlertAmber,
                            bgColor = AlertAmberBg,
                            onClick = onStockClick
                        )
                    }
                    item {
                        AlertBadge(
                            icon = Icons.Default.Inventory2,
                            label = "Low Stock",
                            count = "${metrics.lowStockCount} items",
                            color = AlertRed,
                            bgColor = AlertRedBg,
                            onClick = onStockClick
                        )
                    }
                    item {
                        AlertBadge(
                            icon = Icons.Default.NotificationsActive,
                            label = "Receivables",
                            count = "₹${String.format(Locale.US, "%,.0f", metrics.outstandingReceivable)}",
                            color = AlertBlue,
                            bgColor = AlertBlueBg,
                            onClick = onCustomerClick
                        )
                    }
                    item {
                        AlertBadge(
                            icon = Icons.Default.CheckCircle,
                            label = "Collections",
                            count = "₹${String.format(Locale.US, "%,.0f", metrics.collections)}",
                            color = VedaEmerald,
                            bgColor = VedaGreenLight,
                            onClick = onReceiptClick
                        )
                    }
                }
            }

            // Dedicated Field Sales Module Entry Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFieldSalesClick() }
                        .testTag("card_field_sales_module"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B4332)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2D6A4F))
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
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8F5E9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📍", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Field Sales & Customer Mgmt",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF65A30D))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("NEW", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Dedicated 2-Column Dashboard • GPS Map • Product Quotas",
                                    fontSize = 11.sp,
                                    color = Color(0xFFA7F3D0)
                                )
                            }
                        }

                        Button(
                            onClick = onFieldSalesClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF65A30D)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Open", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Quick Actions Bar
            item {
                Text(
                    text = "QUICK ACTIONS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PharmaTextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        QuickActionButton(
                            icon = Icons.Default.Explore,
                            label = "Field Sales",
                            containerColor = Color(0xFF1B4332),
                            contentColor = Color.White,
                            onClick = onFieldSalesClick,
                            tag = "quick_field_sales"
                        )
                    }
                    item {
                        QuickActionButton(
                            icon = Icons.Default.Receipt,
                            label = "New Sale",
                            containerColor = VedaGreen,
                            contentColor = Color.White,
                            onClick = onNewSaleClick,
                            tag = "quick_new_sale"
                        )
                    }
                    item {
                        QuickActionButton(
                            icon = Icons.Default.AddShoppingCart,
                            label = "Purchase",
                            containerColor = Color(0xFF1565C0),
                            contentColor = Color.White,
                            onClick = onNewPurchaseClick,
                            tag = "quick_purchase"
                        )
                    }
                    item {
                        QuickActionButton(
                            icon = Icons.Default.Payment,
                            label = "Receipt",
                            containerColor = Color(0xFF2E7D32),
                            contentColor = Color.White,
                            onClick = onReceiptClick,
                            tag = "quick_receipt"
                        )
                    }
                    item {
                        QuickActionButton(
                            icon = Icons.Default.Payment,
                            label = "Expense",
                            containerColor = Color(0xFFC2185B),
                            contentColor = Color.White,
                            onClick = onPaymentClick,
                            tag = "quick_payment"
                        )
                    }
                    item {
                        QuickActionButton(
                            icon = Icons.Default.Business,
                            label = "Parties",
                            containerColor = Color(0xFF00838F),
                            contentColor = Color.White,
                            onClick = onCustomerClick,
                            tag = "quick_party"
                        )
                    }
                    item {
                        QuickActionButton(
                            icon = Icons.Default.Medication,
                            label = "Products",
                            containerColor = Color(0xFFE65100),
                            contentColor = Color.White,
                            onClick = onProductClick,
                            tag = "quick_product"
                        )
                    }
                    item {
                        QuickActionButton(
                            icon = Icons.Default.AutoAwesome,
                            label = "Veda AI",
                            containerColor = VedaGold,
                            contentColor = Color.Black,
                            onClick = onVedaAiClick,
                            tag = "quick_ai"
                        )
                    }
                }
            }

            // Summary Financial Cards Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EXECUTIVE SUMMARY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharmaTextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "LIVE ERP FEED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = VedaEmerald
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Primary 2 Big KPI Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = "TOTAL SALES",
                        amount = "₹${String.format(Locale.US, "%,.2f", metrics.totalSales)}",
                        subtext = "Cash: ₹${metrics.cashSales.toInt()} | Cr: ₹${metrics.creditSales.toInt()}",
                        accentColor = VedaGreen,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "PURCHASES",
                        amount = "₹${String.format(Locale.US, "%,.2f", metrics.totalPurchase)}",
                        subtext = "Stock: ₹${String.format(Locale.US, "%,.0f", metrics.stockValue)}",
                        accentColor = Color(0xFF1E88E5),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary 2 KPI Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = "ESTIMATED PROFIT",
                        amount = "₹${String.format(Locale.US, "%,.2f", metrics.grossProfit)}",
                        subtext = "Net (after exp): ₹${metrics.netProfit.toInt()}",
                        accentColor = VedaEmerald,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "RECEIVABLES",
                        amount = "₹${String.format(Locale.US, "%,.2f", metrics.outstandingReceivable)}",
                        subtext = "Payable: ₹${String.format(Locale.US, "%,.0f", metrics.outstandingPayable)}",
                        accentColor = Color(0xFFD97706),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Recent Bills Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT INVOICES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharmaTextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "View All (${recentInvoices.size}) →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = VedaGreen,
                        modifier = Modifier
                            .clickable { onViewAllInvoicesClick() }
                            .padding(4.dp)
                    )
                }
            }

            // Recent Invoices Items
            if (recentInvoices.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "No invoices generated yet. Tap 'New Sale' to create your first bill!",
                            fontSize = 13.sp,
                            color = PharmaTextSecondary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(recentInvoices.take(5)) { item ->
                    RecentInvoiceRow(
                        invoiceWithItems = item,
                        onClick = { onInvoiceClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun AlertBadge(
    icon: ImageVector,
    label: String,
    count: String,
    color: Color,
    bgColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = Modifier.width(135.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = PharmaTextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = count,
                    fontSize = 12.sp,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    tag: String
) {
    ElevatedButton(
        onClick = onClick,
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        modifier = Modifier.testTag(tag)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    amount: String,
    subtext: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, PharmaBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = PharmaTextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = amount,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtext,
                fontSize = 10.sp,
                color = PharmaTextSecondary
            )
        }
    }
}

@Composable
fun RecentInvoiceRow(
    invoiceWithItems: InvoiceWithItems,
    onClick: () -> Unit
) {
    val inv = invoiceWithItems.invoice
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, PharmaBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = inv.invoiceNumber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = VedaGreen
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (inv.status == "PAID") Color(0xFFE8F5E9) else Color(0xFFFFF3E0))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = inv.status,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (inv.status == "PAID") Color(0xFF2E7D32) else Color(0xFFE65100)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = inv.partyName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = PharmaTextPrimary
                )
                Text(
                    text = "${inv.invoiceDate} • ${invoiceWithItems.items.size} medicines • ${inv.paymentMode}",
                    fontSize = 10.sp,
                    color = PharmaTextSecondary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${String.format(Locale.US, "%,.2f", inv.grandTotal)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PharmaTextPrimary
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "View Bill",
                    tint = PharmaTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
