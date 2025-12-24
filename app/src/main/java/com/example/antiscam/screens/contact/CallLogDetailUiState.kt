package com.example.antiscam.screens.contact

import com.example.antiscam.data.model.CallLog

data class CallLogDetailUiState(
    val isLoading: Boolean = true,
    val phoneNumber: String = "",
    val callLogs: List<CallLog> = emptyList()
)
