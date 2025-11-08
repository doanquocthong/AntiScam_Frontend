package com.example.antiscam.screens.navigation

import android.R.attr.layoutDirection
import androidx.compose.foundation.background
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Message
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.example.antiscam.screens.contact.ContactScreen
import com.example.antiscam.screens.message.MessageScreen
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalLayoutDirection

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = Color.Black,  // Nền chính scaffold màu đen
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF121212)  // Nền bottom nav màu tối hơn chút
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White) },
                    label = { Text("Điện thoại", color = Color.White) },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Cyan,
                        selectedTextColor = Color.Cyan,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0xFF222222)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Message, contentDescription = "Message", tint = Color.White) },
                    label = { Text("Tin nhắn", color = Color.White) },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Cyan,
                        selectedTextColor = Color.Cyan,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0xFF222222)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.Black)  // Nền màn hình con cũng màu đen
        ) {
            when (selectedTab) {
                0 -> ContactScreen()
                1 -> MessageScreen()
            }
        }
    }
}