package com.example.antiscam.screens.contact

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.antiscam.data.model.CallLog
import com.example.antiscam.data.model.GroupedCallLog
import com.example.antiscam.data.repository.CallLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CallLogViewModel(private val repository: CallLogRepository) : ViewModel() {
    
    private val _callLogs = MutableStateFlow<List<CallLog>>(emptyList())
    val callLogs: StateFlow<List<CallLog>> = _callLogs.asStateFlow()
    
    // Grouped call logs - các cuộc gọi đã được gộp theo phoneNumber
    val groupedCallLogs: StateFlow<List<GroupedCallLog>> = _callLogs.map { logs ->
        groupCallLogs(logs)
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    init {
        // Delay một chút để đảm bảo database đã sẵn sàng
        viewModelScope.launch {
            kotlinx.coroutines.delay(100) // Delay 100ms
            loadCallLogs()
        }
    }
    
    private fun loadCallLogs() {
        viewModelScope.launch {
            try {
                repository.getAllCallLogs().collect { logs ->
                    _callLogs.value = logs
                }
            } catch (e: Exception) {
                android.util.Log.e("CallLogViewModel", "Error loading call logs", e)
                e.printStackTrace()
                // Nếu có lỗi, giữ danh sách rỗng
                _callLogs.value = emptyList()
            }
        }
    }
    
    fun insertCallLog(callLog: CallLog) {
        viewModelScope.launch {
            try {
                repository.insertCallLog(callLog)
                // Log để debug
                android.util.Log.d("CallLogViewModel", "CallLog inserted: ${callLog.phoneNumber}")
            } catch (e: Exception) {
                android.util.Log.e("CallLogViewModel", "Error inserting CallLog", e)
                e.printStackTrace()
            }
        }
    }
    
    fun deleteCallLog(id: Int) {
        viewModelScope.launch {
            repository.deleteCallLog(id)
        }
    }
    
    /**
     * Sync CallLog từ hệ thống
     */
    fun syncFromSystem(context: Context) {
        viewModelScope.launch {
            try {
                repository.syncFromSystemCallLog(context)
            } catch (e: Exception) {
                android.util.Log.e("CallLogViewModel", "Error syncing from system", e)
            }
        }
    }
    
    /**
     * Group các cuộc gọi theo phoneNumber
     */
    private fun groupCallLogs(logs: List<CallLog>): List<GroupedCallLog> {
        val grouped = logs.groupBy { it.phoneNumber }
        
        return grouped.map { (phoneNumber, callLogs) ->
            val sortedLogs = callLogs.sortedByDescending { it.timestamp }
            val lastCall = sortedLogs.first()
            
            GroupedCallLog(
                phoneNumber = phoneNumber,
                contactName = lastCall.contactName,
                callCount = callLogs.size,
                lastCallTimestamp = lastCall.timestamp,
                lastCallType = lastCall.callType,
                avatarColor = lastCall.avatarColor,
                isScam = callLogs.any { it.isScam },
                totalDuration = callLogs.sumOf { it.duration }
            )
        }.sortedByDescending { it.lastCallTimestamp }
    }
}

