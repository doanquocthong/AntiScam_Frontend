package com.example.antiscam.observer

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.util.Log
import com.example.antiscam.di.ServiceLocator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsObserver(
    private val context: Context
) : ContentObserver(Handler(Looper.getMainLooper())) {

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)

        Log.d(TAG, "SMS database changed")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val cursor = context.contentResolver.query(
                    Telephony.Sms.CONTENT_URI,
                    arrayOf(
                        Telephony.Sms._ID,
                        Telephony.Sms.ADDRESS,
                        Telephony.Sms.BODY,   // 🔥 THÊM
                        Telephony.Sms.DATE,
                        Telephony.Sms.READ
                    ),
                    null,
                    null,
                    "${Telephony.Sms.DATE} DESC"
                )


                cursor?.use { c ->
                    if (!c.moveToFirst()) return@launch

                    val address =
                        c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)) ?: return@launch

                    val body =
                        c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY)) ?: ""

                    val timestamp =
                        c.getLong(c.getColumnIndexOrThrow(Telephony.Sms.DATE))

                    val isRead =
                        c.getInt(c.getColumnIndexOrThrow(Telephony.Sms.READ)) == 1


                    Log.d(
                        TAG,
                        "Latest SMS → address=$address read=$isRead"
                    )

                    // 🔥 Update DB local
                    ServiceLocator.messageRepository.insertIncomingSms(
                        address = address,
                        body = body,
                        timestamp = timestamp
                    )

                }

            } catch (e: Exception) {
                Log.e(TAG, "Read SMS failed", e)
            }
        }
    }

    companion object {
        private const val TAG = "SmsObserver"
        private var observer: SmsObserver? = null

        fun register(context: Context) {
            if (observer != null) return

            observer = SmsObserver(context.applicationContext)

            context.contentResolver.registerContentObserver(
                Telephony.Sms.CONTENT_URI,
                true,
                observer!!
            )

            Log.d(TAG, "SMS Observer registered")
        }
    }
}
