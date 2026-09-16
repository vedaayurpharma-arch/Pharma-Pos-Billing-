package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tour_expenses")
data class TourExpense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val expenseDate: String,
    val salesRep: String = "Ramesh Kumar",
    val travelAmount: Double = 0.0,
    val foodAmount: Double = 0.0,
    val accommodationAmount: Double = 0.0,
    val otherAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val notes: String = "",
    val receiptUri: String = "",
    val status: String = "PENDING" // PENDING, APPROVED, REJECTED
) {
    val tourTitle: String get() = notes.ifBlank { "Tour Expense ($expenseDate)" }
    val date: String get() = expenseDate
    val lodgingAmount: Double get() = accommodationAmount
}
