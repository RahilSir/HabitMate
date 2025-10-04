package com.example.habittracker

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.content.Intent
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.habittracker.models.Habit
import com.example.habittracker.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProgressActivity : AppCompatActivity() {

    // Navigation buttons
    private lateinit var homeNavBtn: Button
    private lateinit var progressNavBtn: Button
    private lateinit var settingsNavBtn: Button

    // Circular progress views
    private lateinit var circularProgressBar: ProgressBar
    private lateinit var progressText: TextView

    // Completed habits container (instead of ListView)
    private lateinit var completedHabitsContainer: LinearLayout

    private val habitsList = mutableListOf<Habit>()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()

        // Initialize navigation buttons
        homeNavBtn = findViewById(R.id.homeNavBtn)
        progressNavBtn = findViewById(R.id.progressNavBtn)
        settingsNavBtn = findViewById(R.id.settingsNavBtn)

        homeNavBtn.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        progressNavBtn.setOnClickListener {
            Toast.makeText(this, "You are already on the Progress screen.", Toast.LENGTH_SHORT).show()
        }

        settingsNavBtn.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }

        // Initialize circular progress views
        circularProgressBar = findViewById(R.id.circularProgressBar)
        progressText = findViewById(R.id.progressText)

        // Initialize the Completed Habits container
        completedHabitsContainer = findViewById(R.id.completedHabitsContainer)

        // Fetch habits and update progress
        fetchHabitsAndUpdateProgress()

        Toast.makeText(this, "Welcome to the Progress page!", Toast.LENGTH_SHORT).show()
    }

    private fun fetchHabitsAndUpdateProgress() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.api.getHabits(userId)
            .enqueue(object : Callback<List<Habit>> {
                override fun onResponse(call: Call<List<Habit>>, response: Response<List<Habit>>) {
                    if (response.isSuccessful) {
                        habitsList.clear()
                        habitsList.addAll(response.body() ?: emptyList())
                        updateProgress()
                        updateCompletedHabitsList()
                    } else {
                        Toast.makeText(this@ProgressActivity, "API Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Habit>>, t: Throwable) {
                    Toast.makeText(this@ProgressActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateProgress() {
        val completedCount = habitsList.count { it.isChecked }
        val totalCount = habitsList.size
        val percent = if (totalCount > 0) (completedCount * 100 / totalCount) else 0

        circularProgressBar.progress = percent
        progressText.text = "$percent%"
    }

    private fun updateCompletedHabitsList() {
        // Clear old items
        completedHabitsContainer.removeAllViews()

        // Only add completed habits
        val completedHabits = habitsList.filter { it.isChecked }

        for (habit in completedHabits) {
            val checkBox = CheckBox(this)
            checkBox.text = habit.habitName
            checkBox.isChecked = true
            checkBox.isEnabled = false // prevent unchecking
            completedHabitsContainer.addView(checkBox)
        }
    }
}
