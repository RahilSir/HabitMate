package com.example.habittracker

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.habittracker.notifications.ImprovedAlarmScheduler
import com.example.habittracker.repository.HabitRepository
import com.example.habittracker.utils.Constants
import com.example.habittracker.utils.LanguageHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.*

class AddHabitActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var isOfflineMode = false
    private lateinit var repository: HabitRepository

    // ✅ ADD THIS for language support
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageHelper.updateBaseContextLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_habit)

        auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser?.uid

        // Initialize repository
        repository = HabitRepository(this)

        if (currentUserId == null) {
            Toast.makeText(this, getString(R.string.auth_error), Toast.LENGTH_LONG).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // UI Initialization
        val habitNameInput: EditText = findViewById(R.id.habitNameInput)
        val durationInput: EditText = findViewById(R.id.durationInput)
        val daysCheckboxes = listOf<CheckBox>(
            findViewById(R.id.mondayCheck),
            findViewById(R.id.tuesdayCheck),
            findViewById(R.id.wednesdayCheck),
            findViewById(R.id.thursdayCheck),
            findViewById(R.id.fridayCheck),
            findViewById(R.id.saturdayCheck),
            findViewById(R.id.sundayCheck)
        )
        val reminderTimeBtn: Button = findViewById(R.id.reminderTimeBtn)
        val saveHabitBtn: Button = findViewById(R.id.saveHabitBtn)

        var selectedTime: String? = null

        // Open time picker
        reminderTimeBtn.setOnClickListener {
            val cal = Calendar.getInstance()
            val timePicker = TimePickerDialog(
                this,
                R.style.GreenTimePicker,
                { _, hourOfDay, minute ->
                    selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                    reminderTimeBtn.text = getString(R.string.reminder_at, selectedTime)
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            )
            timePicker.show()
        }

        // Save habit AND schedule notification
        saveHabitBtn.setOnClickListener {
            val name = habitNameInput.text.toString().trim()
            val durationString = durationInput.text.toString().trim()
            val days = daysCheckboxes.filter { it.isChecked }.map { it.text.toString() }
            val duration = durationString.toIntOrNull()

            if (name.isEmpty() || duration == null || days.isEmpty() || selectedTime == null) {
                Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    // Save habit and get the habit ID back
                    val habitId = repository.addHabit(
                        userId = currentUserId,
                        habitName = name,
                        days = days,
                        reminderTime = selectedTime!!
                    )

                    Log.d("NotificationDebug", "Habit saved with ID: $habitId")
                    Log.d("NotificationDebug", "Scheduling notification for: $selectedTime")

                    // Schedule the notification
                    val scheduler = ImprovedAlarmScheduler(this@AddHabitActivity)
                    scheduler.scheduleHabitReminder(habitId, name, selectedTime!!)

                    Log.d("NotificationDebug", "✅ Notification scheduled successfully")

                    Toast.makeText(
                        this@AddHabitActivity,
                        getString(R.string.habit_saved_reminder, selectedTime),
                        Toast.LENGTH_LONG
                    ).show()

                    finish()

                } catch (e: Exception) {
                    Log.e("NotificationDebug", "❌ Error: ${e.message}", e)
                    Toast.makeText(
                        this@AddHabitActivity,
                        getString(R.string.error_saving_habit, e.message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        val userID = if (isOfflineMode) {
            Constants.OFFLINE_USER_ID
        } else {
            auth.currentUser?.uid
        }
    }
}