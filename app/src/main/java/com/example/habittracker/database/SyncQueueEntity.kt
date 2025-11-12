package com.example.habittracker.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val queueId: Int = 0,
    val habitId: String,
    val action: String, // "CREATE", "UPDATE", "DELETE"
    val timestamp: Long = System.currentTimeMillis(),
    val retryCount: Int = 0
)