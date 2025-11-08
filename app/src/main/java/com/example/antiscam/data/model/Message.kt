package com.example.antiscam.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Đại diện cho một tin nhắn SMS trong hệ thống.
 * Có thể lấy từ ứng dụng nhắn tin mặc định qua ContentResolver.
 */
@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val address: String,               // Số điện thoại gửi / nhận
    val body: String,                  // Nội dung tin nhắn
    val date: Long,                    // Thời gian nhận tin nhắn
    val type: String,                  // "INBOX" hoặc "SENT"
    val isSpam: Boolean = false,       // Có bị phát hiện là spam không
    val detectedKeywords: String? = null // Các từ khóa spam phát hiện được
)