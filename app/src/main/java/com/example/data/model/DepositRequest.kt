package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DepositStatus {
    PENDING,
    APPROVED,
    REJECTED
}

@Entity(tableName = "deposit_requests")
data class DepositRequestEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val userPhone: String,
    val method: String, // bKash, Nagad, Rocket, Upay, Bank Transfer
    val senderNumber: String,
    val transactionId: String,
    val amount: Double,
    val status: DepositStatus = DepositStatus.PENDING,
    val userNote: String = "",
    val adminFeedback: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val processedAt: Long? = null
)
