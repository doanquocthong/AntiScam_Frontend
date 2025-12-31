package com.example.antiscam.data.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.antiscam.data.model.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY date DESC")
    fun getAllMessages(): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessage(message: Message)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessages(messages: List<Message>)

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()
    @Query("DELETE FROM messages WHERE address = :address")
    suspend fun deleteMessagesByAddress(address: String)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessageById(id: Long)

    @Query("""
    UPDATE messages
    SET isRead = 1
    WHERE address = :address
    """)
    suspend fun markMessageAsRead(address: String)

    @Query("""
    SELECT * FROM messages
    WHERE address = :address
    ORDER BY date ASC
    """)
    fun getMessagesByAddress(address: String): Flow<List<Message>>

    @Query("""
    SELECT EXISTS(
    SELECT 1 FROM messages
    WHERE systemSmsId = :systemId
        )
    """)
        suspend fun existsBySystemId(systemId: Long): Boolean
    @Query("SELECT * FROM messages WHERE systemSmsId = :systemSmsId LIMIT 1")
    suspend fun getBySystemSmsId(systemSmsId: Long): Message?
    @Query("""
    UPDATE messages
    SET 
        isScamMessage = :isScam,
        isMessageChecked = 1
    WHERE systemSmsId = :systemSmsId
    """)
    suspend fun updateMessageScamResult(
        systemSmsId: Long,
        isScam: Boolean
    )
    @Query("""
    UPDATE messages
    SET 
        isScamNumber = :isScam,
        isPhoneChecked = 1
    WHERE systemSmsId = :systemSmsId
    """)
    suspend fun updatePhoneScamResult(
        systemSmsId: Long,
        isScam: Boolean
    )

}