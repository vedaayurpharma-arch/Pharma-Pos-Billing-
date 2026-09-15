package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = Invoice::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("invoiceId")]
)
data class InvoiceItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long = 0,
    val serialNo: Int = 1,
    val qty: Int = 1,
    val freeQty: Int = 0,
    val pack: String = "1*10",
    val productName: String = "",
    val batch: String = "",
    val exp: String = "",
    val hsn: String = "",
    val mrp: Double = 0.0,
    val rate: Double = 0.0,
    val discountPercent: Double = 0.0,
    val sgstPercent: Double = 6.0,
    val cgstPercent: Double = 6.0,
    val gstVal: Double = 0.0,
    val amount: Double = 0.0
)
