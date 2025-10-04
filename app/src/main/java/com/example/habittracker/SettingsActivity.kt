package com.example.habittracker

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.habittracker.models.Habit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri

// -----------------------------------------------------------------------------------

class SettingsActivity : AppCompatActivity() {

    private lateinit var homeNavBtn: Button
    private lateinit var progressNavBtn: Button
    private lateinit var settingsNavBtn: Button
    private lateinit var themeSwitch: Switch
    private lateinit var exportDataSetting: Button
    private lateinit var prefs: SharedPreferences

    private var tempCsvContent: String? = null
    private val activityScope = CoroutineScope(Dispatchers.Main + Job())

    // ⭐️ FIX: Append your collection name (e.g., "/habits") to your base URL.
    private val HABITS_API_URL = "https://68d973cb90a75154f0da715c.mockapi.io/habits"

//private val HABITS_API_URL = " https://68d973cb90a75154f0da715c.mockapi.io/habits?userId=YOUR_USER_ID"






    /**
     * 1. Register the Activity Result Launcher for SAF (Storage Access Framework).
     */
    private val createDocumentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        exportDataSetting.isEnabled = true
        exportDataSetting.text = "Export Data"

        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                tempCsvContent?.let { csv ->
                    writeCsvToUri(uri, csv)
                }
            }
        } else {
            Toast.makeText(this, "Export cancelled by user.", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = getSharedPreferences("AppSettingsPrefs", MODE_PRIVATE)

        homeNavBtn = findViewById(R.id.homeNavBtn)
        progressNavBtn = findViewById(R.id.progressNavBtn)
        settingsNavBtn = findViewById(R.id.settingsNavBtn)
        themeSwitch = findViewById(R.id.themeSwitch)
        exportDataSetting = findViewById(R.id.exportDataSetting)

        // --- Theme Logic ---
        val isDarkMode = prefs.getBoolean("DarkMode", false)
        themeSwitch.isChecked = isDarkMode
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Save preference
            prefs.edit().putBoolean("DarkMode", isChecked).apply()

            // Apply theme change
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }


        // --- Navigation Logic --- (Removed for brevity)
        homeNavBtn.setOnClickListener { startActivity(Intent(this, HomeActivity::class.java)); finish() }
        progressNavBtn.setOnClickListener { startActivity(Intent(this, ProgressActivity::class.java)); finish() }
        settingsNavBtn.setOnClickListener { Toast.makeText(this, "You are already here!", Toast.LENGTH_SHORT).show() }


        // --- EXPORT DATA LOGIC: Link the button to the export process ---
        exportDataSetting.setOnClickListener {
            exportDataSetting.isEnabled = false
            exportDataSetting.text = "Exporting..."
            exportHabitsToCSV()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activityScope.cancel()
    }

    /**
     * Main export function: runs the network call in the background using a coroutine.
     */
    private fun exportHabitsToCSV() = activityScope.launch {
        try {
            Log.d("API_CALL", "Attempting to fetch habits from: $HABITS_API_URL")

            // 1. Fetch data from API (runs on background thread Dispatchers.IO)
            val habitList = withContext(Dispatchers.IO) {
                fetchHabitsFromApi() // Removed auth token parameter
            }

            if (habitList.isNullOrEmpty()) {
                Toast.makeText(this@SettingsActivity, "No habits found to export. Check API URL and data.", Toast.LENGTH_LONG).show()
                exportDataSetting.isEnabled = true
                exportDataSetting.text = "Export Data"
                return@launch
            }

            // 2. Convert to CSV format
            val csvContent = generateCsvContent(habitList)

            // 3. Store content and launch SAF intent (runs on main thread)
            startSaveFileIntent(csvContent)

        } catch (e: Exception) {
            Toast.makeText(this@SettingsActivity, "Export Failed: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("Export", "API/File operation failed", e)
            exportDataSetting.isEnabled = true
            exportDataSetting.text = "Export Data"
        }
    }


    /**
     * Fetches habit data from the custom API using URL and Gson (NO AUTH TOKEN).
     * This runs on a background thread (Dispatchers.IO).
     */
    private fun fetchHabitsFromApi(): List<Habit>? { // Removed authToken parameter
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(HABITS_API_URL)
            connection = url.openConnection() as HttpURLConnection

            // Removed Authorization Header logic for MockAPI

            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.requestMethod = "GET"

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e("API_ERROR", "HTTP Error: $responseCode - ${connection.responseMessage}")
                return null
            }

            InputStreamReader(connection.inputStream).use { reader ->
                val type = object : TypeToken<List<Habit>>() {}.type
                return Gson().fromJson(reader, type)
            }
        } catch (e: Exception) {
            Log.e("API_ERROR", "Failed to fetch habits from API: ${e.message}", e)
            return null
        } finally {
            connection?.disconnect()
        }
    }


    /**
     * Converts a list of Habit objects into a CSV formatted string.
     */
    private fun generateCsvContent(habits: List<Habit>): String {
        val header = "ID,User ID,Habit Name,Duration (Days),Days Active,Reminder Time,Is Completed\n"
        val builder = StringBuilder(header)

        habits.forEach { habit ->
            builder.append("${habit.id ?: "N/A"},")
            builder.append("${habit.userId ?: "N/A"},")
            builder.append("\"${habit.habitName?.replace("\"", "\"\"") ?: "N/A"}\",")
            builder.append("${habit.duration ?: "0"},")
            builder.append("\"${habit.days?.joinToString("|") ?: "N/A"}\",")
            builder.append("${habit.reminderTime ?: "N/A"},")
            builder.append("${habit.isChecked}\n")
        }
        return builder.toString()
    }


    /**
     * Uses Storage Access Framework (SAF) to let the user select a save location.
     */
    private fun startSaveFileIntent(csvContent: String) {
        tempCsvContent = csvContent

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, "HabitMate_Export_${System.currentTimeMillis()}.csv")
        }

        createDocumentLauncher.launch(intent)
    }

    /**
     * Writes the CSV content to the URI selected by the user.
     */
    private fun writeCsvToUri(uri: Uri, csvContent: String) = activityScope.launch {
        try {
            withContext(Dispatchers.IO) {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(csvContent.toByteArray())
                }
            }
            runOnUiThread {
                Toast.makeText(this@SettingsActivity, "Data exported successfully!", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e("Export", "Writing to URI failed: ${e.message}", e)
            runOnUiThread {
                Toast.makeText(this@SettingsActivity, "File save failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } finally {
            tempCsvContent = null
        }
    }
}