package com.example.antiscam.screens.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class NotificationType {
    SUCCESS,
    WARNING,
    ERROR,
    INFO
}

@Composable
fun Notification(
    message: String,
    type: NotificationType = NotificationType.INFO,
    visible: Boolean = true
) {
    AnimatedVisibility(visible = visible) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .shadow(8.dp, RoundedCornerShape(14.dp))
                .background(
                    color = backgroundColor(type),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon(type),
                contentDescription = null,
                tint = iconColor(type),
                modifier = Modifier.size(22.dp)
            )

            Text(
                text = message,
                color = Color.White,
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/* ---------------- Helpers ---------------- */

private fun backgroundColor(type: NotificationType): Color =
    when (type) {
        NotificationType.SUCCESS -> Color(0xFF1E3A2F)   // xanh đậm
        NotificationType.WARNING -> Color(0xFF3A2F1E)   // cam đậm
        NotificationType.ERROR -> Color(0xFF3A1E1E)     // đỏ đậm
        NotificationType.INFO -> Color(0xFF2C2C2E)      // xám tối
    }

private fun iconColor(type: NotificationType): Color =
    when (type) {
        NotificationType.SUCCESS -> Color(0xFF4CD964)
        NotificationType.WARNING -> Color(0xFFFF9F0A)
        NotificationType.ERROR -> Color(0xFFFF453A)
        NotificationType.INFO -> Color(0xFF64D2FF)
    }

private fun icon(type: NotificationType) =
    when (type) {
        NotificationType.SUCCESS -> Icons.Default.CheckCircle
        NotificationType.WARNING -> Icons.Default.Warning
        NotificationType.ERROR -> Icons.Default.Error
        NotificationType.INFO -> Icons.Default.Info
    }
