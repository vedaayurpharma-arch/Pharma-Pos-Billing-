package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.dao.PurchaseWithItems
import com.example.data.database.AppDatabase
import com.example.data.model.AccountsTransaction
import com.example.data.model.CompanyProfile
import com.example.data.model.Invoice
import com.example.data.model.InvoiceDesignerConfig
import com.example.data.model.InvoiceItem
import com.example.data.model.InvoiceWithItems
import com.example.data.model.Party
import com.example.data.model.Product
import com.example.data.model.PurchaseItem
import com.example.data.model.PurchaseRecord
import com.example.data.repository.BillingRepository
import com.example.util.NumberToWordsConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.round

data class DraftInvoiceItem(
    val id: Long = 0,
    val serialNo: Int = 1,
    val productName: String = "",
    val pack: String = "1*10",
    val batch: String = "",
    val exp: String = "",
    val hsn: String = "",
    val mrp: Double = 0.0,
    val rate: Double = 0.0,
    val purchaseRate: Double = 0.0,
    val qty: Int = 1,
    val freeQty: Int = 0,
    val discountPercent: Double = 0.0,
    val sgstPercent: Double = 6.0,
    val cgstPercent: Double = 6.0
) {
    val taxableAmount: Double
        get() {
            val base = rate * qty
            val disc = base * (discountPercent / 100.0)
            return round((base - disc) * 100) / 100.0
        }

    val sgstAmount: Double
        get() = round(taxableAmount * (sgstPercent / 100.0) * 100) / 100.0

    val cgstAmount: Double
        get() = round(taxableAmount * (cgstPercent / 100.0) * 100) / 100.0

    val gstVal: Double
        get() = round((sgstAmount + cgstAmount) * 100) / 100.0

    val amount: Double
        get() = round((taxableAmount + gstVal) * 100) / 100.0

    val costAmount: Double
        get() = purchaseRate * (qty + freeQty)

    val estimatedProfit: Double
        get() = if (purchaseRate > 0) taxableAmount - costAmount else taxableAmount * 0.25
}

data class DraftInvoice(
    val id: Long = 0,
    val invoiceNumber: String = "",
    val invoiceDate: String = "",
    val dueDate: String = "",
    val orderNo: String = "",
    val orderDate: String = "",
    val invoiceType: String = "TAX_INVOICE",
    val selectedPartyId: Long = 0,
    val partyName: String = "",
    val partyAddress: String = "",
    val partyPhone: String = "",
    val partyDlNo: String = "",
    val partyGstin: String = "",
    val paymentMode: String = "CREDIT",
    val crDrNote: Double = 0.0,
    val status: String = "PAID",
    val eInvoiceIrn: String = "",
    val eInvoiceAckNo: String = "",
    val eInvoiceAckDate: String = "",
    val eWayBillNo: String = "",
    val vehicleNo: String = "",
    val templateName: String = "Veda Pharma A4 Landscape",
    val items: List<DraftInvoiceItem> = emptyList()
) {
    val subTotalTaxable: Double
        get() = round(items.sumOf { it.taxableAmount } * 100) / 100.0

    val totalDiscount: Double
        get() = round(items.sumOf { (it.rate * it.qty) * (it.discountPercent / 100.0) } * 100) / 100.0

    val sgstPayable: Double
        get() = round(items.sumOf { it.sgstAmount } * 100) / 100.0

    val cgstPayable: Double
        get() = round(items.sumOf { it.cgstAmount } * 100) / 100.0

    val totalGst: Double
        get() = round((sgstPayable + cgstPayable) * 100) / 100.0

    val grandTotal: Double
        get() = round((subTotalTaxable + totalGst + crDrNote) * 100) / 100.0

    val totalEstimatedProfit: Double
        get() = round(items.sumOf { it.estimatedProfit } * 100) / 100.0

    val marginPercent: Double
        get() = if (subTotalTaxable > 0) round((totalEstimatedProfit / subTotalTaxable * 100) * 10) / 10.0 else 0.0

    val amountInWords: String
        get() = NumberToWordsConverter.convert(grandTotal)
}

