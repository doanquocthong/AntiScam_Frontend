package com.example.antiscam.screens.navigation
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.graphics.Color
import com.example.antiscam.screens.contact.ContactScreen
import androidx.navigation.NavController
import com.example.antiscam.screens.message.MessageDetailScreen
import com.example.antiscam.screens.message.MessageScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // 👉 state để mở màn hình chi tiết
    var openedAddress by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            if (openedAddress == null) { // Ẩn bottom bar khi xem chi tiết
                NavigationBar(
                    containerColor = Color(0xFF121212)
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Call, contentDescription = null) },
                        label = { Text("Điện thoại") },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        colors = NavigationBarItemDefaults.colors( selectedIconColor = Color.White, selectedTextColor = Color.White, unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray, indicatorColor = Color(0xFF222222) )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Message, contentDescription = null) },
                        label = { Text("Tin nhắn") },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        colors = NavigationBarItemDefaults.colors( selectedIconColor = Color.White, selectedTextColor = Color.White, unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray, indicatorColor = Color(0xFF222222) )
                    )
                }
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Log.d("openedAddress= ", "${openedAddress}")
            when {
                // 👉 Màn hình chi tiết
                openedAddress != null -> {
                    MessageDetailScreen (
                        address = openedAddress!!,
                        onBack = { openedAddress = null }
                    )
                }

                // 👉 Tab bình thường
                selectedTab == 0 -> ContactScreen(
                    openCallLogDetail = { phoneNumber ->
                        navController.navigate("call_log/$phoneNumber")
                    }
                )

                selectedTab == 1 -> MessageScreen(
                    onOpenMessageDetail = { address ->
                        openedAddress = address
                    }
                )
            }
        }
    }
}
