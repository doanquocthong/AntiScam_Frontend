package com.example.antiscam.di

import android.content.Context
import com.example.antiscam.data.repository.CallLogRepository
import com.example.antiscam.data.repository.ContactRepository
import com.example.antiscam.data.repository.ScamCheckRepository

object ServiceLocator {
    private lateinit var appContext: Context
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    val callLogRepository: CallLogRepository by lazy {
        CallLogRepository(appContext)
    }
}
