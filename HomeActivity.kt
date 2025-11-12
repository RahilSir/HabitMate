package com.example.habittracker

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.habittracker.database.HabitEntity
import com.example.habittracker.models.Habit
import com.example.habittracker.network.Quote
import com.example.habittracker.network.RetrofitClient
import com.example.habittracker.network.RetrofitClientQuotes
import com.example.habittracker.repository.HabitRepository
import com.example.habittracker.utils.Constants
import com.example.habittracker.utils.LanguageHelper
import com.example.habittracker.utils.NetworkUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import java.util.jar.Manifest

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var habitListView: ListView
    private lateinit var adapter: HabitChecklistAdapter
    private val habitsList = mutableListOf<Habit>()
    private lateinit var repository: HabitRepository
    private lateinit var userId: String
    private lateinit var homeNavBtn: Button
    private lateinit var progressNavBtn: Button
    private lateinit var settingsNavBtn: Button
    private var isOfflineMode = false



    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageHelper.updateBaseContextLocale(newBase))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()


        // Request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    "android.permission.POST_NOTIFICATIONS"  // ✅ Use string instead
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf("android.permission.POST_NOTIFICATIONS"),  // ✅ Use string instead
                    100
                )
            }
        }


        fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == 100) {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("NotificationDebug", "✅ Notification permission GRANTED")
                } else {
                    Log.d("NotificationDebug", "❌ Notification permission DENIED")
                }
            }
        }



        // Check offline mode
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        isOfflineMode = prefs.getBoolean("offline_mode", false)

        // Get userId once
        userId = if (isOfflineMode) {
            Constants.OFFLINE_USER_ID
        } else {
            auth.currentUser?.uid ?: ""
        }

        // Initialize repository
        repository = HabitRepository(this)

        val quoteTextView = findViewById<TextView>(R.id.motivationQuote)

        // Fetch a random quote from the API
        RetrofitClientQuotes.instance.getRandomQuote().enqueue(object : retrofit2.Callback<List<Quote>> {
            override fun onResponse(call: retrofit2.Call<List<Quote>>, response: retrofit2.Response<List<Quote>>) {
                if (response.isSuccessful) {
                    val quote = response.body()?.firstOrNull()
                    if (quote != null) {
                        quoteTextView.text = "\"${quote.q}\" \n- ${quote.a}"
                        val prefs = getSharedPreferences("habit_prefs", MODE_PRIVATE)
                        prefs.edit().putString("last_quote", "\"${quote.q}\" - ${quote.a}").apply()
                    }
                }
            }

            override fun onFailure(call: retrofit2.Call<List<Quote>>, t: Throwable) {
                val prefs = getSharedPreferences("habit_prefs", MODE_PRIVATE)
                val lastQuote = prefs.getString("last_quote", "Stay motivated! Keep building your habits 💪")
                quoteTextView.text = lastQuote
            }
        })

        val addHabitButton: Button = findViewById(R.id.addHabitButton)

        // Initialize adapter
        habitListView = findViewById(R.id.habitListView)
        adapter = HabitChecklistAdapter(this, habitsList)
        habitListView.adapter = adapter

        // ✅ SINGLE observer - observe habits from local database
        repository.getAllHabits(userId).observe(this) { habitEntities ->
            val habits = habitEntities.map { entity ->
                Habit(
                    id = entity.id,
                    userId = entity.userId,
                    habitName = entity.habitName,
                    days = entity.days.split(",").filter { it.isNotEmpty() },
                    reminderTime = entity.reminderTime,
                    isChecked = entity.isChecked , // ✅ Include checked status
                            currentStreak = entity.currentStreak,      // ✅ ADD THIS
                    longestStreak = entity.longestStreak,      // ✅ ADD THIS
                    lastCheckedDate = entity.lastCheckedDate   // ✅ ADD THIS
                )
            }

            habitsList.clear()
            habitsList.addAll(habits)
            adapter.notifyDataSetChanged()

            

            // Show sync status
            val unsyncedCount = habitEntities.count { !it.isSynced }
            if (unsyncedCount > 0) {
                Toast.makeText(this, "$unsyncedCount habits pending sync", Toast.LENGTH_SHORT).show()
            }
        }

        addHabitButton.setOnClickListener {
            val intent = Intent(this, AddHabitActivity::class.java)
            startActivity(intent)
        }

        // Navigation bar code
        homeNavBtn = findViewById(R.id.homeNavBtn)
        progressNavBtn = findViewById(R.id.progressNavBtn)
        settingsNavBtn = findViewById(R.id.settingsNavBtn)

        homeNavBtn.setOnClickListener {
            Toast.makeText(this, "You are already on the Home screen.", Toast.LENGTH_SHORT).show()
        }
        progressNavBtn.setOnClickListener {
            val intent = Intent(this, ProgressActivity::class.java)
            startActivity(intent)
            finish()
        }
        settingsNavBtn.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        val user = auth.currentUser
        if (user == null && !isOfflineMode) {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }




        // Check network and sync
        if (NetworkUtils.isNetworkAvailable(this) && !isOfflineMode) {
            lifecycleScope.launch {
                repository.syncAllPendingChanges()
            }
            fetchHabitsFromApi()
        } else {
            Toast.makeText(this, "Offline mode - showing local data", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ FIXED: Preserve isChecked when syncing from server
    private fun fetchHabitsFromApi() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "Authentication error. Please log in.", Toast.LENGTH_LONG).show()
            return
        }

        RetrofitClient.api.getHabits(currentUserId)
            .enqueue(object : Callback<List<Habit>> {
                override fun onResponse(call: Call<List<Habit>>, response: Response<List<Habit>>) {
                    if (response.isSuccessful) {
                        val habits = response.body() ?: emptyList()

                        lifecycleScope.launch {
                            habits.forEach { habit ->
                                // ✅ Get existing habit to preserve isChecked
                                val existingHabit = repository.getHabitById(habit.id ?: "")

                                val entity = HabitEntity(
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

                        Toast.makeText(this@HomeActivity, "Synced with server", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@HomeActivity, "API Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Habit>>, t: Throwable) {
                    Toast.makeText(this@HomeActivity, "Using offline data: ${t.message}", Toast.LENGTH_SHORT).show()
                }




            })
    }
}