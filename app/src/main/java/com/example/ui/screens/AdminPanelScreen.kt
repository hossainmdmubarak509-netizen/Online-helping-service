package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.DepositStatusBadge
import com.example.ui.components.OrderStatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminPanelScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Reports, 1: Deposits, 2: Orders, 3: Users, 4: Products, 5: Payment Config

    val allUsers by viewModel.allUsers.collectAsState()
    val allDeposits by viewModel.allDepositRequests.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val paymentMethods by viewModel.paymentMethods.collectAsState()

    // Dialog States
    var activeDepositToReject by remember { mutableStateOf<DepositRequestEntity?>(null) }
    var rejectionReason by remember { mutableStateOf("") }

    var activeOrderToComplete by remember { mutableStateOf<OrderEntity?>(null) }
    var operatorTrxIdInput by remember { mutableStateOf("") }
    var orderCompleteNote by remember { mutableStateOf("") }
    var deliveryPdfFileNameInput by remember { mutableStateOf("") }
    var deliveryPdfSizeInput by remember { mutableStateOf("248 KB") }

    var activeOrderToReject by remember { mutableStateOf<OrderEntity?>(null) }
    var orderRejectReason by remember { mutableStateOf("") }

    var activeUserToAdjust by remember { mutableStateOf<UserEntity?>(null) }
    var adjustmentAmount by remember { mutableStateOf("100") }
    var isCreditAdjustment by remember { mutableStateOf(true) }
    var adjustmentReason by remember { mutableStateOf("Admin adjustment") }

    var showAddProductDialog by remember { mutableStateOf(false) }
    var activeProductToEditPrice by remember { mutableStateOf<ProductEntity?>(null) }
    var updatedPriceInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_panel_screen")
    ) {
        // Admin Top Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(MaterialTheme.colorScheme.tertiary, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Text("Admin Control Center", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }

                    FilledTonalButton(
                        onClick = { viewModel.toggleAdminMode() },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("App View", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Admin Navigation Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val tabs = listOf(
                        "Reports",
                        "Deposits (${allDeposits.count { it.status == DepositStatus.PENDING }})",
                        "Orders (${allOrders.count { it.status == OrderStatus.PENDING }})",
                        "Users (${allUsers.size})",
                        "Products",
                        "Payment Accounts",
                        "Support Settings"
                    )
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }
            }
        }

        // Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> AdminReportsTab(allUsers, allDeposits, allOrders)
                1 -> AdminDepositsTab(
                    deposits = allDeposits,
                    onApprove = { dep -> viewModel.approveDeposit(dep.id) },
                    onRejectPrompt = { dep ->
                        activeDepositToReject = dep
                        rejectionReason = "Invalid Transaction ID or unverified"
                    },
                    onCopy = { text ->
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("TrxID", text))
                        viewModel.showNotice("Copied $text")
                    }
                )
                2 -> AdminOrdersTab(
                    orders = allOrders,
                    onStartProcessing = { order ->
                        viewModel.updateOrderStatus(order.id, OrderStatus.PROCESSING)
                    },
                    onCompletePrompt = { order ->
                        activeOrderToComplete = order
                        operatorTrxIdInput = if (order.serviceType == ServiceType.NID_SERVICE) {
                            "EC-SRV-" + (100000..999999).random().toString()
                        } else {
                            "OP-" + (100000..999999).random().toString()
                        }
                        orderCompleteNote = if (order.serviceType == ServiceType.NID_SERVICE) {
                            "Verified against Election Commission database. Official copy delivered."
                        } else {
                            "Recharge successful via operator gateway."
                        }
                        deliveryPdfFileNameInput = if (order.serviceType == ServiceType.NID_SERVICE) {
                            val cleanNid = order.nidNumber?.filter { it.isDigit() } ?: "19942691234567"
                            if (order.title.contains("Sign", ignoreCase = true)) {
                                "NID_Sign_Slip_${cleanNid}.pdf"
                            } else {
                                "NID_Server_Copy_${cleanNid}.pdf"
                            }
                        } else {
                            "Delivery_Slip_${order.id.takeLast(6).uppercase()}.pdf"
                        }
                        deliveryPdfSizeInput = "248 KB"
                    },
                    onRejectPrompt = { order ->
                        activeOrderToReject = order
                        orderRejectReason = "Operator failed to deliver package. Refund issued."
                    }
                )
                3 -> AdminUsersTab(
                    users = allUsers,
                    onAdjustBalancePrompt = { user ->
                        activeUserToAdjust = user
                        adjustmentAmount = "100"
                        isCreditAdjustment = true
                        adjustmentReason = "Admin bonus"
                    },
                    onToggleBlock = { user ->
                        viewModel.toggleUserBlock(user.id, user.isBlocked)
                    }
                )
                4 -> AdminProductsTab(
                    products = allProducts,
                    onToggleStock = { prod -> viewModel.toggleProductStock(prod) },
                    onDelete = { prod -> viewModel.deleteProduct(prod) },
                    onEditPrice = { prod ->
                        activeProductToEditPrice = prod
                        updatedPriceInput = prod.price.toString()
                    },
                    onAddNew = { showAddProductDialog = true }
                )
                5 -> AdminPaymentConfigTab(
                    configs = paymentMethods,
                    onSave = { updated -> viewModel.updatePaymentMethod(updated) }
                )
                6 -> AdminSupportSettingsTab(viewModel = viewModel)
            }
        }
    }

    // Modal: Reject Deposit
    activeDepositToReject?.let { dep ->
        AlertDialog(
            onDismissRequest = { activeDepositToReject = null },
            title = { Text("Reject Deposit Request", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Deposit ID: ${dep.id} • ৳${dep.amount}")
                    Text("Sender: ${dep.senderNumber} (${dep.method})")
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        label = { Text("Rejection Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rejectDeposit(dep.id, rejectionReason)
                        activeDepositToReject = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm Rejection")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeDepositToReject = null }) { Text("Cancel") }
            }
        )
    }

    // Modal: Complete Order (With PDF Document Delivery Attachment)
    activeOrderToComplete?.let { order ->
        AlertDialog(
            onDismissRequest = { activeOrderToComplete = null },
            title = { Text("Complete Order #${order.id.takeLast(6).uppercase()}", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("${order.title} (${order.operatorOrBrand}) • ৳${order.amount}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("Recipient / NID: ${order.recipientTarget}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)

                    OutlinedTextField(
                        value = operatorTrxIdInput,
                        onValueChange = { operatorTrxIdInput = it },
                        label = { Text("Operator TrxID / Ref Code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = orderCompleteNote,
                        onValueChange = { orderCompleteNote = it },
                        label = { Text("Fulfillment Note") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))

                    // PDF Delivery Attachment Section
                    Text("Delivery Document (PDF Upload):", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    Surface(
                        color = Color(0xFFFDE8E8),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFF8B4B4)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        tint = Color(0xFFE02424),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (deliveryPdfFileNameInput.isNotBlank()) "Attached PDF Document" else "No PDF Attached (Optional)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (deliveryPdfFileNameInput.isNotBlank()) Color(0xFF9B1C1C) else Color.Gray
                                    )
                                }

                                if (deliveryPdfFileNameInput.isNotBlank()) {
                                    TextButton(
                                        onClick = { deliveryPdfFileNameInput = "" },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Remove", fontSize = 11.sp, color = Color(0xFFE02424))
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = deliveryPdfFileNameInput,
                                onValueChange = { deliveryPdfFileNameInput = it },
                                label = { Text("PDF File Name") },
                                placeholder = { Text("e.g. NID_Server_Copy_1994...pdf") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_admin_pdf_filename")
                            )

                            // Quick Attach Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val nidDigits = order.nidNumber?.filter { it.isDigit() } ?: "19942691234567"
                                SuggestionChip(
                                    onClick = {
                                        deliveryPdfFileNameInput = "NID_Server_Copy_${nidDigits}.pdf"
                                        deliveryPdfSizeInput = "248 KB"
                                    },
                                    label = { Text("NID Server PDF", fontSize = 10.sp) }
                                )
                                SuggestionChip(
                                    onClick = {
                                        deliveryPdfFileNameInput = "NID_Sign_Slip_${nidDigits}.pdf"
                                        deliveryPdfSizeInput = "312 KB"
                                    },
                                    label = { Text("Sign Slip PDF", fontSize = 10.sp) }
                                )
                                SuggestionChip(
                                    onClick = {
                                        deliveryPdfFileNameInput = "Recharge_Slip_${order.id.takeLast(6).uppercase()}.pdf"
                                        deliveryPdfSizeInput = "175 KB"
                                    },
                                    label = { Text("Slip PDF", fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateOrderStatus(
                            orderId = order.id,
                            newStatus = OrderStatus.COMPLETED,
                            operatorTrxId = operatorTrxIdInput,
                            adminNote = orderCompleteNote,
                            deliveryPdfFileName = deliveryPdfFileNameInput.ifBlank { null },
                            deliveryPdfSize = deliveryPdfSizeInput.ifBlank { null },
                            deliveryPdfTitle = order.title
                        )
                        activeOrderToComplete = null
                    },
                    modifier = Modifier.testTag("btn_confirm_complete_order")
                ) {
                    Text(if (deliveryPdfFileNameInput.isNotBlank()) "Mark Completed & Deliver PDF" else "Mark Completed")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeOrderToComplete = null }) { Text("Cancel") }
            }
        )
    }

    // Modal: Reject Order (With Auto-Refund)
    activeOrderToReject?.let { order ->
        AlertDialog(
            onDismissRequest = { activeOrderToReject = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = StatusRejectedText)
                    Text("Reject Order & Refund Wallet", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = StatusRejectedBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "CRITICAL: Rejecting this order will IMMEDIATELY refund ৳${String.format("%.2f", order.amount)} back to user's (${order.userName}) wallet balance.",
                            color = StatusRejectedText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Text("Service: ${order.title} • Target: ${order.recipientTarget}")
                    OutlinedTextField(
                        value = orderRejectReason,
                        onValueChange = { orderRejectReason = it },
                        label = { Text("Rejection Reason for Customer") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateOrderStatus(order.id, OrderStatus.REJECTED, null, orderRejectReason)
                        activeOrderToReject = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRejectedText)
                ) {
                    Text("Reject & Refund ৳${String.format("%.0f", order.amount)}")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeOrderToReject = null }) { Text("Cancel") }
            }
        )
    }

    // Modal: Adjust User Balance
    activeUserToAdjust?.let { user ->
        AlertDialog(
            onDismissRequest = { activeUserToAdjust = null },
            title = { Text("Adjust Wallet Balance", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("User: ${user.name} (${user.phone})")
                    Text("Current Balance: ৳${String.format("%.2f", user.walletBalance)}", fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = isCreditAdjustment,
                            onClick = { isCreditAdjustment = true },
                            label = { Text("+ Credit (Add)", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !isCreditAdjustment,
                            onClick = { isCreditAdjustment = false },
                            label = { Text("- Debit (Deduct)", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = adjustmentAmount,
                        onValueChange = { adjustmentAmount = it },
                        label = { Text("Amount (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = adjustmentReason,
                        onValueChange = { adjustmentReason = it },
                        label = { Text("Reason / Reference") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = adjustmentAmount.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.adjustUserBalance(user.id, amt, isCreditAdjustment, adjustmentReason)
                            activeUserToAdjust = null
                        }
                    }
                ) {
                    Text("Apply Adjustment")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeUserToAdjust = null }) { Text("Cancel") }
            }
        )
    }

    // Modal: Edit Product Price
    activeProductToEditPrice?.let { product ->
        AlertDialog(
            onDismissRequest = { activeProductToEditPrice = null },
            title = { Text("Update Price: ${product.title}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Category: ${product.category.name} • ${product.operatorOrBrand}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    Text("Current Price: ৳${product.price}", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = updatedPriceInput,
                        onValueChange = { updatedPriceInput = it },
                        label = { Text("New Selling Price (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newPrice = updatedPriceInput.toDoubleOrNull()
                        if (newPrice != null && newPrice > 0) {
                            viewModel.saveProduct(
                                id = product.id,
                                title = product.title,
                                category = product.category,
                                operatorOrBrand = product.operatorOrBrand,
                                description = product.description,
                                price = newPrice,
                                regularPrice = product.regularPrice,
                                validity = product.validity,
                                badge = product.badge
                            )
                            activeProductToEditPrice = null
                        }
                    }
                ) {
                    Text("Update Price")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeProductToEditPrice = null }) { Text("Cancel") }
            }
        )
    }

    // Modal: Add New Product
    if (showAddProductDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newCategory by remember { mutableStateOf(ProductCategory.DRIVE_OFFER) }
        var newOperator by remember { mutableStateOf("Grameenphone") }
        var newDesc by remember { mutableStateOf("") }
        var newPrice by remember { mutableStateOf("450") }
        var newRegPrice by remember { mutableStateOf("500") }
        var newValidity by remember { mutableStateOf("30 Days") }
        var newBadge by remember { mutableStateOf("Hot Offer") }

        AlertDialog(
            onDismissRequest = { showAddProductDialog = false },
            title = { Text("Add Service / Package", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            label = { Text("Package Title") },
                            placeholder = { Text("e.g. NID Server Copy / GP 50GB") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Text("Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                ProductCategory.NID_SERVICES,
                                ProductCategory.DRIVE_OFFER,
                                ProductCategory.GAMING,
                                ProductCategory.STREAMING
                            ).forEach { cat ->
                                FilterChip(
                                    selected = newCategory == cat,
                                    onClick = {
                                        newCategory = cat
                                        if (cat == ProductCategory.NID_SERVICES) {
                                            newOperator = "Election Commission"
                                            newValidity = "Instant"
                                        }
                                    },
                                    label = { Text(cat.name.replace("_", " "), fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = newOperator,
                            onValueChange = { newOperator = it },
                            label = { Text("Operator or Brand") },
                            placeholder = { Text("e.g. Grameenphone / Free Fire") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newPrice,
                                onValueChange = { newPrice = it },
                                label = { Text("Selling Price (৳)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = newRegPrice,
                                onValueChange = { newRegPrice = it },
                                label = { Text("Regular Price (৳)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = newValidity,
                            onValueChange = { newValidity = it },
                            label = { Text("Validity") },
                            placeholder = { Text("e.g. 30 Days / Instant") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = newBadge,
                            onValueChange = { newBadge = it },
                            label = { Text("Badge (Optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = newDesc,
                            onValueChange = { newDesc = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = newPrice.toDoubleOrNull() ?: 0.0
                        val rp = newRegPrice.toDoubleOrNull() ?: p
                        if (newTitle.isNotBlank() && p > 0) {
                            viewModel.saveProduct(
                                title = newTitle,
                                category = newCategory,
                                operatorOrBrand = newOperator,
                                description = newDesc,
                                price = p,
                                regularPrice = rp,
                                validity = newValidity,
                                badge = newBadge
                            )
                            showAddProductDialog = false
                        }
                    }
                ) {
                    Text("Add Package")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProductDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ----------------- Sub-Tab Composables -----------------

@Composable
private fun AdminReportsTab(
    users: List<UserEntity>,
    deposits: List<DepositRequestEntity>,
    orders: List<OrderEntity>
) {
    val totalApprovedDepositsAmount = deposits.filter { it.status == DepositStatus.APPROVED }.sumOf { it.amount }
    val totalPendingDepositsAmount = deposits.filter { it.status == DepositStatus.PENDING }.sumOf { it.amount }
    val totalCompletedOrdersAmount = orders.filter { it.status == OrderStatus.COMPLETED }.sumOf { it.amount }
    val totalUserWalletsAmount = users.sumOf { it.walletBalance }
    val totalRefundedAmount = orders.filter { it.isRefunded }.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Business Performance & Overview", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        // Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Total Deposits",
                    value = "৳${String.format("%.0f", totalApprovedDepositsAmount)}",
                    subtitle = "${deposits.count { it.status == DepositStatus.APPROVED }} approved",
                    icon = Icons.Default.AccountBalanceWallet,
                    color = Color(0xFF0D9488),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Pending Deposits",
                    value = "৳${String.format("%.0f", totalPendingDepositsAmount)}",
                    subtitle = "${deposits.count { it.status == DepositStatus.PENDING }} awaiting",
                    icon = Icons.Default.HourglassTop,
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Completed Sales",
                    value = "৳${String.format("%.0f", totalCompletedOrdersAmount)}",
                    subtitle = "${orders.count { it.status == OrderStatus.COMPLETED }} fulfilled",
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Platform Wallets",
                    value = "৳${String.format("%.0f", totalUserWalletsAmount)}",
                    subtitle = "${users.size} registered users",
                    icon = Icons.Default.People,
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Order Status Distribution", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Pending Orders:", fontSize = 13.sp)
                        Text("${orders.count { it.status == OrderStatus.PENDING }} orders", fontWeight = FontWeight.Bold, color = StatusPendingText)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Processing Orders:", fontSize = 13.sp)
                        Text("${orders.count { it.status == OrderStatus.PROCESSING }} orders", fontWeight = FontWeight.Bold, color = StatusProcessingText)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Completed Orders:", fontSize = 13.sp)
                        Text("${orders.count { it.status == OrderStatus.COMPLETED }} orders", fontWeight = FontWeight.Bold, color = StatusApprovedText)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Rejected & Auto-Refunded:", fontSize = 13.sp)
                        Text("${orders.count { it.status == OrderStatus.REJECTED }} (৳${String.format("%.0f", totalRefundedAmount)})", fontWeight = FontWeight.Bold, color = StatusRejectedText)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Medium)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(color.copy(alpha = 0.15f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun AdminDepositsTab(
    deposits: List<DepositRequestEntity>,
    onApprove: (DepositRequestEntity) -> Unit,
    onRejectPrompt: (DepositRequestEntity) -> Unit,
    onCopy: (String) -> Unit
) {
    var statusFilter by remember { mutableStateOf<DepositStatus?>(DepositStatus.PENDING) }
    val filtered = deposits.filter { statusFilter == null || it.status == statusFilter }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = statusFilter == DepositStatus.PENDING,
                    onClick = { statusFilter = DepositStatus.PENDING },
                    label = { Text("Pending (${deposits.count { it.status == DepositStatus.PENDING }})") }
                )
                FilterChip(
                    selected = statusFilter == DepositStatus.APPROVED,
                    onClick = { statusFilter = DepositStatus.APPROVED },
                    label = { Text("Approved") }
                )
                FilterChip(
                    selected = statusFilter == null,
                    onClick = { statusFilter = null },
                    label = { Text("All (${deposits.size})") }
                )
            }
        }

        if (filtered.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(30.dp), contentAlignment = Alignment.Center) {
                        Text("No deposits matching filter", color = MaterialTheme.colorScheme.outline, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(filtered) { dep ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(dep.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Phone: ${dep.userPhone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                            Text(
                                "৳${String.format("%.2f", dep.amount)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Method: ${dep.method} • Sender: ${dep.senderNumber}",
                                fontSize = 12.sp
                            )
                            DepositStatusBadge(status = dep.status)
                        }

                        // TrxID with Copy Button
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("TrxID: ${dep.transactionId}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                IconButton(onClick = { onCopy(dep.transactionId) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        if (dep.userNote.isNotBlank()) {
                            Text("User Note: ${dep.userNote}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }

                        // Action Buttons if PENDING
                        if (dep.status == DepositStatus.PENDING) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { onApprove(dep) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Approve & Credit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { onRejectPrompt(dep) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reject", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminOrdersTab(
    orders: List<OrderEntity>,
    onStartProcessing: (OrderEntity) -> Unit,
    onCompletePrompt: (OrderEntity) -> Unit,
    onRejectPrompt: (OrderEntity) -> Unit
) {
    var statusFilter by remember { mutableStateOf<OrderStatus?>(null) }
    val filtered = orders.filter { statusFilter == null || it.status == statusFilter }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = statusFilter == null,
                    onClick = { statusFilter = null },
                    label = { Text("All (${orders.size})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = statusFilter == OrderStatus.PENDING,
                    onClick = { statusFilter = OrderStatus.PENDING },
                    label = { Text("Pending (${orders.count { it.status == OrderStatus.PENDING }})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = statusFilter == OrderStatus.PROCESSING,
                    onClick = { statusFilter = OrderStatus.PROCESSING },
                    label = { Text("Processing", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = statusFilter == OrderStatus.COMPLETED,
                    onClick = { statusFilter = OrderStatus.COMPLETED },
                    label = { Text("Completed", fontSize = 11.sp) }
                )
            }
        }

        if (filtered.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(30.dp), contentAlignment = Alignment.Center) {
                        Text("No orders matching filter", color = MaterialTheme.colorScheme.outline, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(filtered) { order ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#${order.id.takeLast(6).uppercase()} • ${order.operatorOrBrand}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            OrderStatusBadge(status = order.status, isRefunded = order.isRefunded)
                        }

                        Text(order.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Customer: ${order.userName} (${order.userPhone})", fontSize = 12.sp)
                                Text("Target: ${order.recipientTarget} (${order.rechargeType})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Text("৳${String.format("%.2f", order.amount)}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        }

                        // If NID application order, show details for admin review
                        if (order.nidNumber != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("NID APPLICATION DATA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "EC Verification",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Text("Applicant: ${order.applicantName ?: "N/A"}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("NID: ${order.nidNumber} • DOB: ${order.dateOfBirth ?: "N/A"}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)

                                    // Location Fields clearly highlighted
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text("LOCATION DETAILS:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Division: ${order.division ?: "N/A"}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                                Text("District: ${order.district ?: "N/A"}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                            Text("Upazila / Thana: ${order.upazila ?: "N/A"}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }

                                    // Document status (Optional)
                                    if (order.documentFileName != null) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.secondary)
                                            Text("Document Attached: ${order.documentFileName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Text("Document: None attached (Optional)", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                    }

                                    order.additionalNotes?.let { note ->
                                        Text("User Note: $note", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                            }
                        }

                        // Order Actions based on status
                        when (order.status) {
                            OrderStatus.PENDING -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onStartProcessing(order) },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Process", fontSize = 12.sp)
                                    }
                                    Button(
                                        onClick = { onCompletePrompt(order) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Complete", fontSize = 12.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { onRejectPrompt(order) },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Reject", fontSize = 12.sp)
                                    }
                                }
                            }
                            OrderStatus.PROCESSING -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onCompletePrompt(order) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Complete Order", fontSize = 12.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { onRejectPrompt(order) },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Reject & Refund", fontSize = 12.sp)
                                    }
                                }
                            }
                            OrderStatus.COMPLETED -> {
                                order.operatorTrxId?.let {
                                    Text("Fulfilled TrxID: $it", fontSize = 11.sp, color = Color(0xFF065F46), fontWeight = FontWeight.SemiBold)
                                }
                            }
                            OrderStatus.REJECTED -> {
                                Text("Refunded ৳${order.amount} to user wallet.", fontSize = 11.sp, color = StatusRejectedText, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminUsersTab(
    users: List<UserEntity>,
    onAdjustBalancePrompt: (UserEntity) -> Unit,
    onToggleBlock: (UserEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(users) { user ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            if (user.role == UserRole.ADMIN) {
                                Surface(color = MaterialTheme.colorScheme.tertiary, shape = RoundedCornerShape(6.dp)) {
                                    Text("ADMIN", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                            if (user.isBlocked) {
                                Surface(color = StatusRejectedBg, shape = RoundedCornerShape(6.dp)) {
                                    Text("BLOCKED", color = StatusRejectedText, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                        }
                        Text("${user.email} • ${user.phone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Text(
                            "Wallet: ৳${String.format("%.2f", user.walletBalance)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledTonalButton(
                            onClick = { onAdjustBalancePrompt(user) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("± Balance", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        if (user.role != UserRole.ADMIN) {
                            OutlinedButton(
                                onClick = { onToggleBlock(user) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(if (user.isBlocked) "Unblock" else "Block", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminProductsTab(
    products: List<ProductEntity>,
    onToggleStock: (ProductEntity) -> Unit,
    onDelete: (ProductEntity) -> Unit,
    onEditPrice: (ProductEntity) -> Unit,
    onAddNew: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Service Catalog (${products.size} items)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Button(
                onClick = onAddNew,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Package", fontSize = 12.sp)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(products) { prod ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(prod.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${prod.operatorOrBrand} • ${prod.category.name} • ${prod.validity}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text("Price: ৳${prod.price} (Reg: ৳${prod.regularPrice})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilledTonalButton(
                                onClick = { onEditPrice(prod) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("৳ Edit", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            FilterChip(
                                selected = prod.inStock,
                                onClick = { onToggleStock(prod) },
                                label = { Text(if (prod.inStock) "In Stock" else "Out", fontSize = 10.sp) }
                            )
                            IconButton(onClick = { onDelete(prod) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminPaymentConfigTab(
    configs: List<PaymentMethodConfigEntity>,
    onSave: (PaymentMethodConfigEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Deposit Gateways & Numbers", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text("Set your receiving account numbers for bKash, Nagad, Rocket, Upay", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
        }

        items(configs) { item ->
            var number by remember { mutableStateOf(item.accountNumber) }
            var minDeposit by remember { mutableStateOf(item.minDeposit.toString()) }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    OutlinedTextField(
                        value = number,
                        onValueChange = { number = it },
                        label = { Text("Receiver Account Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = minDeposit,
                        onValueChange = { minDeposit = it },
                        label = { Text("Minimum Deposit (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            val min = minDeposit.toDoubleOrNull() ?: item.minDeposit
                            onSave(item.copy(accountNumber = number, minDeposit = min))
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save Gateway Details")
                    }
                }
            }
        }
    }
}
