package com.example.antiscam.screens.auth

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

private const val OTP_LENGTH = 6
private const val TAG = "OtpScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    navController: NavController,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var otp by remember { mutableStateOf("") }

    val loading = viewModel.loading
    val errorMessage = viewModel.errorMessage

    // 🔍 Theo dõi state
    LaunchedEffect(loading) {
        Log.d(TAG, "Loading = $loading")
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            Log.e(TAG, "ErrorMessage = $errorMessage")
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            Row(
                modifier = Modifier
                    .background(Color(0xFF1C1C1E))
                    .fillMaxWidth()
                    .padding(
                        top = 40.dp,
                        start = 8.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    ),
                verticalAlignment = Alignment.Top
            ) {
                IconButton(
                    onClick = {
                        Log.d(TAG, "Back pressed")
                        navController.popBackStack()
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "Nhập mã OTP",
                        color = Color.White,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Nhập mã xác thực được gửi đến số điện thoại của bạn",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(40.dp))

            // 🔢 OTP INPUT
            BasicTextField(
                value = otp,
                onValueChange = { value ->
                    if (value.length <= OTP_LENGTH && value.all { it.isDigit() }) {
                        otp = value
                        Log.d(TAG, "OTP input changed = $otp (${otp.length}/$OTP_LENGTH)")
                    } else {
                        Log.w(TAG, "Invalid OTP input ignored: $value")
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                decorationBox = {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        repeat(OTP_LENGTH) { index ->
                            OtpDigitBox(
                                digit = otp.getOrNull(index)?.toString(),
                                isFocused = index == otp.length
                            )
                        }
                    }
                }
            )

            // ❌ Error message từ ViewModel
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(32.dp))

            // ✅ VERIFY OTP
            Button(
                onClick = {
                    Log.d(TAG, "Verify OTP clicked")
                    Log.d(TAG, "OTP submitted = $otp")

                    if (otp.length != OTP_LENGTH) {
                        Log.e(TAG, "OTP length invalid: ${otp.length}")
                        return@Button
                    }

                    viewModel.verifyOtp(
                        otp = otp,
                        onLoginSuccess = {
                            Log.d(TAG, "OTP verified successfully → Login success")
                            onLoginSuccess()
                        }
                    )
                },
                enabled = otp.length == OTP_LENGTH && !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0A84FF),
                    disabledContainerColor = Color(0xFF2C2C2E),
                    contentColor = Color.White,
                    disabledContentColor = Color.Gray
                )
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("Xác nhận", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun OtpDigitBox(
    digit: String?,
    isFocused: Boolean
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2C2C2E))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) Color(0xFF0A84FF) else Color.DarkGray,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Text(
            text = digit ?: "",
            fontSize = 20.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}
