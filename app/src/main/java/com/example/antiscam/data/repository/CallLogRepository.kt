package com.example.antiscam.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CallLog as SystemCallLog
import androidx.core.content.ContextCompat
import com.example.antiscam.data.dao.CallLogDao
import com.example.antiscam.data.database.AppDatabase
import com.example.antiscam.data.model.CallLog
import kotlinx.coroutines.flow.Flow
import kotlin.math.absoluteValue

//CẦN LÀM: 1. Chỉ đọc callLog 1 lần duy nhất khi tải app, khi có cuộc gọi đến thì lưu vào database
class CallLogRepository(context: Context) {
    
    // Khởi tạo database ngay lập tức để đảm bảo sẵn sàng
    private val database: AppDatabase = try {
        AppDatabase.getDatabase(context)
    } catch (e: Exception) {
        android.util.Log.e("CallLogRepository", "Error initializing database", e)
        throw e
    }
    
    private val callLogDao: CallLogDao = try {
        database.callLogDao()
    } catch (e: Exception) {
        android.util.Log.e("CallLogRepository", "Error getting CallLogDao", e)
        throw e
    }
    
    fun getAllCallLogs(): Flow<List<CallLog>> {
        return callLogDao.getAllCallLogs()
    }
    
    fun getCallLogsByPhoneNumber(phoneNumber: String): Flow<List<CallLog>> {
        return callLogDao.getCallLogsByPhoneNumber(phoneNumber)
    }

    suspend fun insertCallLog(callLog: CallLog) {
        try {
            callLogDao.insertCallLog(callLog)
            android.util.Log.d("CallLogRepository", "CallLog inserted successfully: ${callLog.phoneNumber}")
        } catch (e: Exception) {
            android.util.Log.e("CallLogRepository", "Error inserting CallLog: ${callLog.phoneNumber}", e)
            throw e
        }
    }
    
    suspend fun insertCallLogs(callLogs: List<CallLog>) {
        callLogDao.insertCallLogs(callLogs)
    }
    
    suspend fun deleteCallLog(id: Int) {
        callLogDao.deleteCallLog(id)
    }
    
    suspend fun deleteAllCallLogs() {
        callLogDao.deleteAllCallLogs()
    }
    
    /**
     * Đọc CallLog từ hệ thống và sync vào database
     */
    suspend fun syncFromSystemCallLog(context: Context) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALL_LOG
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        try {
//            // Xóa hết lịch sử cũ trước khi đồng bộ
//            callLogDao.deleteAllCallLogs()

            val resolver = context.contentResolver
            val cursor = resolver.query(
                SystemCallLog.Calls.CONTENT_URI,
                arrayOf(
                    SystemCallLog.Calls.NUMBER,
                    SystemCallLog.Calls.CACHED_NAME,
                    SystemCallLog.Calls.TYPE,
                    SystemCallLog.Calls.DATE,
                    SystemCallLog.Calls.DURATION
                ),
                null,
                null,
                "${SystemCallLog.Calls.DATE} DESC"
            )

            val colors = listOf(
                0xFF3D3B8E,
                0xFF2C5F2D,
                0xFF7B3F00,
                0xFF5A189A,
                0xFF1E6091,
                0xFF9A031E,
                0xFF364F6B
            )

            cursor?.use {
                val numberIdx = it.getColumnIndexOrThrow(SystemCallLog.Calls.NUMBER)
                val nameIdx = it.getColumnIndexOrThrow(SystemCallLog.Calls.CACHED_NAME)
                val typeIdx = it.getColumnIndexOrThrow(SystemCallLog.Calls.TYPE)
                val dateIdx = it.getColumnIndexOrThrow(SystemCallLog.Calls.DATE)
                val durationIdx = it.getColumnIndexOrThrow(SystemCallLog.Calls.DURATION)

                val callLogsToInsert = mutableListOf<CallLog>()

                while (it.moveToNext()) {
                    val number = it.getString(numberIdx) ?: continue
                    val name = it.getString(nameIdx)
                    val type = it.getInt(typeIdx)
                    val date = it.getLong(dateIdx)
                    val duration = it.getInt(durationIdx)

                    val callType = when (type) {
                        SystemCallLog.Calls.OUTGOING_TYPE -> "OUTGOING"
                        SystemCallLog.Calls.INCOMING_TYPE -> "INCOMING"
                        SystemCallLog.Calls.MISSED_TYPE -> "MISSED"
                        else -> "OUTGOING"
                    }

                    val colorIndex = number.hashCode().absoluteValue % colors.size
                    val avatarColor = colors[colorIndex].toInt()

                    val callLog = CallLog(
                        phoneNumber = number,
                        contactName = name,
                        callType = callType,
                        timestamp = date,
                        duration = duration,
                        isScam = false,
                        avatarColor = avatarColor
                    )

                    callLogsToInsert.add(callLog)
                }

                if (callLogsToInsert.isNotEmpty()) {
                    callLogDao.insertCallLogs(callLogsToInsert)
                    android.util.Log.d("CallLogRepository", "Synced ${callLogsToInsert.size} call logs from system")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CallLogRepository", "Error syncing from system call log", e)
        }
    }


    suspend fun insertFromSystem(
        phoneNumber: String,
        callType: Int,
        timestamp: Long,
        duration: Long,
        isScam: Boolean,
    ) {
        val callTypeStr = when (callType) {
            SystemCallLog.Calls.OUTGOING_TYPE -> "OUTGOING"
            SystemCallLog.Calls.INCOMING_TYPE -> "INCOMING"
            SystemCallLog.Calls.MISSED_TYPE -> "MISSED"
            else -> "OUTGOING"
        }

        val colors = listOf(
            0xFF3D3B8E,
            0xFF2C5F2D,
            0xFF7B3F00,
            0xFF5A189A,
            0xFF1E6091,
            0xFF9A031E,
            0xFF364F6B
        )

        val avatarColor = colors[
            phoneNumber.hashCode().absoluteValue % colors.size
        ].toInt()
        android.util.Log.d(
            "CallLogRepository",
            "isScam = $isScam",

        )
        val callLog = CallLog(
            phoneNumber = phoneNumber,
            contactName = null,          // system chưa resolve
            callType = callTypeStr,
            timestamp = timestamp,
            duration = duration.toInt(),
            isScam = isScam,
            avatarColor = avatarColor
        )

        try {
            callLogDao.insertCallLog(callLog)
            android.util.Log.d(
                "CallLogRepository",
                "Inserted new system call: $phoneNumber - $callTypeStr"
            )
        } catch (e: Exception) {
            android.util.Log.e(
                "CallLogRepository",
                "Duplicate or error inserting system call",
                e
            )
        }
    }
}

