package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    DEPOSIT_APPROVED,
    ORDER_PAYMENT,
    ORDER_REFUND,
    ADMIN_CREDIT,
    ADMIN_DEBIT
}

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val type: TransactionType,
    val amount: Double,
    val balanceAfter: Double,
    val title: String,
    val referenceId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
