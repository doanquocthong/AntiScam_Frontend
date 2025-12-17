package com.example.antiscam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.antiscam.screens.auth.OtpScreen
import com.example.antiscam.screens.auth.PhoneInputScreen
import com.example.antiscam.screens.call_receive.CallScreen
import com.example.antiscam.screens.navigation.AppNavGraph
import com.example.antiscam.screens.navigation.MainScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavGraph()
        }
    }
}