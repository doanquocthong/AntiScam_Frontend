package com.example.antiscam.di

import android.content.Context
import com.example.antiscam.data.repository.CallLogRepository
import com.example.antiscam.data.repository.ContactRepository
import com.example.antiscam.data.repository.MessageRepository
import com.example.antiscam.data.repository.ScamCheckRepository

object ServiceLocator {

    private var appContext: Context? = null

    val callLogRepository: CallLogRepository by lazy {
        requireNotNull(appContext) {
            "ServiceLocator.init(context) must be called before using repositories"
        }
        CallLogRepository(appContext!!)
    }

    val messageRepository: MessageRepository by lazy {
        requireNotNull(appContext) {
            "ServiceLocator.init(context) must be called before using repositories"
        }
        MessageRepository(appContext!!)
    }

    fun init(context: Context) {
        appContext = context.applicationContext
    }
}

