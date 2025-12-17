package com.example.antiscam.screens.auth

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneInputScreen(
    onNavigateToOtp: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as Activity

    var phone by remember { mutableStateOf("") }

    val loading = viewModel.loading
    val errorMessage = viewModel.errorMessage

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 🔧 Chuẩn hoá số VN về +84 (E.164)
    fun formatVietnamPhone(raw: String): String? {
        val p = raw.trim().replace(" ", "")

        return when {
            p.isEmpty() -> null
            p.startsWith("+84") && p.length >= 11 -> p
            p.startsWith("0") && p.length == 10 -> "+84" + p.drop(1)
            p.length == 9 -> "+84$p"
            else -> null
        }
    }

    Scaffold(
        containerColor = Color.Black,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                modifier = Modifier
                    .background(Color(0xFF1C1C1E))
                    .padding(
                        top = 40.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Xác thực số điện thoại",
                    color = Color.White,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Nhập số điện thoại để nhận mã OTP",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            // 🔢 Input số điện thoại
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF2C2C2E))
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = {
                        Text("Số điện thoại", color = Color.Gray)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }

            // ❌ Lỗi từ ViewModel
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 🚀 Gửi OTP
            Button(
                onClick = {

                    Log.d("PhoneInput", "Raw phone = $phone")

                    // ⛔ Trống
                    if (phone.isBlank()) {
                        Log.e("PhoneInput", "Phone is empty")
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Vui lòng nhập số điện thoại"
                            )
                        }
                        return@Button
                    }

                    // 🔍 Format + validate
                    val formattedPhone = formatVietnamPhone(phone)

                    Log.d("PhoneInput", "Formatted phone = $formattedPhone")

                    if (formattedPhone == null) {
                        Log.e("PhoneInput", "Invalid phone number")
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Số điện thoại không hợp lệ"
                            )
                        }
                        return@Button
                    }

                    // ✅ Gửi OTP Firebase
                    viewModel.sendOtp(
                        phone = formattedPhone,
                        activity = activity,
                        onOtpSent = {
                            Log.d("PhoneInput", "OTP sent successfully")
                            onNavigateToOtp()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0A84FF)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Gửi OTP", fontSize = 16.sp)
                }
            }
        }
    }
}
