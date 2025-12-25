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

/**
 * Repository chỉ nhận APPLICATION CONTEXT
 * ❌ Không nhận Activity / Compose context
 */
class CallLogRepository(
    private val appContext: Context
) {

    // Luôn dùng applicationContext để tránh leak
    private val database: AppDatabase = AppDatabase.getDatabase(appContext)
    private val callLogDao: CallLogDao = database.callLogDao()

    /* ------------------ QUERY ------------------ */

    fun getAllCallLogs(): Flow<List<CallLog>> =
        callLogDao.getAllCallLogs()

    fun getCallLogsByPhoneNumber(phoneNumber: String): Flow<List<CallLog>> =
        callLogDao.getCallLogsByPhoneNumber(phoneNumber)

    /* ------------------ INSERT / UPDATE ------------------ */

    suspend fun insertCallLog(callLog: CallLog) {
        callLogDao.insertCallLog(callLog)
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

    suspend fun updateContactName(phoneNumber: String, name: String) {
        callLogDao.updateContactNameByPhone(phoneNumber, name)
    }

    /* ------------------ SYNC SYSTEM CALL LOG ------------------ */

    /**
     * Đọc CallLog từ hệ thống và sync vào database
     * ⚠️ Chỉ gọi khi đã có READ_CALL_LOG
     */
    suspend fun syncFromSystemCallLog() {
        if (ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.READ_CALL_LOG
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        try {
            val resolver = appContext.contentResolver

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

                    val avatarColor = colors[
                        number.hashCode().absoluteValue % colors.size
                    ].toInt()

                    callLogsToInsert.add(
                        CallLog(
                            phoneNumber = number,
                            contactName = name,
                            callType = callType,
                            timestamp = date,
                            duration = duration,
                            isScam = false,
                            avatarColor = avatarColor
                        )
                    )
                }

                if (callLogsToInsert.isNotEmpty()) {
                    callLogDao.insertCallLogs(callLogsToInsert)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(
                "CallLogRepository",
                "Error syncing system call log",
                e
            )
        }
    }

    /* ------------------ INSERT REAL-TIME SYSTEM CALL ------------------ */

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

        val callLog = CallLog(
            phoneNumber = phoneNumber,
            contactName = null,
            callType = callTypeStr,
            timestamp = timestamp,
            duration = duration.toInt(),
            isScam = isScam,
            avatarColor = avatarColor
        )

        callLogDao.insertCallLog(callLog)
    }
}
