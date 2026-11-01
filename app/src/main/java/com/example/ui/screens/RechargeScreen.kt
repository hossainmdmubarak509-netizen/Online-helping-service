package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductCategory
import com.example.data.model.ProductEntity
import com.example.data.model.ServiceType
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.OrderDraft

@Composable
fun RechargeScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val products by viewModel.allProducts.collectAsState()

    var selectedOperator by remember { mutableStateOf("Grameenphone") }
    var mobileNumber by remember { mutableStateOf(currentUser?.phone ?: "") }
    var connectionType by remember { mutableStateOf("PREPAID") }
    var selectedCategoryTab by remember { mutableStateOf(0) } // 0: Quick Amount, 1: Internet, 2: Minutes, 3: Drive Packs
    var customAmount by remember { mutableStateOf("50") }

    val operators = listOf(
        OperatorData("Grameenphone", "GP", ColorGP),
        OperatorData("Banglalink", "BL", ColorBanglalink),
        OperatorData("Robi", "Robi", ColorRobi),
        OperatorData("Airtel", "Airtel", ColorAirtel),
        OperatorData("Teletalk", "TT", ColorTeletalk),
        OperatorData("Skitto", "Skitto", ColorSkitto)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("recharge_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Text(
                text = "Mobile Recharge & Flexiload",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Recharge any operator SIM with instant delivery from wallet",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }

        // 1. Operator Selection
        item {
            Text(
                text = "Select Operator",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                operators.take(3).forEach { op ->
                    OperatorCard(
                        operator = op,
                        isSelected = selectedOperator == op.name,
                        onClick = { selectedOperator = op.name },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                operators.drop(3).forEach { op ->
                    OperatorCard(
                        operator = op,
                        isSelected = selectedOperator == op.name,
                        onClick = { selectedOperator = op.name },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 2. Mobile Number Input
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Recipient Mobile Number", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { mobileNumber = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recharge_number_input"),
                        placeholder = { Text("01XXXXXXXXX") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            if (mobileNumber.isNotEmpty()) {
                                IconButton(onClick = { mobileNumber = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Quick test numbers
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Quick:", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        listOf("01712345678", "01911223344", "01855667788").forEach { num ->
                            SuggestionChip(
                                onClick = { mobileNumber = num },
                                label = { Text(num.takeLast(6), fontSize = 10.sp) }
                            )
                        }
                    }

                    // Connection Type (Prepaid / Postpaid / Skitto)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("PREPAID", "POSTPAID", "SKITTO").forEach { type ->
                            FilterChip(
                                selected = connectionType == type,
                                onClick = { connectionType = type },
                                label = { Text(type, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 3. Category Tabs: Quick Amount, Internet, Minutes, Drive Packs
        item {
            val tabTitles = listOf("Amount", "Internet", "Minutes", "Drive Offers")
            TabRow(
                selectedTabIndex = selectedCategoryTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.testTag("recharge_tabs")
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedCategoryTab == index,
                        onClick = { selectedCategoryTab = index },
                        text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }
        }

        // 4. Tab Contents
        when (selectedCategoryTab) {
            0 -> {
                // Quick Amount chips + Custom amount input
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Select Quick Amount", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        val quickAmounts = listOf(20, 50, 100, 200, 300, 500, 1000)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            quickAmounts.take(4).forEach { amt ->
                                OutlinedButton(
                                    onClick = { customAmount = amt.toString() },
                                    border = BorderStroke(
                                        width = if (customAmount == amt.toString()) 2.dp else 1.dp,
                                        color = if (customAmount == amt.toString()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("৳$amt", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            quickAmounts.drop(4).forEach { amt ->
                                OutlinedButton(
                                    onClick = { customAmount = amt.toString() },
                                    border = BorderStroke(
                                        width = if (customAmount == amt.toString()) 2.dp else 1.dp,
                                        color = if (customAmount == amt.toString()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("৳$amt", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = customAmount,
                            onValueChange = { customAmount = it },
                            label = { Text("Recharge Amount (৳)") },
                            leadingIcon = { Text("৳", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(start = 12.dp)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("recharge_custom_amount"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = {
                                val amt = customAmount.toDoubleOrNull() ?: 0.0
                                if (mobileNumber.length < 10) {
                                    viewModel.showNotice("Please enter a valid recipient mobile number")
                                    return@Button
                                }
                                if (amt <= 0) {
                                    viewModel.showNotice("Please enter a valid amount")
                                    return@Button
                                }
                                viewModel.prepareOrderDraft(
                                    OrderDraft(
                                        serviceType = ServiceType.MOBILE_RECHARGE,
                                        title = "$selectedOperator ৳$amt Flexiload",
                                        operatorOrBrand = selectedOperator,
                                        recipientTarget = mobileNumber,
                                        rechargeType = connectionType,
                                        amount = amt
                                    )
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("proceed_recharge_button")
                        ) {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Recharge ৳$customAmount Now", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }

            1, 2, 3 -> {
                // Internet, Minutes, or Drive Packs
                val targetCategory = when (selectedCategoryTab) {
                    1 -> ProductCategory.INTERNET
                    2 -> ProductCategory.MINUTES
                    else -> ProductCategory.DRIVE_OFFER
                }

                val filteredPacks = products.filter {
                    it.category == targetCategory &&
                            (it.operatorOrBrand.equals(selectedOperator, ignoreCase = true) || it.operatorOrBrand.contains(selectedOperator, ignoreCase = true) || selectedOperator.contains(it.operatorOrBrand, ignoreCase = true))
                }

                if (filteredPacks.isEmpty()) {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No packages found for $selectedOperator in this category.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                } else {
                    items(filteredPacks) { pack ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = pack.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        pack.badge?.let { b ->
                                            Surface(
                                                color = Color(0xFFFEE2E2),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = b,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF991B1B),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = pack.description,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline,
                                        maxLines = 2
                                    )
                                    Text(
                                        text = "Validity: ${pack.validity} • ${pack.operatorOrBrand}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "৳${String.format("%.0f", pack.price)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Button(
                                        onClick = {
                                            if (mobileNumber.length < 10) {
                                                viewModel.showNotice("Please enter a valid recipient mobile number")
                                                return@Button
                                            }
                                            viewModel.prepareOrderDraft(
                                                OrderDraft(
                                                    serviceType = if (selectedCategoryTab == 3) ServiceType.DRIVE_PACK else ServiceType.MOBILE_RECHARGE,
                                                    title = pack.title,
                                                    operatorOrBrand = pack.operatorOrBrand,
                                                    recipientTarget = mobileNumber,
                                                    rechargeType = connectionType,
                                                    amount = pack.price
                                                )
                                            )
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Recharge", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class OperatorData(val name: String, val shortName: String, val color: Color)

@Composable
private fun OperatorCard(
    operator: OperatorData,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) operator.color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) operator.color else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(operator.color, shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = operator.shortName.take(2),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
            Text(
                text = operator.shortName,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) operator.color else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
