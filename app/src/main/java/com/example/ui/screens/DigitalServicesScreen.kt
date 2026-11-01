package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductCategory
import com.example.data.model.ProductEntity
import com.example.data.model.ServiceType
import com.example.ui.components.NidApplicationDialog
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.OrderDraft

@Composable
fun DigitalServicesScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val products by viewModel.allProducts.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<ProductCategory?>(null) }

    // Dialog state for item purchase prompt
    var activeProductToBuy by remember { mutableStateOf<ProductEntity?>(null) }
    var selectedNidProductForApplication by remember { mutableStateOf<ProductEntity?>(null) }
    var recipientInput by remember { mutableStateOf(currentUser?.email ?: "") }

    val digitalProducts = products.filter {
        it.category in listOf(
            ProductCategory.GAMING,
            ProductCategory.STREAMING,
            ProductCategory.GIFT_CARD,
            ProductCategory.NID_SERVICES
        )
    }.filter {
        selectedCategoryFilter == null || it.category == selectedCategoryFilter
    }.filter {
        searchQuery.isEmpty() ||
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.operatorOrBrand.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("digital_services_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Text(
                text = "Digital Services & Gaming",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Buy gaming diamonds, OTT subscriptions, and gift cards with wallet",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Free Fire, Netflix, PUBG, Google Play...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategoryFilter == null,
                    onClick = { selectedCategoryFilter = null },
                    label = { Text("All", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedCategoryFilter == ProductCategory.GAMING,
                    onClick = { selectedCategoryFilter = ProductCategory.GAMING },
                    label = { Text("Gaming Top-Up", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedCategoryFilter == ProductCategory.STREAMING,
                    onClick = { selectedCategoryFilter = ProductCategory.STREAMING },
                    label = { Text("Streaming & OTT", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedCategoryFilter == ProductCategory.GIFT_CARD,
                    onClick = { selectedCategoryFilter = ProductCategory.GIFT_CARD },
                    label = { Text("Gift Cards", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedCategoryFilter == ProductCategory.NID_SERVICES,
                    onClick = { selectedCategoryFilter = ProductCategory.NID_SERVICES },
                    label = { Text("NID & EC", fontSize = 11.sp) }
                )
            }
        }

        // Product Cards
        if (digitalProducts.isEmpty()) {
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
                        Text(
                            text = "No digital services matching your search.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            items(digitalProducts) { item ->
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
                                Surface(
                                    color = when (item.category) {
                                        ProductCategory.GAMING -> Color(0xFFEDE9FE)
                                        ProductCategory.STREAMING -> Color(0xFFCCFBF1)
                                        else -> Color(0xFFFEF3C7)
                                    },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = item.operatorOrBrand,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (item.category) {
                                            ProductCategory.GAMING -> Color(0xFF6D28D9)
                                            ProductCategory.STREAMING -> Color(0xFF0F766E)
                                            else -> Color(0xFFB45309)
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                item.badge?.let { badge ->
                                    Surface(
                                        color = Color(0xFFFEE2E2),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = badge,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF991B1B),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = item.description,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline,
                                maxLines = 2
                            )
                            Text(
                                text = "Delivery: Instant/Manual • ${item.validity}",
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
                                text = "৳${String.format("%.0f", item.price)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (item.regularPrice > item.price) {
                                Text(
                                    text = "৳${String.format("%.0f", item.regularPrice)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Button(
                                onClick = {
                                    activeProductToBuy = item
                                    recipientInput = if (item.category == ProductCategory.GAMING) "UID: " else currentUser?.email ?: ""
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("Buy", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal to collect Player UID or Email before order confirmation
    activeProductToBuy?.let { product ->
        val promptLabel = when (product.category) {
            ProductCategory.GAMING -> "Enter Player UID / Character ID:"
            ProductCategory.STREAMING -> "Enter Delivery Email / Gmail:"
            ProductCategory.GIFT_CARD -> "Enter Delivery Email or WhatsApp:"
            else -> "Enter Recipient Contact / Target:"
        }

        AlertDialog(
            onDismissRequest = { activeProductToBuy = null },
            title = {
                Text(text = "Purchase ${product.title}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = promptLabel, fontSize = 13.sp)
                    OutlinedTextField(
                        value = recipientInput,
                        onValueChange = { recipientInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = {
                            Text(if (product.category == ProductCategory.GAMING) "e.g. 984729104" else "user@gmail.com")
                        }
                    )
                    Text(
                        text = "Price: ৳${String.format("%.2f", product.price)} (Deducted from wallet)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (recipientInput.trim().isEmpty()) {
                            viewModel.showNotice("Please provide recipient information")
                            return@Button
                        }
                        val chosenProduct = activeProductToBuy ?: return@Button
                        activeProductToBuy = null
                        viewModel.prepareOrderDraft(
                            OrderDraft(
                                serviceType = when (chosenProduct.category) {
                                    ProductCategory.GAMING -> ServiceType.GAMING_TOPUP
                                    ProductCategory.GIFT_CARD -> ServiceType.GIFT_CARD
                                    else -> ServiceType.DIGITAL_SERVICE
                                },
                                title = chosenProduct.title,
                                operatorOrBrand = chosenProduct.operatorOrBrand,
                                recipientTarget = recipientInput.trim(),
                                rechargeType = "DIGITAL",
                                amount = chosenProduct.price
                            )
                        )
                    }
                ) {
                    Text("Proceed to Checkout")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeProductToBuy = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    selectedNidProductForApplication?.let { nidProduct ->
        NidApplicationDialog(
            product = nidProduct,
            user = currentUser,
            onDismiss = { selectedNidProductForApplication = null },
            onSubmitDraft = { draft ->
                selectedNidProductForApplication = null
                viewModel.prepareOrderDraft(draft)
            },
            onNavigateDeposit = {
                selectedNidProductForApplication = null
                viewModel.navigateTo("wallet")
            }
        )
    }
}
