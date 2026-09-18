package com.example.data.model

data class ShippingLabelSettings(
    val qrIncludeAddressOnly: Boolean = true, // When scanned, shipping address is visible in QR code
    val hideProductAndPrice: Boolean = true, // Don't mention any product or price
    val showReturnAddress: Boolean = true,
    val showHandlingInstructions: Boolean = true,
    val showCourierPartner: Boolean = true,
    val showBarcode: Boolean = true,
    val defaultLabelSize: String = "4x6_INCH", // 4x6_INCH, 100x150_MM, A4_SHEET
    val defaultCourier: String = "ST COURIER / BLUEDART / SPEED POST",
    val defaultCity: String = "Kurnool",
    val defaultState: String = "Andhra Pradesh",
    val defaultPincode: String = "518002",
    val customFooterNote: String = "FRAGILE - HANDLE WITH CARE - AYURVEDIC FORMULATIONS"
)

data class ShippingPackageItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val boxNumber: String = "Box 1",
    val description: String = "Package Parcel",
    val weightKg: Double = 1.0,
    val dimensions: String = "25 x 20 x 15 cm"
)
