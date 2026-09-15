package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts_transactions")
data class AccountsTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val type: String, // RECEIPT, PAYMENT, EXPENSE, CONTRA, JOURNAL
    val category: String, // Customer Receipt, Supplier Payment, Rent, Electricity, Salaries, Transport, Office Expense, Bank Deposit
    val amount: Double,
    val partyName: String = "",
    val paymentMode: String = "CASH", // CASH, BANK, UPI, CHEQUE
    val referenceNo: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
