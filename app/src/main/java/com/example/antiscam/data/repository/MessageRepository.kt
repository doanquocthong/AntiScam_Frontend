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
import com.example.antiscam.data.model.request.ScamPredictRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MessageRepository(
    private val appContext: Context
) {

    private val database = AppDatabase.getDatabase(appContext)
    private val messageDao = database.messageDao()
    val scamCheckRepository = ScamCheckRepository()

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

        val resolver = appContext.contentResolver

        val cursor = resolver.query(
            Telephony.Sms.CONTENT_URI,
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
            "${Telephony.Sms.DATE} ASC"
        )

        val messages = mutableListOf<Message>()

        cursor?.use { c ->
            val idIdx = c.getColumnIndexOrThrow(Telephony.Sms._ID)
            val addressIdx = c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIdx = c.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIdx = c.getColumnIndexOrThrow(Telephony.Sms.DATE)
            val typeIdx = c.getColumnIndexOrThrow(Telephony.Sms.TYPE)
            val readIdx = c.getColumnIndexOrThrow(Telephony.Sms.READ)

            while (c.moveToNext()) {
                val systemId = c.getLong(idIdx)

                messages.add(
                    Message(
                        systemSmsId = systemId,
                        address = c.getString(addressIdx) ?: "",
                        contactName = null,
                        body = c.getString(bodyIdx) ?: "",
                        date = c.getLong(dateIdx),
                        type = c.getInt(typeIdx),
                        isScamNumber = false,
                        isScamMessage = false,
                        isSentByUser = c.getInt(typeIdx) == Telephony.Sms.MESSAGE_TYPE_SENT,
                        isRead = c.getInt(readIdx) == 1
                    )
                )
            }
        }

        messageDao.insertMessages(messages)

        Log.d("MessageRepository", "Sync system SMS done: ${messages.size}")
    }

    suspend fun insertIncomingSms(
        systemSmsId: Long,
        address: String,
        body: String,
        timestamp: Long,
        isRead: Boolean
    ) = withContext(Dispatchers.IO) {

        val message = Message(
            systemSmsId = systemSmsId,
            address = address,
            contactName = null,
            body = body,
            date = timestamp,
            type = Telephony.Sms.MESSAGE_TYPE_INBOX,
            isSentByUser = false,
            isRead = isRead
        )

        messageDao.insertMessage(message)

        Log.d("MessageRepository", "Inserted incoming SMS systemId=$systemSmsId")
    }
    fun formatPhoneNumberForApi(rawNumber: String): String {
        var formatted = rawNumber.trim()

        // Nếu bắt đầu bằng +84, thay bằng 0
        if (formatted.startsWith("+84")) {
            formatted = "0" + formatted.removePrefix("+84")
        }

        // Nếu bắt đầu bằng 84 không dấu + (trường hợp khác), cũng thay thành 0
        else if (formatted.startsWith("84")) {
            formatted = "0" + formatted.removePrefix("84")
        }

        // Có thể thêm các xử lý loại bỏ khoảng trắng, dấu, ký tự không hợp lệ nếu cần

        return formatted
    }

    suspend fun handleIncomingSms(
        systemSmsId: Long,
        address: String,
        body: String,
        timestamp: Long
    ) = withContext(Dispatchers.IO) {

        Log.d("MessageRepository", "HandleIncomingSms id=$systemSmsId")

        var message = messageDao.getBySystemSmsId(systemSmsId)

        // 1️⃣ Insert nếu chưa tồn tại
        if (message == null) {
            messageDao.insertMessage(
                Message(
                    systemSmsId = systemSmsId,
                    address = address,
                    body = body,
                    date = timestamp,
                    type = Telephony.Sms.MESSAGE_TYPE_INBOX,
                    isMessageChecked = false,
                    isPhoneChecked = false
                )
            )
            message = messageDao.getBySystemSmsId(systemSmsId)
        }

        if (message == null) {
            Log.e("MessageRepository", "Message not found after insert")
            return@withContext
        }

        // 2️⃣ Check nội dung nếu CHƯA check
        if (!message.isMessageChecked) {
            Log.d("MessageRepository", "Checking MESSAGE scam")

            val response = scamCheckRepository.checkMessage(
                ScamPredictRequest(text = message.body)
            )

            if (response?.data != null) {
                val isScam = response.data.label == "scam"
                messageDao.updateMessageScamResult(systemSmsId, isScam)
                Log.d("MessageRepository", "Message check DONE: $isScam")
            } else {
                Log.w("MessageRepository", "Message check FAILED → retry later")
            }
        } else {
            Log.d("MessageRepository", "Message already checked → skip")
        }

        // 3️⃣ Check số điện thoại nếu CHƯA check
        if (!message.isPhoneChecked) {
            val formatted = formatPhoneNumberForApi(message.address)
            Log.d("MessageRepository", "Checking PHONE scam: $formatted")

            val response = scamCheckRepository.checkPhoneNumber(formatted)

            if (response?.data != null) {
                val isScam = response.data.isScam == true
                messageDao.updatePhoneScamResult(systemSmsId, isScam)
                Log.d("MessageRepository", "Phone check DONE: $isScam")
            } else {
                Log.w("MessageRepository", "Phone check FAILED → retry later")
            }
        } else {
            Log.d("MessageRepository", "Phone already checked → skip")
        }
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
