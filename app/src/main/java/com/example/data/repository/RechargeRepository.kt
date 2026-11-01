package com.example.data.repository

import com.example.data.local.RechargeDao
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class RechargeRepository(private val dao: RechargeDao) {

    // --- Users & Auth ---
    fun getAllUsersFlow(): Flow<List<UserEntity>> = dao.getAllUsersFlow()

    suspend fun getUserById(userId: String): UserEntity? = withContext(Dispatchers.IO) {
        dao.getUserById(userId)
    }

    suspend fun login(identifier: String, password: String):Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanId = identifier.trim()
        val user = dao.getUserByEmailOrPhone(cleanId)
            ?: return@withContext Result.failure(Exception("Account not found with $cleanId. Please register first."))

        if (user.passwordHash != password.trim()) {
            return@withContext Result.failure(Exception("Incorrect password. Please try again."))
        }

        if (user.isBlocked) {
            return@withContext Result.failure(Exception("Account is temporarily blocked by admin. Contact support."))
        }

        Result.success(user)
    }

    suspend fun register(
        name: String,
        email: String,
        phone: String,
        password: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val cleanPhone = phone.trim()

        if (cleanEmail.isEmpty() || cleanPhone.isEmpty() || name.trim().isEmpty() || password.length < 4) {
            return@withContext Result.failure(Exception("Please fill all fields accurately. Password must be at least 4 characters."))
        }

        val existingUser = dao.getUserByEmailOrPhone(cleanEmail) ?: dao.getUserByEmailOrPhone(cleanPhone)
        if (existingUser != null) {
            return@withContext Result.failure(Exception("An account with this email or phone number already exists."))
        }

        val newUser = UserEntity(
            id = "user_" + UUID.randomUUID().toString().take(8),
            name = name.trim(),
            email = cleanEmail,
            phone = cleanPhone,
            passwordHash = password.trim(),
            role = UserRole.CUSTOMER,
            walletBalance = 50.0 // Welcome bonus ৳50!
        )
        dao.insertUser(newUser)

        // Log welcome credit
        dao.insertTransaction(
            WalletTransactionEntity(
                id = "tx_" + UUID.randomUUID().toString().take(8),
                userId = newUser.id,
                type = TransactionType.ADMIN_CREDIT,
                amount = 50.0,
                balanceAfter = 50.0,
                title = "Welcome Bonus Added to Wallet"
            )
        )

        Result.success(newUser)
    }

    suspend fun toggleUserBlock(userId: String, blocked: Boolean) = withContext(Dispatchers.IO) {
        dao.updateUserBlockStatus(userId, blocked)
    }

    suspend fun adjustUserBalance(
        userId: String,
        amountDelta: Double,
        isCredit: Boolean,
        reason: String
    ): Result<Double> = withContext(Dispatchers.IO) {
        val user = dao.getUserById(userId) ?: return@withContext Result.failure(Exception("User not found"))
        val finalDelta = if (isCredit) kotlin.math.abs(amountDelta) else -kotlin.math.abs(amountDelta)
        val newBalance = user.walletBalance + finalDelta

        if (newBalance < 0) {
            return@withContext Result.failure(Exception("Deduction exceeds user's current balance (৳${user.walletBalance})."))
        }

        dao.updateUserBalance(userId, newBalance)
        dao.insertTransaction(
            WalletTransactionEntity(
                id = "tx_" + UUID.randomUUID().toString().take(8),
                userId = userId,
                type = if (isCredit) TransactionType.ADMIN_CREDIT else TransactionType.ADMIN_DEBIT,
                amount = kotlin.math.abs(amountDelta),
                balanceAfter = newBalance,
                title = if (isCredit) "Admin Balance Credit: $reason" else "Admin Balance Debit: $reason"
            )
        )
        Result.success(newBalance)
    }

    // --- Deposits & Wallet ---
    fun getAllDepositRequestsFlow(): Flow<List<DepositRequestEntity>> = dao.getAllDepositRequestsFlow()
    fun getDepositRequestsByUserFlow(userId: String): Flow<List<DepositRequestEntity>> = dao.getDepositRequestsByUserFlow(userId)
    fun getTransactionsByUserFlow(userId: String): Flow<List<WalletTransactionEntity>> = dao.getTransactionsByUserFlow(userId)
    fun getAllTransactionsFlow(): Flow<List<WalletTransactionEntity>> = dao.getAllTransactionsFlow()
    fun getPendingDepositCountFlow(): Flow<Int> = dao.getPendingDepositCountFlow()

    suspend fun submitDepositRequest(
        user: UserEntity,
        method: String,
        senderNumber: String,
        transactionId: String,
        amount: Double,
        userNote: String
    ): Result<DepositRequestEntity> = withContext(Dispatchers.IO) {
        if (amount <= 0) {
            return@withContext Result.failure(Exception("Deposit amount must be greater than zero."))
        }
        if (transactionId.trim().isEmpty() || senderNumber.trim().isEmpty()) {
            return@withContext Result.failure(Exception("Sender number and Transaction ID (TrxID) are required."))
        }

        val deposit = DepositRequestEntity(
            id = "dep_" + UUID.randomUUID().toString().take(8),
            userId = user.id,
            userName = user.name,
            userPhone = user.phone,
            method = method,
            senderNumber = senderNumber.trim(),
            transactionId = transactionId.trim().uppercase(),
            amount = amount,
            status = DepositStatus.PENDING,
            userNote = userNote.trim()
        )
        dao.insertDepositRequest(deposit)
        Result.success(deposit)
    }

    suspend fun approveDepositRequest(depositId: String, feedback: String?): Result<Unit> = withContext(Dispatchers.IO) {
        val deposit = dao.getDepositRequestById(depositId)
            ?: return@withContext Result.failure(Exception("Deposit request not found"))

        if (deposit.status == DepositStatus.APPROVED) {
            return@withContext Result.failure(Exception("This deposit is already approved."))
        }

        val user = dao.getUserById(deposit.userId)
            ?: return@withContext Result.failure(Exception("User no longer exists"))

        val newBalance = user.walletBalance + deposit.amount
        dao.updateUserBalance(user.id, newBalance)

        val updatedDeposit = deposit.copy(
            status = DepositStatus.APPROVED,
            adminFeedback = feedback ?: "Approved by admin",
            processedAt = System.currentTimeMillis()
        )
        dao.updateDepositRequest(updatedDeposit)

        dao.insertTransaction(
            WalletTransactionEntity(
                id = "tx_" + UUID.randomUUID().toString().take(8),
                userId = user.id,
                type = TransactionType.DEPOSIT_APPROVED,
                amount = deposit.amount,
                balanceAfter = newBalance,
                title = "Deposit Approved via ${deposit.method}",
                referenceId = deposit.transactionId
            )
        )

        Result.success(Unit)
    }

    suspend fun rejectDepositRequest(depositId: String, reason: String): Result<Unit> = withContext(Dispatchers.IO) {
        val deposit = dao.getDepositRequestById(depositId)
            ?: return@withContext Result.failure(Exception("Deposit request not found"))

        if (deposit.status != DepositStatus.PENDING) {
            return@withContext Result.failure(Exception("Can only reject pending requests."))
        }

        val updatedDeposit = deposit.copy(
            status = DepositStatus.REJECTED,
            adminFeedback = if (reason.isBlank()) "Invalid Transaction ID or payment unverified" else reason.trim(),
            processedAt = System.currentTimeMillis()
        )
        dao.updateDepositRequest(updatedDeposit)
        Result.success(Unit)
    }

    // --- Orders & Instant Refunds ---
    fun getAllOrdersFlow(): Flow<List<OrderEntity>> = dao.getAllOrdersFlow()
    fun getOrdersByUserFlow(userId: String): Flow<List<OrderEntity>> = dao.getOrdersByUserFlow(userId)
    fun getPendingOrderCountFlow(): Flow<Int> = dao.getPendingOrderCountFlow()

    suspend fun placeOrder(
        user: UserEntity,
        serviceType: ServiceType,
        title: String,
        operatorOrBrand: String,
        recipientTarget: String,
        rechargeType: String,
        amount: Double,
        applicantName: String? = null,
        nidNumber: String? = null,
        dateOfBirth: String? = null,
        applicantAddress: String? = null,
        division: String? = null,
        district: String? = null,
        upazila: String? = null,
        documentFileName: String? = null,
        additionalNotes: String? = null
    ): Result<OrderEntity> = withContext(Dispatchers.IO) {
        val freshUser = dao.getUserById(user.id) ?: return@withContext Result.failure(Exception("User not found"))

        if (freshUser.walletBalance < amount) {
            return@withContext Result.failure(
                Exception("Insufficient wallet balance (Current: ৳${String.format("%.2f", freshUser.walletBalance)}). Please deposit at least ৳${String.format("%.2f", amount - freshUser.walletBalance)} to continue.")
            )
        }

        if (recipientTarget.trim().isEmpty()) {
            return@withContext Result.failure(Exception("Please enter a valid recipient number or account identifier."))
        }

        // Deduct from wallet atomically
        val newBalance = freshUser.walletBalance - amount
        dao.updateUserBalance(freshUser.id, newBalance)

        val order = OrderEntity(
            id = "ord_" + UUID.randomUUID().toString().take(8),
            userId = freshUser.id,
            userName = freshUser.name,
            userPhone = freshUser.phone,
            serviceType = serviceType,
            title = title,
            operatorOrBrand = operatorOrBrand,
            recipientTarget = recipientTarget.trim(),
            rechargeType = rechargeType,
            amount = amount,
            status = OrderStatus.PENDING,
            applicantName = applicantName,
            nidNumber = nidNumber,
            dateOfBirth = dateOfBirth,
            applicantAddress = applicantAddress,
            division = division,
            district = district,
            upazila = upazila,
            documentFileName = documentFileName,
            additionalNotes = additionalNotes
        )
        dao.insertOrder(order)

        // Log transaction
        dao.insertTransaction(
            WalletTransactionEntity(
                id = "tx_" + UUID.randomUUID().toString().take(8),
                userId = freshUser.id,
                type = TransactionType.ORDER_PAYMENT,
                amount = amount,
                balanceAfter = newBalance,
                title = "Payment for $title (${order.operatorOrBrand})",
                referenceId = order.id
            )
        )

        Result.success(order)
    }

    suspend fun updateOrderStatus(
        orderId: String,
        newStatus: OrderStatus,
        operatorTrxId: String? = null,
        adminNote: String? = null,
        deliveryPdfFileName: String? = null,
        deliveryPdfSize: String? = null,
        deliveryPdfTitle: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val order = dao.getOrderById(orderId)
            ?: return@withContext Result.failure(Exception("Order not found"))

        if (order.status == OrderStatus.COMPLETED && newStatus != OrderStatus.COMPLETED) {
            return@withContext Result.failure(Exception("Completed orders cannot be modified."))
        }

        // CRITICAL: "Rejected orders refund to wallet."
        if (newStatus == OrderStatus.REJECTED && !order.isRefunded) {
            val user = dao.getUserById(order.userId)
            if (user != null) {
                val newBalance = user.walletBalance + order.amount
                dao.updateUserBalance(user.id, newBalance)

                dao.insertTransaction(
                    WalletTransactionEntity(
                        id = "tx_" + UUID.randomUUID().toString().take(8),
                        userId = user.id,
                        type = TransactionType.ORDER_REFUND,
                        amount = order.amount,
                        balanceAfter = newBalance,
                        title = "Refund for Rejected Order: ${order.title}",
                        referenceId = order.id
                    )
                )
            }

            val updatedOrder = order.copy(
                status = OrderStatus.REJECTED,
                isRefunded = true,
                adminNote = adminNote ?: "Order rejected by operator. Amount ৳${order.amount} refunded back to wallet.",
                updatedAt = System.currentTimeMillis()
            )
            dao.updateOrder(updatedOrder)
            return@withContext Result.success(Unit)
        }

        val hasNewPdf = !deliveryPdfFileName.isNullOrBlank()
        val updatedOrder = order.copy(
            status = newStatus,
            operatorTrxId = operatorTrxId ?: order.operatorTrxId,
            adminNote = adminNote ?: order.adminNote,
            deliveryPdfFileName = if (hasNewPdf) deliveryPdfFileName else order.deliveryPdfFileName,
            deliveryPdfSize = if (hasNewPdf) deliveryPdfSize else order.deliveryPdfSize,
            deliveryPdfTitle = if (hasNewPdf) deliveryPdfTitle else order.deliveryPdfTitle,
            deliveryPdfUploadedAt = if (hasNewPdf) System.currentTimeMillis() else order.deliveryPdfUploadedAt,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateOrder(updatedOrder)
        Result.success(Unit)
    }

    // --- Products & Services ---
    fun getAllProductsFlow(): Flow<List<ProductEntity>> = dao.getAllProductsFlow()
    fun getProductsByCategoryFlow(category: ProductCategory): Flow<List<ProductEntity>> = dao.getProductsByCategoryFlow(category)

    suspend fun saveProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        dao.insertProduct(product)
    }

    suspend fun toggleProductStock(product: ProductEntity) = withContext(Dispatchers.IO) {
        dao.updateProduct(product.copy(inStock = !product.inStock))
    }

    suspend fun deleteProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        dao.deleteProduct(product)
    }

    // --- Payment Methods ---
    fun getAllPaymentMethodsFlow(): Flow<List<PaymentMethodConfigEntity>> = dao.getAllPaymentMethodsFlow()

    suspend fun updatePaymentMethod(method: PaymentMethodConfigEntity) = withContext(Dispatchers.IO) {
        dao.updatePaymentMethod(method)
    }

    // --- Support & FAQ ---
    fun getSupportConfigFlow(): Flow<SupportConfigEntity?> = dao.getSupportConfigFlow()

    fun getAllFaqsFlow(): Flow<List<FaqItemEntity>> = dao.getAllFaqsFlow()

    suspend fun saveSupportConfig(config: SupportConfigEntity) = withContext(Dispatchers.IO) {
        dao.insertOrUpdateSupportConfig(config.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun saveFaq(faq: FaqItemEntity) = withContext(Dispatchers.IO) {
        dao.insertFaq(faq)
    }

    suspend fun deleteFaq(faq: FaqItemEntity) = withContext(Dispatchers.IO) {
        dao.deleteFaq(faq)
    }
}
