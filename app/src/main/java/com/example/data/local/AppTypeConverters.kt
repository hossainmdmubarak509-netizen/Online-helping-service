package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.*

class AppTypeConverters {
    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = runCatching { UserRole.valueOf(value) }.getOrDefault(UserRole.CUSTOMER)

    @TypeConverter
    fun fromDepositStatus(value: DepositStatus): String = value.name

    @TypeConverter
    fun toDepositStatus(value: String): DepositStatus = runCatching { DepositStatus.valueOf(value) }.getOrDefault(DepositStatus.PENDING)

    @TypeConverter
    fun fromOrderStatus(value: OrderStatus): String = value.name

    @TypeConverter
    fun toOrderStatus(value: String): OrderStatus = runCatching { OrderStatus.valueOf(value) }.getOrDefault(OrderStatus.PENDING)

    @TypeConverter
    fun fromServiceType(value: ServiceType): String = value.name

    @TypeConverter
    fun toServiceType(value: String): ServiceType = runCatching { ServiceType.valueOf(value) }.getOrDefault(ServiceType.MOBILE_RECHARGE)

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = runCatching { TransactionType.valueOf(value) }.getOrDefault(TransactionType.DEPOSIT_APPROVED)

    @TypeConverter
    fun fromProductCategory(value: ProductCategory): String = value.name

    @TypeConverter
    fun toProductCategory(value: String): ProductCategory = runCatching { ProductCategory.valueOf(value) }.getOrDefault(ProductCategory.RECHARGE)
}
