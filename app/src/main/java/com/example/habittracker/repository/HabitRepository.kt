package com.example.habittracker.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.habittracker.database.AppDatabase
import com.example.habittracker.database.HabitEntity
import com.example.habittracker.database.SyncQueueEntity
import com.example.habittracker.models.Habit
import com.example.habittracker.network.RetrofitClient
import com.example.habittracker.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class HabitRepository(private val context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val habitDao = database.habitDao()
    private val syncQueueDao = database.syncQueueDao()
    private val api = RetrofitClient.api

    // Get habits from local database (LiveData for UI observation)
    fun getAllHabits(userId: String): LiveData<List<HabitEntity>> {
        return habitDao.getAllHabits(userId)
    }

    // Add habit (works offline)




    // Add habit (works offline)
    suspend fun addHabit(userId: String, habitName: String, days: List<String>, reminderTime: String): String {  // ✅ Add ": String" here
        return withContext(Dispatchers.IO) {  // ✅ Add "return" here
            val habitId = UUID.randomUUID().toString()
            val habit = HabitEntity(
                id = habitId,
                userId = userId,
                habitName = habitName,
                days = days.joinToString(","),
                reminderTime = reminderTime,
                isChecked = false,
                isSynced = false
            )

            // Save to local database
            habitDao.insertHabit(habit)
            Log.d("OfflineTest", "✅ Habit saved locally: $habitName")

            // Add to sync queue
            syncQueueDao.addToQueue(
                SyncQueueEntity(habitId = habitId, action = "CREATE")
            )
            Log.d("OfflineTest", "📋 Added to sync queue: $habitName")

            // Try to sync immediately if online
            if (NetworkUtils.isNetworkAvailable(context)) {
                Log.d("OfflineTest", "🌐 Network available, syncing now...")
                syncHabit(habit)
            } else {
                Log.d("OfflineTest", "📵 Offline - will sync later")
            }

            habitId  // ✅ Return the habitId at the end
        }
    }

    // Update habit (works offline)
    suspend fun updateHabit(habitId: String, habitName: String, days: List<String>, reminderTime: String) {
        withContext(Dispatchers.IO) {
            val existingHabit = habitDao.getHabitById(habitId)
            if (existingHabit != null) {
                val updatedHabit = existingHabit.copy(
                    habitName = habitName,
                    days = days.joinToString(","),
                    reminderTime = reminderTime,
                    isSynced = false,
                    lastModified = System.currentTimeMillis()
                )

                habitDao.updateHabit(updatedHabit)

                // Add to sync queue
                syncQueueDao.addToQueue(
                    SyncQueueEntity(habitId = habitId, action = "UPDATE")
                )

                // Try to sync immediately if online
                if (NetworkUtils.isNetworkAvailable(context)) {
                    syncHabit(updatedHabit)
                }
            }
        }
    }

    // ✅ NEW: Update habit checked status (for marking habits as complete)
    suspend fun updateHabitCheckedStatus(habitId: String, isChecked: Boolean) {



        if (isChecked) {
            // Update streak
            val habit = habitDao.getHabitById(habitId)
            if (habit != null) {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                    Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
                )

                val newStreak = when (habit.lastCheckedDate) {
                    yesterday -> habit.currentStreak + 1  // Continuing streak
                    today -> habit.currentStreak  // Already checked today
                    else -> 1  // New streak
                }

                val newLongest = maxOf(newStreak, habit.longestStreak)

                habitDao.updateStreak(habitId, newStreak, newLongest, today)
            }
        }



        withContext(Dispatchers.IO) {
            // Update the checked status in database
            habitDao.updateHabitCheckedStatus(habitId, isChecked)
            Log.d("HabitRepository", "Updated habit $habitId checked status to $isChecked")

            // Get the updated habit
            val habit = habitDao.getHabitById(habitId)
            if (habit != null) {
                // Mark as unsynced so it syncs to server
                val updatedHabit = habit.copy(
                    isChecked = isChecked,
                    isSynced = false,
                    lastModified = System.currentTimeMillis()
                )
                habitDao.updateHabit(updatedHabit)

                // Add to sync queue
                syncQueueDao.addToQueue(
                    SyncQueueEntity(habitId = habitId, action = "UPDATE")
                )

                // Try to sync immediately if online
                if (NetworkUtils.isNetworkAvailable(context)) {
                    syncHabit(updatedHabit)
                }
            }
        }
    }

    // Delete habit (works offline)
    suspend fun deleteHabit(habitId: String) {
        withContext(Dispatchers.IO) {
            // Soft delete
            habitDao.softDeleteHabit(habitId)

            // Add to sync queue
            syncQueueDao.addToQueue(
                SyncQueueEntity(habitId = habitId, action = "DELETE")
            )

            // Try to sync immediately if online
            if (NetworkUtils.isNetworkAvailable(context)) {
                syncDeletedHabit(habitId)
            }
        }
    }

    // Sync single habit with server
    private suspend fun syncHabit(habit: HabitEntity) {
        try {
            val apiHabit = Habit(
                id = habit.id,
                userId = habit.userId,
                habitName = habit.habitName,
                days = habit.days.split(",").filter { it.isNotEmpty() },
                reminderTime = habit.reminderTime,
                isChecked = habit.isChecked // ✅ Include checked status
            )

            val response = api.addHabit(apiHabit).execute()
            if (response.isSuccessful) {
                habitDao.markAsSynced(habit.id)
                syncQueueDao.clearHabitFromQueue(habit.id)
                Log.d("Sync", "Habit ${habit.id} synced successfully")
            } else {
                Log.e("Sync", "Failed to sync habit: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("Sync", "Failed to sync habit: ${e.message}")
        }
    }

    // Sync deleted habit with server
    private suspend fun syncDeletedHabit(habitId: String) {
        try {
            // Call your delete API here
            // val response = api.deleteHabit(habitId).execute()
            // if (response.isSuccessful) {
            syncQueueDao.clearHabitFromQueue(habitId)
            habitDao.cleanupDeletedHabits()
            Log.d("Sync", "Deleted habit $habitId synced")
            // }
        } catch (e: Exception) {
            Log.e("Sync", "Failed to delete habit on server: ${e.message}")
        }
    }

    // Sync all pending changes
    suspend fun syncAllPendingChanges() {
        withContext(Dispatchers.IO) {
            if (!NetworkUtils.isNetworkAvailable(context)) {
                Log.d("Sync", "No network available, skipping sync")
                return@withContext
            }

            val unsyncedHabits = habitDao.getUnsyncedHabits()
            Log.d("Sync", "Found ${unsyncedHabits.size} unsynced habits")

            unsyncedHabits.forEach { habit ->
                syncHabit(habit)
            }
        }
    }

    // Fetch habits from server and update local database
    suspend fun fetchHabitsFromServer(userId: String) {
        withContext(Dispatchers.IO) {
            try {
                val response = api.getHabits(userId).execute()
                if (response.isSuccessful) {
                    val serverHabits = response.body() ?: emptyList()
                    val entities = serverHabits.map { habit ->
                        HabitEntity(
                            id = habit.id ?: UUID.randomUUID().toString(),
                            userId = habit.userId,
                            habitName = habit.habitName,
                            days = habit.days?.joinToString(",") ?: "",
                            reminderTime = habit.reminderTime,
                            isChecked = habit.isChecked,
                            isSynced = true
                        )
                    }
                    habitDao.insertAll(entities)
                    Log.d("Sync", "Fetched ${entities.size} habits from server")
                }
            } catch (e: Exception) {
                Log.e("Sync", "Failed to fetch habits from server: ${e.message}")
            }
        }
    }

    // Save habit from server
    suspend fun saveHabitFromServer(habit: HabitEntity) {
        withContext(Dispatchers.IO) {
            habitDao.insertHabit(habit)
        }
    }

    // ✅ NEW: Get all habits synchronously (for migrations, etc.)
    suspend fun getAllHabitsSync(userId: String): List<HabitEntity> {
        return withContext(Dispatchers.IO) {
            habitDao.getAllHabitsSync(userId)
        }
    }

    suspend fun getHabitById(habitId: String): HabitEntity? {
        return habitDao.getHabitById(habitId)
    }




}