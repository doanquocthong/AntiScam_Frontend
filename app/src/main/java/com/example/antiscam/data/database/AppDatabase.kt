package com.example.antiscam.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.antiscam.data.dao.CallLogDao
import com.example.antiscam.data.model.CallLog

@Database(
    entities = [CallLog::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun callLogDao(): CallLogDao
    
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

