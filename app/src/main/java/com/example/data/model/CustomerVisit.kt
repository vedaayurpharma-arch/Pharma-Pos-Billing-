package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customer_visits")
data class CustomerVisit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val clinicOrPharmacyName: String,
    val salesRep: String = "Ramesh Kumar",
    val visitDateTime: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val visitNotes: String = "",
    val outcome: String = "ORDER_TAKEN" // ORDER_TAKEN, PAYMENT_COLLECTED, SAMPLE_GIVEN, FOLLOW_UP
)
