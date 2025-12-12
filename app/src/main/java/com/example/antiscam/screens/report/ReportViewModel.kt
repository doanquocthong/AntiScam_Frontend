package com.example.antiscam.screens.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.antiscam.data.model.request.ReportRequest
import com.example.antiscam.data.model.response.ReportResponse
import com.example.antiscam.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReportUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val response: ReportResponse? = null
)

class ReportViewModel(
    private val reportRepository: ReportRepository = ReportRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState = _uiState.asStateFlow()

    fun submitReport(request: ReportRequest) {
        _uiState.value = ReportUiState(isLoading = true)

        viewModelScope.launch {
            val result = reportRepository.reportPhoneNumber(request)

            if (result != null) {
                // Thành công
                _uiState.value = ReportUiState(
                    isLoading = false,
                    isSuccess = true,
                    response = result
                )
            } else {
                // Lỗi
                _uiState.value = ReportUiState(
                    isLoading = false,
                    isSuccess = false,
                    errorMessage = "Không thể gửi báo cáo! Vui lòng thử lại."
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = ReportUiState()
    }
}
