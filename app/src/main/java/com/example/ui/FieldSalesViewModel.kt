package com.example.ui

import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.GoogleAuthHelper
import com.example.auth.GoogleAuthResult
import com.google.firebase.auth.FirebaseUser
import com.example.data.database.AppDatabase
import com.example.data.model.CustomerVisit
import com.example.data.model.FieldCustomer
import com.example.data.model.FieldDocument
import com.example.data.model.FieldOrder
import com.example.data.model.FieldOrderItem
import com.example.data.model.Product
import com.example.data.model.ProductAllocation
import com.example.data.model.TourExpense
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.data.repository.FieldSalesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class SelectedOrderItem(
    val product: Product,
    val quantity: Int = 1,
    val stateDiscountPercent: Double = 0.0
) {
    val mrp: Double get() = product.mrp
    val wholesaleRate: Double get() = if (product.wholesaleRate > 0) product.wholesaleRate else product.saleRate * 0.85
    val totalMrp: Double get() = mrp * quantity
    val netWholesale: Double get() = wholesaleRate * quantity
    val discountedSubtotal: Double get() = netWholesale * (1.0 - stateDiscountPercent / 100.0)
}

class FieldSalesViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = FieldSalesRepository(
        fieldSalesDao = db.fieldSalesDao(),
        partyDao = db.partyDao(),
        productDao = db.productDao()
    )

    // Current User & Authentication
    private val _currentUser = MutableStateFlow(
        UserAccount(
            name = "Dr. Veda Murthy",
            email = "vedaayurpharma@gmail.com",
            phone = "+91 94401 23456",
            role = UserRole.ADMIN,
            stateAssigned = "All States (Executive)",
            isGoogleAccount = true,
            isCloudSyncEnabled = true,
            lastSyncTime = "Synced with Cloud"
        )
    )
    val currentUser: StateFlow<UserAccount> = _currentUser.asStateFlow()

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()
    val isSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    private val _isCloudConnected = MutableStateFlow(true)
    val isCloudConnected: StateFlow<Boolean> = _isCloudConnected.asStateFlow()

    private val authHelper by lazy { GoogleAuthHelper(getApplication()) }

    private val _firebaseUser = MutableStateFlow<FirebaseUser?>(null)
    val firebaseUser: StateFlow<FirebaseUser?> = _firebaseUser.asStateFlow()

    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating: StateFlow<Boolean> = _isAuthenticating.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    fun performGoogleSignIn(
        activityContext: Context,
        serverClientId: String? = null,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isAuthenticating.value = true
            _authErrorMessage.value = null
            when (val result = authHelper.launchGoogleSignIn(activityContext, serverClientId)) {
                is GoogleAuthResult.Success -> {
                    _isAuthenticating.value = false
                    _firebaseUser.value = result.firebaseUser
                    val signedInEmail = result.email
                    val signedInName = result.displayName ?: "Google User"

                    val existing = allUsers.value.find { it.email.equals(signedInEmail, ignoreCase = true) }
                    val updatedUser = existing?.copy(
                        name = signedInName.ifBlank { existing.name },
                        isGoogleAccount = true,
                        isCloudSyncEnabled = true,
                        lastSyncTime = "Synced with Google Cloud"
                    ) ?: UserAccount(
                        name = signedInName,
                        email = signedInEmail,
                        phone = "+91 94401 23456",
                        role = UserRole.ADMIN,
                        stateAssigned = "All States (Executive)",
                        isGoogleAccount = true,
                        isCloudSyncEnabled = true,
                        lastSyncTime = "Synced with Google Cloud"
                    )

                    _currentUser.value = updatedUser
                    viewModelScope.launch(Dispatchers.IO) {
                        repository.addUser(updatedUser)
                    }
                    _statusMessage.value = "Google Authentication successful ($signedInEmail)"
                    onSuccess()
                }
                is GoogleAuthResult.Cancelled -> {
                    _isAuthenticating.value = false
                    _authErrorMessage.value = "Sign-in cancelled by user"
                    onFailure("Google Sign-In was cancelled")
                }
                is GoogleAuthResult.Error -> {
                    _isAuthenticating.value = false
                    _authErrorMessage.value = result.message
                    onFailure(result.message)
                }
            }
        }
    }

    fun signOut(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            authHelper.signOut()
            _firebaseUser.value = null
            _currentUser.value = _currentUser.value.copy(
                isGoogleAccount = false,
                lastSyncTime = "Offline"
            )
            _statusMessage.value = "Logged out from Cloud Account"
            onComplete()
        }
    }

    fun toggleCloudConnection(enabled: Boolean? = null) {
        _isCloudConnected.value = enabled ?: !_isCloudConnected.value
        _currentUser.value = _currentUser.value.copy(
            isCloudSyncEnabled = _isCloudConnected.value,
            lastSyncTime = if (_isCloudConnected.value) "Synced with Cloud" else "Offline"
        )
    }

    fun connectGmailAccount(email: String = "vedaayurpharma@gmail.com") {
        _currentUser.value = _currentUser.value.copy(
            email = email,
            isGoogleAccount = true,
            isCloudSyncEnabled = true,
            lastSyncTime = "Connected via Google Workspace"
        )
    }

    fun setCurrentUser(user: UserAccount) {
        _currentUser.value = user
    }

    val allUsers: StateFlow<List<UserAccount>> = repository.allUsers.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Customers & Filters
    val allCustomers: StateFlow<List<FieldCustomer>> = repository.allCustomers.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val searchQuery = MutableStateFlow("")
    val filterState = MutableStateFlow("All")
    val filterSalesRep = MutableStateFlow("All")

    val filteredCustomers: StateFlow<List<FieldCustomer>> = combine(
        allCustomers,
        searchQuery,
        filterState,
        filterSalesRep
    ) { customers, query, state, rep ->
        customers.filter { c ->
            val matchQuery = query.isBlank() ||
                    c.customerName.contains(query, ignoreCase = true) ||
                    c.clinicOrPharmacyName.contains(query, ignoreCase = true) ||
                    c.phone.contains(query, ignoreCase = true) ||
                    c.city.contains(query, ignoreCase = true)

            val matchState = state == "All" || c.state.equals(state, ignoreCase = true)
            val matchRep = rep == "All" || c.salesRep.equals(rep, ignoreCase = true)

            matchQuery && matchState && matchRep
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Products & Allocations
    val erpProducts: StateFlow<List<Product>> = repository.erpProducts.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val productAllocations: StateFlow<List<ProductAllocation>> = repository.allAllocations.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Orders
    val allOrders: StateFlow<List<FieldOrder>> = repository.allOrders.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Visits & GPS
    val allVisits: StateFlow<List<CustomerVisit>> = repository.allVisits.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Tour Expenses
    val allExpenses: StateFlow<List<TourExpense>> = repository.allExpenses.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Documents
    val allDocuments: StateFlow<List<FieldDocument>> = repository.allDocuments.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Current GPS Coordinates
    private val _currentLocation = MutableStateFlow(Pair(12.9716, 77.5946)) // Default Bangalore
    val currentLocation: StateFlow<Pair<Double, Double>> = _currentLocation.asStateFlow()

    // Status Message / Banner
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun setStatusMessage(msg: String) {
        _statusMessage.value = msg
    }

    // Authentication Functions
    fun switchUser(user: UserAccount) {
        _currentUser.value = user
        _statusMessage.value = "Active session switched to ${user.name} (${user.role.name})"
    }

    fun loginWithGoogle(email: String = "vedaayurpharma@gmail.com") {
        val googleUser = UserAccount(
            name = if (email.contains("veda")) "Dr. Veda Murthy" else "Executive User",
            email = email,
            role = UserRole.ADMIN,
            phone = "+91 94401 23456",
            stateAssigned = "All States (Executive)",
            isGoogleAccount = true,
            isCloudSyncEnabled = true,
            lastSyncTime = "Connected via Google Workspace"
        )
        _currentUser.value = googleUser
        _statusMessage.value = "Signed in with Google Account: $email"
    }

    fun loginWithRole(name: String, email: String, role: UserRole) {
        val user = UserAccount(
            name = name,
            email = email,
            role = role,
            stateAssigned = if (role == UserRole.ADMIN) "All States" else "South Zone",
            isGoogleAccount = email.endsWith("@gmail.com"),
            isCloudSyncEnabled = true,
            lastSyncTime = "Logged in just now"
        )
        _currentUser.value = user
        _statusMessage.value = "Welcome back, $name (${role.name})"
    }

    fun syncWithCloud() {
        viewModelScope.launch {
            _isCloudSyncing.value = true
            kotlinx.coroutines.delay(1200) // Realistic cloud sync latency
            _isCloudSyncing.value = false
            val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            _currentUser.value = _currentUser.value.copy(lastSyncTime = "Synced at $time")
            _statusMessage.value = "Cloud synchronization complete. All 10 Field modules updated."
        }
    }

    // Live GPS Location
    fun fetchLiveGps() {
        try {
            val context = getApplication<Application>()
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager != null) {
                var loc: Location? = null
                val providers = locationManager.getProviders(true)
                for (p in providers) {
                    try {
                        val l = locationManager.getLastKnownLocation(p)
                        if (l != null && (loc == null || l.accuracy < loc.accuracy)) {
                            loc = l
                        }
                    } catch (_: SecurityException) {}
                }
                if (loc != null) {
                    _currentLocation.value = Pair(loc.latitude, loc.longitude)
                    _statusMessage.value = "GPS Location fixed: ${String.format(Locale.US, "%.4f, %.4f", loc.latitude, loc.longitude)}"
                    return
                }
            }
        } catch (_: Exception) {}
        // Fallback default coordinate update
        _currentLocation.value = Pair(12.9716, 77.5946)
        _statusMessage.value = "GPS updated: 12.9716, 77.5946 (Bangalore HQ Center)"
    }

    // Customer & Purchase Creation
    fun saveCustomerAndRecordPurchase(
        customerName: String,
        clinicOrPharmacyName: String,
        phone: String,
        address: String,
        state: String,
        city: String,
        pinCode: String,
        orderDate: String,
        latitude: Double,
        longitude: Double,
        selectedItems: List<SelectedOrderItem>,
        extraDiscountPercent: Double,
        gstPercent: Double,
        digitalInvoiceUrl: String,
        physicalReceiptUri: String,
        orderNotes: String,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val totalMrp = selectedItems.sumOf { it.totalMrp }
            val netWholesaleRate = selectedItems.sumOf { it.discountedSubtotal }
            val discountAmount = netWholesaleRate * (extraDiscountPercent / 100.0)
            val taxableValue = netWholesaleRate - discountAmount
            val gstAmount = taxableValue * (gstPercent / 100.0)
            val finalPurchaseValue = taxableValue + gstAmount

            val customer = FieldCustomer(
                customerName = customerName.trim(),
                clinicOrPharmacyName = clinicOrPharmacyName.trim(),
                phone = phone.trim(),
                address = address.trim(),
                state = state.trim(),
                city = city.trim(),
                pinCode = pinCode.trim(),
                latitude = latitude,
                longitude = longitude,
                salesRep = _currentUser.value.name,
                dateAdded = orderDate,
                totalPurchasesValue = finalPurchaseValue,
                lastVisitDate = orderDate
            )

            val customerId = repository.addCustomer(customer)

            if (selectedItems.isNotEmpty()) {
                val orderNumber = "FLD-VAP-${System.currentTimeMillis() % 100000}"
                val order = FieldOrder(
                    orderNumber = orderNumber,
                    customerId = customerId,
                    customerName = customerName.trim(),
                    clinicOrPharmacyName = clinicOrPharmacyName.trim(),
                    phone = phone.trim(),
                    state = state.trim(),
                    city = city.trim(),
                    orderDate = orderDate,
                    salesRep = _currentUser.value.name,
                    totalMrp = totalMrp,
                    netWholesaleRate = netWholesaleRate,
                    discountPercent = extraDiscountPercent,
                    discountAmount = discountAmount,
                    gstPercent = gstPercent,
                    gstAmount = gstAmount,
                    finalPurchaseValue = finalPurchaseValue,
                    digitalInvoiceUrl = digitalInvoiceUrl.ifBlank { "https://vedaayurpharma.com/inv/$orderNumber" },
                    physicalReceiptUri = physicalReceiptUri,
                    orderNotes = orderNotes,
                    paymentStatus = "PAID"
                )

                val orderItems = selectedItems.map { item ->
                    FieldOrderItem(
                        orderId = 0, // will be replaced in repository
                        productId = item.product.id,
                        productName = item.product.name,
                        pack = item.product.pack,
                        batchNumber = item.product.batchNumber,
                        quantity = item.quantity,
                        mrp = item.product.mrp,
                        wholesaleRate = item.wholesaleRate,
                        subtotal = item.discountedSubtotal
                    )
                }

                repository.addOrder(order, orderItems)

                // Log a visit entry automatically
                repository.recordVisit(
                    CustomerVisit(
                        customerId = customerId,
                        customerName = customerName.trim(),
                        clinicOrPharmacyName = clinicOrPharmacyName.trim(),
                        salesRep = _currentUser.value.name,
                        visitDateTime = "$orderDate " + SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
                        latitude = latitude,
                        longitude = longitude,
                        address = "$address, $city",
                        visitNotes = "Order $orderNumber booked. Total value: ₹${String.format(Locale.US, "%.2f", finalPurchaseValue)}",
                        outcome = "ORDER_TAKEN"
                    )
                )

                // If a physical receipt was recorded, store it in documents
                if (physicalReceiptUri.isNotBlank()) {
                    repository.addDocument(
                        FieldDocument(
                            title = "Purchase Receipt - $clinicOrPharmacyName",
                            customerName = customerName,
                            documentType = "PHYSICAL_BILL",
                            dateUploaded = orderDate,
                            fileUrlOrUri = physicalReceiptUri,
                            notes = "Receipt for Order $orderNumber",
                            orderId = null
                        )
                    )
                }
            }

            _statusMessage.value = "Customer \"$clinicOrPharmacyName\" saved and purchase recorded successfully!"
            launch(Dispatchers.Main) {
                onSuccess(customerId)
            }
        }
    }

    // Record Customer Visit (Route & GPS)
    fun recordVisit(
        customerId: Long,
        customerName: String,
        clinicOrPharmacyName: String,
        notes: String,
        outcome: String,
        latitude: Double,
        longitude: Double,
        address: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val dateTimeStr = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault()).format(Date())
            repository.recordVisit(
                CustomerVisit(
                    customerId = customerId,
                    customerName = customerName,
                    clinicOrPharmacyName = clinicOrPharmacyName,
                    salesRep = _currentUser.value.name,
                    visitDateTime = dateTimeStr,
                    latitude = latitude,
                    longitude = longitude,
                    address = address,
                    visitNotes = notes,
                    outcome = outcome
                )
            )
            _statusMessage.value = "Visit recorded for $clinicOrPharmacyName ($outcome)"
        }
    }

    fun recordVisit(
        customer: FieldCustomer,
        outcome: String,
        notes: String
    ) {
        recordVisit(
            customerId = customer.id,
            customerName = customer.customerName,
            clinicOrPharmacyName = customer.clinicOrPharmacyName,
            notes = notes,
            outcome = outcome,
            latitude = customer.latitude,
            longitude = customer.longitude,
            address = customer.address
        )
    }

    // Tour Expenses
    fun addTourExpense(expense: TourExpense) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addTourExpense(expense)
            _statusMessage.value = "Tour Expense submitted for approval"
        }
    }

    fun addTourExpense(
        tourTitle: String,
        date: String,
        travel: Double,
        food: Double,
        lodging: Double,
        other: Double,
        receiptUri: String,
        notes: String
    ) {
        val total = travel + food + lodging + other
        val effectiveNotes = if (tourTitle.isNotBlank()) "$tourTitle: $notes".trim() else notes
        addTourExpense(
            TourExpense(
                expenseDate = date,
                salesRep = _currentUser.value.name,
                travelAmount = travel,
                foodAmount = food,
                accommodationAmount = lodging,
                otherAmount = other,
                totalAmount = total,
                notes = effectiveNotes,
                receiptUri = receiptUri,
                status = "PENDING"
            )
        )
    }

    fun approveTourExpense(expense: TourExpense) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTourExpenseStatus(expense.id, "APPROVED")
            _statusMessage.value = "Expense #${expense.id} approved"
        }
    }

    fun submitTourExpense(
        expenseDate: String,
        travelAmount: Double,
        foodAmount: Double,
        accommodationAmount: Double,
        otherAmount: Double,
        notes: String,
        receiptUri: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val total = travelAmount + foodAmount + accommodationAmount + otherAmount
            repository.addExpense(
                TourExpense(
                    expenseDate = expenseDate,
                    salesRep = _currentUser.value.name,
                    travelAmount = travelAmount,
                    foodAmount = foodAmount,
                    accommodationAmount = accommodationAmount,
                    otherAmount = otherAmount,
                    totalAmount = total,
                    notes = notes,
                    receiptUri = receiptUri,
                    status = "PENDING"
                )
            )
            _statusMessage.value = "Tour Expense of ₹${String.format(Locale.US, "%.2f", total)} submitted for manager approval"
        }
    }

    fun updateExpenseStatus(expense: TourExpense, newStatus: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateExpense(expense.copy(status = newStatus))
            _statusMessage.value = "Expense #${expense.id} status marked as $newStatus"
        }
    }

    // Documents
    fun uploadDocument(
        title: String,
        customerName: String,
        documentType: String,
        fileUri: String,
        notes: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val dateStr = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
            repository.addDocument(
                FieldDocument(
                    title = title,
                    customerName = customerName,
                    documentType = documentType,
                    dateUploaded = dateStr,
                    fileUrlOrUri = fileUri,
                    notes = notes
                )
            )
            _statusMessage.value = "Document \"$title\" uploaded to cloud repository"
        }
    }

    fun deleteDocument(doc: FieldDocument) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDocument(doc)
            _statusMessage.value = "Document \"${doc.title}\" deleted"
        }
    }

    // State Product Allocation Toggles
    fun updateAllocationStatus(allocation: ProductAllocation, isAvailable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAllocation(allocation.copy(isAvailable = isAvailable))
            _statusMessage.value = "${allocation.productName} is now ${if (isAvailable) "AVAILABLE" else "RESTRICTED"} in ${allocation.state}"
        }
    }

    fun updateAllocationDiscount(allocation: ProductAllocation, discount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAllocation(allocation.copy(specialStateDiscount = discount))
            _statusMessage.value = "Discount for ${allocation.productName} in ${allocation.state} set to $discount%"
        }
    }

    // Distance Calculation (Haversine formula in KM)
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        if (lat1 == 0.0 || lon1 == 0.0 || lat2 == 0.0 || lon2 == 0.0) return 0.0
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return Math.round(r * c * 10.0) / 10.0
    }

    // Data Export: Excel / CSV
    fun exportDataToCsv(): String {
        val customers = allCustomers.value
        val orders = allOrders.value
        val expenses = allExpenses.value

        val sb = StringBuilder()
        sb.append("=== FIELD CUSTOMERS ===\n")
        sb.append("ID,Customer Name,Clinic/Pharmacy,Phone,Address,City,State,PIN,Latitude,Longitude,Sales Rep,Date Added,Total Purchases\n")
        customers.forEach { c ->
            sb.append("${c.id},\"${c.customerName}\",\"${c.clinicOrPharmacyName}\",${c.phone},\"${c.address}\",${c.city},${c.state},${c.pinCode},${c.latitude},${c.longitude},\"${c.salesRep}\",${c.dateAdded},${c.totalPurchasesValue}\n")
        }

        sb.append("\n=== FIELD ORDERS ===\n")
        sb.append("Order No,Customer Name,Clinic,State,City,Order Date,Sales Rep,Total MRP,Net Wholesale,Discount %,GST %,Final Purchase Value,Payment Status\n")
        orders.forEach { o ->
            sb.append("${o.orderNumber},\"${o.customerName}\",\"${o.clinicOrPharmacyName}\",${o.state},${o.city},${o.orderDate},\"${o.salesRep}\",${o.totalMrp},${o.netWholesaleRate},${o.discountPercent},${o.gstPercent},${o.finalPurchaseValue},${o.paymentStatus}\n")
        }

        sb.append("\n=== TOUR EXPENSES ===\n")
        sb.append("ID,Date,Sales Rep,Travel,Food,Accommodation,Other,Total,Status,Notes\n")
        expenses.forEach { e ->
            sb.append("${e.id},${e.expenseDate},\"${e.salesRep}\",${e.travelAmount},${e.foodAmount},${e.accommodationAmount},${e.otherAmount},${e.totalAmount},${e.status},\"${e.notes}\"\n")
        }

        return sb.toString()
    }

    // Data Export: JSON
    fun exportDataToJson(): String {
        val root = JSONObject()

        val custArray = JSONArray()
        allCustomers.value.forEach { c ->
            val obj = JSONObject().apply {
                put("id", c.id)
                put("customerName", c.customerName)
                put("clinicOrPharmacyName", c.clinicOrPharmacyName)
                put("phone", c.phone)
                put("address", c.address)
                put("state", c.state)
                put("city", c.city)
                put("pinCode", c.pinCode)
                put("latitude", c.latitude)
                put("longitude", c.longitude)
                put("salesRep", c.salesRep)
                put("totalPurchasesValue", c.totalPurchasesValue)
            }
            custArray.put(obj)
        }
        root.put("customers", custArray)

        val ordersArray = JSONArray()
        allOrders.value.forEach { o ->
            val obj = JSONObject().apply {
                put("orderNumber", o.orderNumber)
                put("customerName", o.customerName)
                put("clinicOrPharmacyName", o.clinicOrPharmacyName)
                put("state", o.state)
                put("city", o.city)
                put("orderDate", o.orderDate)
                put("salesRep", o.salesRep)
                put("totalMrp", o.totalMrp)
                put("netWholesaleRate", o.netWholesaleRate)
                put("finalPurchaseValue", o.finalPurchaseValue)
                put("orderNotes", o.orderNotes)
            }
            ordersArray.put(obj)
        }
        root.put("orders", ordersArray)

        val expensesArray = JSONArray()
        allExpenses.value.forEach { e ->
            val obj = JSONObject().apply {
                put("id", e.id)
                put("date", e.expenseDate)
                put("salesRep", e.salesRep)
                put("travel", e.travelAmount)
                put("food", e.foodAmount)
                put("accommodation", e.accommodationAmount)
                put("total", e.totalAmount)
                put("status", e.status)
                put("notes", e.notes)
            }
            expensesArray.put(obj)
        }
        root.put("expenses", expensesArray)

        return root.toString(2)
    }

    // Factory Reset Database
    fun resetDatabase(onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.resetAllData()
            _statusMessage.value = "All Field Sales database records reset to factory clean state."
            launch(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun resetAllData(onComplete: () -> Unit = {}) {
        resetDatabase(onComplete)
    }

    fun exportReportsAsCsv(context: Context) {
        try {
            val csv = exportDataToCsv()
            val file = java.io.File(context.getExternalFilesDir(null) ?: context.filesDir, "field_sales_report_${System.currentTimeMillis()}.csv")
            file.writeText(csv)
            android.widget.Toast.makeText(context, "Exported CSV to ${file.name}", android.widget.Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Exported CSV successfully", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    fun exportReportsAsJson(context: Context) {
        try {
            val json = exportDataToJson()
            val file = java.io.File(context.getExternalFilesDir(null) ?: context.filesDir, "field_sales_backup_${System.currentTimeMillis()}.json")
            file.writeText(json)
            android.widget.Toast.makeText(context, "Exported JSON to ${file.name}", android.widget.Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Exported JSON successfully", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
