package com.example.antiscam

import android.app.Application
import android.util.Log
import com.example.antiscam.data.repository.CallLogRepository
import com.example.antiscam.di.ServiceLocator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.content.edit
import com.example.antiscam.observer.SmsObserver
import kotlin.math.log

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("MyApp", "Application onCreate")
        ServiceLocator.init(this)
        SmsObserver.register(this)
//        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
//        val firstSyncDone = prefs.getBoolean("first_sync_done", false)
//
//        Log.d("MyAppPrefs", "first_sync_done = $firstSyncDone")
//
//        if (!firstSyncDone) {
//            Log.d("MyAppPrefs", "⏳ Sync lần đầu – đọc toàn bộ CallLog")
//
//            CoroutineScope(Dispatchers.IO).launch {
//                ServiceLocator.callLogRepository.syncFromSystemCallLog(this@MyApp)
//                ServiceLocator.messageRepository.syncFromSystemMessages(this@MyApp)
//                prefs.edit { putBoolean("first_sync_done", true) }
//
//                Log.d("MyAppPrefs", "✔ Sync xong – đã set first_sync_done = true")
//            }
//        } else {
//            Log.d("MyAppPrefs", "⏭ Bỏ qua sync – đã chạy trước đó")
//        }
    }
}


