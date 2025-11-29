package com.example.antiscam.screens.call

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
    val scamInfo: ScamCheckResponse? = null
)

class CallScreenViewModel(
    private val scamCheckRepository: ScamCheckRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallScreenUiState())
    val uiState: StateFlow<CallScreenUiState> = _uiState.asStateFlow()

    fun loadScamInfo(phoneNumber: String) {
        if (phoneNumber.isBlank()) {
            _uiState.update { it.copy(scamInfo = null, isChecking = false) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isChecking = true) }
            val response = scamCheckRepository.checkPhoneNumber(phoneNumber)
            _uiState.update {
                it.copy(
                    isChecking = false,
                    scamInfo = response
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



