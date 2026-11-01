package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.components.OrderStatusBadge
import com.example.ui.theme.StatusApprovedBg
import com.example.ui.theme.StatusApprovedText
import com.example.ui.theme.StatusRejectedBg
import com.example.ui.theme.StatusRejectedText
import com.example.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import com.example.util.PdfDocumentUtil

@Composable
fun OrdersScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val orders by viewModel.userOrders.collectAsState()
    var selectedStatusFilter by remember { mutableStateOf<OrderStatus?>(null) }
    var selectedOrderForPdfPreview by remember { mutableStateOf<OrderEntity?>(null) }

    val filteredOrders = orders.filter {
        selectedStatusFilter == null || it.status == selectedStatusFilter
    }

    selectedOrderForPdfPreview?.let { previewOrder ->
        OrderPdfPreviewDialog(
            order = previewOrder,
            onDismiss = { selectedOrderForPdfPreview = null },
            onDownload = {
                PdfDocumentUtil.openOrSharePdf(context, previewOrder)
                viewModel.showNotice("Downloaded ${previewOrder.deliveryPdfFileName} to device.")
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("orders_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Text(
                text = "Order Management & History",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Track your recharge, drive pack, and digital service status in real-time",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }

        // Status Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedStatusFilter == null,
                    onClick = { selectedStatusFilter = null },
                    label = { Text("All (${orders.size})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedStatusFilter == OrderStatus.PENDING,
                    onClick = { selectedStatusFilter = OrderStatus.PENDING },
                    label = { Text("Pending (${orders.count { it.status == OrderStatus.PENDING }})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedStatusFilter == OrderStatus.PROCESSING,
                    onClick = { selectedStatusFilter = OrderStatus.PROCESSING },
                    label = { Text("Processing", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedStatusFilter == OrderStatus.COMPLETED,
                    onClick = { selectedStatusFilter = OrderStatus.COMPLETED },
                    label = { Text("Completed", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedStatusFilter == OrderStatus.REJECTED,
                    onClick = { selectedStatusFilter = OrderStatus.REJECTED },
                    label = { Text("Rejected", fontSize = 11.sp) }
                )
            }
        }

        // Orders List
        if (filteredOrders.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = if (selectedStatusFilter == null) "No orders placed yet" else "No orders with status ${selectedStatusFilter?.name}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        OutlinedButton(onClick = { viewModel.navigateTo("recharge") }) {
                            Text("Make a Recharge", fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            items(filteredOrders) { order ->
                CustomerOrderCard(
                    order = order,
                    onPreviewPdf = { selectedOrderForPdfPreview = it },
                    onDownloadPdf = {
                        PdfDocumentUtil.openOrSharePdf(context, it)
                        viewModel.showNotice("Downloaded ${it.deliveryPdfFileName ?: "document.pdf"} to device.")
                    }
                )
            }
        }
    }
}

@Composable
private fun CustomerOrderCard(
    order: OrderEntity,
    onPreviewPdf: (OrderEntity) -> Unit,
    onDownloadPdf: (OrderEntity) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = order.operatorOrBrand,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "#${order.id.takeLast(6).uppercase()}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OrderStatusBadge(status = order.status, isRefunded = order.isRefunded)
            }

            Text(
                text = order.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Recipient: ${order.recipientTarget}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Placed: ${dateFormat.format(Date(order.createdAt))}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Text(
                    text = "৳${String.format("%.2f", order.amount)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // NID Application Details if present
            if (order.nidNumber != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Applicant: ${order.applicantName ?: order.userName}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("DOB: ${order.dateOfBirth ?: "N/A"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }
                        Text("NID: ${order.nidNumber}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        if (!order.division.isNullOrBlank() || !order.district.isNullOrBlank() || !order.upazila.isNullOrBlank()) {
                            Text(
                                "Location: ${listOfNotNull(order.upazila, order.district, order.division).filter { it.isNotBlank() }.joinToString(", ")}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text("Address: ${order.applicantAddress ?: "N/A"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        order.documentFileName?.let { doc ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                Text("Uploaded Document: $doc", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        order.additionalNotes?.let { note ->
                            Text("Note: $note", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }

            // Completed Operator TrxID
            order.operatorTrxId?.let { trxId ->
                Surface(
                    color = StatusApprovedBg,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StatusApprovedText,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Delivery TrxID / Ref: $trxId",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusApprovedText
                        )
                    }
                }
            }

            // PDF Delivery Card (Available on completed orders with attached document)
            if (!order.deliveryPdfFileName.isNullOrBlank() && order.status == OrderStatus.COMPLETED) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFDE8E8), // Soft crimson background for PDF
                    border = BorderStroke(1.dp, Color(0xFFF8B4B4)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("delivery_pdf_card_${order.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                                        .background(Color(0xFFE02424), shape = RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = "PDF",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Official Delivery Document (PDF)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF9B1C1C)
                                    )
                                    Text(
                                        text = "${order.deliveryPdfFileName} • ${order.deliveryPdfSize ?: "248 KB"}",
                                        fontSize = 10.sp,
                                        color = Color(0xFF771D1D)
                                    )
                                }
                            }

                            Surface(
                                color = Color(0xFFBCF0DA),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "VERIFIED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF03543F),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Action Buttons: View Preview & Download PDF
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onPreviewPdf(order) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_view_pdf_${order.id}"),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF9B1C1C)
                                ),
                                border = BorderStroke(1.dp, Color(0xFFE02424))
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("View Slip", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onDownloadPdf(order) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_download_pdf_${order.id}"),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE02424)
                                )
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Download PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // CRITICAL: Rejection & Refund Notice
            if (order.status == OrderStatus.REJECTED) {
                Surface(
                    color = StatusRejectedBg,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = null,
                                tint = StatusRejectedText,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "AUTOMATIC WALLET REFUND",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusRejectedText
                            )
                        }
                        Text(
                            text = order.adminNote ?: "Order rejected. Full amount of ৳${String.format("%.2f", order.amount)} has been refunded to your wallet.",
                            fontSize = 11.sp,
                            color = StatusRejectedText
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderPdfPreviewDialog(
    order: OrderEntity,
    onDismiss: () -> Unit,
    onDownload: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header with Bangladesh Seal Simulation
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
                                .size(36.dp)
                                .background(Color(0xFF006A4E), shape = CircleShape), // Bangladesh Green
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(
                                text = "Election Commission BD",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "NID Verification Wing",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Certificate Document Preview Container
                Surface(
                    color = Color(0xFFF9FAFB),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = Color(0xFFDEF7EC),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "AUTHENTICATED SERVER COPY SLIP",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF03543F),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Text(
                            text = order.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF111827)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            DocRow(label = "Applicant Name:", value = order.applicantName ?: order.userName)
                            DocRow(label = "NID Number:", value = order.nidNumber ?: order.recipientTarget)
                            DocRow(label = "Date of Birth:", value = order.dateOfBirth ?: "15/08/1994")
                            if (!order.division.isNullOrBlank() || !order.district.isNullOrBlank()) {
                                DocRow(
                                    label = "Location:",
                                    value = listOfNotNull(order.upazila, order.district, order.division).filter { it.isNotBlank() }.joinToString(", ")
                                )
                            }
                            DocRow(label = "Operator TrxID:", value = order.operatorTrxId ?: "N/A")
                            DocRow(label = "PDF File:", value = order.deliveryPdfFileName ?: "Document.pdf")
                            DocRow(label = "File Size:", value = order.deliveryPdfSize ?: "248 KB")
                        }

                        // Digital Seal / Verification Note
                        Surface(
                            color = Color(0xFFF3F4F6),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Digital Signature & Verification Seal",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF374151)
                                )
                                Text(
                                    text = "Cryptographically signed and delivered through RechargeHub Gateway on ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(order.deliveryPdfUploadedAt ?: System.currentTimeMillis()))}.",
                                    fontSize = 9.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }
                    }
                }

                // Modal Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Close", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onDownload()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE02424))
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download PDF", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DocRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 11.sp, color = Color(0xFF111827), fontWeight = FontWeight.SemiBold)
    }
}

