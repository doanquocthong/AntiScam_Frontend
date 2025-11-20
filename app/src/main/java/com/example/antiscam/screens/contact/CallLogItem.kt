package com.example.antiscam.screens.contact

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.antiscam.data.model.GroupedCallLog
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CallLogItem(groupedCallLog: GroupedCallLog, onCallClick: (String) -> Unit = {}) {
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
    
    val timeText = formatTimestamp(groupedCallLog.lastCallTimestamp)
    val durationText = if (groupedCallLog.totalDuration > 0) formatDuration(groupedCallLog.totalDuration) else ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(Color(0xFF1C1C1E))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape)
                .background(avatarColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = (groupedCallLog.contactName ?: groupedCallLog.phoneNumber).firstOrNull()?.uppercase() ?: "?",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = groupedCallLog.contactName ?: groupedCallLog.phoneNumber,
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
        }

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(start = 8.dp)
        ) {

            if (groupedCallLog.isScam) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = Color(0xFFE74C3C).copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Lừa đảo",
                        color = Color(0xFFE74C3C),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Icon(
            imageVector = Icons.Default.Call,
            contentDescription = "Gọi điện",
            tint = Color.White,
            modifier = Modifier
                .size(26.dp)
                .clickable {
                    onCallClick(groupedCallLog.phoneNumber)
                }
        )
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

