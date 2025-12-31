import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antiscam.data.model.Message
import java.text.SimpleDateFormat
import java.util.*

/**
 * Hiển thị 1 item hội thoại SMS
 */
@Composable
fun MessageItem(
    address: String,
    latestMessage: String,
    latestTime: Long,
    isRead: Boolean,
    unReadCount: Int,
    isScamNumber: Boolean,
    isScamMessage: Boolean,
    openMessageDetail: () -> Unit
) {

    fun formatTimestamp(timestamp: Long): String {
        val formatter = SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    val formattedDate = formatTimestamp(latestTime)

    val backgroundColor =
        if (isScamNumber || isScamMessage)
            Color(0xFF3A1C1C)   // nền cảnh báo
        else
            Color(0xFF2C2C2E)   // nền bình thường

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { openMessageDetail() }
            .padding(horizontal = 18.dp, vertical = 8.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(backgroundColor)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // ===== LEFT CONTENT =====
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            // Số điện thoại
            Text(
                text = address,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            // Badge cảnh báo scam
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isScamNumber && isScamMessage) {
                    ScamBadge(
                        text = "🚫 Cảnh báo nguy hiểm đối với số điện thoại và tin nhắn",
                        background = Color(0xFFD32F2F)
                    )
                } else {
                    if (isScamNumber) {
                        ScamBadge(
                            text = "⚠️ Cẩn thận với số điện thoại này",
                            background = Color(0xFFC62828)
                        )
                    }
                    if (isScamMessage) {
                        ScamBadge(
                            text = "🚨 Cẩn thận với tin nhắn này",
                            background = Color(0xFFF57C00)
                        )
                    }
                }
            }

            // Nội dung tin nhắn cuối
            Text(
                text = latestMessage,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = when {
                    isScamMessage -> Color(0xFFFFCC80) // cam nhạt
                    unReadCount > 0 -> Color.White
                    else -> Color.Gray
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // ===== RIGHT CONTENT =====
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            // Thời gian
            Text(
                text = formattedDate,
                color = if (unReadCount > 0) Color.White else Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )

            // Badge số tin chưa đọc
            if (unReadCount > 0) {
                Surface(
                    color = Color(0xFF2196F3),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = unReadCount.toString(),
                        color = Color(0xFF38383A),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(
                            horizontal = 8.dp,
                            vertical = 2.dp
                        )
                    )
                }
            }
        }
    }
}

/**
 * Badge dùng cho cảnh báo scam
 */
@Composable
fun ScamBadge(
    text: String,
    background: Color,
    contentColor: Color = Color.White
) {
    Surface(
        color = background,
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = 11.sp,
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 3.dp
            )
        )
    }
}
