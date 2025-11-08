package com.example.antiscam.screens.contact


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.absoluteValue
import com.example.antiscam.data.model.CallLog
import com.example.antiscam.data.repository.CallLogRepository
import com.example.antiscam.data.repository.ContactRepository
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen() {

    val context = LocalContext.current
    val contactRepository = remember { ContactRepository(context) }
    val callLogRepository = remember { CallLogRepository(context) }
    
    val factory = remember { ViewModelFactory(contactRepository, callLogRepository) }
    val contactViewModel: ContactViewModel = viewModel(key = "contact", factory = factory)
    val callLogViewModel: CallLogViewModel = viewModel(key = "callLog", factory = factory)

    var hasPermission by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) contactViewModel.loadContacts(context)
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            hasPermission = true
            contactViewModel.loadContacts(context)
        } else {
            launcher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    val contacts by contactViewModel.contacts.collectAsState()
    val groupedCallLogs by callLogViewModel.groupedCallLogs.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0: Lịch sử, 1: Danh bạ
    
    // Sync từ hệ thống CallLog khi app được resume (chỉ khi có permission)
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALL_LOG
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                callLogViewModel.syncFromSystem(context)
            } catch (e: Exception) {
                android.util.Log.e("ContactScreen", "Error syncing call logs", e)
            }
        }
    }

    val filteredContacts = contacts.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.phoneNumber.contains(searchQuery)
    }
    
    val filteredCallLogs = groupedCallLogs.filter {
        (it.contactName ?: "").contains(searchQuery, ignoreCase = true) ||
                it.phoneNumber.contains(searchQuery, ignoreCase = true)
    }
    
    // Xử lý permission và thực hiện cuộc gọi từ lịch sử
    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        // Permission handling sẽ được xử lý trong CallLogItem
    }
    
    fun makePhoneCall(phoneNumber: String) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                val intent = Intent(Intent.ACTION_CALL).apply {
                    data = "tel:$phoneNumber".toUri()
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                // Không lưu CallLog ở đây - sẽ được đọc từ hệ thống CallLog sau khi cuộc gọi kết thúc
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        containerColor = Color(0xFF000000),
        topBar = {
            Column(
                modifier = Modifier
                    .background(Color(0xFF1C1C1E))
                    .padding(top = 20.dp, bottom = 12.dp, start = 12.dp, end = 12.dp)
            ) {
                // Thanh tìm kiếm
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
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Tìm người liên hệ", color = Color.Gray) },
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

                // Bộ lọc dạng chip
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        text = "Lịch sử",
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    FilterChip(
                        text = "Danh bạ",
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                }
            }
        }
    ) { padding ->
        if (!hasPermission) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ứng dụng cần quyền truy cập danh bạ",
                    color = Color.White
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .background(Color(0xFF0D0D0D))
            ) {
                if (selectedTab == 0) {
                    // Hiển thị lịch sử cuộc gọi
                    if (filteredCallLogs.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Chưa có lịch sử cuộc gọi",
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    } else {
                        items(filteredCallLogs) { groupedCallLog ->
                            CallLogItem(
                                groupedCallLog = groupedCallLog,
                                onCallClick = { phoneNumber ->
                                    makePhoneCall(phoneNumber)
                                }
                            )
                        }
                    }
                } else {
                    // Hiển thị danh bạ
                    if (filteredContacts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Chưa có liên hệ",
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    } else {
                        items(filteredContacts) { contact ->
                            ContactItem(contact, callLogViewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) Color(0xFF38383A) else Color(0xFF2C2C2E),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 14.sp
        )
    }
}