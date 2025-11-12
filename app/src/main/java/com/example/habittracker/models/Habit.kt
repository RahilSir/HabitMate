package com.example.habittracker.models

data class Habit(
    // 1. Used by MockAPI for database operations (e.g., DELETE /habits/{id})
    var id: String? = null,

    // 2. CRITICAL: Used to link the habit to the logged-in user.
    var userId: String? = null,

    var habitName: String? = null,
    var duration: Int? = null,
    var days: List<String>? = null,
    var reminderTime: String? = null,
    var isChecked: Boolean = false,
    var currentStreak: Int = 0,      // ✅ ADD THIS
    var longestStreak: Int = 0,      // ✅ ADD THIS
    var lastCheckedDate: String = "" // ✅ ADD THIS
)



