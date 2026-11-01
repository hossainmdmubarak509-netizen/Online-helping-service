package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DepositRequestEntity
import com.example.data.model.PaymentMethodConfigEntity
import com.example.data.model.TransactionType
import com.example.data.model.WalletTransactionEntity
import com.example.ui.components.DepositStatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WalletScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val paymentMethods by viewModel.paymentMethods.collectAsState()
    val userDeposits by viewModel.userDeposits.collectAsState()
    val userTransactions by viewModel.userTransactions.collectAsState()

    var activeSubTab by remember { mutableStateOf(0) } // 0: Add Money (Deposit Form), 1: Deposit History, 2: Transactions Ledger

    // Deposit Form State
    var selectedMethod by remember { mutableStateOf<PaymentMethodConfigEntity?>(null) }
    var senderNumber by remember { mutableStateOf(currentUser?.phone ?: "") }
    var depositAmount by remember { mutableStateOf("500") }
    var transactionId by remember { mutableStateOf("") }
    var userNote by remember { mutableStateOf("") }

    // Initialize selectedMethod if null
    LaunchedEffect(paymentMethods) {
        if (selectedMethod == null && paymentMethods.isNotEmpty()) {
            selectedMethod = paymentMethods.first()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("wallet_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Balance Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Current Wallet Balance",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "৳",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = String.format("%.2f", currentUser?.walletBalance ?: 0.0),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = "Instant deduction on recharge • 100% refund on rejected orders",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        // Sub Tabs: Add Money / Deposit Requests / Ledger
        item {
            val tabs = listOf("Deposit Money", "Deposit Requests", "Ledger")
            TabRow(
                selectedTabIndex = activeSubTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.testTag("wallet_tabs")
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = activeSubTab == index,
                        onClick = { activeSubTab = index },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                if (index == 1 && userDeposits.any { it.status == com.example.data.model.DepositStatus.PENDING }) {
                                    Badge { Text("!", fontSize = 9.sp) }
                                }
                            }
                        }
                    )
                }
            }
        }

        // Tab Contents
        when (activeSubTab) {
            0 -> {
                // Deposit Form
                item {
                    Text("Select Payment Gateway", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        paymentMethods.take(3).forEach { method ->
                            FilterChip(
                                selected = selectedMethod?.id == method.id,
                                onClick = { selectedMethod = method },
                                label = { Text(method.name, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    if (paymentMethods.size > 3) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            paymentMethods.drop(3).forEach { method ->
                                FilterChip(
                                    selected = selectedMethod?.id == method.id,
                                    onClick = { selectedMethod = method },
                                    label = { Text(method.name, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Instructions Card for selected method
                item {
                    val method = selectedMethod
                    if (method != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(16.dp),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${method.name} ${method.accountType}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = method.accountNumber,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 17.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Account Number", method.accountNumber)
                                            clipboard.setPrimaryClip(clip)
                                            viewModel.showNotice("Copied ${method.accountNumber} to clipboard")
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copy", fontSize = 11.sp)
                                    }
                                }

                                HorizontalDivider()
                                Text(
                                    text = method.instructions,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline,
                                    lineHeight = 16.sp
                                )
                                Text(
                                    text = "Min Deposit: ৳${String.format("%.0f", method.minDeposit)} • Max: ৳${String.format("%.0f", method.maxDeposit)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Deposit Form Inputs
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Submit Deposit Verification", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                            // Quick Amount chips
                            Text("Deposit Amount (৳):", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(100, 300, 500, 1000, 2000).forEach { amt ->
                                    SuggestionChip(
                                        onClick = { depositAmount = amt.toString() },
                                        label = { Text("৳$amt", fontSize = 11.sp) }
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = depositAmount,
                                onValueChange = { depositAmount = it },
                                label = { Text("Amount (৳)") },
                                leadingIcon = { Text("৳", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(start = 12.dp)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("deposit_amount_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = senderNumber,
                                onValueChange = { senderNumber = it },
                                label = { Text("Sender Phone / Account Number") },
                                placeholder = { Text("e.g. 01712345678") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("deposit_sender_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Column {
                                OutlinedTextField(
                                    value = transactionId,
                                    onValueChange = { transactionId = it },
                                    label = { Text("Transaction ID (TrxID)") },
                                    placeholder = { Text("e.g. BK89X0021A") },
                                    leadingIcon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                                    trailingIcon = {
                                        TextButton(onClick = {
                                            // Generate random TrxID for rapid testing
                                            val randomTrx = "TRX" + (10000000..99999999).random().toString()
                                            transactionId = randomTrx
                                            viewModel.showNotice("Filled sample TrxID: $randomTrx")
                                        }) {
                                            Text("Sample TrxID", fontSize = 10.sp)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("deposit_trxid_input"),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Text(
                                    text = "Enter the 8-10 digit Transaction ID from your SMS or app.",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                )
                            }

                            OutlinedTextField(
                                value = userNote,
                                onValueChange = { userNote = it },
                                label = { Text("Optional Note") },
                                placeholder = { Text("e.g. Urgent approval requested") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = {
                                    val amt = depositAmount.toDoubleOrNull() ?: 0.0
                                    val method = selectedMethod?.name ?: "bKash"
                                    if (amt <= 0) {
                                        viewModel.showNotice("Please enter a valid amount")
                                        return@Button
                                    }
                                    if (senderNumber.trim().isEmpty() || transactionId.trim().isEmpty()) {
                                        viewModel.showNotice("Sender number and Transaction ID are required")
                                        return@Button
                                    }
                                    viewModel.submitDeposit(method, senderNumber, transactionId, amt, userNote)
                                    transactionId = ""
                                    userNote = ""
                                    activeSubTab = 1 // Switch to requests history
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("submit_deposit_button")
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Submit Deposit Request", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            1 -> {
                // Deposit Requests History
                if (userDeposits.isEmpty()) {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                                Text("No deposit requests yet", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                } else {
                    items(userDeposits) { deposit ->
                        DepositRequestCard(deposit = deposit)
                    }
                }
            }

            2 -> {
                // Transactions Ledger
                if (userTransactions.isEmpty()) {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("No wallet transactions found", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                } else {
                    items(userTransactions) { tx ->
                        TransactionItemCard(tx = tx)
                    }
                }
            }
        }
    }
}

@Composable
private fun DepositRequestCard(deposit: DepositRequestEntity) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = deposit.method,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = deposit.transactionId,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "৳${String.format("%.2f", deposit.amount)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sender: ${deposit.senderNumber} • ${dateFormat.format(Date(deposit.createdAt))}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                DepositStatusBadge(status = deposit.status)
            }

            deposit.adminFeedback?.let { feedback ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Admin Note: $feedback",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionItemCard(tx: WalletTransactionEntity) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val isCredit = tx.type in listOf(TransactionType.DEPOSIT_APPROVED, TransactionType.ORDER_REFUND, TransactionType.ADMIN_CREDIT)

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = if (isCredit) StatusApprovedBg else StatusRejectedBg,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (isCredit) StatusApprovedText else StatusRejectedText,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = tx.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = dateFormat.format(Date(tx.timestamp)),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isCredit) "+" else "-"}৳${String.format("%.2f", tx.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isCredit) StatusApprovedText else StatusRejectedText
                )
                Text(
                    text = "Bal: ৳${String.format("%.2f", tx.balanceAfter)}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
