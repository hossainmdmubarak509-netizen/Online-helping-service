package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DepositStatus
import com.example.data.model.OrderStatus
import com.example.ui.theme.*

@Composable
fun OrderStatusBadge(
    status: OrderStatus,
    isRefunded: Boolean = false,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label, icon: ImageVector) = when (status) {
        OrderStatus.PENDING -> Quadruple(StatusPendingBg, StatusPendingText, "Pending", Icons.Default.Schedule)
        OrderStatus.PROCESSING -> Quadruple(StatusProcessingBg, StatusProcessingText, "Processing", Icons.Default.Autorenew)
        OrderStatus.COMPLETED -> Quadruple(StatusApprovedBg, StatusApprovedText, "Completed", Icons.Default.CheckCircle)
        OrderStatus.REJECTED -> {
            if (isRefunded) {
                Quadruple(StatusRejectedBg, StatusRejectedText, "Rejected (Refunded)", Icons.Default.Cancel)
            } else {
                Quadruple(StatusRejectedBg, StatusRejectedText, "Rejected", Icons.Default.Cancel)
            }
        }
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DepositStatusBadge(
    status: DepositStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label, icon: ImageVector) = when (status) {
        DepositStatus.PENDING -> Quadruple(StatusPendingBg, StatusPendingText, "Pending Approval", Icons.Default.HourglassTop)
        DepositStatus.APPROVED -> Quadruple(StatusApprovedBg, StatusApprovedText, "Approved & Credited", Icons.Default.CheckCircle)
        DepositStatus.REJECTED -> Quadruple(StatusRejectedBg, StatusRejectedText, "Rejected", Icons.Default.Cancel)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
