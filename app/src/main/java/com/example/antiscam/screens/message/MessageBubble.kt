package com.example.antiscam.screens.message

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antiscam.data.model.Message

@Composable
fun MessageBubble(
    message: Message,
    scanState: ScanState,
    onScanClick: () -> Unit
) {
    val isMe = message.isSentByUser

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {

        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {

            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .background(
                        color = when (scanState) {
                            ScanState.SCAM -> Color(0xFF3A1C1C)
                            else -> if (isMe) Color(0xFF4CAF50) else Color(0xFF2C2C2E)
                        },
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(12.dp)
            ) {

                Column {

                    // ===== Nội dung tin nhắn =====
                    Text(
                        text = message.body,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // ===== Scan state =====
                    // ===== Scan state (chỉ hiện nếu KHÔNG phải tin nhắn của mình) =====
                    if (!isMe) {
                        when (scanState) {
                            ScanState.IDLE -> {
                                ScanButton(onScanClick)
                            }

                            ScanState.SCANNING -> {
                                ScanningIndicator()
                            }

                            ScanState.SAFE -> {
                                ResultBadge(
                                    text = "✔ An toàn",
                                    background = Color(0xFF2E7D32)
                                )
                            }

                            ScanState.SCAM -> {
                                ResultBadge(
                                    text = "🚫 Lừa đảo",
                                    background = Color(0xFFD32F2F)
                                )
                            }
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun ScanningIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(Color.Cyan.copy(alpha = alpha), CircleShape)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = "AI đang quét...",
            fontSize = 12.sp,
            color = Color.Cyan.copy(alpha = alpha)
        )
    }
}

@Composable
fun ScanButton(onClick: () -> Unit) {
    Text(
        text = "🔍 Quét tin nhắn",
        fontSize = 12.sp,
        color = Color(0xFF64B5F6),
        modifier = Modifier
            .clickable { onClick() }
            .padding(top = 4.dp)
    )
}
@Composable
fun ResultBadge(
    text: String,
    background: Color
) {
    Surface(
        color = background,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}


