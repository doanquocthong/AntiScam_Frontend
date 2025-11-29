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

@Composable
fun MessageItem(
    address: String,
    latestMessage: String,
    latestTime: Long,
    onClick: () -> Unit
) {
    fun formatTimestamp(timestamp: Long): String {
        val formatter = SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }
    val formattedDate = formatTimestamp(latestTime)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(Color(0xFF1C1C1E))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {

            // Số điện thoại
            Text(
                text = address,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            // Nội dung tin nhắn cuối
            Text(
                text = latestMessage,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Thời gian
        Text(
            text = formattedDate,
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}
