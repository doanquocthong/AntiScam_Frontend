import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.antiscam.data.repository.MessageRepository
import com.example.antiscam.screens.contact.FilterChip
import com.example.antiscam.screens.message.MessageViewModel
import kotlin.collections.component1
import kotlin.collections.component2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen() {
    val context = LocalContext.current
    val messageRepository = remember { MessageRepository(context) }
    val viewModel: MessageViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MessageViewModel::class.java)) {
                    return MessageViewModel(messageRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    // Gom nhóm tin nhắn theo địa chỉ (số điện thoại), lấy tin nhắn mới nhất
    val conversations = remember(uiState.messages) {
        uiState.messages
            .groupBy { it.address }
            .map { (address, msgs) ->
                val latest = msgs.maxByOrNull { it.date }!!
                Triple(address, latest.body, latest.date)
            }
            .sortedByDescending { it.third }
    }

    LaunchedEffect(Unit) {
        viewModel.syncMessagesFromSystem(context)
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(Color(0xFF1C1C1E))
                    .padding(top = 20.dp, start = 12.dp, end = 12.dp, bottom = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.large)
                        .background(Color(0xFF2C2C2E))
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Menu, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = "",
                        onValueChange = {},
                        placeholder = { Text("Tìm kiếm tin nhắn", color = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                    Icon(Icons.Default.Mic, contentDescription = null, tint = Color.Gray)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        text = "Đã đọc",
                        selected = true,
                        onClick = {}
                    )
                    FilterChip(
                        text = "Chưa đọc",
                        selected = false,
                        onClick = {}
                    )
                }
            }
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isSyncing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (conversations.isEmpty() && !uiState.isSyncing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Chưa có tin nhắn", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF121212)),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(conversations) { (address, body, time) ->
                        MessageItem(
                            address = address,
                            latestMessage = body,
                            latestTime = time,
                            onClick = {
                                // TODO: mở màn hình chi tiết cuộc hội thoại với address
                            }
                        )
                    }
                }
            }
        }
    }
}