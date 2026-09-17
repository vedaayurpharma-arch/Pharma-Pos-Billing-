package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shipping_labels")
data class ShippingLabel(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shippingNumber: String = "", // e.g. VEDA-SHP-2024-000001
    val invoiceId: Long? = null,
    val invoiceNumber: String = "",
    val orderNumber: String = "",
    val customerName: String = "",
    val clinicOrPharmacyName: String = "",
    val address: String = "",
    val cityOrDistrict: String = "",
    val state: String = "",
    val pinCode: String = "",
    val mobileNumber: String = "",
    val alternatePhone: String = "",
    val codAmount: Double = 0.0,
    val isCod: Boolean = false,
    val shippingDate: String = "",
    val courierPartner: String = "ST COURIER / BLUEDART / SPEED POST",
    val packageWeightKg: Double = 1.0,
    val numberOfBoxes: Int = 1,
    val dimensionsCm: String = "25 x 20 x 15 cm",
    val contentDescription: String = "AYURVEDIC MEDICINES & HEALTHCARE PRODUCTS",
    val handlingInstructions: String = "FRAGILE - HANDLE WITH CARE - AYURVEDIC FORMULATIONS",
    val labelSize: String = "4x6_INCH", // 4x6_INCH, 100x150_MM, A4_SHEET, CUSTOM
    val returnAddress: String = "VEDA AYUR PHARMA, H.No. 4-22/A, Ayurveda Bhavan, Herbal Complex, Kurnool - 518002 (A.P.) Ph: +91 94401 23456",
    val createdAt: Long = System.currentTimeMillis()
)
