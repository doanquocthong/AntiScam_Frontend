package com.example.antiscam.screens.contact

import com.example.antiscam.data.model.Contact
import com.example.antiscam.data.model.GroupedCallLog
import com.example.antiscam.data.model.enums.ContactTab

data class ContactUiState(
    val contacts: List<Contact> = emptyList(),
    val groupedCallLogs: List<GroupedCallLog> = emptyList(),
    val filteredContacts: List<Contact> = emptyList(),
    val filteredCallLogs: List<GroupedCallLog> = emptyList(),

    val todayCallLogs: List<GroupedCallLog> = emptyList(),
    val yesterdayCallLogs: List<GroupedCallLog> = emptyList(),
    val olderCallLogs: List<GroupedCallLog> = emptyList(),

    val searchQuery: String = "",
    val selectedTab: ContactTab = ContactTab.CallHistory,
    val isCheckingScam: Boolean = false,
    val isSyncingCallLogs: Boolean = false,
    val scamAlert: ScamAlert? = null
)