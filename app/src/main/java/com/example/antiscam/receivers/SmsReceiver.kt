package com.example.antiscam.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.antiscam.di.ServiceLocator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        // 1️⃣ Log ngay khi receiver được gọi
        Log.d("SmsReceiver", "onReceive CALLED")
        Log.d("SmsReceiver", "Intent action = ${intent.action}")

        // 2️⃣ Check action
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            Log.w(
                "SmsReceiver",
                "Wrong action, ignore: ${intent.action}"
            )
            return
        }

        // 3️⃣ Parse SMS
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        Log.d("SmsReceiver", "messages.size = ${messages.size}")

        if (messages.isEmpty()) {
            Log.w("SmsReceiver", "SMS list is empty")
            return
        }

        val sms = messages[0]

        val systemSmsId = sms.timestampMillis
        val address = sms.originatingAddress
        val body = sms.messageBody
        val timestamp = sms.timestampMillis

        Log.d(
            "SmsReceiver",
            """
            SMS RECEIVED
            ├─ systemSmsId = $systemSmsId
            ├─ address     = $address
            ├─ body        = $body
            └─ timestamp   = $timestamp
            """.trimIndent()
        )

        if (address == null || body == null) {
            Log.e("SmsReceiver", "Address or body is NULL → abort")
            return
        }

        // 4️⃣ Gọi repository
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("SmsReceiver", "Calling handleIncomingSms()")

                ServiceLocator
                    .messageRepository
                    .handleIncomingSms(
                        systemSmsId = timestamp,
                        address = address,
                        body = body,
                        timestamp = timestamp
                    )

                Log.d("SmsReceiver", "handleIncomingSms() DONE")

            } catch (e: Exception) {
                Log.e("SmsReceiver", "handleIncomingSms() FAILED", e)
            }
        }
    }
}
