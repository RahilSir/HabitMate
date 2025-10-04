package com.example.habittracker

import android.content.Intent
import android.os.Bundle
// REMOVED: import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.habittracker.models.Habit
import com.example.habittracker.network.Quote
import com.example.habittracker.network.RetrofitClient
import com.example.habittracker.network.RetrofitClientQuotes
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var habitListView: ListView

    // --- CHANGE 1: Use the new custom adapter and a list of Habit objects ---
    private lateinit var adapter: HabitChecklistAdapter
    private val habitsList = mutableListOf<Habit>()

    private lateinit var homeNavBtn: Button
    private lateinit var progressNavBtn: Button
    private lateinit var settingsNavBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val quoteTextView = findViewById<TextView>(R.id.motivationQuote)

// Fetch a random quote from the API
        RetrofitClientQuotes.instance.getRandomQuote().enqueue(object : retrofit2.Callback<List<Quote>> {
            override fun onResponse(call: retrofit2.Call<List<Quote>>, response: retrofit2.Response<List<Quote>>) {
                if (response.isSuccessful) {
                    val quote = response.body()?.firstOrNull()
                    if (quote != null) {
                        quoteTextView.text = "\"${quote.q}\" \n- ${quote.a}"

                        // Save last quote for offline use
                        val prefs = getSharedPreferences("habit_prefs", MODE_PRIVATE)
                        prefs.edit().putString("last_quote", "\"${quote.q}\" - ${quote.a}").apply()
                    }
                }
            }

            override fun onFailure(call: retrofit2.Call<List<Quote>>, t: Throwable) {
                // Show last saved quote if offline
                val prefs = getSharedPreferences("habit_prefs", MODE_PRIVATE)
                val lastQuote = prefs.getString("last_quote", "Stay motivated! Keep building your habits 💪")
                quoteTextView.text = lastQuote
            }
        })










        auth = FirebaseAuth.getInstance()
        val titleText: TextView = findViewById(R.id.titleText)
        val addHabitButton: Button = findViewById(R.id.addHabitButton)
//        titleText.text = "HabitMate"

        // --- CHANGE 2: Initialize and set the new adapter ---
        habitListView = findViewById(R.id.habitListView)
        adapter = HabitChecklistAdapter(this, habitsList)
        habitListView.adapter = adapter

        addHabitButton.setOnClickListener {
            val intent = Intent(this, AddHabitActivity::class.java)
            startActivity(intent)
        }

        // --- Your navigation bar code remains unchanged ---
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
        if (user == null) {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        fetchHabitsFromApi()
    }

    override fun onPause() {
        super.onPause()
    }

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

                        // --- CHANGE 3: Update the list of Habit objects directly ---
                        // The adapter will handle formatting the text for display.
                        habitsList.clear()
                        habitsList.addAll(habits)
                        adapter.notifyDataSetChanged()

                    } else {
                        Toast.makeText(this@HomeActivity, "API Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Habit>>, t: Throwable) {
                    Toast.makeText(this@HomeActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }







            })
    }
}