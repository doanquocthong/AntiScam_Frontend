package com.example.antiscam.observer

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.util.Log
import com.example.antiscam.data.model.response.ScamCheckResponse
import com.example.antiscam.data.repository.ScamCheckRepository
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

                cursor?.use { c ->
                    if (!c.moveToFirst()) return@launch

                    val number =
                        c.getString(c.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                            ?: return@launch

                    val type =
                        c.getInt(c.getColumnIndexOrThrow(CallLog.Calls.TYPE))

                    val date =
                        c.getLong(c.getColumnIndexOrThrow(CallLog.Calls.DATE))

                    val duration =
                        c.getLong(c.getColumnIndexOrThrow(CallLog.Calls.DURATION))

                    Log.d(
                        TAG,
                        "Latest call → number=$number type=$type duration=$duration"
                    )

                    // ✅ Chỉ xử lý khi call đã kết thúc
                    val shouldInsert =
                        type == CallLog.Calls.MISSED_TYPE || duration > 0

                    if (!shouldInsert) {
                        Log.d(TAG, "Call not finished yet → skip insert")
                        return@launch
                    }

                    // ✅ GỌI API CHECK SCAM
                    val scamCheckRepository = ScamCheckRepository()

                    val response = scamCheckRepository.checkPhoneNumber(number)
                    Log.d(TAG, "response = ${response}")
                    // ✅ UNWRAP ApiResponse → Boolean
                    val isScam = response?.data?.isScam ?: false

                    Log.d(TAG, "Scam check result → isScam=$isScam")

                    // ✅ INSERT DB
                    callLogRepository.insertFromSystem(
                        phoneNumber = number,
                        callType = type,
                        timestamp = date,
                        duration = duration,
                        isScam = isScam
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
