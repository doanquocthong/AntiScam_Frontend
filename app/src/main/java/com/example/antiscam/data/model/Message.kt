package com.example.antiscam.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val address: String,          // Số điện thoại gửi/nhận
    val contactName: String?,     // Tên liên hệ nếu có
    val body: String,             // Nội dung tin nhắn
    val date: Long,               // Thời gian gửi/nhận
    val type: Int,                // Loại tin nhắn: 1 = inbox, 2 = sent, 3 = draft,...
    val isScam: Boolean = false,   // Cờ đánh dấu tin nhắn lừa đảo (mặc định false)
    val isSentByUser: Boolean // true nếu là tin nhắn do user gửi, false nếu nhận
)
