package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductCategory
import com.example.data.model.ProductEntity
import com.example.data.model.ServiceType
import com.example.data.model.UserRole
import androidx.compose.runtime.*
import com.example.ui.components.NidApplicationDialog
import com.example.ui.components.OrderStatusBadge
import com.example.ui.components.WalletCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.OrderDraft

@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isAdminMode by viewModel.isAdminMode.collectAsState()
    val orders by viewModel.userOrders.collectAsState()
    val products by viewModel.allProducts.collectAsState()
    val pendingDepositCount by viewModel.pendingDepositCount.collectAsState()
    val pendingOrderCount by viewModel.pendingOrderCount.collectAsState()

    var selectedNidProductForApplication by remember { mutableStateOf<ProductEntity?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Top Header with User Greeting and Mode Switch
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (currentUser?.name?.firstOrNull() ?: 'U').toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column {
                        Text(
                            text = "Hello, ${currentUser?.name ?: "User"}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(StatusApprovedText)
                            )
                            Text(
                                text = if (currentUser?.role == UserRole.ADMIN) "Admin Account" else "Verified Customer",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                // Quick Admin Mode Switcher Button
                Surface(
                    onClick = { viewModel.toggleAdminMode() },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isAdminMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.testTag("admin_switch_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isAdminMode) Icons.Default.AdminPanelSettings else Icons.Default.SwapHoriz,
                            contentDescription = "Switch Mode",
                            tint = if (isAdminMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isAdminMode) "Admin View" else "Admin Panel",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAdminMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 2. Interactive Wallet Card
        item {
            WalletCard(
                user = currentUser,
                onDepositClick = { viewModel.navigateTo("wallet") },
                onRechargeClick = { viewModel.navigateTo("recharge") },
                onHistoryClick = { viewModel.navigateTo("orders") }
            )
        }

        // 3. Quick Action Services Grid
        item {
            Text(
                text = "Services & Top-Up",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            val serviceItems = listOf(
                ServiceGridItem("Recharge", "Talktime", Icons.Default.PhoneAndroid, Color(0xFF0284C7)) {
                    viewModel.navigateTo("recharge")
                },
                ServiceGridItem("Drive Packs", "Big Discount", Icons.Default.Bolt, Color(0xFFE11D48)) {
                    viewModel.navigateTo("recharge")
                },
                ServiceGridItem("Gaming", "Diamonds & UC", Icons.Default.SportsEsports, Color(0xFF7C3AED)) {
                    viewModel.navigateTo("digital")
                },
                ServiceGridItem("Subscriptions", "Netflix & OTT", Icons.Default.Subscriptions, Color(0xFF0D9488)) {
                    viewModel.navigateTo("digital")
                },
                ServiceGridItem("Gift Cards", "GPlay & Apple", Icons.Default.CardGiftcard, Color(0xFFD97706)) {
                    viewModel.navigateTo("digital")
                },
                ServiceGridItem("Add Money", "Auto Deposit", Icons.Default.AccountBalanceWallet, Color(0xFF059669)) {
                    viewModel.navigateTo("wallet")
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                serviceItems.take(3).forEach { item ->
                    ServiceItemCard(item = item, modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                serviceItems.drop(3).forEach { item ->
                    ServiceItemCard(item = item, modifier = Modifier.weight(1f))
                }
            }
        }

        // 4. Important Announcement / Status Card
        item {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Instant Drive Packs & Auto-Refund",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Orders processed in 2-5 mins. If any order is rejected, payment is automatically refunded back to your wallet!",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // 4.5 NEW: NID & Election Services Section
        item {
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
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0D9488).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = Color(0xFF0D9488),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "NID & Election Services",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Official National ID verification & correction",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Surface(
                    color = Color(0xFFD1FAE5),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Official",
                        color = Color(0xFF065F46),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Fetch NID products from DB or provide defaults
            val nidDbProducts = products.filter { it.category == ProductCategory.NID_SERVICES }
            val nidItems = if (nidDbProducts.isNotEmpty()) {
                nidDbProducts
            } else {
                listOf(
                    ProductEntity(
                        id = "prod_nid_server_01",
                        title = "NID Server Copy",
                        category = ProductCategory.NID_SERVICES,
                        operatorOrBrand = "Election Commission",
                        description = "Instant official online NID server copy with QR code.",
                        price = 60.0,
                        regularPrice = 80.0,
                        validity = "Instant",
                        badge = "Fastest"
                    ),
                    ProductEntity(
                        id = "prod_nid_sign_01",
                        title = "NID Sign Copy",
                        category = ProductCategory.NID_SERVICES,
                        operatorOrBrand = "Election Commission",
                        description = "Full colored NID copy with original signature & biometrics.",
                        price = 120.0,
                        regularPrice = 150.0,
                        validity = "2-4 Hours",
                        badge = "Biometric"
                    ),
                    ProductEntity(
                        id = "prod_nid_correct_01",
                        title = "NID Correction",
                        category = ProductCategory.NID_SERVICES,
                        operatorOrBrand = "Election Commission",
                        description = "Application for Name, DOB, Parents' names, or Address change.",
                        price = 250.0,
                        regularPrice = 300.0,
                        validity = "1-2 Days",
                        badge = "Update"
                    )
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                nidItems.forEach { nidItem ->
                    NidServiceCard(
                        service = nidItem,
                        onApplyClick = {
                            selectedNidProductForApplication = nidItem
                        }
                    )
                }
            }
        }

        // 5. Featured Hot Drive Offers
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hot Drive Packs",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { viewModel.navigateTo("recharge") }) {
                    Text("View All", fontSize = 13.sp)
                }
            }

            val drivePacks = products.filter { it.category == ProductCategory.DRIVE_OFFER }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(drivePacks) { pack ->
                    DrivePackItem(
                        pack = pack,
                        onBuyClick = {
                            viewModel.prepareOrderDraft(
                                OrderDraft(
                                    serviceType = ServiceType.DRIVE_PACK,
                                    title = pack.title,
                                    operatorOrBrand = pack.operatorOrBrand,
                                    recipientTarget = currentUser?.phone ?: "017XXXXXXXX",
                                    rechargeType = "PREPAID",
                                    amount = pack.price
                                )
                            )
                        }
                    )
                }
            }
        }

        // 6. Recent Orders Snapshot
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Orders",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { viewModel.navigateTo("orders") }) {
                    Text("Order History", fontSize = 13.sp)
                }
            }

            if (orders.isEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "No orders yet",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        OutlinedButton(
                            onClick = { viewModel.navigateTo("recharge") },
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text("Recharge Now", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    orders.take(3).forEach { order ->
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(14.dp),
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
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = order.title,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${order.operatorOrBrand} • ${order.recipientTarget}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "৳${String.format("%.2f", order.amount)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OrderStatusBadge(status = order.status, isRefunded = order.isRefunded)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Support & WhatsApp Help Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo("support") }
                    .testTag("home_support_card")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF25D366)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Support",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "24/7 Support Desk & WhatsApp",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Surface(
                                color = StatusApprovedBg,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Online",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusApprovedText,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Live WhatsApp chat, hotline call, & instant answers in our FAQ knowledge base.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline,
                            lineHeight = 15.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Go",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
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

private data class ServiceGridItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
private fun ServiceItemCard(item: ServiceGridItem, modifier: Modifier = Modifier) {
    Surface(
        onClick = item.onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = CardDefaults.outlinedCardBorder(),
        shadowElevation = 1.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(item.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = item.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = item.subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DrivePackItem(
    pack: ProductEntity,
    onBuyClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.width(220.dp)
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
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = pack.operatorOrBrand,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                pack.badge?.let { badge ->
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = pack.title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Validity: ${pack.validity}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "৳${String.format("%.0f", pack.price)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (pack.regularPrice > pack.price) {
                        Text(
                            text = "৳${String.format("%.0f", pack.regularPrice)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Button(
                    onClick = onBuyClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Buy Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun NidServiceCard(
    service: ProductEntity,
    onApplyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = CardDefaults.outlinedCardBorder(),
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                service.title.contains("Server", ignoreCase = true) -> Color(0xFF0D9488).copy(alpha = 0.15f)
                                service.title.contains("Sign", ignoreCase = true) -> Color(0xFF2563EB).copy(alpha = 0.15f)
                                else -> Color(0xFFE11D48).copy(alpha = 0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            service.title.contains("Server", ignoreCase = true) -> Icons.Default.QrCodeScanner
                            service.title.contains("Sign", ignoreCase = true) -> Icons.Default.Fingerprint
                            else -> Icons.Default.Description
                        },
                        contentDescription = null,
                        tint = when {
                            service.title.contains("Server", ignoreCase = true) -> Color(0xFF0D9488)
                            service.title.contains("Sign", ignoreCase = true) -> Color(0xFF2563EB)
                            else -> Color(0xFFE11D48)
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = service.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        service.badge?.let { b ->
                            Surface(
                                color = Color(0xFFEDE9FE),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = b,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7C3AED),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = service.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 2,
                        lineHeight = 15.sp
                    )
                    Text(
                        text = "Delivery: ${service.validity} • Wallet Deduct",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "৳${String.format("%.0f", service.price)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Button(
                    onClick = onApplyClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Apply", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

