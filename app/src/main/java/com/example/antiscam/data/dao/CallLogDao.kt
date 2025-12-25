package com.example.antiscam.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.antiscam.data.model.CallLog
import kotlinx.coroutines.flow.Flow

@Dao
interface CallLogDao {
    
    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC")
    fun getAllCallLogs(): Flow<List<CallLog>>
    
    @Query("SELECT * FROM call_logs WHERE phoneNumber = :phoneNumber ORDER BY timestamp DESC")
    fun getCallLogsByPhoneNumber(phoneNumber: String): Flow<List<CallLog>>

//    Nếu conflict khi thêm dòng mới và bị trùng KEY sẽ REPLACE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLog(callLog: CallLog)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLogs(callLogs: List<CallLog>)
    
    @Query("DELETE FROM call_logs WHERE id = :id")
    suspend fun deleteCallLog(id: Int)
    
    @Query("DELETE FROM call_logs")
    suspend fun deleteAllCallLogs()

    @Query("""
        UPDATE call_logs 
        SET contactName = :contactName
        WHERE phoneNumber = :phoneNumber
    """)
    suspend fun updateContactNameByPhone(
        phoneNumber: String,
        contactName: String
    )
}

