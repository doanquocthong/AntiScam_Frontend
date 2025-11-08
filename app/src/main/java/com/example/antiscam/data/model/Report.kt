package com.example.antiscam.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Lưu thông tin báo cáo của người dùng về số/tin nhắn nghi ngờ lừa đảo.
 * Dữ liệu này có thể gửi lên server Spring Boot để thống kê chung.
 */
@Entity(tableName = "reports")
data class Report(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val phoneNumber: String?,          // Số bị báo cáo (nếu là cuộc gọi)
    val messageContent: String?,       // Nội dung tin nhắn (nếu là SMS)
    val reason: String,                // Lý do người dùng chọn (ví dụ: "Giả mạo ngân hàng")
    val reportType: String,            // "CALL" hoặc "SMS"
    val timestamp: Long,               // Thời gian gửi báo cáo
    val userNote: String? = null       // Ghi chú thêm của người dùng
)