package com.example.antiscam.screens.message

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
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
    private val _scanStates: SnapshotStateMap<Long, ScanState> =
        mutableStateMapOf()

    val scanStates: Map<Long, ScanState> = _scanStates

    init {
        markConversationAsRead()
    }

    private fun markConversationAsRead() {
        viewModelScope.launch {
            messageRepository.markMessageAsRead(address)
        }
    }

    fun scanMessage(message: Message) {
        val messageId = message.id ?: return

        // Nếu đang scan thì không scan lại
        if (_scanStates[messageId] == ScanState.SCANNING) return

        _scanStates[messageId] = ScanState.SCANNING

        viewModelScope.launch {

            val response =
                messageRepository
                    .scamCheckRepository
                    .checkMessage(
                        com.example.antiscam.data.model.request.ScamPredictRequest(
                            text = message.body
                        )
                    )

            _scanStates[messageId] =
                if (response?.data?.label == "scam")
                    ScanState.SCAM
                else
                    ScanState.SAFE
        }
    }

}
