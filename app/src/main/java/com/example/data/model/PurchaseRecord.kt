package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "purchases")
data class PurchaseRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseInvoiceNo: String,
    val purchaseDate: String,
    val supplierId: Long = 0,
    val supplierName: String,
    val supplierGstin: String = "",
    val totalTaxable: Double = 0.0,
    val totalGst: Double = 0.0,
    val grandTotal: Double = 0.0,
    val paymentMode: String = "CREDIT", // CASH, CREDIT, BANK
    val status: String = "RECEIVED", // RECEIVED, PENDING_DELIVERY, RETURNED
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val subTotal: Double
        get() = totalTaxable

    val taxAmount: Double
        get() = totalGst
}

@Entity(
    tableName = "purchase_items",
    foreignKeys = [
        ForeignKey(
            entity = PurchaseRecord::class,
            parentColumns = ["id"],
            childColumns = ["purchaseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("purchaseId")]
)
data class PurchaseItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseId: Long = 0,
    val productName: String,
    val batchNumber: String,
    val expiryDate: String,
    val pack: String = "1*10",
    val qty: Int = 1,
    val freeQty: Int = 0,
    val purchaseRate: Double = 0.0,
    val mrp: Double = 0.0,
    val gstPercent: Double = 12.0,
    val amount: Double = 0.0
)