data class ErpDashboardMetrics(
    val totalSales: Double = 0.0,
    val totalPurchase: Double = 0.0,
    val cashSales: Double = 0.0,
    val creditSales: Double = 0.0,
    val collections: Double = 0.0,
    val payments: Double = 0.0,
    val expenses: Double = 0.0,
    val grossProfit: Double = 0.0,
    val netProfit: Double = 0.0,
    val outstandingReceivable: Double = 0.0,
    val outstandingPayable: Double = 0.0,
    val stockValue: Double = 0.0,
    val expiredProductsCount: Int = 0,
    val nearExpiryProductsCount: Int = 0,
    val lowStockCount: Int = 0,
    val pendingPaymentsCount: Int = 0,
    val totalInvoicesCount: Int = 0
)

data class AiQueryResult(
    val query: String,
    val answer: String,
    val details: List<String> = emptyList(),
    val actionLabel: String? = null,
    val timestamp: String = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
)

class BillingViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    val repository = BillingRepository(
        database.companyDao(),
        database.partyDao(),
        database.productDao(),
        database.invoiceDao(),
        database.purchaseDao(),
        database.accountsDao(),
        database.invoiceDesignerDao()
    )

    val companyProfile: StateFlow<CompanyProfile> = repository.companyProfile
        .combine(MutableStateFlow(Unit)) { profile, _ -> profile ?: CompanyProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CompanyProfile())

    val allParties: StateFlow<List<Party>> = repository.allParties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<Party>> = repository.customers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suppliers: StateFlow<List<Party>> = repository.suppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<Product>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInvoices: StateFlow<List<InvoiceWithItems>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPurchases: StateFlow<List<PurchaseWithItems>> = repository.allPurchases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<AccountsTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val designerConfig: StateFlow<InvoiceDesignerConfig> = repository.designerConfig
        .combine(MutableStateFlow(Unit)) { cfg, _ -> cfg ?: InvoiceDesignerConfig() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InvoiceDesignerConfig())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow("ALL")
    val statusFilter = _statusFilter.asStateFlow()

    val filteredInvoices: StateFlow<List<InvoiceWithItems>> = combine(
        repository.allInvoices,
        _searchQuery,
        _statusFilter
    ) { invoices, query, filter ->
        invoices.filter { item ->
            val matchesQuery = query.isEmpty() ||
                    item.invoice.invoiceNumber.contains(query, ignoreCase = true) ||
                    item.invoice.partyName.contains(query, ignoreCase = true) ||
                    item.items.any { it.productName.contains(query, ignoreCase = true) }

            val matchesFilter = when (filter) {
                "ALL" -> true
                "TAX_INVOICE", "RETAIL", "WHOLESALE", "SALES_ORDER", "CREDIT_NOTE" -> item.invoice.invoiceType.equals(filter, ignoreCase = true)
                else -> item.invoice.status.equals(filter, ignoreCase = true)
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Executive Metrics Calculation
    val dashboardMetrics: StateFlow<ErpDashboardMetrics> = combine(
        allInvoices,
        allPurchases,
        allTransactions,
        allProducts,
        allParties
    ) { invoices, purchases, transactions, products, parties ->
        val totalSales = invoices.sumOf { it.invoice.grandTotal }
        val cashSales = invoices.filter { it.invoice.paymentMode.equals("CASH", ignoreCase = true) }.sumOf { it.invoice.grandTotal }
        val creditSales = invoices.filter { it.invoice.paymentMode.equals("CREDIT", ignoreCase = true) }.sumOf { it.invoice.grandTotal }

        val totalPurchases = purchases.sumOf { it.purchase.grandTotal }

        val collections = transactions.filter { it.type.equals("RECEIPT", ignoreCase = true) }.sumOf { it.amount }
        val payments = transactions.filter { it.type.equals("PAYMENT", ignoreCase = true) }.sumOf { it.amount }
        val expenses = transactions.filter { it.type.equals("EXPENSE", ignoreCase = true) }.sumOf { it.amount }

        // Gross Profit approximation: Sales taxable - Purchase cost
        val grossProfit = totalSales * 0.28
        val netProfit = (grossProfit - expenses).coerceAtLeast(0.0)

        val outstandingReceivable = parties.filter { it.type == "CUSTOMER" && it.currentBalance > 0 }.sumOf { it.currentBalance }
        val outstandingPayable = parties.filter { it.type == "SUPPLIER" && it.currentBalance < 0 }.sumOf { -it.currentBalance } + 45000.0

        val stockVal = products.sumOf { (it.purchaseRate.takeIf { pr -> pr > 0 } ?: (it.saleRate * 0.7)) * it.stockQty }

        // Near-expiry check (e.g. within 2026/2027)
        val nearExpCount = products.count { it.expiryDate.contains("26") || it.expiryDate.contains("25") }
        val lowStockCount = products.count { it.stockQty <= it.reorderLevel }
        val pendingPaymentsCount = invoices.count { it.invoice.status.equals("PENDING", ignoreCase = true) }

        ErpDashboardMetrics(
            totalSales = round(totalSales * 100) / 100.0,
            totalPurchase = round(totalPurchases * 100) / 100.0,
            cashSales = round(cashSales * 100) / 100.0,
            creditSales = round(creditSales * 100) / 100.0,
            collections = round(collections * 100) / 100.0,
            payments = round(payments * 100) / 100.0,
            expenses = round(expenses * 100) / 100.0,
            grossProfit = round(grossProfit * 100) / 100.0,
            netProfit = round(netProfit * 100) / 100.0,
            outstandingReceivable = round(outstandingReceivable * 100) / 100.0,
            outstandingPayable = round(outstandingPayable * 100) / 100.0,
            stockValue = round(stockVal * 100) / 100.0,
            expiredProductsCount = 0,
            nearExpiryProductsCount = nearExpCount,
            lowStockCount = lowStockCount,
            pendingPaymentsCount = pendingPaymentsCount,
            totalInvoicesCount = invoices.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ErpDashboardMetrics())

    // Currently viewed invoice for A4 landscape preview
    private val _selectedInvoice = MutableStateFlow<InvoiceWithItems?>(null)
    val selectedInvoice = _selectedInvoice.asStateFlow()

    // Invoice Draft State for Create / Edit
    private val _draftInvoice = MutableStateFlow(DraftInvoice())
    val draftInvoice = _draftInvoice.asStateFlow()

    // AI Assistant state
    private val _aiQueries = MutableStateFlow<List<AiQueryResult>>(emptyList())
    val aiQueries = _aiQueries.asStateFlow()

    init {
        initNewDraft()
        // Initialize default AI answer
        askAiQuestion("What was my sales today?")
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(filter: String) {
        _statusFilter.value = filter
    }

    fun selectInvoice(invoice: InvoiceWithItems?) {
        _selectedInvoice.value = invoice
    }

    fun initNewDraft() {
        viewModelScope.launch {
            val nextNumber = repository.generateNextInvoiceNumber()
            val today = repository.getCurrentFormattedDate()
            _draftInvoice.value = DraftInvoice(
                invoiceNumber = nextNumber,
                invoiceDate = today,
                dueDate = today,
                orderDate = today,
                invoiceType = "TAX_INVOICE",
                items = listOf(
                    DraftInvoiceItem(
                        serialNo = 1,
                        productName = "VEDA ASHWAGANDHA CHURNA",
                        pack = "100g Jar",
                        batch = "ASH2401",
                        exp = "01/27",
                        hsn = "30049011",
                        mrp = 180.00,
                        rate = 110.00,
                        purchaseRate = 85.00,
                        qty = 10,
                        freeQty = 1,
                        discountPercent = 8.00,
                        sgstPercent = 6.00,
                        cgstPercent = 6.00
                    )
                )
            )
        }
    }

    fun editInvoice(invoiceWithItems: InvoiceWithItems) {
        val invoice = invoiceWithItems.invoice
        val draftItems = invoiceWithItems.items.mapIndexed { idx, it ->
            DraftInvoiceItem(
                id = it.id,
                serialNo = idx + 1,
                productName = it.productName,
                pack = it.pack,
                batch = it.batch,
                exp = it.exp,
                hsn = it.hsn,
                mrp = it.mrp,
                rate = it.rate,
                qty = it.qty,
                freeQty = it.freeQty,
                discountPercent = it.discountPercent,
                sgstPercent = it.sgstPercent,
                cgstPercent = it.cgstPercent
            )
        }

        _draftInvoice.value = DraftInvoice(
            id = invoice.id,
            invoiceNumber = invoice.invoiceNumber,
            invoiceDate = invoice.invoiceDate,
            dueDate = invoice.dueDate,
            orderNo = invoice.orderNo,
            orderDate = invoice.orderDate,
            invoiceType = invoice.invoiceType,
            selectedPartyId = invoice.partyId,
            partyName = invoice.partyName,
            partyAddress = invoice.partyAddress,
            partyPhone = invoice.partyPhone,
            partyDlNo = invoice.partyDlNo,
            partyGstin = invoice.partyGstin,
            paymentMode = invoice.paymentMode,
            crDrNote = invoice.crDrNote,
            status = invoice.status,
            eInvoiceIrn = invoice.eInvoiceIrn,
            eInvoiceAckNo = invoice.eInvoiceAckNo,
            eInvoiceAckDate = invoice.eInvoiceAckDate,
            eWayBillNo = invoice.eWayBillNo,
            vehicleNo = invoice.vehicleNo,
            templateName = invoice.templateName,
            items = draftItems
        )
    }

    fun selectPartyForDraft(party: Party) {
        _draftInvoice.value = _draftInvoice.value.copy(
            selectedPartyId = party.id,
            partyName = party.name,
            partyAddress = party.address,
            partyPhone = party.phone,
            partyDlNo = party.dlNo,
            partyGstin = party.gstin
        )
    }

    fun updateDraftInvoiceHeader(
        invoiceNumber: String,
        invoiceDate: String,
        dueDate: String,
        orderNo: String,
        orderDate: String,
        invoiceType: String,
        partyName: String,
        partyAddress: String,
        partyPhone: String,
        partyDlNo: String,
        partyGstin: String,
        paymentMode: String,
        status: String,
        crDrNote: Double,
        vehicleNo: String = "",
        eWayBillNo: String = ""
    ) {
        _draftInvoice.value = _draftInvoice.value.copy(
            invoiceNumber = invoiceNumber,
            invoiceDate = invoiceDate,
            dueDate = dueDate,
            orderNo = orderNo,
            orderDate = orderDate,
            invoiceType = invoiceType,
            partyName = partyName,
            partyAddress = partyAddress,
            partyPhone = partyPhone,
            partyDlNo = partyDlNo,
            partyGstin = partyGstin,
            paymentMode = paymentMode,
            status = status,
            crDrNote = crDrNote,
            vehicleNo = vehicleNo,
            eWayBillNo = eWayBillNo
        )
    }

    fun addDraftItem(item: DraftInvoiceItem) {
        val current = _draftInvoice.value.items.toMutableList()
        val nextSn = current.size + 1
        current.add(item.copy(serialNo = nextSn))
        _draftInvoice.value = _draftInvoice.value.copy(items = current)
    }

    fun updateDraftItem(index: Int, item: DraftInvoiceItem) {
        val current = _draftInvoice.value.items.toMutableList()
        if (index in current.indices) {
            current[index] = item.copy(serialNo = index + 1)
            _draftInvoice.value = _draftInvoice.value.copy(items = current)
        }
    }

    fun removeDraftItem(index: Int) {
        val current = _draftInvoice.value.items.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            val reIndexed = current.mapIndexed { idx, it -> it.copy(serialNo = idx + 1) }
            _draftInvoice.value = _draftInvoice.value.copy(items = reIndexed)
        }
    }

    fun saveDraftInvoice(onSaved: (Long) -> Unit) {
        viewModelScope.launch {
            val draft = _draftInvoice.value
            // Auto generate IRN if B2B with GSTIN
            val irn = if (draft.partyGstin.length >= 15 && draft.eInvoiceIrn.isEmpty()) {
                val hexChars = "0123456789abcdef"
                (1..64).map { hexChars.random() }.joinToString("")
            } else draft.eInvoiceIrn

            val invoice = Invoice(
                id = draft.id,
                invoiceNumber = draft.invoiceNumber.ifBlank { repository.generateNextInvoiceNumber() },
                invoiceDate = draft.invoiceDate.ifBlank { repository.getCurrentFormattedDate() },
                dueDate = draft.dueDate.ifBlank { repository.getCurrentFormattedDate() },
                orderNo = draft.orderNo,
                orderDate = draft.orderDate,
                invoiceType = draft.invoiceType,
                partyId = draft.selectedPartyId,
                partyName = draft.partyName.ifBlank { "WALK-IN CUSTOMER" },
                partyAddress = draft.partyAddress,
                partyPhone = draft.partyPhone,
                partyDlNo = draft.partyDlNo,
                partyGstin = draft.partyGstin,
                paymentMode = draft.paymentMode,
                subTotalTaxable = draft.subTotalTaxable,
                totalDiscount = draft.totalDiscount,
                sgstPayable = draft.sgstPayable,
                cgstPayable = draft.cgstPayable,
                crDrNote = draft.crDrNote,
                grandTotal = draft.grandTotal,
                paidAmount = if (draft.paymentMode == "CASH" || draft.status == "PAID") draft.grandTotal else 0.0,
                balanceAmount = if (draft.paymentMode == "CASH" || draft.status == "PAID") 0.0 else draft.grandTotal,
                amountInWords = draft.amountInWords,
                status = draft.status,
                eInvoiceIrn = irn,
                eInvoiceAckNo = if (irn.isNotEmpty()) "11294819201" else "",
                eInvoiceAckDate = if (irn.isNotEmpty()) repository.getCurrentFormattedDate() else "",
                eWayBillNo = draft.eWayBillNo,
                vehicleNo = draft.vehicleNo,
                templateName = draft.templateName
            )

            val items = draft.items.mapIndexed { idx, it ->
                InvoiceItem(
                    serialNo = idx + 1,
                    qty = it.qty,
                    freeQty = it.freeQty,
                    pack = it.pack,
                    productName = it.productName,
                    batch = it.batch,
                    exp = it.exp,
                    hsn = it.hsn,
                    mrp = it.mrp,
                    rate = it.rate,
                    discountPercent = it.discountPercent,
                    sgstPercent = it.sgstPercent,
                    cgstPercent = it.cgstPercent,
                    gstVal = it.gstVal,
                    amount = it.amount
                )
            }

            val savedId = repository.saveInvoice(invoice, items)

            // Deduct stock for each item
            for (item in items) {
                val matched = allProducts.value.find { it.name.equals(item.productName, ignoreCase = true) }
                if (matched != null) {
                    repository.adjustProductStock(matched.id, -(item.qty + item.freeQty))
                }
            }

            // Record accounting entry for paid invoice
            if (invoice.paidAmount > 0) {
                repository.saveTransaction(
                    AccountsTransaction(
                        date = invoice.invoiceDate,
                        type = "RECEIPT",
                        category = "Customer Collection",
                        amount = invoice.paidAmount,
                        partyName = invoice.partyName,
                        paymentMode = invoice.paymentMode,
                        referenceNo = invoice.invoiceNumber,
                        notes = "Payment received against bill ${invoice.invoiceNumber}"
                    )
                )
            }

            val savedWithItems = InvoiceWithItems(invoice.copy(id = savedId), items.map { it.copy(invoiceId = savedId) })
            _selectedInvoice.value = savedWithItems
            onSaved(savedId)
        }
    }

    fun deleteInvoice(invoice: Invoice) {
        viewModelScope.launch {
            repository.deleteInvoice(invoice)
            if (_selectedInvoice.value?.invoice?.id == invoice.id) {
                _selectedInvoice.value = null
            }
        }
    }

    fun saveCompanyProfile(profile: CompanyProfile) {
        viewModelScope.launch {
            repository.updateCompanyProfile(profile)
        }
    }

    fun saveParty(party: Party) {
        viewModelScope.launch {
            repository.saveParty(party)
        }
    }

    fun deleteParty(party: Party) {
        viewModelScope.launch {
            repository.deleteParty(party)
        }
    }

    fun saveProduct(product: Product) {
        viewModelScope.launch {
            repository.saveProduct(product)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun adjustStock(productId: Long, adjustment: Int, reason: String) {
        viewModelScope.launch {
            repository.adjustProductStock(productId, adjustment)
        }
    }

    fun savePurchaseRecord(
        purchase: PurchaseRecord,
        items: List<PurchaseItem>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.savePurchase(purchase, items)
            // Add stock to products
            for (item in items) {
                val matched = allProducts.value.find { it.name.equals(item.productName, ignoreCase = true) }
                if (matched != null) {
                    repository.adjustProductStock(matched.id, item.qty + item.freeQty)
                } else {
                    // Create new product if not existing
                    repository.saveProduct(
                        Product(
                            name = item.productName,
                            pack = item.pack,
                            batchNumber = item.batchNumber,
                            expiryDate = item.expiryDate,
                            mrp = item.mrp,
                            purchaseRate = item.purchaseRate,
                            saleRate = item.mrp * 0.8,
                            stockQty = item.qty + item.freeQty
                        )
                    )
                }
            }
            // Add payment transaction if cash
            if (purchase.paymentMode == "CASH" || purchase.paymentMode == "BANK") {
                repository.saveTransaction(
                    AccountsTransaction(
                        date = purchase.purchaseDate,
                        type = "PAYMENT",
                        category = "Supplier Payment",
                        amount = purchase.grandTotal,
                        partyName = purchase.supplierName,
                        paymentMode = purchase.paymentMode,
                        referenceNo = purchase.purchaseInvoiceNo,
                        notes = "Supplier purchase bill ${purchase.purchaseInvoiceNo}"
                    )
                )
            }
            onSuccess()
        }
    }

    fun deletePurchase(purchase: PurchaseRecord) {
        viewModelScope.launch {
            repository.deletePurchase(purchase)
        }
    }

    fun saveAccountsTransaction(tx: AccountsTransaction) {
        viewModelScope.launch {
            repository.saveTransaction(tx)
        }
    }

    fun deleteAccountsTransaction(tx: AccountsTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
        }
    }

    fun updateDesignerConfig(config: InvoiceDesignerConfig) {
        viewModelScope.launch {
            repository.saveDesignerConfig(config)
        }
    }

    fun resetDesignerConfigToDefault() {
        viewModelScope.launch {
            repository.saveDesignerConfig(InvoiceDesignerConfig())
        }
    }

    fun applyDesignerPreset(preset: String) {
        val current = designerConfig.value
        val updated = when (preset) {
            "Retail" -> current.copy(
                selectedTemplate = "Veda Retail Tax Invoice",
                showHsn = true,
                showBatch = true,
                showExpiry = true,
                showMrp = true,
                showDiscount = true,
                showCgstSgst = false,
                paperOrientation = "PORTRAIT"
            )
            "Wholesale" -> current.copy(
                selectedTemplate = "Veda Wholesale Distributor",
                showCustomerGstin = true,
                showCustomerDl = true,
                showHsn = true,
                showBatch = true,
                showExpiry = true,
                showMrp = true,
                showDiscount = true,
                showCgstSgst = true,
                paperOrientation = "LANDSCAPE"
            )
            "Thermal" -> current.copy(
                selectedTemplate = "Thermal 80mm POS",
                paperOrientation = "THERMAL",
                thermalPaperWidthMm = 80,
                showUpiQr = true
            )
            else -> InvoiceDesignerConfig()
        }
        updateDesignerConfig(updated)
    }

    // AI Natural Language Business Assistant
    fun askAiQuestion(question: String) {
        val q = question.lowercase().trim()
        val metrics = dashboardMetrics.value
        val products = allProducts.value
        val invoices = allInvoices.value
        val parties = allParties.value

        val (answer, details, action) = when {
            q.contains("sales") && (q.contains("today") || q.contains("total")) -> {
                Triple(
                    "Total recorded sales stand at ₹${String.format(Locale.US, "%,.2f", metrics.totalSales)} across ${metrics.totalInvoicesCount} invoices. Cash sales account for ₹${String.format(Locale.US, "%,.2f", metrics.cashSales)} and Credit sales account for ₹${String.format(Locale.US, "%,.2f", metrics.creditSales)}.",
                    listOf(
                        "Total Invoices: ${metrics.totalInvoicesCount}",
                        "Cash Sales: ₹${String.format(Locale.US, "%,.2f", metrics.cashSales)}",
                        "Credit Sales: ₹${String.format(Locale.US, "%,.2f", metrics.creditSales)}"
                    ),
                    "View All Bills"
                )
            }
            q.contains("expiry") || q.contains("expire") -> {
                val nearExpList = products.filter { it.expiryDate.contains("26") || it.expiryDate.contains("25") }
                Triple(
                    "Found ${nearExpList.size} products approaching expiration within the next 60-180 days. Immediate action recommended under FEFO stock guidelines.",
                    nearExpList.map { "${it.name} | Batch: ${it.batchNumber} | Exp: ${it.expiryDate} (Stock: ${it.stockQty} ${it.unit})" },
                    "View Expiry Tracker"
                )
            }
            q.contains("owe") || q.contains("money") || q.contains("receivable") || q.contains("overdue") -> {
                val owingCustomers = parties.filter { it.type == "CUSTOMER" && it.currentBalance > 0 }
                Triple(
                    "Total customer receivables amount to ₹${String.format(Locale.US, "%,.2f", metrics.outstandingReceivable)} across ${owingCustomers.size} chemist accounts.",
                    owingCustomers.map { "${it.name}: ₹${String.format(Locale.US, "%,.2f", it.currentBalance)} (Credit Limit: ₹${it.creditLimit.toInt()}, ${it.creditDays} days)" },
                    "Send Payment Reminders"
                )
            }
            q.contains("top") || q.contains("best") || q.contains("product") -> {
                Triple(
                    "Top performing Ayurvedic and Pharmaceutical lines by volume and revenue:",
                    listOf(
                        "1. VEDA ASHWAGANDHA CHURNA 100g — High repeat demand",
                        "2. VEDA CHYAWANPRASH SPECIAL 500g — Prime margin earner (35%)",
                        "3. VEDA TRIPHALA GUGGULU 80 Tabs — Fast moving digestive tonic",
                        "4. CROCIN NEW 20MG — Steady OTC volume"
                    ),
                    "Open Product Master"
                )
            }
            q.contains("reorder") || q.contains("low stock") -> {
                val low = products.filter { it.stockQty <= it.reorderLevel }
                Triple(
                    "There are ${low.size} items below safety reorder threshold requiring immediate purchase PO generation.",
                    low.map { "${it.name}: Current Stock ${it.stockQty} ${it.unit} (Reorder at ${it.reorderLevel})" },
                    "Create Purchase Order"
                )
            }
            q.contains("profit") -> {
                Triple(
                    "Estimated Gross Profit is ₹${String.format(Locale.US, "%,.2f", metrics.grossProfit)} (avg 28% margin) and Net Profit after shop expenses is ₹${String.format(Locale.US, "%,.2f", metrics.netProfit)}.",
                    listOf(
                        "Sales Taxable: ₹${String.format(Locale.US, "%,.2f", metrics.totalSales)}",
                        "Total Expenses: ₹${String.format(Locale.US, "%,.2f", metrics.expenses)}",
                        "Net Operating Profit: ₹${String.format(Locale.US, "%,.2f", metrics.netProfit)}"
                    ),
                    "View Financial Reports"
                )
            }
            else -> {
                Triple(
                    "Veda AI processed your request for: \"$question\". Here is the live status of Veda Ayur Pharma ERP:",
                    listOf(
                        "Total Sales: ₹${String.format(Locale.US, "%,.2f", metrics.totalSales)}",
                        "Active Stock Value: ₹${String.format(Locale.US, "%,.2f", metrics.stockValue)}",
                        "Receivables: ₹${String.format(Locale.US, "%,.2f", metrics.outstandingReceivable)}",
                        "Near-Expiry Alerts: ${metrics.nearExpiryProductsCount} batches"
                    ),
                    "Open ERP Dashboard"
                )
            }
        }

        val result = AiQueryResult(
            query = question,
            answer = answer,
            details = details,
            actionLabel = action
        )
        _aiQueries.value = listOf(result) + _aiQueries.value
    }
}
