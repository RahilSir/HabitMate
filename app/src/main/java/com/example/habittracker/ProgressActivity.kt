package com.example.habittracker

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.content.Intent
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.habittracker.models.Habit
import com.example.habittracker.network.RetrofitClient
import com.example.habittracker.repository.HabitRepository
import com.example.habittracker.utils.Constants
import com.example.habittracker.utils.LanguageHelper
import com.example.habittracker.utils.NetworkUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
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

    // Completed habits container
    private lateinit var completedHabitsContainer: LinearLayout

    private val habitsList = mutableListOf<Habit>()
    private lateinit var auth: FirebaseAuth
    private lateinit var repository: HabitRepository
    private lateinit var userId: String
    private var isOfflineMode = false

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageHelper.updateBaseContextLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()

        // Check if in offline mode
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        isOfflineMode = prefs.getBoolean("offline_mode", false)

        // Get user ID (use special offline ID if in offline mode)
        userId = if (isOfflineMode) {
            Constants.OFFLINE_USER_ID
        } else {
            auth.currentUser?.uid ?: ""
        }

        // Initialize repository
        repository = HabitRepository(this)

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

        // ✅ SINGLE observer - Observe habits from local database
        repository.getAllHabits(userId).observe(this) { habitEntities ->
            // Convert HabitEntity to Habit for display
            val habits = habitEntities.map { entity ->
                Habit(
                    id = entity.id,
                    userId = entity.userId,
                    habitName = entity.habitName,
                    days = entity.days.split(",").filter { it.isNotEmpty() },
                    reminderTime = entity.reminderTime,
                    isChecked = entity.isChecked,  // ✅ Use the stored checked status
                            currentStreak = entity.currentStreak,      // ✅ ADD THIS
                    longestStreak = entity.longestStreak,      // ✅ ADD THIS
                    lastCheckedDate = entity.lastCheckedDate   // ✅ ADD THIS
                )
            }

            habitsList.clear()
            habitsList.addAll(habits)
            updateProgress()
            updateCompletedHabitsList()
            displayBadges()
        }

        // Sync with server if online
        if (!isOfflineMode && NetworkUtils.isNetworkAvailable(this)) {
            lifecycleScope.launch {
                repository.syncAllPendingChanges()
            }
            fetchHabitsAndUpdateProgress()
        } else if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Offline mode - showing local data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchHabitsAndUpdateProgress() {
        if (userId.isEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.api.getHabits(userId)
            .enqueue(object : Callback<List<Habit>> {
                override fun onResponse(call: Call<List<Habit>>, response: Response<List<Habit>>) {
                    if (response.isSuccessful) {
                        val serverHabits = response.body() ?: emptyList()

                        // ✅ FIXED: Preserve isChecked when syncing
                        lifecycleScope.launch {
                            serverHabits.forEach { habit ->
                                // Get existing habit to preserve isChecked
                                val existingHabit = repository.getHabitById(habit.id ?: "")

                                val entity = com.example.habittracker.database.HabitEntity(
                                    id = habit.id ?: "",
                                    userId = habit.userId,
                                    habitName = habit.habitName,
                                    days = habit.days?.joinToString(",") ?: "",
                                    reminderTime = habit.reminderTime,
                                    isSynced = true,
                                    isChecked = existingHabit?.isChecked ?: false  // ✅ Preserve checked status
                                )
                                repository.saveHabitFromServer(entity)
                            }
                        }

                        Toast.makeText(this@ProgressActivity, "Synced with server", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ProgressActivity, "API Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Habit>>, t: Throwable) {
                    Toast.makeText(this@ProgressActivity, "Using offline data: ${t.message}", Toast.LENGTH_SHORT).show()
                    // LiveData observer will continue showing local data
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

        if (completedHabits.isEmpty()) {
            val emptyText = TextView(this)
            emptyText.text = "No completed habits yet. Keep going! 💪"
            emptyText.textSize = 16f
            emptyText.setPadding(16, 16, 16, 16)
            completedHabitsContainer.addView(emptyText)
        } else {
            for (habit in completedHabits) {
                val checkBox = CheckBox(this)
                checkBox.text = habit.habitName
                checkBox.isChecked = true
                checkBox.isEnabled = false // prevent unchecking
                completedHabitsContainer.addView(checkBox)
            }
        }
    }

    private fun displayBadges() {
        val badgesContainer = findViewById<LinearLayout>(R.id.badgesContainer)
        badgesContainer.removeAllViews()

        val totalCompleted = habitsList.sumOf { it.currentStreak }
        val maxStreak = habitsList.maxOfOrNull { it.longestStreak } ?: 0

        // Badge 1: First habit
        if (habitsList.isNotEmpty()) {
            addBadge(badgesContainer, "🌱", "Getting Started")
        }

        // Badge 2: 7 day streak
        if (maxStreak >= 7) {
            addBadge(badgesContainer, "⭐", "Week Warrior")
        }

        // Badge 3: 30 day streak
        if (maxStreak >= 30) {
            addBadge(badgesContainer, "🏆", "Monthly Master")
        }

        // Badge 4: Complete all habits
        val allCompleted = habitsList.isNotEmpty() && habitsList.all { it.isChecked }
        if (allCompleted) {
            addBadge(badgesContainer, "💯", "Perfect Day")
        }
    }

    private fun addBadge(container: LinearLayout, emoji: String, title: String) {
        val badgeView = layoutInflater.inflate(R.layout.badge_item, container, false)
        val emojiText = badgeView.findViewById<TextView>(R.id.badgeEmoji)
        val titleText = badgeView.findViewById<TextView>(R.id.badgeTitle)

        emojiText.text = emoji
        titleText.text = title

        container.addView(badgeView)
    }





}