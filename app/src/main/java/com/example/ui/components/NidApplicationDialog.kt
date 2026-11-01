package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ProductEntity
import com.example.data.model.ServiceType
import com.example.data.model.UserEntity
import com.example.ui.theme.StatusApprovedBg
import com.example.ui.theme.StatusApprovedText
import com.example.ui.theme.StatusRejectedBg
import com.example.ui.theme.StatusRejectedText
import com.example.ui.viewmodel.OrderDraft
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NidApplicationDialog(
    product: ProductEntity,
    user: UserEntity?,
    onDismiss: () -> Unit,
    onSubmitDraft: (OrderDraft) -> Unit,
    onNavigateDeposit: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // Required Form Fields
    var applicantName by remember { mutableStateOf(user?.name ?: "") }
    var nidNumber by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("1995-08-15") }
    var mobileNumber by remember { mutableStateOf(user?.phone ?: "") }
    var division by remember { mutableStateOf("Dhaka") }
    var district by remember { mutableStateOf("Dhaka") }
    var upazila by remember { mutableStateOf("Mirpur") }

    // Optional Fields
    var additionalNotes by remember { mutableStateOf("") }

    // Optional Document Upload State
    var uploadedFileName by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }

    // Validation State
    var formError by remember { mutableStateOf<String?>(null) }

    val currentBalance = user?.walletBalance ?: 0.0
    val hasSufficientBalance = currentBalance >= product.price

    // Photo/Document Picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "document_${System.currentTimeMillis()}.pdf"
            coroutineScope.launch {
                isUploading = true
                uploadProgress = 0.15f
                delay(300)
                uploadProgress = 0.6f
                delay(300)
                uploadProgress = 1.0f
                delay(150)
                isUploading = false
                uploadedFileName = fileName
            }
        }
    }

    fun simulateSampleUpload(sampleName: String) {
        coroutineScope.launch {
            isUploading = true
            uploadProgress = 0.2f
            delay(250)
            uploadProgress = 0.7f
            delay(250)
            uploadProgress = 1.0f
            delay(150)
            isUploading = false
            uploadedFileName = sampleName
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("nid_application_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Top Header
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
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = product.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Election Commission • ${product.validity}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Form Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Fee Banner
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Service Fee:", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                Text(
                                    "৳${String.format("%.2f", product.price)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Your Wallet Balance:", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                Text(
                                    "৳${String.format("%.2f", currentBalance)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasSufficientBalance) StatusApprovedText else StatusRejectedText
                                )
                            }
                        }
                    }

                    // 1. Applicant's Name (Required)
                    OutlinedTextField(
                        value = applicantName,
                        onValueChange = { applicantName = it },
                        label = { Text("Applicant's Full Name *") },
                        placeholder = { Text("As printed on NID card") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("nid_input_name"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // 2. NID Number (Required)
                    Column {
                        OutlinedTextField(
                            value = nidNumber,
                            onValueChange = {
                                nidNumber = it.filter { char -> char.isDigit() || char.isLetter() }
                            },
                            label = { Text("NID Number (Required) *") },
                            placeholder = { Text("10, 13, or 17 digit National ID") },
                            leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            trailingIcon = {
                                if (nidNumber.isEmpty()) {
                                    TextButton(onClick = {
                                        nidNumber = (1000000000..9999999999).random().toString()
                                    }) {
                                        Text("Sample", fontSize = 10.sp)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("nid_input_number"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Text(
                            text = "Smart NID: 10 digits | Classic NID: 13/17 digits",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }

                    // 3. Date of Birth (Required)
                    OutlinedTextField(
                        value = dateOfBirth,
                        onValueChange = { dateOfBirth = it },
                        label = { Text("Date of Birth (YYYY-MM-DD) *") },
                        placeholder = { Text("1995-08-15") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        trailingIcon = {
                            TextButton(onClick = { dateOfBirth = "1994-05-20" }) {
                                Text("Fill Sample", fontSize = 10.sp)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("nid_input_dob"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // 4. Contact Mobile Number (Required)
                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { mobileNumber = it },
                        label = { Text("Contact Mobile Number *") },
                        placeholder = { Text("017XXXXXXXX") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("nid_input_phone"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // --- LOCATION SECTION (REQUIRED: Division, District, Upazila) ---
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Location Information (Required) *",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            // 5. Division (Required)
                            OutlinedTextField(
                                value = division,
                                onValueChange = { division = it },
                                label = { Text("Division *") },
                                placeholder = { Text("e.g. Dhaka, Chittagong, Rajshahi") },
                                leadingIcon = { Icon(Icons.Default.Map, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("nid_input_division"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            // Quick Division selection chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("Dhaka", "Chittagong", "Rajshahi", "Khulna", "Sylhet").forEach { divName ->
                                    SuggestionChip(
                                        onClick = { division = divName },
                                        label = { Text(divName, fontSize = 10.sp) },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }

                            // 6. District (Required)
                            OutlinedTextField(
                                value = district,
                                onValueChange = { district = it },
                                label = { Text("District *") },
                                placeholder = { Text("e.g. Dhaka, Gazipur, Cumilla, Bogura") },
                                leadingIcon = { Icon(Icons.Default.Apartment, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("nid_input_district"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            // 7. Upazila / Thana (Required)
                            OutlinedTextField(
                                value = upazila,
                                onValueChange = { upazila = it },
                                label = { Text("Upazila / Thana *") },
                                placeholder = { Text("e.g. Mirpur, Savar, Sadar, Sreepur") },
                                leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("nid_input_upazila"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }
                    }

                    // 8. OPTIONAL: Non-mandatory Document Upload
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (uploadedFileName != null) StatusApprovedText else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("nid_optional_file_upload_card")
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
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AttachFile,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Supporting Document (Optional)",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Optional",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Attach old NID copy, birth certificate, or slip if available (not required).",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )

                            // Upload Progress Indicator
                            if (isUploading) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Attaching document...",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "${(uploadProgress * 100).toInt()}%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = { uploadProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                    )
                                }
                            } else if (uploadedFileName != null) {
                                // Attached file preview
                                Surface(
                                    color = StatusApprovedBg,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = StatusApprovedText,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = uploadedFileName ?: "file.pdf",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp,
                                                color = StatusApprovedText
                                            )
                                        }

                                        IconButton(
                                            onClick = { uploadedFileName = null },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove",
                                                tint = Color.Red,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Small non-mandatory upload button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { filePickerLauncher.launch("*/*") },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Upload File", fontSize = 11.sp)
                                    }

                                    OutlinedButton(
                                        onClick = { simulateSampleUpload("nid_doc_${(100..999).random()}.pdf") },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Attachment, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Sample Doc", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // 9. Additional Notes (Optional)
                    OutlinedTextField(
                        value = additionalNotes,
                        onValueChange = { additionalNotes = it },
                        label = { Text("Additional Notes (Optional)") },
                        placeholder = { Text("e.g. Any special instructions or details") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 1,
                        maxLines = 2
                    )

                    // Error Message Banner if validation fails
                    formError?.let { err ->
                        Surface(
                            color = StatusRejectedBg,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = StatusRejectedText, modifier = Modifier.size(18.dp))
                                Text(err, color = StatusRejectedText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // Insufficient balance warning
                    if (!hasSufficientBalance) {
                        Surface(
                            color = StatusRejectedBg,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Insufficient Wallet Balance! Need ৳${String.format("%.2f", product.price - currentBalance)} more.",
                                    color = StatusRejectedText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Button(
                                    onClick = {
                                        onDismiss()
                                        onNavigateDeposit()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Money to Wallet", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            // Validate ONLY required fields
                            if (applicantName.trim().isEmpty()) {
                                formError = "Please enter Applicant's Full Name"
                                return@Button
                            }
                            if (nidNumber.trim().length < 8) {
                                formError = "Please enter a valid NID Number (min 8 digits)"
                                return@Button
                            }
                            if (dateOfBirth.trim().isEmpty()) {
                                formError = "Please enter Date of Birth"
                                return@Button
                            }
                            if (mobileNumber.trim().length < 10) {
                                formError = "Please enter Contact Mobile Number"
                                return@Button
                            }
                            if (division.trim().isEmpty()) {
                                formError = "Please enter Division"
                                return@Button
                            }
                            if (district.trim().isEmpty()) {
                                formError = "Please enter District"
                                return@Button
                            }
                            if (upazila.trim().isEmpty()) {
                                formError = "Please enter Upazila"
                                return@Button
                            }
                            if (!hasSufficientBalance) {
                                formError = "Insufficient balance. Please deposit money to proceed."
                                return@Button
                            }

                            formError = null

                            // Submit NID Order Draft (Notice: Document upload is optional)
                            val draft = OrderDraft(
                                serviceType = ServiceType.NID_SERVICE,
                                title = "${product.title} - $nidNumber",
                                operatorOrBrand = "Election Commission",
                                recipientTarget = "NID: $nidNumber",
                                rechargeType = "NID_APPLICATION",
                                amount = product.price,
                                applicantName = applicantName.trim(),
                                nidNumber = nidNumber.trim(),
                                dateOfBirth = dateOfBirth.trim(),
                                applicantAddress = "${upazila.trim()}, ${district.trim()}, ${division.trim()}",
                                division = division.trim(),
                                district = district.trim(),
                                upazila = upazila.trim(),
                                documentFileName = uploadedFileName,
                                additionalNotes = additionalNotes.trim().ifEmpty { null }
                            )
                            onSubmitDraft(draft)
                        },
                        enabled = !isUploading && hasSufficientBalance,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.8f)
                            .testTag("nid_submit_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Apply & Pay ৳${String.format("%.0f", product.price)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
