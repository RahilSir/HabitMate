package com.example.habittracker.database

import androidx.room.*

@Dao
interface SyncQueueDao {

    @Insert
    suspend fun addToQueue(syncItem: SyncQueueEntity)

    @Query("SELECT * FROM sync_queue ORDER BY timestamp ASC")
    suspend fun getAllPendingSync(): List<SyncQueueEntity>

    @Delete
    suspend fun removeFromQueue(syncItem: SyncQueueEntity)

    @Query("UPDATE sync_queue SET retryCount = retryCount + 1 WHERE queueId = :queueId")
    suspend fun incrementRetryCount(queueId: Int)

    @Query("DELETE FROM sync_queue WHERE habitId = :habitId")
    suspend fun clearHabitFromQueue(habitId: String)
}