package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String = "",
    val invoiceDate: String = "",
    val dueDate: String = "",
    val orderNo: String = "",
    val orderDate: String = "",
    val invoiceType: String = "TAX_INVOICE", // TAX_INVOICE, RETAIL, WHOLESALE, PROFORMA, SALES_ORDER, SALES_RETURN, CREDIT_NOTE, DELIVERY_CHALLAN
    val partyId: Long = 0,
    val partyName: String = "",
    val partyAddress: String = "",
    val partyPhone: String = "",
    val partyDlNo: String = "",
    val partyGstin: String = "",
    val paymentMode: String = "CREDIT", // CASH, CREDIT, UPI, BANK_TRANSFER
    val subTotalTaxable: Double = 0.0,
    val totalDiscount: Double = 0.0,
    val sgstPayable: Double = 0.0,
    val cgstPayable: Double = 0.0,
    val igstPayable: Double = 0.0,
    val crDrNote: Double = 0.0,
    val grandTotal: Double = 0.0,
    val paidAmount: Double = 0.0,
    val balanceAmount: Double = 0.0,
    val amountInWords: String = "",
    val status: String = "PAID", // PAID, PENDING, OVERDUE, CANCELLED
    val eInvoiceIrn: String = "",
    val eInvoiceAckNo: String = "",
    val eInvoiceAckDate: String = "",
    val eWayBillNo: String = "",
    val vehicleNo: String = "",
    val templateName: String = "Veda Pharma A4 Landscape",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
