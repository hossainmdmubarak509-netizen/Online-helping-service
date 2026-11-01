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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.theme.StatusApprovedText
import com.example.ui.theme.StatusRejectedBg
import com.example.ui.theme.StatusRejectedText
import com.example.ui.viewmodel.OrderDraft

@Composable
fun OrderConfirmationDialog(
    draft: OrderDraft?,
    user: UserEntity?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onNavigateDeposit: () -> Unit
) {
    if (draft == null) return

    val currentBal = user?.walletBalance ?: 0.0
    val hasEnoughBalance = currentBal >= draft.amount
    val remainingBal = currentBal - draft.amount

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircleOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Confirm Your Order", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = draft.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Operator / Service:", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                            Text(draft.operatorOrBrand, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Recipient / ID:", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                            Text(draft.recipientTarget, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Type:", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                            Text(draft.rechargeType, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }

                        draft.applicantName?.let { applicant ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Applicant:", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                                Text(applicant, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }

                        draft.documentFileName?.let { doc ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Document:", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                                Text(doc, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Balance summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Order Amount:", fontSize = 14.sp)
                    Text("৳${String.format("%.2f", draft.amount)}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Current Wallet Balance:", fontSize = 14.sp)
                    Text("৳${String.format("%.2f", currentBal)}", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }

                if (hasEnoughBalance) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Balance After Deduction:", fontSize = 14.sp, color = StatusApprovedText)
                        Text(
                            "৳${String.format("%.2f", remainingBal)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = StatusApprovedText
                        )
                    }
                } else {
                    Surface(
                        color = StatusRejectedBg,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = StatusRejectedText,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Insufficient balance! Need ৳${String.format("%.2f", draft.amount - currentBal)} more.",
                                color = StatusRejectedText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (hasEnoughBalance) {
                Button(
                    onClick = onConfirm,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pay ৳${String.format("%.2f", draft.amount)} Now")
                }
            } else {
                Button(
                    onClick = {
                        onDismiss()
                        onNavigateDeposit()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Deposit Money")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
