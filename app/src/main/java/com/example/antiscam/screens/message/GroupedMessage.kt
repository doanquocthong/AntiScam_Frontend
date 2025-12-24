package com.example.antiscam.screens.message

data class GroupedMessage(
    val address: String,
    val lastMessage: String,
    val lastTimestamp: Long,
    val messageCount: Int,
    val isScam: Boolean,
    val isSentByUser: Boolean,
    val unReadCount: Int,
    val isRead: Boolean
)
