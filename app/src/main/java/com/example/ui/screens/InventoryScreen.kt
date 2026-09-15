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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.example.data.model.Product
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.AlertRed
import com.example.ui.theme.PharmaBorder
import com.example.ui.theme.PharmaTextPrimary
import com.example.ui.theme.PharmaTextSecondary
import com.example.ui.theme.VedaEmerald
import com.example.ui.theme.VedaGreen
import com.example.ui.theme.VedaGreenDark
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    products: List<Product>,
    onSaveProduct: (Product) -> Unit,
    onDeleteProduct: (Product) -> Unit,
    onAdjustStock: (Long, Int, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var showDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var adjustingProduct by remember { mutableStateOf<Product?>(null) }

    val filteredProducts = products.filter { p ->
        val matchesSearch = searchQuery.isEmpty() ||
                p.name.contains(searchQuery, ignoreCase = true) ||
                p.batchNumber.contains(searchQuery, ignoreCase = true) ||
                p.ayurvedicComposition.contains(searchQuery, ignoreCase = true) ||
                p.rackNumber.contains(searchQuery, ignoreCase = true)

        val matchesCat = when (selectedCategory) {
            "AYURVEDIC" -> p.category.equals("AYURVEDIC", ignoreCase = true) || p.ayurvedicComposition.isNotEmpty()
            "ALLOPATHIC" -> p.category.equals("ALLOPATHIC", ignoreCase = true)
            "LOW_STOCK" -> p.stockQty <= p.reorderLevel
            "NEAR_EXPIRY" -> p.expiryDate.contains("26") || p.expiryDate.contains("25")
            else -> true
        }

        matchesSearch && matchesCat
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Medication, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Medicine Stock & Batches (${products.size})",
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
                    editingProduct = null
                    showDialog = true
                },
                containerColor = VedaGreen,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8FAFC))
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search medicine, batch, composition, rack...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("inventory_search_input"),
                singleLine = true
            )

            // Category filter chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == "ALL",
                        onClick = { selectedCategory = "ALL" },
                        label = { Text("All (${products.size})") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategory == "AYURVEDIC",
                        onClick = { selectedCategory = "AYURVEDIC" },
                        label = { Text("🌿 Ayurvedic Line") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategory == "ALLOPATHIC",
                        onClick = { selectedCategory = "ALLOPATHIC" },
                        label = { Text("💊 Allopathic") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategory == "LOW_STOCK",
                        onClick = { selectedCategory = "LOW_STOCK" },
                        label = { Text("⚠️ Low Stock") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategory == "NEAR_EXPIRY",
                        onClick = { selectedCategory = "NEAR_EXPIRY" },
                        label = { Text("⌛ Near Expiry") }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (filteredProducts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inventory, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No products match your criteria", fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                            Text("Tap + to add medicines with batch, expiry & rack", color = Color(0xFF64748B), fontSize = 13.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredProducts, key = { it.id }) { product ->
                            val isLowStock = product.stockQty <= product.reorderLevel
                            val isNearExp = product.expiryDate.contains("26") || product.expiryDate.contains("25")

                            Card(
                                modifier = Modifier.fillMaxWidth().testTag("product_card_${product.id}"),
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
                                        Text(
                                            text = product.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = VedaGreen
                                        )

                                        if (product.ayurvedicComposition.isNotEmpty()) {
                                            Text(
                                                text = product.ayurvedicComposition,
                                                fontSize = 11.sp,
                                                color = Color(0xFF2E7D32),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Surface(color = Color(0xFFEFF6FF), shape = RoundedCornerShape(4.dp)) {
                                                Text(
                                                    text = "Pack: ${product.pack}",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF1D4ED8),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(4.dp)) {
                                                Text(
                                                    text = "Batch: ${product.batchNumber}",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF475569),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Surface(
                                                color = if (isNearExp) Color(0xFFFEF3C7) else Color(0xFFE8F5E9),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "Exp: ${product.expiryDate}",
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isNearExp) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isNearExp) AlertAmber else VedaEmerald,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "Rack: ${product.rackNumber} • HSN: ${product.hsnCode} • MRP: ₹${String.format(Locale.US, "%.2f", product.mrp)} • Rate: ₹${String.format(Locale.US, "%.2f", product.saleRate)}",
                                            fontSize = 11.sp,
                                            color = PharmaTextSecondary
                                        )

                                        Text(
                                            text = "Stock: ${product.stockQty} ${product.unit} ${if (isLowStock) "(LOW STOCK ALERT!)" else ""}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isLowStock) AlertRed else PharmaTextPrimary
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { adjustingProduct = product },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.CompareArrows, contentDescription = "Adjust Stock", tint = Color(0xFF1565C0), modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(
                                            onClick = {
                                                editingProduct = product
                                                showDialog = true
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = VedaGreen, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(
                                            onClick = { onDeleteProduct(product) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        ProductDialog(
            product = editingProduct,
            onDismiss = { showDialog = false },
            onSave = {
                onSaveProduct(it)
                showDialog = false
            }
        )
    }

    if (adjustingProduct != null) {
        StockAdjustmentDialog(
            product = adjustingProduct!!,
            onDismiss = { adjustingProduct = null },
            onConfirm = { adj, reason ->
                onAdjustStock(adjustingProduct!!.id, adj, reason)
                adjustingProduct = null
            }
        )
    }
}

@Composable
fun StockAdjustmentDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var adjustQtyStr by remember { mutableStateOf("10") }
    var isAddition by remember { mutableStateOf(true) }
    var reason by remember { mutableStateOf("Physical Count Correction") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Adjust Stock: ${product.name}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Current Stock: ${product.stockQty} ${product.unit}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = isAddition,
                        onClick = { isAddition = true },
                        label = { Text("+ Add Stock") }
                    )
                    FilterChip(
                        selected = !isAddition,
                        onClick = { isAddition = false },
                        label = { Text("- Deduct Stock") }
                    )
                }

                OutlinedTextField(
                    value = adjustQtyStr,
                    onValueChange = { adjustQtyStr = it },
                    label = { Text("Quantity to ${if (isAddition) "Add" else "Deduct"}") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Adjustment Reason (e.g. Audit, Damage, Sample)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = adjustQtyStr.toIntOrNull() ?: 0
                    val finalAdj = if (isAddition) qty else -qty
                    onConfirm(finalAdj, reason)
                },
                colors = ButtonDefaults.buttonColors(containerColor = VedaGreen)
            ) {
                Text("Save Adjustment")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ProductDialog(
    product: Product?,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var composition by remember { mutableStateOf(product?.ayurvedicComposition ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "AYURVEDIC") }
    var pack by remember { mutableStateOf(product?.pack ?: "100g") }
    var batchNumber by remember { mutableStateOf(product?.batchNumber ?: "") }
    var expiryDate by remember { mutableStateOf(product?.expiryDate ?: "") }
    var hsnCode by remember { mutableStateOf(product?.hsnCode ?: "30049011") }
    var rackNumber by remember { mutableStateOf(product?.rackNumber ?: "A-1") }
    var mrpStr by remember { mutableStateOf(product?.mrp?.toString() ?: "150.0") }
    var purchaseRateStr by remember { mutableStateOf(product?.purchaseRate?.toString() ?: "80.0") }
    var saleRateStr by remember { mutableStateOf(product?.saleRate?.toString() ?: "120.0") }
    var stockStr by remember { mutableStateOf(product?.stockQty?.toString() ?: "50") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (product == null) "New Medicine Master" else "Edit Medicine",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = VedaGreen
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = category == "AYURVEDIC",
                        onClick = { category = "AYURVEDIC" },
                        label = { Text("🌿 Ayurvedic") }
                    )
                    FilterChip(
                        selected = category == "ALLOPATHIC",
                        onClick = { category = "ALLOPATHIC" },
                        label = { Text("💊 Allopathic") }
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Medicine / Formulation Name") },
                    modifier = Modifier.fillMaxWidth().testTag("product_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = composition,
                    onValueChange = { composition = it },
                    label = { Text("Ayurvedic Composition / Salt") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pack,
                        onValueChange = { pack = it },
                        label = { Text("Pack") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = rackNumber,
                        onValueChange = { rackNumber = it },
                        label = { Text("Rack / Shelf") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = batchNumber,
                        onValueChange = { batchNumber = it },
                        label = { Text("Batch No.") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { expiryDate = it },
                        label = { Text("Expiry (MM/YY)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hsnCode,
                        onValueChange = { hsnCode = it },
                        label = { Text("HSN Code") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = stockStr,
                        onValueChange = { stockStr = it },
                        label = { Text("Stock Qty") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = purchaseRateStr,
                        onValueChange = { purchaseRateStr = it },
                        label = { Text("Purchase ₹") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = saleRateStr,
                        onValueChange = { saleRateStr = it },
                        label = { Text("Sale Rate ₹") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = mrpStr,
                        onValueChange = { mrpStr = it },
                        label = { Text("M.R.P ₹") },
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
                                    product?.copy(
                                        name = name,
                                        composition = composition,
                                        category = category,
                                        pack = pack,
                                        rackNumber = rackNumber,
                                        batchNumber = batchNumber,
                                        expiryDate = expiryDate,
                                        hsnCode = hsnCode,
                                        mrp = mrpStr.toDoubleOrNull() ?: 0.0,
                                        purchaseRate = purchaseRateStr.toDoubleOrNull() ?: 0.0,
                                        saleRate = saleRateStr.toDoubleOrNull() ?: 0.0,
                                        stockQty = stockStr.toIntOrNull() ?: 0
                                    ) ?: Product(
                                        name = name,
                                        composition = composition,
                                        category = category,
                                        pack = pack,
                                        rackNumber = rackNumber,
                                        batchNumber = batchNumber,
                                        expiryDate = expiryDate,
                                        hsnCode = hsnCode,
                                        mrp = mrpStr.toDoubleOrNull() ?: 0.0,
                                        purchaseRate = purchaseRateStr.toDoubleOrNull() ?: 0.0,
                                        saleRate = saleRateStr.toDoubleOrNull() ?: 0.0,
                                        stockQty = stockStr.toIntOrNull() ?: 0
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VedaGreen)
                    ) {
                        Text("Save Medicine")
                    }
                }
            }
        }
    }
}
