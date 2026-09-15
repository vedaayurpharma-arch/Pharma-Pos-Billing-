package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val code: String = "",
    val barcode: String = "",
    val category: String = "Ayurvedic Classical", // Ayurvedic Classical, Ayurvedic Patent, Allopathic, Herbal Tonic
    val composition: String = "",
    val pack: String = "1*10",
    val unit: String = "Bottle",
    val batchNumber: String,
    val mfgDate: String = "01/24",
    val expiryDate: String,
    val hsnCode: String = "30049011",
    val rackNumber: String = "A-1",
    val mrp: Double,
    val purchaseRate: Double = 0.0, // PTR / PTS
    val saleRate: Double,
    val wholesaleRate: Double = 0.0,
    val dealerRate: Double = 0.0,
    val defaultDiscount: Double = 0.0,
    val sgstPercent: Double = 6.0,
    val cgstPercent: Double = 6.0,
    val igstPercent: Double = 0.0,
    val stockQty: Int = 100,
    val reorderLevel: Int = 20,
    val minStock: Int = 10,
    val storage: String = "Store in cool and dry place",
    val dosage: String = "As directed by the Ayurvedic physician",
    val shelfLifeMonths: Int = 36
) {
    val ayurvedicComposition: String
        get() = composition
}
