package com.example.antiscam.screens.contact

import android.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antiscam.data.model.Contact
@Composable
fun ContactItem(
    contact: Contact,
    openCallLogDetail: (String) -> Unit,
    onCallRequested: (Contact) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val avatarColor = remember(contact.avatarColor) {
        Color(contact.avatarColor)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(Color(0xFF1C1C1E))
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {

            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.firstOrNull()?.uppercase() ?: "?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Di động • ${contact.phoneNumber}",
                    color = Color(0xFFAAAAAA),
                    fontSize = 13.sp
                )
            }

            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Gọi điện",
                tint = Color.White,
                modifier = Modifier
                    .size(26.dp)
                    .clickable { onCallRequested(contact) }
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .padding(top = 14.dp)
                    .background(Color(0xFF2C2C2E), shape = MaterialTheme.shapes.medium)
                    .clip(MaterialTheme.shapes.medium)
            ) {
                ActionItem(Icons.Default.Message, "Tin nhắn", {})
                Divider(color = Color.Black.copy(alpha = 0.3f))
                ActionItem(Icons.Default.History, "Nhật ký", { openCallLogDetail(contact.phoneNumber) })
            }
        }
    }
}

@Composable
fun ActionItem(icon: ImageVector, text: String, onclick:() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable{onclick()}
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, color = Color.White, fontSize = 15.sp)
    }
}

