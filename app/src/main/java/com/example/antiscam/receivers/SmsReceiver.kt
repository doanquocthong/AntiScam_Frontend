package com.example.antiscam.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.antiscam.di.ServiceLocator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isEmpty()) return

        val message = messages[0]

        val address = message.originatingAddress ?: return
        val body = message.messageBody ?: ""
        val timestamp = message.timestampMillis

        CoroutineScope(Dispatchers.IO).launch {
            ServiceLocator.messageRepository.insertIncomingSms(
                address = address,
                body = body,
                timestamp = timestamp
            )
        }
    }
}
