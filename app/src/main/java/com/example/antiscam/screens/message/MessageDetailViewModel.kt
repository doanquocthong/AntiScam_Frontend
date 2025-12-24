package com.example.antiscam.screens.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.antiscam.data.model.Message
import com.example.antiscam.data.repository.MessageRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MessageDetailViewModel(
    private val address: String,
    private val messageRepository: MessageRepository
) : ViewModel() {

    val messages: StateFlow<List<Message>> =
        messageRepository
            .getMessagesByAddress(address)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    init {
        markConversationAsRead()
    }

    private fun markConversationAsRead() {
        viewModelScope.launch {
            messageRepository.markMessageAsRead(address)
        }
    }
}
