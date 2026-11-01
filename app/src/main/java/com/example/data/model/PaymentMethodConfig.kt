package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_methods")
data class PaymentMethodConfigEntity(
    @PrimaryKey val id: String,
    val name: String, // bKash, Nagad, Rocket, Upay, Bank Transfer
    val accountType: String, // Personal, Agent, Merchant
    val accountNumber: String,
    val instructions: String,
    val minDeposit: Double = 50.0,
    val maxDeposit: Double = 25000.0,
    val isEnabled: Boolean = true
)
