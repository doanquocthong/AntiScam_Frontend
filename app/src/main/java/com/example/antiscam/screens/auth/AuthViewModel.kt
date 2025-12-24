package com.example.antiscam.screens.auth

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit

private const val TAG = "AuthViewModel"

class AuthViewModel : ViewModel() {


    private val auth = FirebaseAuth.getInstance()

    var verificationId by mutableStateOf<String?>(null)
        private set

    var loading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set
    private val _reporterPhone = MutableStateFlow<String?>(null)
    val reporterPhone: StateFlow<String?> = _reporterPhone
    fun setReporterPhone(phone: String) {
        _reporterPhone.value = phone
    }

    init {
        FirebaseAuth.getInstance().currentUser?.phoneNumber?.let {
            _reporterPhone.value = it
            Log.d("AuthVM", "Init reporterPhone = $it")
        }
    }
    // ============================
    // 📩 SEND OTP
    // ============================
    fun sendOtp(
        phone: String,
        activity: Activity,
        onOtpSent: () -> Unit
    ) {
        Log.d(TAG, "sendOtp() called")
        Log.d(TAG, "Phone = $phone")
        Log.d(TAG, "Activity = ${activity::class.java.simpleName}")
        setReporterPhone(phone)
        Log.d(TAG, "Đã lưu số điện thoại = ${setReporterPhone(phone)}")
        loading = true
        errorMessage = null

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                // ✅ Auto verify (SMS Retriever / Instant verification)
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Log.d(TAG, "onVerificationCompleted()")
                    Log.d(TAG, "Auto credential received")

                    auth.signInWithCredential(credential)
                        .addOnSuccessListener {
                            Log.d(TAG, "Auto sign-in success")
                            loading = false
                        }
                        .addOnFailureListener {
                            Log.e(TAG, "Auto sign-in failed", it)
                            loading = false
                            errorMessage = it.localizedMessage
                        }
                }

                // ❌ Gửi OTP thất bại
                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e(TAG, "onVerificationFailed()", e)

                    loading = false
                    errorMessage = e.localizedMessage ?: "Gửi OTP thất bại"
                }

                // 📤 OTP đã được gửi
                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    Log.d(TAG, "onCodeSent()")
                    Log.d(TAG, "verificationId = $verificationId")
                    Log.d(TAG, "resendToken = $token")

                    this@AuthViewModel.verificationId = verificationId
                    loading = false

                    onOtpSent()
                }
            })
            .build()

        Log.d(TAG, "Calling PhoneAuthProvider.verifyPhoneNumber()")
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // ============================
    // 🔐 VERIFY OTP
    // ============================
    fun verifyOtp(
        otp: String,
        onLoginSuccess: () -> Unit
    ) {
        Log.d("AuthViewModel", "verifyOtp() called")
        Log.d("AuthViewModel", "verificationId = $verificationId")
        Log.d("AuthViewModel", "otp = $otp")

        val id = verificationId
        if (id == null) {
            Log.e(TAG, "verificationId is NULL → cannot verify OTP")
            errorMessage = "Lỗi xác thực, vui lòng thử lại"
            return
        }

        Log.d(TAG, "verificationId = $id")

        loading = true
        errorMessage = null

        fun logout() {
            FirebaseAuth.getInstance().signOut()
        }

        val credential = PhoneAuthProvider.getCredential(id, otp)
        Log.d(TAG, "Credential created, signing in...")

        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                Log.d(TAG, "OTP verified successfully → Login success")

                loading = false
                onLoginSuccess()
            }
            .addOnFailureListener {
                Log.e(TAG, "OTP verification failed", it)

                loading = false
                errorMessage = "OTP không đúng"
            }
    }
}
