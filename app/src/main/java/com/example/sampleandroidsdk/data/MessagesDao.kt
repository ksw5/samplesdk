package com.example.sampleandroidsdk.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface MessagesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(messages: Messages)



    @Query("SELECT * FROM messages Limit :count OFFSET :from ")
    suspend fun getCount(from: Int, count: Int): List<Messages>

    @Query("SELECT * FROM messages WHERE groupId=:groupId ORDER BY inDate Limit :count OFFSET :from ")
    suspend fun getCount(groupId: String, from: Int, count: Int): List<Messages>

    @Query("SELECT COUNT(*) FROM messages WHERE groupId=:groupId")
    suspend fun count(groupId: String): Int

    @Query("SELECT COUNT(*) FROM messages")
    suspend fun count(): Int

    @Query("UPDATE messages SET `key`=:key, `status`=:status, `timestamp`=:timestamp WHERE groupId=:groupId and id=:id")
    suspend fun update(groupId: String, id: String, timestamp: Long, key: ByteArray, status: Int)

    @Query("UPDATE messages SET `key`=:key, status=:status WHERE groupId=:groupId and timestamp=:timestamp ")
    suspend fun update(groupId: String, timestamp: Long, key: ByteArray, status: Int)

    @Query("DELETE FROM messages WHERE groupId=:groupId and id=:id")
    suspend fun delete(groupId: String, id: String)

    @Query("DELETE FROM messages WHERE groupId=:groupId and timestamp=:timestamp")
    suspend fun delete(groupId: String, timestamp: Long)

    @Query("DELETE FROM messages WHERE groupId=:groupId")
    suspend fun delete(groupId: String)
}