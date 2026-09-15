package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "company_profile")
data class CompanyProfile(
    @PrimaryKey val id: Long = 1,
    val companyName: String = "VEDA AYUR PHARMA",
    val tagline: String = "Ayurvedic & Pharmaceutical Healthcare ERP",
    val addressLine1: String = "H.No. 4-22/A, Ayurveda Bhavan, Herbal Complex",
    val addressLine2: String = "Kurnool - 518002 (A.P.) / Hyderabad / Bangalore",
    val phone: String = "+91 94401 23456 / 98450 12345",
    val email: String = "vedaayurpharma@gmail.com",
    val dlNo: String = "20B-AP-KNL-102930, 21B-AP-KNL-102931",
    val fssaiNo: String = "10020044001234",
    val gstin: String = "37AAKFV1234F1Z8",
    val pan: String = "AAKFV1234F",
    val state: String = "Andhra Pradesh (37)",
    val bankName: String = "State Bank of India",
    val accountNo: String = "39882201948",
    val ifscCode: String = "SBIN0001234",
    val branch: String = "Main Branch, Kurnool",
    val upiId: String = "vedapay@upi",
    val invoicePrefix: String = "VAP-",
    val termsConditions: String = "1. Goods once sold will not be taken back or exchanged.\n2. Expiry & breakage claims must be submitted within 60 days.\n3. Payment due within 21 days from invoice date. Overdue attracts 18% p.a.\n4. All disputes subject to Kurnool (A.P.) jurisdiction only."
)
