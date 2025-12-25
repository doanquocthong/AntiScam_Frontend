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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MessageRepository(
    private val appContext: Context
) {

    private val database = AppDatabase.getDatabase(appContext)
    private val messageDao = database.messageDao()


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
    suspend fun syncFromSystemMessages() = withContext(Dispatchers.IO) {

        if (
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.READ_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("MessageRepository", "READ_SMS permission not granted!")
            return@withContext
        }

        val prefs = appContext.getSharedPreferences(
            "sms_prefs",
            Context.MODE_PRIVATE
        )

        val lastSyncedDate = prefs.getLong("last_sms_date", 0L)

        val resolver = appContext.contentResolver
        val smsUri = Telephony.Sms.CONTENT_URI

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
            "${Telephony.Sms.DATE} > ?",
            arrayOf(lastSyncedDate.toString()),
            "${Telephony.Sms.DATE} ASC"
        )

        val messagesToInsert = mutableListOf<Message>()
        var newestDate = lastSyncedDate

        cursor?.use { c ->
            val idIdx = c.getColumnIndexOrThrow(Telephony.Sms._ID)
            val addressIdx = c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIdx = c.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIdx = c.getColumnIndexOrThrow(Telephony.Sms.DATE)
            val typeIdx = c.getColumnIndexOrThrow(Telephony.Sms.TYPE)
            val readIdx = c.getColumnIndexOrThrow(Telephony.Sms.READ)

            while (c.moveToNext()) {
//                val systemId = c.getLong(idIdx)
                val address = c.getString(addressIdx) ?: ""
                val body = c.getString(bodyIdx) ?: ""
                val date = c.getLong(dateIdx)
                val type = c.getInt(typeIdx)
                val isRead = c.getInt(readIdx) == 1

                newestDate = maxOf(newestDate, date)

                messagesToInsert.add(
                    Message(
                        address = address,
                        contactName = null,
                        body = body,
                        date = date,
                        type = type,
                        isScam = false,
                        isSentByUser = type == Telephony.Sms.MESSAGE_TYPE_SENT,
                        isRead = isRead
                    )
                )
            }
        }

        if (messagesToInsert.isNotEmpty()) {
            messageDao.insertMessages(messagesToInsert)

            prefs.edit().putLong("last_sms_date", newestDate).apply()

            Log.d(
                "MessageRepository",
                "Inserted ${messagesToInsert.size} new messages"
            )
        }
    }
    suspend fun insertIncomingSms(
        address: String,
        body: String,
        timestamp: Long
    ) = withContext(Dispatchers.IO) {
        val message = Message(
            address = address,
            contactName = null,
            body = body,
            date = timestamp,
            type = Telephony.Sms.MESSAGE_TYPE_INBOX,
            isScam = false,
            isSentByUser = false,
            isRead = false
        )

        messageDao.insertMessage(message)

        Log.d("MessageRepository", "Inserted incoming SMS from $address")
    }




    fun getMessagesByAddress(address: String): Flow<List<Message>> {
        return messageDao.getMessagesByAddress(address)
    }

    suspend fun markMessageAsRead(address: String) {
        messageDao.markMessageAsRead(address)
    }

    suspend fun deleteMessagesByAddress(address: String) {
        messageDao.deleteMessagesByAddress(address)
        Log.d("MessageRepository", "Deleted messages of $address")
    }

    suspend fun deleteMessageById(messageId: Long) {
        messageDao.deleteMessageById(messageId)
    }

}
