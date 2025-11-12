package com.example.habittracker.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val userId: String?,
    val habitName: String?,
    val days: String, // Store as JSON string or comma-separated
    val reminderTime: String?,
    val isChecked: Boolean = false,
    val isSynced: Boolean = false, // Track if synced with server
    val isDeleted: Boolean = false, // Soft delete
    val lastModified: Long = System.currentTimeMillis(),
    val currentStreak: Int = 0,  // ✅ ADD
    val longestStreak: Int = 0,  // ✅ ADD
    val lastCheckedDate: String = ""  // ✅ ADD (format: "yyyy-MM-dd")
)