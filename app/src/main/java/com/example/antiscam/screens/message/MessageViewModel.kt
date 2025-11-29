package com.example.antiscam.screens.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.antiscam.data.model.Message
import com.example.antiscam.data.repository.MessageRepository

data class MessageScreenUiState(
    val messages: List<Message> = emptyList(),
    val isSyncing: Boolean = false,
    val errorMessage: String? = null
)

class MessageViewModel(
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessageScreenUiState())
    val uiState: StateFlow<MessageScreenUiState> = _uiState.asStateFlow()

    init {
        observeMessages()
    }

    private fun observeMessages() {
        viewModelScope.launch {
            messageRepository.getAllMessages().collect { msgs ->
                _uiState.update { it.copy(messages = msgs) }
            }
        }
    }

    fun syncMessagesFromSystem(context: android.content.Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, errorMessage = null) }
            try {
                messageRepository.syncFromSystemMessages(context)
                _uiState.update { it.copy(isSyncing = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSyncing = false, errorMessage = "Lỗi đồng bộ tin nhắn") }
            }
        }
    }
}