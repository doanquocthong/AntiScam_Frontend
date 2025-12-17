package com.example.antiscam

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class DialerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dialer chỉ là entry point cho hệ thống
        // UI thật bạn xử lý ở MainActivity
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        )
        finish()
    }
}
