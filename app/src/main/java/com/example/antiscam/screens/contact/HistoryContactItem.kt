package com.example.antiscam.screens.contact
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antiscam.data.model.GroupedCallLog
import com.example.antiscam.data.model.request.ReportRequest
import com.example.antiscam.screens.report.ReportUiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryContactItem(
    groupedCallLog: GroupedCallLog,
    reporterPhone: String?,
    onCallClick: (GroupedCallLog) -> Unit = {},
    reportUiState: ReportUiState,
    openCallLogDetail:(String) -> Unit,
    onReportClick: (ReportRequest) -> Unit = {},
    onDelete: (GroupedCallLog) -> Unit, // 👈 THÊM
    onAddContactClick: (GroupedCallLog) -> Unit,
) {
    // Sử dụng màu đã lưu trong groupedCallLog
    val avatarColor = remember(groupedCallLog.avatarColor) {
        if (groupedCallLog.avatarColor != 0) {
            Color(groupedCallLog.avatarColor)
        } else {
            Color(0xFF3D3B8E) // Màu mặc định
        }
    }
    
    val callIcon: ImageVector = when (groupedCallLog.lastCallType) {
        "OUTGOING" -> Icons.Default.CallMade
        "INCOMING" -> Icons.Default.CallReceived
        "MISSED" -> Icons.Default.CallMissed
        else -> Icons.Default.Call
    }
    
    val callIconColor = when (groupedCallLog.lastCallType) {
        "OUTGOING" -> Color(0xFF2ECC71) // Xanh lá
        "INCOMING" -> Color(0xFF3498DB) // Xanh dương
        "MISSED" -> Color(0xFFE74C3C) // Đỏ
        else -> Color.Gray
    }
    
    val callTypeText = when (groupedCallLog.lastCallType) {
        "OUTGOING" -> "Gọi đi"
        "INCOMING" -> "Gọi đến"
        "MISSED" -> "Nhỡ"
        else -> "Không xác định"
    }
    var expanded by remember { mutableStateOf(false) }
    var isSelected by remember { mutableStateOf(false) }
    var isDeleteMode by remember { mutableStateOf(false) }

    val offsetX by animateDpAsState(
        targetValue = if (isDeleteMode) (-72).dp else 0.dp,
        animationSpec = tween(
            durationMillis = 220,
            easing = FastOutSlowInEasing
        ),
        label = "slide_delete"
    )
    val isScamText : String = "Số điện thoại nằm trong danh sách đáng ngờ"

    val timeText = formatTimestamp(groupedCallLog.lastCallTimestamp)
    val durationText = if (groupedCallLog.totalDuration > 0) formatDuration(groupedCallLog.totalDuration) else ""
    val displayName = groupedCallLog.contactName
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?: groupedCallLog.phoneNumber

    val firstChar = displayName
        .trim()
        .firstOrNull()
        ?.takeIf { it.isLetter() }
        ?.uppercase()
        ?: "?"
    val isNotInContacts = groupedCallLog.contactName.isNullOrBlank()

    val reporterPhone = reporterPhone
    Log.d("reporterPhone", "= $reporterPhone")
    var showReportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(
        reportUiState.isSuccess,
        reportUiState.errorMessage
    ) {
        if (
            showReportDialog &&
            (reportUiState.isSuccess || reportUiState.errorMessage != null)
        ) {
            showReportDialog = false
        }
    }

    // A ?: B (If A null, result: B, and vice versa)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {

        // 🔴 Background delete
        if (isDeleteMode) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(MaterialTheme.shapes.medium)
                    .background(Color(0xFFE53935)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Xóa",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(end = 24.dp)
                        .size(26.dp)
                        .clickable {
                            onDelete(groupedCallLog)   // ✅ PHÁT SỰ KIỆN
                            isDeleteMode = false
                        }
                )
            }
        }
        Column(
            modifier = Modifier
                .offset(x = offsetX)
                .clip(MaterialTheme.shapes.medium)
                .background(Color(0xFF1C1C1E))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            if (isDeleteMode) {
                                isDeleteMode = false
                            } else {
                                expanded = !expanded
                            }
                        },
                        onLongPress = {
                            isDeleteMode = true
                        }
                    )
                }
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Log.d("Before if groupedCallLog.isScam", "= ${groupedCallLog.isScam}")
                // Avatar
                if(groupedCallLog.isScam) {
                    Log.d("groupedCallLog.phone", "= ${groupedCallLog.phoneNumber}")
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFB71C1C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Avatar cảnh báo",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                } else {
                    Log.d("groupedCallLog.phone", "= ${groupedCallLog.phoneNumber}")
                    if (isNotInContacts) {
                        // 👤❓ Chưa có trong danh bạ
                        Surface(
                            modifier = Modifier.size(45.dp),
                            shape = CircleShape,
                            color = Color.Black,
                            border = BorderStroke(1.dp, Color(0xFFCBBCBC))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "?",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }

                    } else {
                        Box(
                            modifier = Modifier
                                .size(45.dp)
                                .clip(CircleShape)
                                .background(avatarColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = firstChar,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }

                }
                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = displayName,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium
                        )
                        // Hiển thị số lần gọi nếu > 1
                        if (groupedCallLog.callCount > 1) {
                            Surface(
                                color = Color(0xFF38383A),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "(${groupedCallLog.callCount})",
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = callIcon,
                            contentDescription = null,
                            tint = callIconColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = callTypeText,
                            color = Color(0xFFAAAAAA),
                            fontSize = 13.sp
                        )
                        if (durationText.isNotEmpty()) {
                            Text(
                                text = " • $durationText",
                                color = Color(0xFFAAAAAA),
                                fontSize = 13.sp
                            )
                        }
                    }
                    Text(
                        text = timeText,
                        color = Color(0xFFAAAAAA),
                        fontSize = 13.sp
                    )
                    if (groupedCallLog.isScam) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = Color(0xFFE74C3C).copy(alpha = 0.2f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "Cẩn thận với số điện thoại này",
                                color = Color(0xFFE74C3C),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Xóa",
                        tint = Color.Red,
                        modifier = Modifier
                            .size(26.dp)
                            .clickable {
                                isSelected = false
                            }
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Gọi điện",
                        tint = Color.White,
                        modifier = Modifier
                            .size(26.dp)
                            .clickable { onCallClick(groupedCallLog) }
                    )

                    Spacer(modifier = Modifier.width(10.dp))


                    if (isNotInContacts) {
                        Spacer(modifier = Modifier.width(10.dp))

                        Icon(
                            imageVector = Icons.Default.Report,
                            contentDescription = "Báo cáo",
                            tint = Color(0xFFA6382D),
                            modifier = Modifier
                                .size(26.dp)
                                .clickable { showReportDialog = true }
                        )
                    }

                }

                if (showReportDialog) {
                    ReportDialog(
                        phoneNumber = groupedCallLog.phoneNumber,
                        reporterPhone = reporterPhone,
                        onDismiss = { showReportDialog = false },
                        uiState = reportUiState,
                        onSubmit = { reportRequest ->
                            onReportClick(reportRequest)
                            Log.d("Reported check","Clicked historyContactItem to report, request by HistoryContactItem = $reportRequest")
                        }
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .padding(top = 14.dp)
                        .background(Color(0xFF2C2C2E), shape = MaterialTheme.shapes.medium)
                        .clip(MaterialTheme.shapes.medium)
                ) {
                    //Nếu số không nằm trong danh bạ hiện thêm danh bạ
                    if (isNotInContacts) {
                        ActionItem(Icons.Default.PersonAdd, "Thêm số vào danh bạ", {onAddContactClick(groupedCallLog) })
                        Divider(color = Color.Black.copy(alpha = 0.3f))
                    }
                    ActionItem(Icons.Default.Message, "Tin nhắn", {})
                    Divider(color = Color.Black.copy(alpha = 0.3f))
                    ActionItem(Icons.Default.History, "Nhật ký", {openCallLogDetail(groupedCallLog.phoneNumber)})
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60 * 1000 -> "Vừa xong"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} phút trước"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} giờ trước"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} ngày trước"
        else -> {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return when {
        hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, secs)
        minutes > 0 -> String.format("%d:%02d", minutes, secs)
        else -> "${secs}s"
    }
}

