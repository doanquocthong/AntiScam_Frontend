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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<Message>)

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()

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

}