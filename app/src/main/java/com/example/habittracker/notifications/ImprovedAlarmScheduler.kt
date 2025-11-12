package com.example.habittracker.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.*

class ImprovedAlarmScheduler(private val context: Context) {

    fun scheduleHabitReminder(habitId: String, habitName: String, time: String) {  // ✅ Changed Unit to String
        Log.d("AlarmScheduler", "📅 Scheduling alarm for: $habitName at $time")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Parse time (format: "14:30")
        val timeParts = time.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        Log.d("AlarmScheduler", "Parsed time: $hour:$minute")

        // Set the alarm time
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)

            // If time has passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
                Log.d("AlarmScheduler", "Time passed today, scheduling for tomorrow")
            } else {
                Log.d("AlarmScheduler", "Scheduling for today")
            }
        }

        val triggerTime = calendar.timeInMillis
        val currentTime = System.currentTimeMillis()
        val delayMinutes = (triggerTime - currentTime) / 1000 / 60

        Log.d("AlarmScheduler", "⏰ Alarm will trigger in $delayMinutes minutes")
        Log.d("AlarmScheduler", "Trigger time: ${calendar.time}")

        // Create intent
        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            putExtra("HABIT_NAME", habitName)
            putExtra("HABIT_ID", habitId)  // ✅ Now this works because habitId is String
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId.hashCode(),  // ✅ Now this works too
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Schedule repeating alarm
        try {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY, // Repeat every 24 hours
                pendingIntent
            )
            Log.d("AlarmScheduler", "✅ Alarm scheduled successfully!")
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "❌ Failed to schedule alarm: ${e.message}", e)
        }

        // Save to SharedPreferences for reboot recovery
        saveScheduledAlarm(habitId, habitName, time)
    }

    private fun saveScheduledAlarm(habitId: String, habitName: String, time: String) {  // ✅ Changed Unit to String
        val prefs = context.getSharedPreferences("habit_alarms", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(habitId, "$habitName|$time")
        editor.apply()
        Log.d("AlarmScheduler", "💾 Saved alarm to SharedPreferences")
    }

    fun getAllScheduledAlarms(): Map<String, Pair<String, String>> {
        val prefs = context.getSharedPreferences("habit_alarms", Context.MODE_PRIVATE)
        val alarms = mutableMapOf<String, Pair<String, String>>()

        prefs.all.forEach { (habitId, value) ->
            val parts = (value as String).split("|")
            if (parts.size == 2) {
                alarms[habitId] = Pair(parts[0], parts[1]) // name, time
            }
        }

        return alarms
    }

    fun cancelHabitReminder(habitId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, HabitReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.cancel(pendingIntent)

        // Remove from SharedPreferences
        val prefs = context.getSharedPreferences("habit_alarms", Context.MODE_PRIVATE)
        prefs.edit().remove(habitId).apply()

        Log.d("AlarmScheduler", "Cancelled alarm for habit: $habitId")
    }
}