package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoice_designer_config")
data class InvoiceDesignerConfig(
    @PrimaryKey val id: Long = 1,
    val selectedTemplate: String = "Veda Pharma A4 Landscape", // 20+ templates
    val showLogo: Boolean = true,
    val showCustomerGstin: Boolean = true,
    val showCustomerDl: Boolean = true,
    val showHsn: Boolean = true,
    val showBatch: Boolean = true,
    val showExpiry: Boolean = true,
    val showMrp: Boolean = true,
    val showDiscount: Boolean = true,
    val showCgstSgst: Boolean = true,
    val showUpiQr: Boolean = true,
    val showEInvoiceQr: Boolean = true,
    val showBankDetails: Boolean = true,
    val showTerms: Boolean = true,
    val showSignature: Boolean = true,
    val primaryColorHex: String = "#0D5C3A", // Veda Ayurvedic Deep Green
    val accentColorHex: String = "#1E824C",
    val fontSizePt: Int = 10,
    val paperOrientation: String = "LANDSCAPE", // LANDSCAPE, PORTRAIT, THERMAL
    val thermalPaperWidthMm: Int = 80 // 80mm or 58mm
)
