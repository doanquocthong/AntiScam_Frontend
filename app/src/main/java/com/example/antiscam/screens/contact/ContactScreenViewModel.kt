package com.example.antiscam.screens.contact

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.antiscam.data.model.Contact
import com.example.antiscam.data.model.CallLog
import com.example.antiscam.data.model.GroupedCallLog
import com.example.antiscam.data.repository.CallLogRepository
import com.example.antiscam.data.repository.ContactRepository
import com.example.antiscam.data.repository.ScamCheckRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContactScreenViewModel(
    private val contactRepository: ContactRepository,
    private val callLogRepository: CallLogRepository,
    private val scamCheckRepository: ScamCheckRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactUiState())
    val uiState: StateFlow<ContactUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ContactEffect>()
    val effects: SharedFlow<ContactEffect> = _effects.asSharedFlow()

    private var pendingScamAlert: ScamAlert? = null

    init {
        observeCallLogs()
    }

    fun loadContacts() {
        viewModelScope.launch {
            val contacts = contactRepository.getAllContacts()
            _uiState.update { state ->
                state.updateFilters(
                    contacts = contacts
                )
            }
        }
    }

    private fun observeCallLogs() {
        viewModelScope.launch {
            callLogRepository.getAllCallLogs().collect { logs ->
                _uiState.update { state ->
                    state.updateFilters(
                        groupedLogs = logs.toGrouped()
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            state.copy(searchQuery = query).applyFilters()
        }
    }

    fun onTabSelected(tab: ContactTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun syncCallLogs(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingCallLogs = true) }
            runCatching { callLogRepository.syncFromSystemCallLog(context) }
                .onFailure { _effects.emit(ContactEffect.ShowToast("Không thể đồng bộ lịch sử cuộc gọi")) }
            _uiState.update { it.copy(isSyncingCallLogs = false) }
        }
    }

    fun requestCall(phoneNumber: String, contactName: String?) {
        if (_uiState.value.isCheckingScam) return
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingScam = true, scamAlert = null) }
            val response = scamCheckRepository.checkPhoneNumber(phoneNumber)
            if (response == null) {
                _effects.emit(ContactEffect.ShowToast("Không thể kiểm tra số điện thoại"))
                _effects.emit(ContactEffect.StartCall(phoneNumber))
                _uiState.update { it.copy(isCheckingScam = false) }
                return@launch
            }

            if (response.reported) {
                val alert = ScamAlert(
                    phoneNumber = response.phone,
                    contactName = contactName,
                    count = response.count,
                    status = response.status,
                    lastReport = response.lastReport
                )
                pendingScamAlert = alert
                _uiState.update {
                    it.copy(
                        isCheckingScam = false,
                        scamAlert = alert
                    )
                }
            } else {
                _uiState.update { it.copy(isCheckingScam = false) }
                _effects.emit(ContactEffect.StartCall(phoneNumber))
            }
        }
    }

    fun dismissScamAlert() {
        pendingScamAlert = null
        _uiState.update { it.copy(scamAlert = null) }
    }

    fun confirmScamCall() {
        val alert = pendingScamAlert ?: return
        viewModelScope.launch {
            _effects.emit(ContactEffect.StartCall(alert.phoneNumber))
            pendingScamAlert = null
            _uiState.update { it.copy(scamAlert = null) }
        }
    }

    private fun ContactUiState.updateFilters(
        contacts: List<Contact> = this.contacts,
        groupedLogs: List<GroupedCallLog> = this.groupedCallLogs
    ): ContactUiState {
        val filteredContacts = filterContacts(contacts, searchQuery)
        val filteredLogs = filterLogs(groupedLogs, searchQuery)
        return copy(
            contacts = contacts,
            groupedCallLogs = groupedLogs,
            filteredContacts = filteredContacts,
            filteredCallLogs = filteredLogs
        )
    }

    private fun ContactUiState.applyFilters(): ContactUiState =
        updateFilters(contacts, groupedCallLogs)

    companion object {
        private fun filterContacts(contacts: List<Contact>, query: String): List<Contact> {
            if (query.isBlank()) return contacts
            return contacts.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.phoneNumber.contains(query, ignoreCase = true)
            }
        }

        private fun filterLogs(logs: List<GroupedCallLog>, query: String): List<GroupedCallLog> {
            if (query.isBlank()) return logs
            return logs.filter {
                (it.contactName ?: "").contains(query, ignoreCase = true) ||
                        it.phoneNumber.contains(query, ignoreCase = true)
            }
        }
    }
}

class ContactScreenViewModelFactory(
    private val contactRepository: ContactRepository,
    private val callLogRepository: CallLogRepository,
    private val scamCheckRepository: ScamCheckRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactScreenViewModel::class.java)) {
            return ContactScreenViewModel(contactRepository, callLogRepository, scamCheckRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

data class ContactUiState(
    val contacts: List<Contact> = emptyList(),
    val groupedCallLogs: List<GroupedCallLog> = emptyList(),
    val filteredContacts: List<Contact> = emptyList(),
    val filteredCallLogs: List<GroupedCallLog> = emptyList(),
    val searchQuery: String = "",
    val selectedTab: ContactTab = ContactTab.CallHistory,
    val isCheckingScam: Boolean = false,
    val isSyncingCallLogs: Boolean = false,
    val scamAlert: ScamAlert? = null
)

enum class ContactTab { CallHistory, Contacts }

data class ScamAlert(
    val phoneNumber: String,
    val contactName: String?,
    val count: Long,
    val status: String,
    val lastReport: String?
)

sealed interface ContactEffect {
    data class StartCall(val phoneNumber: String) : ContactEffect
    data class ShowToast(val message: String) : ContactEffect
}

data class PendingCall(
    val phoneNumber: String,
    val contactName: String?
)

private fun List<GroupedCallLog>.filter(query: String, selector: (GroupedCallLog) -> String): List<GroupedCallLog> {
    if (query.isBlank()) return this
    return filter { selector(it).contains(query, ignoreCase = true) }
}

private fun List<CallLog>.toGrouped(): List<GroupedCallLog> =
    groupBy { it.phoneNumber }
        .map { (phone, logs) ->
            val sorted = logs.sortedByDescending { it.timestamp }
            val last = sorted.first()
            GroupedCallLog(
                phoneNumber = phone,
                contactName = last.contactName,
                callCount = logs.size,
                lastCallTimestamp = last.timestamp,
                lastCallType = last.callType,
                avatarColor = last.avatarColor,
                isScam = logs.any { it.isScam },
                totalDuration = logs.sumOf { it.duration }
            )
        }
        .sortedByDescending { it.lastCallTimestamp }

