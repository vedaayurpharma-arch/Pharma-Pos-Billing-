package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "field_orders")
data class FieldOrder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderNumber: String,
    val customerId: Long,
    val customerName: String,
    val clinicOrPharmacyName: String,
    val phone: String = "",
    val state: String,
    val city: String,
    val orderDate: String,
    val salesRep: String = "Ramesh Kumar",
    val totalMrp: Double = 0.0,
    val netWholesaleRate: Double = 0.0,
    val discountPercent: Double = 0.0,
    val discountAmount: Double = 0.0,
    val gstPercent: Double = 5.0,
    val gstAmount: Double = 0.0,
    val finalPurchaseValue: Double = 0.0,
    val digitalInvoiceUrl: String = "",
    val physicalReceiptUri: String = "",
    val orderNotes: String = "",
    val paymentStatus: String = "PAID" // PAID, CREDIT, PARTIAL
)

@Entity(tableName = "field_order_items")
data class FieldOrderItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val productId: Long,
    val productName: String,
    val pack: String = "100g",
    val batchNumber: String = "",
    val quantity: Int = 1,
    val mrp: Double = 0.0,
    val wholesaleRate: Double = 0.0,
    val subtotal: Double = 0.0
)
