package com.example.antiscam.screens.contact
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.antiscam.data.model.Contact
import kotlin.math.absoluteValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.net.toUri
import kotlin.math.absoluteValue

@Composable
fun ContactItem(contact: Contact, callLogViewModel: CallLogViewModel? = null) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    
    // Xử lý permission và thực hiện cuộc gọi
    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            makePhoneCall(context, contact.phoneNumber, contact.name, callLogViewModel)
        }
    }

    // Sử dụng màu đã lưu trong contact
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

            // Avatar
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

            // Icon Call với logic gọi điện
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Gọi điện",
                tint = Color.White,
                modifier = Modifier
                    .size(26.dp)
                    .clickable {
                        // Kiểm tra permission trước khi gọi
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CALL_PHONE
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            makePhoneCall(context, contact.phoneNumber, contact.name, callLogViewModel)
                        } else {
                            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                        }
                    }
            )
        }

        // Nếu expanded thì show các lựa chọn
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .padding(top = 14.dp)
                    .background(Color(0xFF2C2C2E), shape = MaterialTheme.shapes.medium)
                    .clip(MaterialTheme.shapes.medium)
            ) {
                ActionItem(Icons.Default.Message, "Tin nhắn")
                Divider(color = Color.Black.copy(alpha = 0.3f))
                ActionItem(Icons.Default.History, "Nhật ký")
            }
        }

    }
}
// Hàm helper để thực hiện cuộc gọi
private fun makePhoneCall(
    context: android.content.Context, 
    phoneNumber: String,
    contactName: String? = null,
    callLogViewModel: CallLogViewModel? = null
) {
    try {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = "tel:$phoneNumber".toUri()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        // Không lưu CallLog ở đây - sẽ được đọc từ hệ thống CallLog sau khi cuộc gọi kết thúc
    } catch (e: Exception) {
        android.util.Log.e("ContactItem", "Error making phone call", e)
        e.printStackTrace()
    }
}

@Composable
fun ActionItem(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
