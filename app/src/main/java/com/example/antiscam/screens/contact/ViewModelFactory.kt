package com.example.antiscam.screens.contact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.antiscam.data.repository.CallLogRepository
import com.example.antiscam.data.repository.ContactRepository

class ViewModelFactory(
    private val contactRepository: ContactRepository,
    private val callLogRepository: CallLogRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ContactViewModel::class.java) -> {
                ContactViewModel(contactRepository) as T
            }
            modelClass.isAssignableFrom(CallLogViewModel::class.java) -> {
                CallLogViewModel(callLogRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

