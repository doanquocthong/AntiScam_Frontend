package com.example.antiscam.screens.message

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.antiscam.data.repository.MessageRepository
import com.example.antiscam.screens.auth.AuthViewModel
import com.example.antiscam.screens.contact.ReportDialog
import com.example.antiscam.screens.report.ReportViewModel

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
//fun MessageDetailScreen(
//    address: String,
//    reporterPhone: String,
//    reportUiState: ReportUiState,
//    onReportClick: (ReportRequest) -> Unit,
//    onBack: () -> Unit
//) {
fun MessageDetailScreen(
    address: String,
    onBack: () -> Unit,
    reportViewModel: ReportViewModel = viewModel()
) {
    val reportUiState = reportViewModel.uiState.collectAsState().value

    //AuthViewModel
    val authViewModel: AuthViewModel =
        viewModel(LocalContext.current as ComponentActivity)
    val reporterPhone by authViewModel.reporterPhone.collectAsState()
    val context = LocalContext.current
    val repository = remember { MessageRepository(context) }

    val viewModel: MessageDetailViewModel = viewModel(
        key = address,
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MessageDetailViewModel(address, repository) as T
            }
        }
    )

    val listState = rememberLazyListState()
    val messages by viewModel.messages.collectAsState()
    var showReportDialog by remember { mutableStateOf(false) }
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.lastIndex)
        }
    }
    LaunchedEffect(
        reportUiState.isSuccess,
    ) {
        if (
            showReportDialog && reportUiState.isSuccess
        ) {
            showReportDialog = false
            reportViewModel.resetState()
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = address,
                            fontSize = 20.sp,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showReportDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Report,
                            contentDescription = "Báo cáo",
                            tint = Color(0xFFA6382D),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1C1C1E)
                )
            )

        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(messages) { msg ->
                MessageBubble(
                    message = msg,
                    scanState = viewModel.scanStates[msg.id] ?: ScanState.IDLE,
                    onScanClick = {
                        viewModel.scanMessage(msg)
                    }
                )
            }
        }
        if (showReportDialog) {
            ReportDialog(
                phoneNumber = address,
                reporterPhone = reporterPhone,
                onDismiss = { showReportDialog = false },
                uiState = reportUiState,
                onSubmit = { reportRequest ->
                    reportViewModel.submitReport(reportRequest)
                    Log.d("Report", "Submit report = $reportRequest")
                }
            )
        }
    }
}
