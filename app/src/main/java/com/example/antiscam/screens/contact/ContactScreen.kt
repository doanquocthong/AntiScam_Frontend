package com.example.antiscam.screens.contact

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.telecom.TelecomManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.antiscam.data.model.GroupedCallLog
import com.example.antiscam.data.model.PendingCall
import com.example.antiscam.data.model.ScamAlert
import com.example.antiscam.data.model.enums.ContactTab
import com.example.antiscam.data.repository.CallLogRepository
import com.example.antiscam.data.repository.ContactRepository
import com.example.antiscam.data.repository.ScamCheckRepository
import com.example.antiscam.screens.auth.AuthViewModel
import com.example.antiscam.screens.components.Notification
import com.example.antiscam.screens.components.NotificationType
import com.example.antiscam.screens.report.ReportViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.core.content.edit

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(
    openCallLogDetail: (String) -> Unit,
) {
    val context = LocalContext.current

//    // Theo dõi trạng thái app làm default dialer
//    var isDefaultDialer by remember { mutableStateOf(isDefaultDialer(context)) }
////
//    // Launcher để mở màn hình đổi ứng dụng gọi mặc định và nhận kết quả
//    val defaultDialerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.StartActivityForResult()
//    ) {
//        // Cập nhật lại trạng thái dialer mặc định sau khi user chọn xong
//        isDefaultDialer = isDefaultDialer(context)
//    }

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
    val snackbarHostState = remember { SnackbarHostState() }
    var notificationType by remember {
        mutableStateOf(NotificationType.SUCCESS)
    }
    val authViewModel: AuthViewModel =
        viewModel(LocalContext.current as ComponentActivity)

    val reporterPhone by authViewModel.reporterPhone.collectAsState()

    Log.d("reporterPhone - ContactScreen", "Current default = $reporterPhone")
    val prefs = remember {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    var firstCallLogSyncDone by remember {
        mutableStateOf(
            prefs.getBoolean("first_calllog_sync_done", false)
        )
    }
    var showDialPad by remember { mutableStateOf(false) }



    var hasCallLogPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALL_LOG
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val callLogPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasCallLogPermission = granted
            if (!granted) {
                Toast.makeText(
                    context,
                    "Cần quyền truy cập lịch sử cuộc gọi",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    LaunchedEffect(viewModel) {
        if (!hasCallLogPermission) {
            callLogPermissionLauncher.launch(
                Manifest.permission.READ_CALL_LOG
            )
        }
    }

    LaunchedEffect(hasCallLogPermission) {
        if (hasCallLogPermission && !firstCallLogSyncDone) {
            Log.d("CallLogSync", "🔄 First time sync CallLog")
            viewModel.syncCallLogs() {
                prefs.edit { putBoolean("first_calllog_sync_done", true) }
                firstCallLogSyncDone = true
            }

        }
    }


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
                        Unit
                    is ContactEffect.ContactAddFailed -> {
                        notificationType = NotificationType.ERROR
                        snackbarHostState.showSnackbar("Lỗi không xác định")
                    }

                    ContactEffect.ContactAddedSuccess -> {
                        notificationType = NotificationType.SUCCESS
                        snackbarHostState.showSnackbar("Đã thêm vào danh bạ")
                    }

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    LaunchedEffect(
        reportUiState.isSuccess,
        reportUiState.errorMessage
    ) {
        when {
            reportUiState.isSuccess -> {
                notificationType = NotificationType.SUCCESS
                snackbarHostState.showSnackbar(
                    "Báo cáo đã được gửi đi"
                )
                reportViewModel.resetState()
            }

            reportUiState.errorMessage != null -> {
                notificationType = NotificationType.ERROR
                snackbarHostState.showSnackbar(
                    reportUiState.errorMessage!!
                )
                reportViewModel.resetState()
            }
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
//
//    // Hàm gọi intent yêu cầu đổi ứng dụng gọi mặc định
//    fun requestDefaultDialer() {
//        val telecomManager =
//            context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
//
//        Log.d("DialerCheck", "Current default = ${telecomManager.defaultDialerPackage}")
//        Log.d("DialerCheck", "My package = ${context.packageName}")
//
//        val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
//            putExtra(
//                TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
//                context.packageName
//            )
//        }
//        defaultDialerLauncher.launch(intent)
//    }

    var showAddContactDialog by remember { mutableStateOf(false) }
    var selectedCallLog by remember { mutableStateOf<GroupedCallLog?>(null) }
    val writeContactPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted && selectedCallLog != null) {
                viewModel.addContactToPhoneBook(
                    name = selectedCallLog!!.contactName ?: "",
                    phoneNumber = selectedCallLog!!.phoneNumber
                )
            } else {
                Toast.makeText(context, "Cần quyền ghi danh bạ", Toast.LENGTH_SHORT).show()
            }

            showAddContactDialog = false
            selectedCallLog = null
        }
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Black,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Notification(
                        message = data.visuals.message,
                        type = notificationType
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showDialPad = true }
                ) {
                    Icon(Icons.Default.Call, null)
                }

            },
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
                    //                if (isDefaultDialer) {
                    //                    item {
                    //                        Card(
                    //                            colors = CardDefaults.cardColors(
                    //                                containerColor = Color(0xFF2C2C2E)
                    //                            ),
                    //                            modifier = Modifier
                    //                                .padding(12.dp)
                    //                                .fillMaxWidth()
                    //                        ) {
                    //                            Row(
                    //                                modifier = Modifier.padding(12.dp),
                    //                                verticalAlignment = Alignment.CenterVertically
                    //                            ) {
                    //                                Icon(
                    //                                    Icons.Default.Warning,
                    //                                    contentDescription = null,
                    //                                    tint = Color(0xFFFF9F0A)
                    //                                )
                    //                                Spacer(modifier = Modifier.width(8.dp))
                    //                                Column(modifier = Modifier.weight(1f)) {
                    //                                    Text(
                    //                                        text = "Cần đặt làm ứng dụng gọi mặc định",
                    //                                        color = Color.White,
                    //                                        fontSize = 14.sp
                    //                                    )
                    //                                    Text(
                    //                                        text = "Để phát hiện & cảnh báo cuộc gọi lừa đảo",
                    //                                        color = Color.Gray,
                    //                                        fontSize = 12.sp
                    //                                    )
                    //                                }
                    //                                TextButton(onClick = { requestDefaultDialer() }) {
                    //                                    Text("Đặt ngay")
                    //                                }
                    //                            }
                    //                        }
                    //                    }
                    //                }
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
                                            openCallLogDetail = openCallLogDetail,
                                            reporterPhone = reporterPhone,
                                            onCallClick = {
                                                handleCallRequest(
                                                    it.phoneNumber,
                                                    it.contactName
                                                )
                                            },
                                            onReportClick = { request ->
                                                reportViewModel.submitReport(request)
                                                Log.d(
                                                    "Reported check",
                                                    "Clicked historyContactItem to report by ContactScreen, request = $request"
                                                )
                                            },
                                            reportUiState = reportUiState,
                                            onDelete = { log ->
                                                viewModel.deleteCallLog(log.id)
                                            },
                                            onAddContactClick = {
                                                selectedCallLog = it
                                                showAddContactDialog = true
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
                                            reporterPhone = reporterPhone,
                                            openCallLogDetail = openCallLogDetail,
                                            onCallClick = {
                                                handleCallRequest(
                                                    it.phoneNumber,
                                                    it.contactName
                                                )
                                            },
                                            onReportClick = { request ->
                                                reportViewModel.submitReport(request)
                                            },
                                            reportUiState = reportUiState,
                                            onDelete = { log ->
                                                viewModel.deleteCallLog(log.id)
                                            },
                                            onAddContactClick = {
                                                selectedCallLog = it
                                                showAddContactDialog = true
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
                                            openCallLogDetail = openCallLogDetail,
                                            reporterPhone = reporterPhone,
                                            onCallClick = {
                                                handleCallRequest(
                                                    it.phoneNumber,
                                                    it.contactName
                                                )
                                            },
                                            onReportClick = { request ->
                                                reportViewModel.submitReport(request)
                                            },
                                            reportUiState = reportUiState,
                                            onDelete = { log ->
                                                viewModel.deleteCallLog(log.id)
                                            },
                                            onAddContactClick = {
                                                selectedCallLog = it
                                                showAddContactDialog = true
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
                                    ContactItem(contact, openCallLogDetail) { selected ->
                                        handleCallRequest(selected.phoneNumber, selected.name)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (showAddContactDialog && selectedCallLog != null) {
                AddContactDialog(
                    phoneNumber = selectedCallLog!!.phoneNumber,
                    initialName = selectedCallLog!!.contactName,
                    onDismiss = {
                        showAddContactDialog = false
                        selectedCallLog = null
                    },
                    onConfirm = { name ->
                        if (
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.WRITE_CONTACTS
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            viewModel.addContactToPhoneBook(
                                name = name,
                                phoneNumber = selectedCallLog!!.phoneNumber
                            )

                            showAddContactDialog = false
                            selectedCallLog = null
                        } else {
                            // lưu tạm name nếu cần
                            selectedCallLog = selectedCallLog!!.copy(contactName = name)
                            writeContactPermissionLauncher.launch(Manifest.permission.WRITE_CONTACTS)
                        }
                    }

                )
            }

        }

        uiState.scamAlert?.let { alert ->
            ScamWarningDialog(
                alert = alert,
                onDismiss = viewModel::dismissScamAlert,
                onContinue = viewModel::confirmScamCall
            )
        }
        // 2️⃣ DialPad overlay (ĐẶT Ở ĐÂY)
        if (showDialPad) {
            DialPadBottomSheet(
                onDismiss = { showDialPad = false },
                onCall = { phone ->
                    showDialPad = false
                    handleCallRequest(phone, null)
                }
            )
        }
    }
}

@Composable
fun DialPadBottomSheet(
    onDismiss: () -> Unit,
    onCall: (String) -> Unit
) {
    var phone by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)) // nền mờ
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() } // bấm ngoài để đóng
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .heightIn(max = 520.dp) // 👈 KHÔNG che hết
                .clip(MaterialTheme.shapes.extraLarge)
                .background(Color(0xFF1C1C1E))
                .padding(16.dp)
                .pointerInput(Unit) {},
                horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ⬆️ Thanh kéo
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = phone.ifBlank { "Nhập số" },
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.Center)
                )

                IconButton(
                    onClick = {
                        if (phone.isNotEmpty()) phone = phone.dropLast(1)
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Backspace,
                        contentDescription = "Delete",
                        tint = Color.White
                    )
                }
            }



            Spacer(Modifier.height(16.dp))

            DialPad(
                onNumberClick = { phone += it },
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { if (phone.isNotBlank()) onCall(phone) },
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2ECC71)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call",
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }

        }
    }
}

