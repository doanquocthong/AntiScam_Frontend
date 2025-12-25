package com.example.antiscam.screens.message

import MessageItem
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Telephony
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.antiscam.data.repository.MessageRepository
import com.example.antiscam.screens.contact.FilterChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    onOpenMessageDetail: (String) -> Unit
) {
    // ----------------------------------------------------
    // Context / Activity
    // ----------------------------------------------------
    val context = LocalContext.current
    val activity = context as Activity

    // ----------------------------------------------------
    // ViewModel
    // ----------------------------------------------------
    val messageRepository = remember {
        MessageRepository(context.applicationContext)
    }

    val viewModel: MessageViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return MessageViewModel(messageRepository) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    // ----------------------------------------------------
    // READ_SMS permission
    // ----------------------------------------------------
    var hasSmsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_SMS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val smsPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasSmsPermission = granted
        }

    // ----------------------------------------------------
    // Default SMS App
    // ----------------------------------------------------
    var isDefaultSmsApp by remember {
        mutableStateOf(
            Telephony.Sms.getDefaultSmsPackage(context) ==
                    context.packageName
        )
    }

    val defaultSmsLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            isDefaultSmsApp =
                Telephony.Sms.getDefaultSmsPackage(context) ==
                        context.packageName
        }

    fun requestDefaultSmsApp() {
        val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
        intent.putExtra(
            Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
            context.packageName
        )
        defaultSmsLauncher.launch(intent)
    }

    // ----------------------------------------------------
    // First-time sync flag
    // ----------------------------------------------------
    val prefs = remember {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    var firstSmsSyncDone by remember {
        mutableStateOf(
            prefs.getBoolean("first_sms_sync_done", false)
        )
    }


    // ----------------------------------------------------
    // Request permission when screen opens
    // ----------------------------------------------------
    LaunchedEffect(Unit) {
        if (!hasSmsPermission) {
            smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
        }
    }

    // ----------------------------------------------------
    // Sync SMS ONLY ONCE
    // ----------------------------------------------------
    LaunchedEffect(hasSmsPermission, isDefaultSmsApp) {
        if (
            hasSmsPermission &&
            isDefaultSmsApp &&
            !firstSmsSyncDone
        ) {
            viewModel.syncMessagesFromSystem()

            prefs.edit {
                putBoolean("first_sms_sync_done", true)
            }

            firstSmsSyncDone = true
        }
    }
    // ----------------------------------------------------
    // UI
    // ----------------------------------------------------
    Scaffold(
        containerColor = Color.Black,
        topBar = {
            Column(
                modifier = Modifier
                    .background(Color(0xFF1C1C1E))
                    .padding(
                        top = 20.dp,
                        start = 12.dp,
                        end = 12.dp,
                        bottom = 8.dp
                    )
            ) {
                // Search bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.large)
                        .background(Color(0xFF2C2C2E))
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Menu, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))

                    TextField(
                        value = "",
                        onValueChange = {},
                        placeholder = {
                            Text("Tìm kiếm tin nhắn", color = Color.Gray)
                        },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    Icon(Icons.Default.Mic, contentDescription = null, tint = Color.Gray)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(text = "Đã đọc", selected = true, onClick = {})
                    FilterChip(text = "Chưa đọc", selected = false, onClick = {})
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ------------------------------------------------
            // Banner yêu cầu Default SMS App
            // ------------------------------------------------
            if (!isDefaultSmsApp) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2C2C2E)
                    ),
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cần đặt ứng dụng làm SMS mặc định để nhận & phân tích tin nhắn lừa đảo",
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { requestDefaultSmsApp() }) {
                            Text("Đặt ngay")
                        }
                    }
                }
            }

            // ------------------------------------------------
            // Loading
            // ------------------------------------------------
            if (uiState.isSyncing) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ------------------------------------------------
            // Empty state
            // ------------------------------------------------
            if (uiState.conversations.isEmpty() && !uiState.isSyncing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Chưa có tin nhắn", color = Color.Gray)
                }
            }

            // ------------------------------------------------
            // Message list
            // ------------------------------------------------
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212)),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.conversations) { conversation ->
                    MessageItem(
                        address = conversation.address,
                        latestMessage = conversation.lastMessage,
                        latestTime = conversation.lastTimestamp,
                        isRead = conversation.isRead,
                        unReadCount = conversation.unReadCount,
                        openMessageDetail = {
                            onOpenMessageDetail(conversation.address)
                        }
                    )
                }
            }
        }
    }
}

