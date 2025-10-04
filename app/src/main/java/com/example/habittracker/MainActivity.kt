package com.example.habittracker

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp
import retrofit2.Call
import retrofit2.Response
import android.content.Intent
import com.example.habittracker.models.Habit
import com.example.habittracker.network.RetrofitClient


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        FirebaseApp.initializeApp(this)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize Views
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        registerButton = findViewById(R.id.registerButton)
        loginButton = findViewById(R.id.loginButton)

        // ---------- Go to Registration Page ----------
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }


        // ---------- Firebase Login ----------
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

                        // ✅ Navigate to HomeActivity
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish() // close login so user can't go back


                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // ---------- Retrofit example (FIXED) ----------
        // NOTE: This test code is run every time MainActivity loads and is not needed
        // for the final app, but is kept here to demonstrate the correct API call structure.
        val testUserId = "testUser123"
        val testHabit = Habit(
            userId = testUserId, // Must include userId for filtering
            habitName = "Drink Water",
            days = listOf("Monday", "Wednesday", "Friday"),
            reminderTime = "08:00"
        )

        // 1. Removed userId as a separate argument.
        // 2. Changed expected response type from Map<String, String> to Habit.
        RetrofitClient.api.addHabit(testHabit)
            .enqueue(object : retrofit2.Callback<Habit> {
                override fun onResponse(
                    call: Call<Habit>,
                    response: Response<Habit>
                ) {
                    if (response.isSuccessful) {
                        Log.d("API", "Test Habit added successfully. ID: ${response.body()?.id}")
                    } else {
                        Log.e("API", "Test Habit addition failed. Code: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Habit>, t: Throwable) {
                    Log.e("API", "Test Network Error: ${t.message}")
                }
            })
    }
}