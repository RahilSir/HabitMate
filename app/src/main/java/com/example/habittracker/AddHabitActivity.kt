package com.example.habittracker

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.habittracker.models.Habit
import com.example.habittracker.network.RetrofitClient // <-- NEW: Import Retrofit Client
import com.google.firebase.auth.FirebaseAuth // <-- KEEP: Used only for user ID
import retrofit2.Call // <-- NEW: Retrofit Call
import retrofit2.Callback // <-- NEW: Retrofit Callback
import retrofit2.Response // <-- NEW: Retrofit Response
import java.util.*

class AddHabitActivity : AppCompatActivity() {

    // Initialize Auth to get the current user's ID
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_habit)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser?.uid

        // --- CRITICAL CHECK: Ensure user is logged in before proceeding ---
        if (currentUserId == null) {
            Toast.makeText(this, "Authentication error. Please log in.", Toast.LENGTH_LONG).show()
            // Redirect or finish if user is not logged in
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
                { _, hourOfDay, minute ->
                    selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                    reminderTimeBtn.text = "Reminder: $selectedTime"
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            )
            timePicker.show()
        }

        // --- RETROFIT POST LOGIC HERE (Replaces FirebaseDatabase logic) ---
        saveHabitBtn.setOnClickListener {
            val name = habitNameInput.text.toString().trim()
            val durationString = durationInput.text.toString().trim()
            val days = daysCheckboxes.filter { it.isChecked }.map { it.text.toString() }
            val duration = durationString.toIntOrNull()

            if (name.isEmpty() || duration == null || days.isEmpty() || selectedTime == null) {
                Toast.makeText(this, "Please fill in all fields correctly", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1. Create the Habit object (CRITICAL: Includes the userId for filtering)
            val newHabit = Habit(
                userId = currentUserId, // <<< LINKS HABIT TO USER
                habitName = name,
                duration = duration,
                days = days,
                reminderTime = selectedTime!!
            )

            // 2. Make the Retrofit API call (POST request)
            RetrofitClient.api.addHabit(newHabit)
                .enqueue(object : Callback<Habit> {
                    override fun onResponse(call: Call<Habit>, response: Response<Habit>) {
                        if (response.isSuccessful) {
                            // If successful, the habit is now saved on MockAPI.io
                            Toast.makeText(this@AddHabitActivity, "Habit Saved via REST API!", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            // Display API error code (e.g., 404 if the URL is wrong)
                            Toast.makeText(this@AddHabitActivity, "API Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Habit>, t: Throwable) {
                        // Display network failure (e.g., no internet connection)
                        Toast.makeText(this@AddHabitActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
