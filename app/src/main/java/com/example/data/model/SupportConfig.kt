package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "support_config")
data class SupportConfigEntity(
    @PrimaryKey val id: String = "primary_support_config",
    val phoneHotline: String = "+8801712345678",
    val whatsappNumber: String = "+8801712345678",
    val email: String = "support@rechargehub.com",
    val officeHours: String = "Daily 9:00 AM - 11:30 PM (BST)",
    val noticeBanner: String = "WhatsApp & Hotline support is live! For instant NID file or recharge verification, share your Order ID.",
    val isLiveSupportOnline: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "faq_items")
data class FaqItemEntity(
    @PrimaryKey val id: String,
    val question: String,
    val answer: String,
    val category: String, // "Recharge", "NID Services", "Wallet & Deposit", "Refunds", "General"
    val sortOrder: Int = 0
)
