package com.example.antiscam.screens.message

import MessageItem
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.antiscam.data.repository.MessageRepository
import com.example.antiscam.screens.contact.FilterChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    onOpenMessageDetail: (String) -> Unit
) {

    // ---------- ViewModel ----------
    val context = LocalContext.current
    val messageRepository = remember { MessageRepository(context) }

    val viewModel: MessageViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MessageViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return MessageViewModel(messageRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    // ---------- Sync once ----------
//    LaunchedEffect(Unit) {
//        viewModel.syncMessagesFromSystem(context)
//    }

    // ---------- UI ----------
    Scaffold(
        containerColor = Color.Black,
        topBar = {
            Column(
                modifier = Modifier
                    .background(Color(0xFF1C1C1E))
                    .padding(top = 20.dp, start = 12.dp, end = 12.dp, bottom = 8.dp)
            ) {

                // Search bar
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
                        placeholder = {
                            Text("Tìm kiếm tin nhắn", color = Color.Gray)
                        },
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

                // Filters (UI only, chưa xử lý logic)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(text = "Đã đọc", selected = true, onClick = {})
                    FilterChip(text = "Chưa đọc", selected = false, onClick = {})
                }
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // Loading
            if (uiState.isSyncing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Empty state
            if (uiState.conversations.isEmpty() && !uiState.isSyncing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Chưa có tin nhắn", color = Color.Gray)
                }
            }

            // List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212)),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.conversations) { conversation ->
                    MessageItem(
                        address = conversation.address,
                        latestMessage = conversation.lastMessage,
                        latestTime = conversation.lastTimestamp,
                        isRead = conversation.isRead,
                        unReadCount = conversation.unReadCount,
                        openMessageDetail = {
                            onOpenMessageDetail(conversation.address)
                        }
                    )
                }
            }
        }
    }
}
