package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "field_customers")
data class FieldCustomer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerName: String,
    val clinicOrPharmacyName: String,
    val phone: String = "",
    val address: String,
    val state: String,
    val city: String,
    val pinCode: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val salesRep: String = "Ramesh Kumar",
    val dateAdded: String,
    val totalPurchasesValue: Double = 0.0,
    val lastVisitDate: String = "",
    val existingPartyId: Long? = null
)
