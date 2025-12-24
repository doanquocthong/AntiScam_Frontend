package com.example.antiscam.screens.contact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.antiscam.data.repository.CallLogRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class CallLogDetailViewModel(
    private val phoneNumber: String,
    callLogRepository: CallLogRepository
) : ViewModel() {

    val uiState: StateFlow<CallLogDetailUiState> =
        callLogRepository
            .getCallLogsByPhoneNumber(phoneNumber)
            .map { logs ->
                CallLogDetailUiState(
                    isLoading = false,
                    phoneNumber = phoneNumber,
                    callLogs = logs
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = CallLogDetailUiState(
                    isLoading = true,
                    phoneNumber = phoneNumber
                )
            )
}

class CallLogDetailViewModelFactory(
    private val phoneNumber: String,
    private val repository: CallLogRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CallLogDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CallLogDetailViewModel(phoneNumber, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
