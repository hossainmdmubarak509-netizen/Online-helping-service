package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class OrderStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    REJECTED
}

enum class ServiceType {
    MOBILE_RECHARGE,
    DRIVE_PACK,
    DIGITAL_SERVICE,
    GAMING_TOPUP,
    GIFT_CARD,
    NID_SERVICE
}

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val userPhone: String,
    val serviceType: ServiceType,
    val title: String,
    val operatorOrBrand: String, // GP, Banglalink, Robi, Airtel, Teletalk, Election Commission, etc.
    val recipientTarget: String, // Mobile number, Player ID, NID Number, account email
    val rechargeType: String = "PREPAID", // PREPAID, POSTPAID, SKITTO, PIN, NID_APPLICATION
    val amount: Double,
    val status: OrderStatus = OrderStatus.PENDING,
    val operatorTrxId: String? = null,
    val adminNote: String? = null,
    val isRefunded: Boolean = false,
    val applicantName: String? = null,
    val nidNumber: String? = null,
    val dateOfBirth: String? = null,
    val applicantAddress: String? = null,
    val division: String? = null,
    val district: String? = null,
    val upazila: String? = null,
    val documentFileName: String? = null,
    val additionalNotes: String? = null,
    val deliveryPdfFileName: String? = null,
    val deliveryPdfSize: String? = null,
    val deliveryPdfTitle: String? = null,
    val deliveryPdfUploadedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
