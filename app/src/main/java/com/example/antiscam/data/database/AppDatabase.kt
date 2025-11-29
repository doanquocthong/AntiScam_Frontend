package com.example.antiscam.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.antiscam.data.dao.CallLogDao
import com.example.antiscam.data.dao.MessageDao       // <-- Thêm import MessageDao
import com.example.antiscam.data.model.CallLog
import com.example.antiscam.data.model.Message        // <-- Thêm import Message entity

@Database(
    entities = [CallLog::class, Message::class],    // <-- Thêm Message vào danh sách entities
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun callLogDao(): CallLogDao

    abstract fun messageDao(): MessageDao           // <-- Thêm abstract fun cho MessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "antiscam_database"
                )
                    .fallbackToDestructiveMigration() // Cho phép xóa và tạo lại database nếu schema thay đổi
                    .build()
                INSTANCE = instance
                android.util.Log.d("AppDatabase", "Database initialized successfully")
                instance
            }
        }
    }
}
