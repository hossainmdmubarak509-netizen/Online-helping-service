package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import com.example.data.model.FaqItemEntity
import com.example.ui.theme.StatusApprovedBg
import com.example.ui.theme.StatusApprovedText
import com.example.ui.viewmodel.AppViewModel

@Composable
fun AdminSupportSettingsTab(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val supportConfig by viewModel.supportConfig.collectAsState()
    val faqs by viewModel.faqList.collectAsState()

    var phoneInput by remember(supportConfig) { mutableStateOf(supportConfig.phoneHotline) }
    var whatsappInput by remember(supportConfig) { mutableStateOf(supportConfig.whatsappNumber) }
    var emailInput by remember(supportConfig) { mutableStateOf(supportConfig.email) }
    var officeHoursInput by remember(supportConfig) { mutableStateOf(supportConfig.officeHours) }
    var noticeBannerInput by remember(supportConfig) { mutableStateOf(supportConfig.noticeBanner) }
    var isLiveSupportOnline by remember(supportConfig) { mutableStateOf(supportConfig.isLiveSupportOnline) }

    // Dialog state for adding or editing an FAQ
    var showFaqDialog by remember { mutableStateOf(false) }
    var editingFaqId by remember { mutableStateOf<String?>(null) }
    var faqQuestionInput by remember { mutableStateOf("") }
    var faqAnswerInput by remember { mutableStateOf("") }
    var faqCategoryInput by remember { mutableStateOf("Recharge") }

    val categories = listOf("Recharge", "NID Services", "Wallet & Deposit", "Refunds", "Digital Services", "General")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_support_settings_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Customer Support & FAQ Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Configure customer contact details, WhatsApp direct line, office hours, and FAQ entries.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        // Card 1: Contact Lines & Office Hours Configuration
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            Icon(
                                imageVector = Icons.Default.ContactPhone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Support Channels & Live Status",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isLiveSupportOnline) "Online" else "Offline",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isLiveSupportOnline) StatusApprovedText else Color.Gray
                            )
                            Switch(
                                checked = isLiveSupportOnline,
                                onCheckedChange = { isLiveSupportOnline = it },
                                modifier = Modifier.testTag("switch_live_support_status")
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Call Hotline Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_admin_support_phone")
                    )

                    OutlinedTextField(
                        value = whatsappInput,
                        onValueChange = { whatsappInput = it },
                        label = { Text("WhatsApp Direct Number (with Country Code)") },
                        leadingIcon = { Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(18.dp)) },
                        supportingText = { Text("Example: +8801700112233. Users tap to chat directly.") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_admin_support_whatsapp")
                    )

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Official Support Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_admin_support_email")
                    )

                    OutlinedTextField(
                        value = officeHoursInput,
                        onValueChange = { officeHoursInput = it },
                        label = { Text("Support Working Hours") },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_admin_support_hours")
                    )

                    OutlinedTextField(
                        value = noticeBannerInput,
                        onValueChange = { noticeBannerInput = it },
                        label = { Text("Support Notice Banner (Shown on user support page)") },
                        leadingIcon = { Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        minLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_admin_support_banner")
                    )

                    Button(
                        onClick = {
                            viewModel.updateSupportConfig(
                                phoneHotline = phoneInput,
                                whatsappNumber = whatsappInput,
                                email = emailInput,
                                officeHours = officeHoursInput,
                                noticeBanner = noticeBannerInput,
                                isLiveSupportOnline = isLiveSupportOnline
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_save_support_settings")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Support Settings", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Card 2: FAQ Knowledge Base Management
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FAQ Knowledge Base (${faqs.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Manage help questions shown in the customer app.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Button(
                    onClick = {
                        editingFaqId = null
                        faqQuestionInput = ""
                        faqAnswerInput = ""
                        faqCategoryInput = "Recharge"
                        showFaqDialog = true
                    },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_add_faq")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add FAQ", fontSize = 12.sp)
                }
            }
        }

        items(faqs, key = { it.id }) { faq ->
            Card(
                shape = RoundedCornerShape(12.dp),
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
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = faq.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = {
                                    editingFaqId = faq.id
                                    faqQuestionInput = faq.question
                                    faqAnswerInput = faq.answer
                                    faqCategoryInput = faq.category
                                    showFaqDialog = true
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit FAQ", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            }

                            IconButton(
                                onClick = { viewModel.deleteFaq(faq) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete FAQ", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    Text(
                        text = faq.question,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    Text(
                        text = faq.answer,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Modal: Add / Edit FAQ Item
    if (showFaqDialog) {
        AlertDialog(
            onDismissRequest = { showFaqDialog = false },
            title = {
                Text(
                    text = if (editingFaqId == null) "Add New FAQ Item" else "Edit FAQ Item",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select Category:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.take(3).forEach { cat ->
                            FilterChip(
                                selected = faqCategoryInput == cat,
                                onClick = { faqCategoryInput = cat },
                                label = { Text(cat, fontSize = 10.sp) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.drop(3).forEach { cat ->
                            FilterChip(
                                selected = faqCategoryInput == cat,
                                onClick = { faqCategoryInput = cat },
                                label = { Text(cat, fontSize = 10.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = faqQuestionInput,
                        onValueChange = { faqQuestionInput = it },
                        label = { Text("Question") },
                        placeholder = { Text("e.g. How to download NID copy?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_faq_dialog_question")
                    )

                    OutlinedTextField(
                        value = faqAnswerInput,
                        onValueChange = { faqAnswerInput = it },
                        label = { Text("Detailed Answer") },
                        minLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_faq_dialog_answer")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (faqQuestionInput.isNotBlank() && faqAnswerInput.isNotBlank()) {
                            viewModel.saveFaq(
                                id = editingFaqId,
                                question = faqQuestionInput,
                                answer = faqAnswerInput,
                                category = faqCategoryInput
                            )
                            showFaqDialog = false
                        }
                    },
                    modifier = Modifier.testTag("btn_save_faq_dialog")
                ) {
                    Text("Save FAQ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFaqDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
