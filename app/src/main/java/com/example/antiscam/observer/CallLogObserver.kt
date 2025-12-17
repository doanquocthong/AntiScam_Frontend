package com.example.antiscam.observer

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.util.Log
import com.example.antiscam.di.ServiceLocator.callLogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Lắng nghe thay đổi từ System CallLog
// Mỗi khi Android ghi / update CallLog → observer được gọi
class CallLogObserver(
    private val context: Context
) : ContentObserver(Handler(Looper.getMainLooper())) {

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)

        Log.d(TAG, "CallLog changed")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val cursor = context.contentResolver.query(
                    CallLog.Calls.CONTENT_URI,
                    arrayOf(
                        CallLog.Calls.NUMBER,
                        CallLog.Calls.TYPE,
                        CallLog.Calls.DATE,
                        CallLog.Calls.DURATION
                    ),
                    null,
                    null,
                    "${CallLog.Calls.DATE} DESC"
                )

                cursor?.use {
                    if (!it.moveToFirst()) return@launch

                    val number =
                        it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                    val type =
                        it.getInt(it.getColumnIndexOrThrow(CallLog.Calls.TYPE))
                    val date =
                        it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DATE))
                    val duration =
                        it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DURATION))

                    Log.d(
                        TAG,
                        "Latest call → number=$number type=$type duration=$duration"
                    )

                    // ⚠️ QUAN TRỌNG:
                    // CallLog được ghi nhiều lần.
                    // Chỉ insert khi call đã kết thúc:
                    val shouldInsert =
                        type == CallLog.Calls.MISSED_TYPE || duration > 0

                    if (!shouldInsert) {
                        Log.d(TAG, "Call not finished yet → skip insert")
                        return@launch
                    }

                    callLogRepository.insertFromSystem(
                        phoneNumber = number,
                        callType = type,
                        timestamp = date,
                        duration = duration
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Read CallLog failed", e)
            }
        }
    }

    companion object {
        private const val TAG = "CallLogObserver"
        private var observer: CallLogObserver? = null

        /**
         * Gọi 1 lần duy nhất (vd: trong Application.onCreate)
         */
        fun register(context: Context) {
            if (observer != null) return

            observer = CallLogObserver(context.applicationContext)

            context.contentResolver.registerContentObserver(
                CallLog.Calls.CONTENT_URI,
                true,
                observer!!
            )

            Log.d(TAG, "Registered")
        }
    }
}
