package com.example.antiscam.screens.contact

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun AddContactDialog(
    phoneNumber: String,
    initialName: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName ?: "") }
    var nameError by remember { mutableStateOf(false) }

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
                    shape = MaterialTheme.shapes.small
                ),
            shape = MaterialTheme.shapes.medium,
            color = Color(0xFF1C1C1E)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // 🔵 HEADER
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = Color(0xFF0A84FF)
                    )
                    Text(
                        text = "Thêm vào danh bạ",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                }

                // ℹ️ Gợi ý
                Surface(
                    color = Color(0xFF0A84FF).copy(alpha = 0.12f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Lưu số này vào danh bạ để dễ nhận biết và tránh nhầm lẫn.",
                        color = Color(0xFF0A84FF),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                // 👤 Tên liên hệ
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Tên liên hệ *") },
                    singleLine = true,
                    isError = nameError,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    supportingText = {
                        if (nameError) {
                            Text("Tên liên hệ không được để trống")
                        }
                    }
                )

                // 📞 Số điện thoại (readonly)
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {},
                    label = { Text("Số điện thoại") },
                    singleLine = true,
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        disabledTextColor = Color.White,
                        disabledContainerColor = Color(0xFF2C2C2E),
                        disabledLabelColor = Color.Gray,
                        disabledIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 🔘 ACTIONS
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
                            nameError = name.isBlank()
                            if (!nameError) {
                                onConfirm(name.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0A84FF),
                            disabledContainerColor = Color.Gray
                        )
                    ) {
                        Text("Lưu", color = Color.White)
                    }
                }
            }
        }
    }
}
