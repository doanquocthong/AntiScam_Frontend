package com.example.antiscam.screens.contact

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.antiscam.data.model.request.ReportRequest
import com.example.antiscam.screens.report.ReportUiState

@Composable
fun ReportDialog(
    phoneNumber: String,
    reporterPhone: String?,
    uiState: ReportUiState,
    onDismiss: () -> Unit,
    onSubmit: (ReportRequest) -> Unit
) {
    var reporterName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(phoneNumber) }
    var email by remember { mutableStateOf("") }
    var scamType by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var evidenceLink by remember { mutableStateOf("") }

    var phoneError by remember { mutableStateOf(false) }
    var scamTypeError by remember { mutableStateOf(false) }
    val textFieldColors = TextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedContainerColor = Color(0xFF2C2C2E),
        unfocusedContainerColor = Color(0xFF2C2C2E),
        errorContainerColor = Color(0xFFB71C1C).copy(alpha = 0.15f),
        cursorColor = Color.White,
        errorCursorColor = Color(0xFFB71C1C),
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        errorIndicatorColor = Color(0xFFB71C1C),
        focusedLabelColor = Color.White,
        unfocusedLabelColor = Color.Gray,
        errorLabelColor = Color(0xFFB71C1C)
    )
    val ownerPhone = reporterPhone ?: ""
    fun displayVietnamPhone(phone: String): String {
        val p = phone.trim().replace(" ", "")

        return when {
            p.startsWith("+84") && p.length >= 11 ->
                "0" + p.drop(3)

            else -> p
        }
    }
    fun normalizeVietnamPhone(phone: String): String {
        val p = phone.trim().replace(" ", "").replace("-", "")

        return when {
            p.startsWith("+84") -> "0" + p.drop(3)
            p.startsWith("84") -> "0" + p.drop(2)
            p.startsWith("0") -> p
            else -> p
        }
    }
    val normalizedPhone = normalizeVietnamPhone(phone)
    val normalizedReporterPhone = normalizeVietnamPhone(ownerPhone)
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .imePadding()
                .border(
                    width = 1.dp,
                    color = Color(0xFFCBBCBC),
                    shape = MaterialTheme.shapes.small)
                .verticalScroll(rememberScrollState()),
            shape = MaterialTheme.shapes.medium,
            color = Color(0xFF1C1C1E)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // 🔴 CẢNH BÁO
                Surface(
                    color = Color(0xFFB71C1C).copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFB71C1C)
                        )
                        Text(
                            text = "Chỉ báo cáo khi bạn chắc chắn đây là số điện thoại lừa đảo.",
                            color = Color(0xFFB71C1C),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Text(
                    text = "Báo cáo số điện thoại",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )


                OutlinedTextField(
                    value = reporterName,
                    onValueChange = { reporterName = it },
                    label = { Text("Tên người báo cáo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = displayVietnamPhone(ownerPhone),
                    onValueChange = {},
                    label = { Text("Số điện thoại người báo cáo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false,
                    colors = TextFieldDefaults.colors(
                        disabledTextColor = Color.White,
                        disabledContainerColor = Color(0xFF2C2C2E),
                        disabledLabelColor = Color.Gray,
                        disabledIndicatorColor = Color.Transparent
                    )
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        phoneError = false
                    },
                    label = { Text("Số điện thoại báo cáo*") },
                    singleLine = true,
                    isError = phoneError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    supportingText = {
                        if (phoneError) {
                            Text("Số điện thoại không được để trống")
                        }
                    }
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = scamType,
                    onValueChange = {
                        scamType = it
                        scamTypeError = false
                    },
                    label = { Text("Loại lừa đảo *") },
                    singleLine = true,
                    isError = scamTypeError,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    supportingText = {
                        if (scamTypeError) {
                            Text("Vui lòng nhập loại lừa đảo")
                        }
                    }
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả chi tiết") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4,
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = evidenceLink,
                    onValueChange = { evidenceLink = it },
                    label = { Text("Link bằng chứng (nếu có)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Huỷ", color = Color.White)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            phoneError = phone.isBlank()
                            scamTypeError = scamType.isBlank()
                            Log.d("Reported check","Clicked historyContactItem to report by ReportDialog, Before report")

                            if (!phoneError && !scamTypeError) {
                                onSubmit(
                                    ReportRequest(
                                        reporterName = reporterName,
                                        reporterPhone = normalizedReporterPhone,
                                        phone = phone,
                                        email = email,
                                        scamType = scamType,
                                        description = description,
                                        evidenceLink = evidenceLink
                                    )
                                )
                                Log.d("Report","Clicked historyContactItem to report by ReportDialog, After report")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB71C1C),
                            disabledContainerColor = Color.Gray
                        )

                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text("Gửi báo cáo", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
