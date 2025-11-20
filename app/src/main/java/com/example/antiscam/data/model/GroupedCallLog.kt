package com.example.antiscam.data.model

/**
 * CallLog đã được group theo phoneNumber, hiển thị số lần gọi
 */
data class GroupedCallLog(
    val phoneNumber: String,
    val contactName: String?,
    val callCount: Int,                    // Số lần gọi
    val lastCallTimestamp: Long,          // Thời gian cuộc gọi gần nhất
    val lastCallType: String,             // Loại cuộc gọi gần nhất
    val avatarColor: Int,                  // Mã màu avatar
    val isScam: Boolean = false,          // Có phải số lừa đảo không
    val totalDuration: Int = 0             // Tổng thời lượng tất cả cuộc gọi
)

