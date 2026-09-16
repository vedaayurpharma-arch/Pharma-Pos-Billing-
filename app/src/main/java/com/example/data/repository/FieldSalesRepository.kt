package com.example.data.repository

import com.example.data.dao.FieldSalesDao
import com.example.data.dao.PartyDao
import com.example.data.dao.ProductDao
import com.example.data.model.CustomerVisit
import com.example.data.model.FieldCustomer
import com.example.data.model.FieldDocument
import com.example.data.model.FieldOrder
import com.example.data.model.FieldOrderItem
import com.example.data.model.Party
import com.example.data.model.Product
import com.example.data.model.ProductAllocation
import com.example.data.model.TourExpense
import com.example.data.model.UserAccount
import kotlinx.coroutines.flow.Flow

class FieldSalesRepository(
    private val fieldSalesDao: FieldSalesDao,
    private val partyDao: PartyDao,
    private val productDao: ProductDao
) {
    val allCustomers: Flow<List<FieldCustomer>> = fieldSalesDao.getAllCustomers()
    val allOrders: Flow<List<FieldOrder>> = fieldSalesDao.getAllOrders()
    val allVisits: Flow<List<CustomerVisit>> = fieldSalesDao.getAllVisits()
    val allExpenses: Flow<List<TourExpense>> = fieldSalesDao.getAllExpenses()
    val allAllocations: Flow<List<ProductAllocation>> = fieldSalesDao.getAllAllocations()
    val allDocuments: Flow<List<FieldDocument>> = fieldSalesDao.getAllDocuments()
    val allUsers: Flow<List<UserAccount>> = fieldSalesDao.getAllUsers()
    val erpProducts: Flow<List<Product>> = productDao.getAllProducts()
    val erpParties: Flow<List<Party>> = partyDao.getAllParties()

    suspend fun addCustomer(customer: FieldCustomer): Long {
        val customerId = fieldSalesDao.insertCustomer(customer)
        // Also ensure an ERP Party entry exists to prevent data duplication and bridge with ERP
        try {
            partyDao.insert(
                Party(
                    name = customer.clinicOrPharmacyName.ifBlank { customer.customerName },
                    type = "CUSTOMER",
                    address = "${customer.address}, ${customer.city}, ${customer.state} ${customer.pinCode}",
                    phone = customer.phone,
                    state = customer.state,
                    salesman = customer.salesRep,
                    route = customer.city
                )
            )
        } catch (_: Exception) {}
        return customerId
    }

    suspend fun updateCustomer(customer: FieldCustomer) {
        fieldSalesDao.updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: FieldCustomer) {
        fieldSalesDao.deleteCustomer(customer)
    }

    suspend fun addOrder(order: FieldOrder, items: List<FieldOrderItem>): Long {
        val orderId = fieldSalesDao.insertOrder(order)
        val linkedItems = items.map { it.copy(orderId = orderId) }
        fieldSalesDao.insertOrderItems(linkedItems)

        // Update customer total purchases
        val customer = fieldSalesDao.getCustomerById(order.customerId)
        if (customer != null) {
            val updated = customer.copy(
                totalPurchasesValue = customer.totalPurchasesValue + order.finalPurchaseValue,
                lastVisitDate = order.orderDate
            )
            fieldSalesDao.updateCustomer(updated)
        }
        return orderId
    }

    suspend fun getOrderItems(orderId: Long): List<FieldOrderItem> {
        return fieldSalesDao.getOrderItems(orderId)
    }

    suspend fun recordVisit(visit: CustomerVisit): Long {
        val id = fieldSalesDao.insertVisit(visit)
        val customer = fieldSalesDao.getCustomerById(visit.customerId)
        if (customer != null) {
            val dateStr = visit.visitDateTime.split(" ").firstOrNull() ?: visit.visitDateTime
            fieldSalesDao.updateCustomer(customer.copy(lastVisitDate = dateStr))
        }
        return id
    }

    suspend fun addExpense(expense: TourExpense): Long {
        return fieldSalesDao.insertExpense(expense)
    }

    suspend fun addTourExpense(expense: TourExpense): Long {
        return fieldSalesDao.insertExpense(expense)
    }

    suspend fun updateTourExpenseStatus(expenseId: Long, status: String) {
        fieldSalesDao.updateExpenseStatus(expenseId, status)
    }

    suspend fun updateExpense(expense: TourExpense) {
        fieldSalesDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: TourExpense) {
        fieldSalesDao.deleteExpense(expense)
    }

    suspend fun addAllocation(allocation: ProductAllocation): Long {
        return fieldSalesDao.insertAllocation(allocation)
    }

    suspend fun updateAllocation(allocation: ProductAllocation) {
        fieldSalesDao.updateAllocation(allocation)
    }

    suspend fun addDocument(document: FieldDocument): Long {
        return fieldSalesDao.insertDocument(document)
    }

    suspend fun deleteDocument(document: FieldDocument) {
        fieldSalesDao.deleteDocument(document)
    }

    suspend fun addUser(user: UserAccount): Long {
        return fieldSalesDao.insertUser(user)
    }

    suspend fun updateUser(user: UserAccount) {
        fieldSalesDao.updateUser(user)
    }

    suspend fun resetAllData() {
        fieldSalesDao.clearFieldCustomers()
        fieldSalesDao.clearFieldOrders()
        fieldSalesDao.clearFieldOrderItems()
        fieldSalesDao.clearCustomerVisits()
        fieldSalesDao.clearTourExpenses()
        fieldSalesDao.clearProductAllocations()
        fieldSalesDao.clearFieldDocuments()
    }
}
