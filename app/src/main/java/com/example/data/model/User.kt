package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    CUSTOMER,
    ADMIN
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val passwordHash: String,
    val role: UserRole = UserRole.CUSTOMER,
    val walletBalance: Double = 0.0,
    val isBlocked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
