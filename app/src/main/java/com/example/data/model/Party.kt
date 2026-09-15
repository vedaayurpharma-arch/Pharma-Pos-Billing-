package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parties")
data class Party(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String = "CUSTOMER", // CUSTOMER, SUPPLIER, DISTRIBUTOR
    val address: String,
    val phone: String = "",
    val email: String = "",
    val dlNo: String = "",
    val gstin: String = "",
    val pan: String = "",
    val state: String = "Andhra Pradesh (37)",
    val creditLimit: Double = 50000.0,
    val creditDays: Int = 21,
    val openingBalance: Double = 0.0,
    val currentBalance: Double = 0.0, // Positive: Receivable (Customer owes us), Negative: Payable
    val area: String = "Town",
    val route: String = "Route 1",
    val salesman: String = "Direct"
)
