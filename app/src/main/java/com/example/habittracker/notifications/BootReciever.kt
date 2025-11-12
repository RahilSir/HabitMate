package com.example.habittracker.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule all alarms after device reboot
            val scheduler = ImprovedAlarmScheduler(context)
            val alarms = scheduler.getAllScheduledAlarms()

            alarms.forEach { (habitId, data) ->
                val (habitName, time) = data
                scheduler.scheduleHabitReminder(habitId, habitName, time)
            }
        }
    }
}