package com.example.antiscam.screens.call_receive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.antiscam.data.model.response.ScamCheckResponse
import com.example.antiscam.data.repository.ScamCheckRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CallScreenUiState(
    val isChecking: Boolean = false,
    val scamInfo: ScamCheckResponse? = null,
    val errorMessage: String? = null
)

class CallScreenViewModel(
    private val scamCheckRepository: ScamCheckRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallScreenUiState())
    val uiState: StateFlow<CallScreenUiState> = _uiState.asStateFlow()

    fun loadScamInfo(phoneNumber: String) {
        if (phoneNumber.isBlank()) {
            _uiState.update { it.copy(scamInfo = null, isChecking = false, errorMessage = null) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isChecking = true, errorMessage = null) }
            val response = scamCheckRepository.checkPhoneNumber(phoneNumber)

            //Backend không trả dữ liệu gì về
            if (response == null) {
                _uiState.update {
                    it.copy(
                        isChecking = false,
                        scamInfo = null,
                        errorMessage = "Không nhận được phản hồi từ máy chủ"
                    )
                }
                return@launch
            }

            //Response trả về các mã lỗi
            if (response.code != 200) {
                _uiState.update {
                    it.copy(
                        isChecking = false,
                        scamInfo = null,
                        errorMessage = response.message ?: "Có lỗi xảy ra"
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isChecking = false,
                    scamInfo = response.data,
                    errorMessage = null
                )
            }
        }
    }
}

class CallScreenViewModelFactory(
    private val scamCheckRepository: ScamCheckRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CallScreenViewModel::class.java)) {
            return CallScreenViewModel(scamCheckRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}



