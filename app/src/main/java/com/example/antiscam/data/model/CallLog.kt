package com.example.antiscam.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
/**
 * Đại diện cho một cuộc gọi trong hệ thống.
 * Có thể lấy từ nhật ký cuộc gọi (CallLog.Calls)
 */
@Entity(
    tableName = "call_logs",
    indices = [
        Index(value = ["phoneNumber", "timestamp"], unique = true)
    ]
)
data class CallLog (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val phoneNumber: String,           // Số điện thoại gọi đến / đi
    val contactName: String? = null,   // Tên trong danh bạ (nếu có)
    val callType: String,              // Loại: "INCOMING", "OUTGOING", "MISSED"
    val timestamp: Long,               // Thời gian cuộc gọi (epoch millis)
    val duration: Int = 0,             // Thời lượng (giây)
    val isScam: Boolean = false,       // Có phải số nghi ngờ lừa đảo không
    val note: String? = null,          // Ghi chú (nếu người dùng đánh dấu)
    val avatarColor: Int = 0           // Mã màu avatar (ARGB)
)