package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.RechargeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class OrderDraft(
    val serviceType: ServiceType,
    val title: String,
    val operatorOrBrand: String,
    val recipientTarget: String,
    val rechargeType: String = "PREPAID",
    val amount: Double,
    val applicantName: String? = null,
    val nidNumber: String? = null,
    val dateOfBirth: String? = null,
    val applicantAddress: String? = null,
    val division: String? = null,
    val district: String? = null,
    val upazila: String? = null,
    val documentFileName: String? = null,
    val additionalNotes: String? = null
)

class AppViewModel(private val repository: RechargeRepository) : ViewModel() {

    // --- Current Session & Mode ---
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    private val _activeScreen = MutableStateFlow("home")
    val activeScreen: StateFlow<String> = _activeScreen.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    // --- Data Streams ---
    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDepositRequests: StateFlow<List<DepositRequestEntity>> = repository.getAllDepositRequestsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<OrderEntity>> = repository.getAllOrdersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<ProductEntity>> = repository.getAllProductsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val paymentMethods: StateFlow<List<PaymentMethodConfigEntity>> = repository.getAllPaymentMethodsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingDepositCount: StateFlow<Int> = repository.getPendingDepositCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingOrderCount: StateFlow<Int> = repository.getPendingOrderCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val supportConfig: StateFlow<SupportConfigEntity> = repository.getSupportConfigFlow()
        .map { it ?: SupportConfigEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SupportConfigEntity())

    val faqList: StateFlow<List<FaqItemEntity>> = repository.getAllFaqsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current User scoped data
    val userOrders: StateFlow<List<OrderEntity>> = _currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else repository.getOrdersByUserFlow(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userDeposits: StateFlow<List<DepositRequestEntity>> = _currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else repository.getDepositRequestsByUserFlow(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userTransactions: StateFlow<List<WalletTransactionEntity>> = _currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else repository.getTransactionsByUserFlow(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Draft Dialog state
    private val _orderDraft = MutableStateFlow<OrderDraft?>(null)
    val orderDraft: StateFlow<OrderDraft?> = _orderDraft.asStateFlow()

    init {
        // Auto-login default customer user on first launch so user can immediately play with app
        viewModelScope.launch {
            repository.login("hossainmdmubarak509@gmail.com", "pass123")
                .onSuccess { user ->
                    _currentUser.value = user
                }
        }

        // Keep current user updated with database changes (e.g. balance updates)
        viewModelScope.launch {
            allUsers.collect { users ->
                val current = _currentUser.value ?: return@collect
                val updated = users.find { it.id == current.id }
                if (updated != null) {
                    _currentUser.value = updated
                }
            }
        }
    }

    fun navigateTo(screen: String) {
        _activeScreen.value = screen
    }

    fun toggleAdminMode() {
        val willBeAdmin = !_isAdminMode.value
        _isAdminMode.value = willBeAdmin
        if (willBeAdmin) {
            _activeScreen.value = "admin"
            showNotice("Switched to Admin Panel View")
        } else {
            _activeScreen.value = "home"
            showNotice("Switched to Customer App View")
        }
    }

    fun showNotice(msg: String) {
        viewModelScope.launch {
            _snackbarMessage.emit(msg)
        }
    }

    // --- Authentication ---
    fun login(identifier: String, pass: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.login(identifier, pass)
                .onSuccess { user ->
                    _currentUser.value = user
                    if (user.role == UserRole.ADMIN) {
                        _isAdminMode.value = true
                        _activeScreen.value = "admin"
                    } else {
                        _isAdminMode.value = false
                        _activeScreen.value = "home"
                    }
                    showNotice("Welcome back, ${user.name}!")
                    onSuccess()
                }
                .onFailure { err ->
                    showNotice(err.message ?: "Login failed")
                }
        }
    }

    fun register(name: String, email: String, phone: String, pass: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.register(name, email, phone, pass)
                .onSuccess { user ->
                    _currentUser.value = user
                    _isAdminMode.value = false
                    _activeScreen.value = "home"
                    showNotice("Account created! Enjoy ৳50 welcome bonus.")
                    onSuccess()
                }
                .onFailure { err ->
                    showNotice(err.message ?: "Registration failed")
                }
        }
    }

    fun logout() {
        _currentUser.value = null
        _isAdminMode.value = false
        _activeScreen.value = "auth"
        showNotice("Logged out successfully.")
    }

    // --- Deposit Management ---
    fun submitDeposit(method: String, senderPhone: String, trxId: String, amount: Double, note: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.submitDepositRequest(user, method, senderPhone, trxId, amount, note)
                .onSuccess {
                    showNotice("Deposit request for ৳$amount submitted! Awaiting admin approval.")
                    _activeScreen.value = "wallet"
                }
                .onFailure {
                    showNotice(it.message ?: "Failed to submit deposit")
                }
        }
    }

    fun approveDeposit(depositId: String, feedback: String? = null) {
        viewModelScope.launch {
            repository.approveDepositRequest(depositId, feedback)
                .onSuccess {
                    showNotice("Deposit approved! User wallet has been credited.")
                }
                .onFailure {
                    showNotice(it.message ?: "Error approving deposit")
                }
        }
    }

    fun rejectDeposit(depositId: String, reason: String) {
        viewModelScope.launch {
            repository.rejectDepositRequest(depositId, reason)
                .onSuccess {
                    showNotice("Deposit request rejected.")
                }
                .onFailure {
                    showNotice(it.message ?: "Error rejecting deposit")
                }
        }
    }

    // --- Order Management ---
    fun prepareOrderDraft(draft: OrderDraft) {
        _orderDraft.value = draft
    }

    fun clearOrderDraft() {
        _orderDraft.value = null
    }

