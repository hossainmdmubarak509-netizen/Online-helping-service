package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RechargeDao {

    // --- Users ---
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE (email = :identifier OR phone = :identifier) LIMIT 1")
    suspend fun getUserByEmailOrPhone(identifier: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET walletBalance = :newBalance WHERE id = :userId")
    suspend fun updateUserBalance(userId: String, newBalance: Double)

    @Query("UPDATE users SET isBlocked = :blocked WHERE id = :userId")
    suspend fun updateUserBlockStatus(userId: String, blocked: Boolean)

    // --- Deposit Requests ---
    @Query("SELECT * FROM deposit_requests ORDER BY createdAt DESC")
    fun getAllDepositRequestsFlow(): Flow<List<DepositRequestEntity>>

    @Query("SELECT * FROM deposit_requests WHERE userId = :userId ORDER BY createdAt DESC")
    fun getDepositRequestsByUserFlow(userId: String): Flow<List<DepositRequestEntity>>

    @Query("SELECT * FROM deposit_requests WHERE id = :id LIMIT 1")
    suspend fun getDepositRequestById(id: String): DepositRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepositRequest(deposit: DepositRequestEntity)

    @Update
    suspend fun updateDepositRequest(deposit: DepositRequestEntity)

    @Query("SELECT COUNT(*) FROM deposit_requests WHERE status = 'PENDING'")
    fun getPendingDepositCountFlow(): Flow<Int>

    // --- Orders ---
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrdersFlow(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY createdAt DESC")
    fun getOrdersByUserFlow(userId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: String): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("SELECT COUNT(*) FROM orders WHERE status = 'PENDING'")
    fun getPendingOrderCountFlow(): Flow<Int>

    // --- Wallet Transactions ---
    @Query("SELECT * FROM wallet_transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsByUserFlow(userId: String): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC LIMIT 100")
    fun getAllTransactionsFlow(): Flow<List<WalletTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransactionEntity)

    // --- Products & Services ---
    @Query("SELECT * FROM products ORDER BY category, price ASC")
    fun getAllProductsFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY price ASC")
    fun getProductsByCategoryFlow(category: ProductCategory): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    // --- Payment Methods ---
    @Query("SELECT * FROM payment_methods ORDER BY name ASC")
    fun getAllPaymentMethodsFlow(): Flow<List<PaymentMethodConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethods(methods: List<PaymentMethodConfigEntity>)

    @Update
    suspend fun updatePaymentMethod(method: PaymentMethodConfigEntity)

    // --- Support & FAQ ---
    @Query("SELECT * FROM support_config WHERE id = 'primary_support_config' LIMIT 1")
    fun getSupportConfigFlow(): Flow<SupportConfigEntity?>

    @Query("SELECT * FROM support_config WHERE id = 'primary_support_config' LIMIT 1")
    suspend fun getSupportConfig(): SupportConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSupportConfig(config: SupportConfigEntity)

    @Query("SELECT * FROM faq_items ORDER BY sortOrder ASC, id ASC")
    fun getAllFaqsFlow(): Flow<List<FaqItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaqs(faqs: List<FaqItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaq(faq: FaqItemEntity)

    @Update
    suspend fun updateFaq(faq: FaqItemEntity)

    @Delete
    suspend fun deleteFaq(faq: FaqItemEntity)
}
