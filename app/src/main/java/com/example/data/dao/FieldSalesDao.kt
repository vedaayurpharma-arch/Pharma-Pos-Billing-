package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CustomerVisit
import com.example.data.model.FieldCustomer
import com.example.data.model.FieldDocument
import com.example.data.model.FieldOrder
import com.example.data.model.FieldOrderItem
import com.example.data.model.ProductAllocation
import com.example.data.model.TourExpense
import com.example.data.model.UserAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface FieldSalesDao {
    // Customers
    @Query("SELECT * FROM field_customers ORDER BY id DESC")
    fun getAllCustomers(): Flow<List<FieldCustomer>>

    @Query("SELECT * FROM field_customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: Long): FieldCustomer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: FieldCustomer): Long

    @Update
    suspend fun updateCustomer(customer: FieldCustomer)

    @Delete
    suspend fun deleteCustomer(customer: FieldCustomer)

    // Orders
    @Query("SELECT * FROM field_orders ORDER BY id DESC")
    fun getAllOrders(): Flow<List<FieldOrder>>

    @Query("SELECT * FROM field_orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: Long): FieldOrder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: FieldOrder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<FieldOrderItem>)

    @Query("SELECT * FROM field_order_items WHERE orderId = :orderId")
    suspend fun getOrderItems(orderId: Long): List<FieldOrderItem>

    // Visits & GPS
    @Query("SELECT * FROM customer_visits ORDER BY id DESC")
    fun getAllVisits(): Flow<List<CustomerVisit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visit: CustomerVisit): Long

    // Tour Expenses
    @Query("SELECT * FROM tour_expenses ORDER BY id DESC")
    fun getAllExpenses(): Flow<List<TourExpense>>

    @Query("SELECT * FROM tour_expenses WHERE id = :id LIMIT 1")
    suspend fun getExpenseById(id: Long): TourExpense?

    @Query("UPDATE tour_expenses SET status = :status WHERE id = :id")
    suspend fun updateExpenseStatus(id: Long, status: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: TourExpense): Long

    @Update
    suspend fun updateExpense(expense: TourExpense)

    @Delete
    suspend fun deleteExpense(expense: TourExpense)

    // Product Allocations
    @Query("SELECT * FROM product_allocations ORDER BY state ASC, productName ASC")
    fun getAllAllocations(): Flow<List<ProductAllocation>>

    @Query("SELECT * FROM product_allocations WHERE state = :state")
    suspend fun getAllocationsForState(state: String): List<ProductAllocation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllocation(allocation: ProductAllocation): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllocations(allocations: List<ProductAllocation>)

    @Update
    suspend fun updateAllocation(allocation: ProductAllocation)

    // Documents
    @Query("SELECT * FROM field_documents ORDER BY id DESC")
    fun getAllDocuments(): Flow<List<FieldDocument>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: FieldDocument): Long

    @Delete
    suspend fun deleteDocument(document: FieldDocument)

    // User Accounts
    @Query("SELECT * FROM user_accounts ORDER BY id ASC")
    fun getAllUsers(): Flow<List<UserAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccount): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserAccount>)

    @Update
    suspend fun updateUser(user: UserAccount)

    // Reset All Field Sales Tables
    @Query("DELETE FROM field_customers")
    suspend fun clearFieldCustomers()

    @Query("DELETE FROM field_orders")
    suspend fun clearFieldOrders()

    @Query("DELETE FROM field_order_items")
    suspend fun clearFieldOrderItems()

    @Query("DELETE FROM customer_visits")
    suspend fun clearCustomerVisits()

    @Query("DELETE FROM tour_expenses")
    suspend fun clearTourExpenses()

    @Query("DELETE FROM product_allocations")
    suspend fun clearProductAllocations()

    @Query("DELETE FROM field_documents")
    suspend fun clearFieldDocuments()
}
