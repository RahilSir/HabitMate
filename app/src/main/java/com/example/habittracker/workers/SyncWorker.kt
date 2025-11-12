package com.example.habittracker.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.habittracker.repository.HabitRepository
import com.google.firebase.auth.FirebaseAuth

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d("SyncWorker", "Starting background sync")

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("SyncWorker", "No user logged in, skipping sync")
            return Result.failure()
        }

        return try {
            val repository = HabitRepository(applicationContext)
            repository.syncAllPendingChanges()
            Log.d("SyncWorker", "Sync completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Sync failed: ${e.message}")
            Result.retry()
        }
    }
}