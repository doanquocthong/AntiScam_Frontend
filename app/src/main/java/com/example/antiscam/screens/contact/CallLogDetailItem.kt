package com.example.antiscam.screens.contact

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antiscam.data.model.CallLog

@Composable
fun CallLogDetailItem(callLog: CallLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C2C2E)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // 🔹 Dòng trên: Icon + trạng thái
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (callLog.callType) {
                        "INCOMING" -> Icons.Default.CallReceived
                        "OUTGOING" -> Icons.Default.CallMade
                        "MISSED" -> Icons.Default.CallMissed
                        else -> Icons.Default.Call
                    },
                    contentDescription = null,
                    tint = when (callLog.callType) {
                        "MISSED" -> Color(0xFFE74C3C)
                        else -> Color(0xFF0A84FF)
                    },
                    modifier = Modifier
                        .padding(end = 10.dp)
                )

                Text(
                    text = when (callLog.callType) {
                        "OUTGOING" -> "Gọi đi"
                        "INCOMING" -> "Gọi đến"
                        "MISSED" -> "Cuộc gọi nhỡ"
                        else -> "Không xác định"
                    },
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // 🔹 Dòng dưới: thời gian + thời lượng
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDate(callLog.timestamp),
                    color = Color.Gray,
                    fontSize = 13.sp
                )

                Text(
                    text = "${callLog.duration} giây",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
        }
    }
}

