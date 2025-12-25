package com.example.antiscam.screens.message

import android.provider.Telephony
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.antiscam.data.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.antiscam.data.repository.MessageRepository
import kotlinx.coroutines.flow.distinctUntilChanged

private const val TAG = "MessageViewModel"

data class MessageScreenUiState(
    val conversations: List<GroupedMessage> = emptyList(),
    val isSyncing: Boolean = false,
    val errorMessage: String? = null
)

class MessageViewModel(
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessageScreenUiState())
    val uiState: StateFlow<MessageScreenUiState> = _uiState.asStateFlow()

    init {
        Log.d(TAG, "init() → observeMessages()")
        observeMessages()
    }

    private fun observeMessages() {
        viewModelScope.launch {
            Log.d(TAG, "Start collecting messages")
            messageRepository
                .getAllMessages()
                .distinctUntilChanged()
                .collect { msgs ->
                Log.d(TAG, "Collected messages: ${msgs.size}")

                // Log thử 3 tin đầu
                msgs.take(3).forEach {
                    Log.d(
                        TAG,
                        "Msg: id=${it.id}, address=${it.address}, date=${it.date}, body=${it.body.take(30)}"
                    )
                }

                val grouped = msgs
                    .groupBy { it.address }
                    .map { (address, messages) ->

                        Log.d(
                            TAG,
                            "Group [$address] → ${messages.size} messages"
                        )

                        val latest = messages.maxByOrNull { it.date }!!

                        Log.d(
                            TAG,
                            "Latest in [$address] → date=${latest.date}, body=${latest.body.take(30)}"
                        )

                        GroupedMessage(
                            address = latest.address,
                            lastMessage = latest.body,
                            lastTimestamp = latest.date,
                            messageCount = messages.size,
                            isScam = messages.any { it.isScam },
                            isRead = latest.isRead,
                            unReadCount = messages.count {
                                !it.isRead && !it.isSentByUser
                            },
                            isSentByUser = latest.isSentByUser,
                        )
                    }
                    .sortedByDescending { it.lastTimestamp }

                Log.d(TAG, "Grouped conversations: ${grouped.size}")

                _uiState.update {
                    Log.d(TAG, "Update uiState.conversations")
                    it.copy(conversations = grouped)
                }
            }
        }
    }


    fun syncMessagesFromSystem() {
        viewModelScope.launch {
            Log.d(TAG, "syncMessagesFromSystem() START")

            _uiState.update {
                it.copy(isSyncing = true, errorMessage = null)
            }

            try {
                messageRepository.syncFromSystemMessages()
                Log.d(TAG, "syncMessagesFromSystem() SUCCESS")
            } catch (e: Exception) {
                Log.e(TAG, "syncMessagesFromSystem() ERROR", e)

                _uiState.update {
                    it.copy(errorMessage = "Lỗi đồng bộ tin nhắn")
                }
            } finally {
                _uiState.update {
                    it.copy(isSyncing = false)
                }
            }
        }
    }

    fun addMessage(
        address: String,
        body: String,
        timestamp: Long = System.currentTimeMillis(),
        isSentByUser: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                if (isSentByUser) {
                    messageRepository.insertMessages(
                        listOf(
                            Message(
                                address = address,
                                contactName = null,
                                body = body,
                                date = timestamp,
                                type = Telephony.Sms.MESSAGE_TYPE_SENT,
                                isScam = false,
                                isSentByUser = true,
                                isRead = true
                            )
                        )
                    )
                } else {
                    messageRepository.insertIncomingSms(
                        address = address,
                        body = body,
                        timestamp = timestamp
                    )
                }

                Log.d(TAG, "addMessage SUCCESS → $address")

            } catch (e: Exception) {
                Log.e(TAG, "addMessage ERROR", e)
            }
        }
    }


}
