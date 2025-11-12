package com.example.habittracker.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits WHERE userId = :userId AND isDeleted = 0")
    fun getAllHabits(userId: String): LiveData<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE userId = :userId AND isDeleted = 0")
    suspend fun getAllHabitsSync(userId: String): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE id = :habitId")
    suspend fun getHabitById(habitId: String): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(habits: List<HabitEntity>)

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("UPDATE habits SET isDeleted = 1, lastModified = :timestamp WHERE id = :habitId")
    suspend fun softDeleteHabit(habitId: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM habits WHERE isSynced = 0 AND isDeleted = 0")
    suspend fun getUnsyncedHabits(): List<HabitEntity>

    @Query("UPDATE habits SET isSynced = 1 WHERE id = :habitId")
    suspend fun markAsSynced(habitId: String)

    @Query("DELETE FROM habits WHERE isDeleted = 1 AND isSynced = 1")
    suspend fun cleanupDeletedHabits()

    @Query("UPDATE habits SET isChecked = :isChecked WHERE id = :habitId")
    suspend fun updateHabitCheckedStatus(habitId: String, isChecked: Boolean)

    @Query("UPDATE habits SET isChecked = :isChecked, lastModified = :timestamp WHERE id = :habitId")
    suspend fun updateHabitCheckedStatus(habitId: String, isChecked: Boolean, timestamp: Long = System.currentTimeMillis())

    // ✅ NEW: Get checked/completed habits
    @Query("SELECT * FROM habits WHERE userId = :userId AND isDeleted = 0 AND isChecked = 1")
    fun getCompletedHabits(userId: String): LiveData<List<HabitEntity>>


    @Query("UPDATE habits SET currentStreak = :streak, longestStreak = :longest, lastCheckedDate = :date WHERE id = :habitId")
    suspend fun updateStreak(habitId: String, streak: Int, longest: Int, date: String)
}