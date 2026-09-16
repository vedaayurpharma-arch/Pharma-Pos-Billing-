package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_allocations")
data class ProductAllocation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val state: String,
    val productId: Long,
    val productName: String,
    val isAvailable: Boolean = true,
    val specialStateDiscount: Double = 0.0,
    val allocatedQuota: Int = 500
)

@Entity(tableName = "field_documents")
data class FieldDocument(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val customerName: String,
    val documentType: String, // PHYSICAL_BILL, DRUG_LICENSE, GST_CERT, DELIVERY_CHALLAN, RECEIPT
    val dateUploaded: String,
    val fileUrlOrUri: String,
    val notes: String = "",
    val orderId: Long? = null
)

enum class UserRole {
    FIELD_SALES,
    MANAGER,
    ADMIN
}

@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val role: UserRole = UserRole.FIELD_SALES,
    val phone: String = "",
    val stateAssigned: String = "All States",
    val isGoogleAccount: Boolean = false,
    val isCloudSyncEnabled: Boolean = true,
    val lastSyncTime: String = "Just now"
) {
    val fullName: String get() = name
}
