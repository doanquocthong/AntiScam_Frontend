package com.example.antiscam.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.example.antiscam.observer.CallLogObserver

//PhoneStateReceiver cho biết: Điện thoại đang rung, cuộc gọi đang bắt đầu, cuộc gọi đã kết thúc trước khi CallLog được ghi vào hệ thống

class PhoneStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

        Log.d("PhoneStateReceiver", "Phone state = $state")

        if (state == TelephonyManager.EXTRA_STATE_IDLE) {
            Log.d("PhoneStateReceiver", "Call ended → observe CallLog")

            CallLogObserver.register(context)
        }
    }
}