    fun confirmPlaceOrder() {
        val draft = _orderDraft.value ?: return
        val user = _currentUser.value ?: return

        viewModelScope.launch {
            repository.placeOrder(
                user = user,
                serviceType = draft.serviceType,
                title = draft.title,
                operatorOrBrand = draft.operatorOrBrand,
                recipientTarget = draft.recipientTarget,
                rechargeType = draft.rechargeType,
                amount = draft.amount,
                applicantName = draft.applicantName,
                nidNumber = draft.nidNumber,
                dateOfBirth = draft.dateOfBirth,
                applicantAddress = draft.applicantAddress,
                division = draft.division,
                district = draft.district,
                upazila = draft.upazila,
                documentFileName = draft.documentFileName,
                additionalNotes = draft.additionalNotes
            ).onSuccess { order ->
                _orderDraft.value = null
                showNotice("Order placed! Status: Pending (Order #${order.id.takeLast(6)})")
                _activeScreen.value = "orders"
            }.onFailure { err ->
                showNotice(err.message ?: "Failed to place order")
            }
        }
    }

    // Admin updates order status
    fun updateOrderStatus(
        orderId: String,
        newStatus: OrderStatus,
        operatorTrxId: String? = null,
        adminNote: String? = null,
        deliveryPdfFileName: String? = null,
        deliveryPdfSize: String? = null,
        deliveryPdfTitle: String? = null
    ) {
        viewModelScope.launch {
            repository.updateOrderStatus(
                orderId = orderId,
                newStatus = newStatus,
                operatorTrxId = operatorTrxId,
                adminNote = adminNote,
                deliveryPdfFileName = deliveryPdfFileName,
                deliveryPdfSize = deliveryPdfSize,
                deliveryPdfTitle = deliveryPdfTitle
            ).onSuccess {
                if (newStatus == OrderStatus.REJECTED) {
                    showNotice("Order marked as REJECTED. Amount automatically refunded to user wallet!")
                } else if (newStatus == OrderStatus.COMPLETED && !deliveryPdfFileName.isNullOrBlank()) {
                    showNotice("Order completed with delivery PDF attached!")
                } else {
                    showNotice("Order updated to ${newStatus.name}")
                }
            }.onFailure {
                showNotice(it.message ?: "Error updating order")
            }
        }
    }

    // --- User & Balance Admin Controls ---
    fun adjustUserBalance(userId: String, amount: Double, isCredit: Boolean, reason: String) {
        viewModelScope.launch {
            repository.adjustUserBalance(userId, amount, isCredit, reason)
                .onSuccess { newBal ->
                    showNotice("User wallet updated. New balance: ৳${String.format("%.2f", newBal)}")
                }
                .onFailure {
                    showNotice(it.message ?: "Adjustment failed")
                }
        }
    }

    fun toggleUserBlock(userId: String, isBlocked: Boolean) {
        viewModelScope.launch {
            repository.toggleUserBlock(userId, !isBlocked)
            showNotice(if (!isBlocked) "User account blocked" else "User account unblocked")
        }
    }

    // --- Product Admin Controls ---
    fun toggleProductStock(product: ProductEntity) {
        viewModelScope.launch {
            repository.toggleProductStock(product)
            showNotice("${product.title} is now ${if (product.inStock) "Out of Stock" else "In Stock"}")
        }
    }

    fun saveProduct(
        id: String? = null,
        title: String,
        category: ProductCategory,
        operatorOrBrand: String,
        description: String,
        price: Double,
        regularPrice: Double,
        validity: String,
        badge: String?
    ) {
        viewModelScope.launch {
            val product = ProductEntity(
                id = id ?: ("prod_" + UUID.randomUUID().toString().take(8)),
                title = title.trim(),
                category = category,
                operatorOrBrand = operatorOrBrand.trim(),
                description = description.trim(),
                price = price,
                regularPrice = regularPrice,
                validity = validity.trim(),
                inStock = true,
                badge = badge?.ifBlank { null }
            )
            repository.saveProduct(product)
            showNotice("Service package saved successfully!")
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            showNotice("Product removed.")
        }
    }

    // --- Payment Methods Admin Controls ---
    fun updatePaymentMethod(method: PaymentMethodConfigEntity) {
        viewModelScope.launch {
            repository.updatePaymentMethod(method)
            showNotice("Payment method updated.")
        }
    }

    // --- Support & FAQ Admin Controls ---
    fun updateSupportConfig(
        phoneHotline: String,
        whatsappNumber: String,
        email: String,
        officeHours: String,
        noticeBanner: String,
        isLiveSupportOnline: Boolean
    ) {
        viewModelScope.launch {
            val updated = SupportConfigEntity(
                id = "primary_support_config",
                phoneHotline = phoneHotline.trim(),
                whatsappNumber = whatsappNumber.trim(),
                email = email.trim(),
                officeHours = officeHours.trim(),
                noticeBanner = noticeBanner.trim(),
                isLiveSupportOnline = isLiveSupportOnline,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveSupportConfig(updated)
            showNotice("Support settings saved successfully!")
        }
    }

    fun saveFaq(id: String?, question: String, answer: String, category: String, sortOrder: Int = 0) {
        viewModelScope.launch {
            val faq = FaqItemEntity(
                id = id ?: ("faq_" + UUID.randomUUID().toString().take(8)),
                question = question.trim(),
                answer = answer.trim(),
                category = category.trim(),
                sortOrder = sortOrder
            )
            repository.saveFaq(faq)
            showNotice("FAQ saved successfully.")
        }
    }

    fun deleteFaq(faq: FaqItemEntity) {
        viewModelScope.launch {
            repository.deleteFaq(faq)
            showNotice("FAQ question removed.")
        }
    }
}

class AppViewModelFactory(private val repository: RechargeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
