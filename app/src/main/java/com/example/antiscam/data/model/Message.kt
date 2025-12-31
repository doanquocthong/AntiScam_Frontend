package com.example.antiscam.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    indices = [
        // 🔥 Chống trùng theo ID hệ thống
        Index(
            value = ["systemSmsId"],
            unique = true
        ),

        // 🔥 Fallback chống trùng (nhiều máy)
        Index(
            value = ["address", "date", "body"],
            unique = true
        )
    ]
)

data class Message(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val systemSmsId: Long,   // 🔥 ID từ Telephony.Sms._ID
    val address: String,          // Số điện thoại gửi/nhận
    val contactName: String? = null,     // Tên liên hệ nếu có
    val body: String,             // Nội dung tin nhắn
    val date: Long,               // Thời gian gửi/nhận
    val type: Int,                // Loại tin nhắn: 1 = inbox, 2 = sent, 3 = draft,...
    // 🚨 Kết quả
    val isScamNumber: Boolean? = null,
    val isScamMessage: Boolean? = null,

    // ✅ Trạng thái xử lý
    val isPhoneChecked: Boolean = false,
    val isMessageChecked: Boolean = false,

    val isSentByUser: Boolean = false,
    val isRead: Boolean = false

)
