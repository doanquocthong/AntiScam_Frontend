package com.example.antiscam.screens.report

import com.example.antiscam.data.model.response.ReportResponse

data class ReportUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val response: ReportResponse? = null
)