@Composable
fun DialPad(
    onNumberClick: (String) -> Unit,
) {
    val keys = listOf(
        listOf("1","2","3"),
        listOf("4","5","6"),
        listOf("7","8","9"),
        listOf("*","0","#")
    )

    Column {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    DialKey(key) { onNumberClick(key) }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
@Composable
fun DialKey(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color(0xFF2C2C2E))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 24.sp)
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ScamWarningDialog(
    alert: ScamAlert,
    onDismiss: () -> Unit,
    onContinue: () -> Unit
) {
    AlertDialog(
        modifier = Modifier.border(
            width = 1.dp,
            color = Color(0xFFDACACA),
            shape = MaterialTheme.shapes.small
        ),
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C1E),
        textContentColor = Color.White,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFB71C1C),
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    text = "Cảnh báo lừa đảo",
                    color = Color(0xFFB71C1C),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                // 🔴 Banner cảnh báo
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color(0xFFB71C1C),
                            shape = MaterialTheme.shapes.small
                        ),
                    color = Color(0xFFB71C1C).copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Số điện thoại này đã bị nhiều người báo cáo là lừa đảo.",
                        color = Color(0xFFB71C1C),
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                // ☎️ Số điện thoại
                InfoRow(
                    label = "Số điện thoại",
                    value = alert.phoneNumber,
                    valueColor = Color.White
                )

                // 📊 Số lần báo cáo
                InfoRow(
                    label = "Số lần bị báo cáo",
                    value = "${alert.count} lần",
                    valueColor = Color(0xFFFF9F0A) // vàng cảnh báo
                )

                // 🟠 Trạng thái
                InfoRow(
                    label = "Trạng thái",
                    value = alert.status,
                    valueColor = Color(0xFFE74C3C)
                )

                // 🕒 Báo cáo gần nhất
                alert.lastReport?.let {
                    InfoRow(
                        label = "Báo cáo gần nhất",
                        value = formatLastReportTime(it),
                        valueColor = Color.Gray
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB71C1C)
                )
            ) {
                Text("Tiếp tục gọi", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Huỷ", color = Color.White)
            }
        }

    )

}

@RequiresApi(Build.VERSION_CODES.O)
fun formatLastReportTime(rawTime: String): String {
    return try {
        // 🔥 Chuẩn hoá chuỗi: xoá khoảng trắng thừa
        val cleaned = rawTime
            .replace(" ", "") // xoá mọi space
            .let {
                // nếu chưa có timezone thì thêm Z
                if (it.endsWith("Z") || it.contains("+")) it
                else it + "Z"
            }

        val instant = Instant.parse(cleaned)

        val formatter = DateTimeFormatter
            .ofPattern("dd/MM/yyyy · HH:mm")
            .withLocale(Locale("vi", "VN"))
            .withZone(ZoneId.systemDefault())

        formatter.format(instant)

    } catch (e: Exception) {
        Log.e("TimeFormat", "Parse failed: $rawTime", e)
        rawTime
    }
}


@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Column {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
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
