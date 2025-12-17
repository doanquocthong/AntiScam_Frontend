package com.example.antiscam.screens.contact

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.telecom.TelecomManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Warning
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
import com.example.antiscam.data.model.PendingCall
import com.example.antiscam.data.model.ScamAlert
import com.example.antiscam.data.model.enums.ContactTab
import com.example.antiscam.data.repository.CallLogRepository
import com.example.antiscam.data.repository.ContactRepository
import com.example.antiscam.data.repository.ScamCheckRepository
import com.example.antiscam.screens.report.ReportViewModel
import kotlinx.coroutines.flow.collect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen() {
    val context = LocalContext.current

    // Theo dõi trạng thái app làm default dialer
    var isDefaultDialer by remember { mutableStateOf(isDefaultDialer(context)) }

    // Launcher để mở màn hình đổi ứng dụng gọi mặc định và nhận kết quả
    val defaultDialerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // Cập nhật lại trạng thái dialer mặc định sau khi user chọn xong
        isDefaultDialer = isDefaultDialer(context)
    }

    var pendingPermissionCall by remember { mutableStateOf<PendingCall?>(null) }
    var hasContactPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val contactPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasContactPermission = granted
            if (!granted) {
                Toast.makeText(
                    context,
                    "Cần quyền truy cập danh bạ",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    if (!hasContactPermission) {
        LaunchedEffect(Unit) {
            contactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Ứng dụng cần quyền truy cập danh bạ", color = Color.White)
        }

        return // ⛔ CHẶN TẠO VIEWMODEL
    }

    val contactRepository = remember { ContactRepository(context) }
    val callLogRepository = remember { CallLogRepository(context) }
    val scamRepository = remember { ScamCheckRepository() }

    val viewModel: ContactScreenViewModel = viewModel(
        factory = ContactScreenViewModelFactory(
            contactRepository,
            callLogRepository,
            scamRepository
        )
    )

    val uiState by viewModel.uiState.collectAsState()
    val reportViewModel: ReportViewModel = viewModel()
    val reportUiState by reportViewModel.uiState.collectAsState()
    var reportDialogVisible by remember { mutableStateOf<String?>(null) }

    // Kiểm tra quyền và xử lý effect
    LaunchedEffect(Unit) {
        try {
            val contactGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED

            if (contactGranted) {
                hasContactPermission = true
                viewModel.loadContacts()
            } else {
                contactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }

            // Thu thập hiệu ứng từ ViewModel
            viewModel.effects.collect { effect ->
                when (effect) {
                    is ContactEffect.StartCall ->
                        launchCallIntent(context, effect.phoneNumber)

                    is ContactEffect.ShowToast ->
                        Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingPermissionCall?.let {
                viewModel.requestCall(it.phoneNumber, it.contactName)
            }
        } else {
            Toast.makeText(context, "Cần cấp quyền gọi điện", Toast.LENGTH_SHORT).show()
        }
        pendingPermissionCall = null
    }

    fun handleCallRequest(phone: String, name: String?) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.requestCall(phone, name)
        } else {
            pendingPermissionCall = PendingCall(phone, name)
            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }

    // Hàm gọi intent yêu cầu đổi ứng dụng gọi mặc định
    fun requestDefaultDialer() {
        val telecomManager =
            context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

        Log.d("DialerCheck", "Current default = ${telecomManager.defaultDialerPackage}")
        Log.d("DialerCheck", "My package = ${context.packageName}")

        val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
            putExtra(
                TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                context.packageName
            )
        }
        defaultDialerLauncher.launch(intent)
    }


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
        topBar = {
            Column(
                modifier = Modifier
                    .background(Color(0xFF1C1C1E))
                    .padding(top = 20.dp, start = 12.dp, end = 12.dp, bottom = 8.dp)
            ) {
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
                        value = uiState.searchQuery,
                        onValueChange = viewModel::onSearchQueryChange,
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

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        text = "Lịch sử",
                        selected = uiState.selectedTab == ContactTab.CallHistory,
                        onClick = { viewModel.onTabSelected(ContactTab.CallHistory) }
                    )
                    FilterChip(
                        text = "Danh bạ",
                        selected = uiState.selectedTab == ContactTab.Contacts,
                        onClick = { viewModel.onTabSelected(ContactTab.Contacts) }
                    )
                }

                if (uiState.isCheckingScam || uiState.isSyncingCallLogs) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    )
                }
            }
        }
    ) { padding ->
        Log.d("Default App", "Permission default App: = ${hasContactPermission}")
        if (!hasContactPermission) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Ứng dụng cần quyền truy cập danh bạ", color = Color.White)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .background(Color(0xFF0D0D0D))
            ) {
                if (isDefaultDialer) {
                    item {
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
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFFF9F0A)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Cần đặt làm ứng dụng gọi mặc định",
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Để phát hiện & cảnh báo cuộc gọi lừa đảo",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                                TextButton(onClick = { requestDefaultDialer() }) {
                                    Text("Đặt ngay")
                                }
                            }
                        }
                    }
                }
                when (uiState.selectedTab) {
                    ContactTab.CallHistory -> {
                        if (uiState.filteredCallLogs.isEmpty()) {
                            item { EmptyState("Chưa có lịch sử cuộc gọi") }
                        } else {
                            if (uiState.todayCallLogs.isNotEmpty()) {
                                item {
                                    Text(
                                        "Hôm nay",
                                        color = Color.Gray,
                                        modifier = Modifier.padding(8.dp),
                                        fontSize = 12.sp
                                    )
                                }
                                items(uiState.todayCallLogs) { log ->
                                    HistoryContactItem(
                                        groupedCallLog = log,
                                        onCallClick = { handleCallRequest(it.phoneNumber, it.contactName) },
                                        onReportClick = { request ->
                                            reportViewModel.submitReport(request)
                                        },
                                        onDelete = {log ->
                                            viewModel.deleteCallLog(log.id)
                                        }
                                    )
                                }
                            }

                            if (uiState.yesterdayCallLogs.isNotEmpty()) {
                                item {
                                    Text(
                                        "Hôm qua",
                                        color = Color.Gray,
                                        modifier = Modifier.padding(8.dp),
                                        fontSize = 12.sp
                                    )
                                }
                                items(uiState.yesterdayCallLogs) { log ->
                                    HistoryContactItem(
                                        groupedCallLog = log,
                                        onCallClick = { handleCallRequest(it.phoneNumber, it.contactName) },
                                        onReportClick = { request ->
                                            reportViewModel.submitReport(request)
                                        },
                                        onDelete = {log ->
                                            viewModel.deleteCallLog(log.id)
                                        }
                                    )
                                }
                            }

                            if (uiState.olderCallLogs.isNotEmpty()) {
                                item {
                                    Text(
                                        "Cũ hơn",
                                        color = Color.Gray,
                                        modifier = Modifier.padding(8.dp),
                                        fontSize = 12.sp
                                    )
                                }
                                items(uiState.olderCallLogs) { log ->
                                    HistoryContactItem(
                                        groupedCallLog = log,
                                        onCallClick = { handleCallRequest(it.phoneNumber, it.contactName) },
                                        onReportClick = { request ->
                                            reportViewModel.submitReport(request)
                                        },
                                        onDelete = {log ->
                                            viewModel.deleteCallLog(log.id)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    ContactTab.Contacts -> {
                        if (uiState.filteredContacts.isEmpty()) {
                            item { EmptyState("Chưa có liên hệ") }
                        } else {
                            items(uiState.filteredContacts) { contact ->
                                ContactItem(contact) { selected ->
                                    handleCallRequest(selected.phoneNumber, selected.name)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    uiState.scamAlert?.let { alert ->
        ScamWarningDialog(
            alert = alert,
            onDismiss = viewModel::dismissScamAlert,
            onContinue = viewModel::confirmScamCall
        )
    }
}

fun isDefaultDialer(context: Context): Boolean {
    val telecomManager =
        context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    return telecomManager.defaultDialerPackage == context.packageName
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = Color.Gray, fontSize = 16.sp)
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

@Composable
private fun ScamWarningDialog(
    alert: ScamAlert,
    onDismiss: () -> Unit,
    onContinue: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF453A),
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Cảnh báo lừa đảo",
                    color = Color(0xFFFF453A),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Số ${alert.phoneNumber} đã bị báo cáo ${alert.count} lần.", color = Color.White)
                Text("Trạng thái: ${alert.status}", color = Color.White)
                alert.lastReport?.let { Text("Báo cáo gần nhất: $it", color = Color.Gray) }
            }
        },
        confirmButton = {
            TextButton(onClick = onContinue) {
                Text("Tiếp tục gọi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Huỷ")
            }
        },
        containerColor = Color(0xFF1C1C1E),
        textContentColor = Color.White
    )
}

private fun launchCallIntent(context: Context, phoneNumber: String) {
    try {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = "tel:$phoneNumber".toUri()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Không thể thực hiện cuộc gọi", Toast.LENGTH_SHORT).show()
    }
}
