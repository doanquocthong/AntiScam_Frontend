package com.example.antiscam.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.antiscam.data.dao.MessageDao
import com.example.antiscam.data.database.AppDatabase
import com.example.antiscam.data.model.Message
import kotlinx.coroutines.flow.Flow

class MessageRepository(context: Context) {

    private val database: AppDatabase = AppDatabase.getDatabase(context)
    private val messageDao: MessageDao = database.messageDao()

    fun getAllMessages(): Flow<List<Message>> {
        return messageDao.getAllMessages()
    }

    suspend fun insertMessages(messages: List<Message>) {
        messageDao.insertMessages(messages)
    }

    suspend fun deleteAllMessages() {
        messageDao.deleteAllMessages()
    }

    /**
     * Đồng bộ tin nhắn từ hệ thống vào database Room
     */
    suspend fun syncFromSystemMessages(context: Context) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("MessageRepository", "READ_SMS permission not granted!")
            return
        }

        val messagesToInsert = mutableListOf<Message>()

        try {
            val resolver = context.contentResolver
            val smsUri: Uri = Telephony.Sms.CONTENT_URI

            val cursor = resolver.query(
                smsUri,
                arrayOf(
                    Telephony.Sms._ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE,
                    Telephony.Sms.TYPE,
                    Telephony.Sms.READ
                ),
                null,
                null,
                "${Telephony.Sms.DATE} DESC"
            )

            cursor?.use { c ->

                if (!c.moveToFirst()) {
                    Log.w("MessageRepository", "SMS cursor empty")
                    return
                }

                val addressIdx = c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val bodyIdx = c.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateIdx = c.getColumnIndexOrThrow(Telephony.Sms.DATE)
                val typeIdx = c.getColumnIndexOrThrow(Telephony.Sms.TYPE)
                val readIdx = c.getColumnIndexOrThrow(Telephony.Sms.READ)

                do {
                    val address = c.getString(addressIdx) ?: ""
                    val body = c.getString(bodyIdx) ?: ""
                    val date = c.getLong(dateIdx)
                    val type = c.getInt(typeIdx)
                    val isRead = c.getInt(readIdx) == 1

                    Log.d(
                        "SMS",
                        "address=$address | type=$type | read=$isRead | body=$body"
                    )

                    messagesToInsert.add(
                        Message(
                            address = address,
                            contactName = null,
                            body = body,
                            date = date,
                            type = type,
                            isScam = false,
                            isSentByUser = (type == Telephony.Sms.MESSAGE_TYPE_SENT),
                            isRead = isRead
                        )
                    )

                } while (c.moveToNext())
            }

            if (messagesToInsert.isNotEmpty()) {
                Log.d(
                    "MessageRepository",
                    "Inserting ${messagesToInsert.size} messages into DB"
                )
                messageDao.insertMessages(messagesToInsert)
            } else {
                Log.d("MessageRepository", "No messages found")
            }

        } catch (e: Exception) {
            Log.e("MessageRepository", "Error syncing SMS messages", e)
        }
    }

    fun getMessagesByAddress(address: String): Flow<List<Message>> {
        return messageDao.getMessagesByAddress(address)
    }

    suspend fun markMessageAsRead(address: String) {
        messageDao.markMessageAsRead(address)
    }
}
