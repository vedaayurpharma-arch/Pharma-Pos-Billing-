package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.CompanyProfile
import com.example.data.model.FieldOrder
import com.example.data.model.Invoice
import com.example.data.model.ShippingLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ShippingLabelViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val shippingDao = db.shippingLabelDao()
    private val invoiceDao = db.invoiceDao()
    private val fieldSalesDao = db.fieldSalesDao()
    private val companyDao = db.companyDao()

    val allShippingLabels: StateFlow<List<ShippingLabel>> = shippingDao.getAllShippingLabels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInvoices: StateFlow<List<Invoice>> = invoiceDao.getAllInvoices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFieldOrders: StateFlow<List<FieldOrder>> = fieldSalesDao.getAllOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val companyProfile: StateFlow<CompanyProfile?> = companyDao.getProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    /**
     * Generates a unique shipping number in the user-specified format:
     * VEDA-SHP-YYYY-000001
     */
    suspend fun generateNextShippingNumber(): String {
        val count = shippingDao.getShippingLabelCount() + 1
        val year = Calendar.getInstance().get(Calendar.YEAR)
        return String.format(Locale.US, "VEDA-SHP-%d-%06d", year, count)
    }

    /**
     * Creates a new shipping label pre-populated from an existing Invoice
     */
    fun createLabelFromInvoice(invoice: Invoice, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val shpNumber = generateNextShippingNumber()
            val today = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date())
            val profile = companyDao.getProfileDirect()
            val returnAddr = profile?.let {
                "${it.companyName}, ${it.addressLine1}, ${it.addressLine2}, Ph: ${it.phone}"
            } ?: "VEDA AYUR PHARMA, Ayurveda Complex, Kurnool - 518002 (A.P.) Ph: +91 94401 23456"

            val label = ShippingLabel(
                shippingNumber = shpNumber,
                invoiceId = invoice.id,
                invoiceNumber = invoice.invoiceNumber,
                orderNumber = invoice.orderNo.ifBlank { invoice.invoiceNumber },
                customerName = invoice.partyName,
                clinicOrPharmacyName = invoice.partyName,
                address = invoice.partyAddress,
                cityOrDistrict = "Kurnool",
                state = "Andhra Pradesh",
                pinCode = "518002",
                mobileNumber = invoice.partyPhone,
                codAmount = if (invoice.paymentMode.equals("COD", ignoreCase = true) || invoice.paymentMode.equals("CREDIT", ignoreCase = true)) invoice.grandTotal else 0.0,
                isCod = invoice.paymentMode.equals("COD", ignoreCase = true),
                shippingDate = today,
                returnAddress = returnAddr
            )

            val id = shippingDao.insertShippingLabel(label)
            _statusMessage.value = "Created Shipping Label $shpNumber"
            launch(Dispatchers.Main) {
                onCreated(id)
            }
        }
    }

    /**
     * Creates a new shipping label pre-populated from an existing Field Order
     */
    fun createLabelFromFieldOrder(order: FieldOrder, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val shpNumber = generateNextShippingNumber()
            val today = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date())
            val profile = companyDao.getProfileDirect()
            val returnAddr = profile?.let {
                "${it.companyName}, ${it.addressLine1}, ${it.addressLine2}, Ph: ${it.phone}"
            } ?: "VEDA AYUR PHARMA, Ayurveda Complex, Kurnool - 518002 (A.P.) Ph: +91 94401 23456"

            val isCod = order.paymentStatus.contains("PENDING", ignoreCase = true) || order.paymentStatus.contains("COD", ignoreCase = true)

            val label = ShippingLabel(
                shippingNumber = shpNumber,
                invoiceId = null,
                invoiceNumber = "",
                orderNumber = order.orderNumber,
                customerName = order.customerName,
                clinicOrPharmacyName = order.clinicOrPharmacyName,
                address = "${order.city}, ${order.state}",
                cityOrDistrict = order.city,
                state = order.state,
                pinCode = "500001",
                mobileNumber = order.salesRep,
                codAmount = if (isCod) order.finalPurchaseValue else 0.0,
                isCod = isCod,
                shippingDate = today,
                returnAddress = returnAddr
            )

            val id = shippingDao.insertShippingLabel(label)
            _statusMessage.value = "Created Shipping Label $shpNumber for ${order.customerName}"
            launch(Dispatchers.Main) {
                onCreated(id)
            }
        }
    }

    /**
     * Saves or updates a shipping label
     */
    fun saveShippingLabel(label: ShippingLabel, onSaved: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            if (label.id == 0L) {
                shippingDao.insertShippingLabel(label)
            } else {
                shippingDao.updateShippingLabel(label)
            }
            _statusMessage.value = "Shipping Label ${label.shippingNumber} saved successfully"
            launch(Dispatchers.Main) {
                onSaved()
            }
        }
    }

    fun deleteShippingLabel(label: ShippingLabel) {
        viewModelScope.launch(Dispatchers.IO) {
            shippingDao.deleteShippingLabel(label)
            _statusMessage.value = "Deleted Shipping Label ${label.shippingNumber}"
        }
    }
}
