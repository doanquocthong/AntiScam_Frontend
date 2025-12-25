package com.example.antiscam.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log

class SmsSendService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val uri = intent?.data ?: return START_NOT_STICKY
        val scheme = uri.scheme

        // android.intent.action.RESPOND_VIA_MESSAGE
        if (scheme == "smsto" || scheme == "sms") {
            val address = uri.schemeSpecificPart
            val body = intent.getStringExtra(Intent.EXTRA_TEXT)

            if (!address.isNullOrEmpty() && !body.isNullOrEmpty()) {
                try {
                    SmsManager.getDefault()
                        .sendTextMessage(address, null, body, null, null)

                    Log.d("SmsSendService", "SMS sent to $address")
                } catch (e: Exception) {
                    Log.e("SmsSendService", "Send SMS failed", e)
                }
            }
        }

        stopSelf()
        return START_NOT_STICKY
    }
}
