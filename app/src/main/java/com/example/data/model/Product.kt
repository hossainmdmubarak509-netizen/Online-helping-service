package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ProductCategory {
    RECHARGE,
    INTERNET,
    MINUTES,
    DRIVE_OFFER,
    GAMING,
    STREAMING,
    GIFT_CARD,
    NID_SERVICES
}

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: ProductCategory,
    val operatorOrBrand: String,
    val description: String,
    val price: Double,
    val regularPrice: Double = price,
    val validity: String = "30 Days",
    val inStock: Boolean = true,
    val badge: String? = null,
    val isFeatured: Boolean = false
)
