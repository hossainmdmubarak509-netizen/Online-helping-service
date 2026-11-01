package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.model.UserRole
import com.example.data.repository.RechargeRepository
import com.example.ui.components.OrderConfirmationDialog
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.AppViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = RechargeRepository(database.rechargeDao())
        val factory = AppViewModelFactory(repository)

        setContent {
            MyApplicationTheme {
                val viewModel: AppViewModel = viewModel(factory = factory)
                RechargeHubApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RechargeHubApp(viewModel: AppViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isAdminMode by viewModel.isAdminMode.collectAsState()
    val activeScreen by viewModel.activeScreen.collectAsState()
    val orderDraft by viewModel.orderDraft.collectAsState()
    val pendingDepositCount by viewModel.pendingDepositCount.collectAsState()
    val pendingOrderCount by viewModel.pendingOrderCount.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
        }
    }

    // Handle Android system back button: return to home screen
    BackHandler(enabled = activeScreen != "home" && currentUser != null) {
        if (isAdminMode) {
            viewModel.toggleAdminMode()
        } else {
            viewModel.navigateTo("home")
        }
    }

    // Modal Order Confirmation Dialog
    OrderConfirmationDialog(
        draft = orderDraft,
        user = currentUser,
        onDismiss = { viewModel.clearOrderDraft() },
        onConfirm = { viewModel.confirmPlaceOrder() },
        onNavigateDeposit = { viewModel.navigateTo("wallet") }
    )

    if (currentUser == null) {
        // Show Auth Screen
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            AuthScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "RechargeHub",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(
                                text = if (isAdminMode) "Admin Panel Mode" else "Wallet: ৳${String.format("%.2f", currentUser?.walletBalance ?: 0.0)}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                },
                actions = {
                    // Support Action Button
                    IconButton(
                        onClick = { viewModel.navigateTo("support") },
                        modifier = Modifier.testTag("topbar_btn_support")
                    ) {
                        Icon(
                            imageVector = if (activeScreen == "support") Icons.Default.HeadsetMic else Icons.Outlined.HeadsetMic,
                            contentDescription = "Customer Support",
                            tint = if (activeScreen == "support") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Admin Switch Button
                    Surface(
                        onClick = { viewModel.toggleAdminMode() },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isAdminMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isAdminMode) Icons.Default.AdminPanelSettings else Icons.Default.SwapHoriz,
                                contentDescription = "Toggle Admin",
                                tint = if (isAdminMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isAdminMode) "Admin" else "Switch to Admin",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAdminMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Logout Action
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (!isAdminMode) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    NavigationBarItem(
                        selected = activeScreen == "home",
                        onClick = { viewModel.navigateTo("home") },
                        icon = {
                            Icon(
                                imageVector = if (activeScreen == "home") Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_home")
                    )

                    NavigationBarItem(
                        selected = activeScreen == "recharge",
                        onClick = { viewModel.navigateTo("recharge") },
                        icon = {
                            Icon(
                                imageVector = if (activeScreen == "recharge") Icons.Filled.Bolt else Icons.Outlined.Bolt,
                                contentDescription = "Recharge"
                            )
                        },
                        label = { Text("Recharge", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_recharge")
                    )

                    NavigationBarItem(
                        selected = activeScreen == "digital",
                        onClick = { viewModel.navigateTo("digital") },
                        icon = {
                            Icon(
                                imageVector = if (activeScreen == "digital") Icons.Filled.SportsEsports else Icons.Outlined.SportsEsports,
                                contentDescription = "Digital"
                            )
                        },
                        label = { Text("Services", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_digital")
                    )

                    NavigationBarItem(
                        selected = activeScreen == "wallet",
                        onClick = { viewModel.navigateTo("wallet") },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (pendingDepositCount > 0 && currentUser?.role == UserRole.ADMIN) {
                                        Badge { Text(pendingDepositCount.toString()) }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (activeScreen == "wallet") Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                                    contentDescription = "Wallet"
                                )
                            }
                        },
                        label = { Text("Wallet", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_wallet")
                    )

                    NavigationBarItem(
                        selected = activeScreen == "orders",
                        onClick = { viewModel.navigateTo("orders") },
                        icon = {
                            Icon(
                                imageVector = if (activeScreen == "orders") Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                                contentDescription = "Orders"
                            )
                        },
                        label = { Text("Orders", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_orders")
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isAdminMode) {
                AdminPanelScreen(viewModel = viewModel)
            } else {
                when (activeScreen) {
                    "home" -> HomeScreen(viewModel = viewModel)
                    "recharge" -> RechargeScreen(viewModel = viewModel)
                    "digital" -> DigitalServicesScreen(viewModel = viewModel)
                    "wallet" -> WalletScreen(viewModel = viewModel)
                    "orders" -> OrdersScreen(viewModel = viewModel)
                    "support" -> SupportScreen(viewModel = viewModel)
                    else -> HomeScreen(viewModel = viewModel)
                }
            }
        }
    }
}
