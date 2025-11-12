package com.example.habittracker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.habittracker.models.Habit
import com.example.habittracker.utils.LanguageHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri

class SettingsActivity : AppCompatActivity() {

    private lateinit var homeNavBtn: Button
    private lateinit var progressNavBtn: Button
    private lateinit var settingsNavBtn: Button
    private lateinit var themeSwitch: Switch
    private lateinit var exportDataSetting: Button
    private lateinit var switchLanguageBtn: Button  // ✅ NEW
    private lateinit var prefs: SharedPreferences

    private var tempCsvContent: String? = null
    private val activityScope = CoroutineScope(Dispatchers.Main + Job())

    private val HABITS_API_URL = "https://68d973cb90a75154f0da715c.mockapi.io/habits"

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

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageHelper.updateBaseContextLocale(newBase))
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Apply language BEFORE setContentView
        LanguageHelper.applyLanguage(this)

        setContentView(R.layout.activity_settings)

        prefs = getSharedPreferences("AppSettingsPrefs", MODE_PRIVATE)

        homeNavBtn = findViewById(R.id.homeNavBtn)
        progressNavBtn = findViewById(R.id.progressNavBtn)
        settingsNavBtn = findViewById(R.id.settingsNavBtn)
        themeSwitch = findViewById(R.id.themeSwitch)
        exportDataSetting = findViewById(R.id.exportDataSetting)
        switchLanguageBtn = findViewById(R.id.switchLanguageBtn)  // ✅ NEW

        // --- Theme Logic ---
        val isDarkMode = prefs.getBoolean("DarkMode", false)
        themeSwitch.isChecked = isDarkMode
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("DarkMode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // ✅ Language switcher
        switchLanguageBtn.setOnClickListener {
            showLanguageDialog()
        }

        // --- Navigation Logic ---
        homeNavBtn.setOnClickListener { startActivity(Intent(this, HomeActivity::class.java)); finish() }
        progressNavBtn.setOnClickListener { startActivity(Intent(this, ProgressActivity::class.java)); finish() }
        settingsNavBtn.setOnClickListener { Toast.makeText(this, "You are already here!", Toast.LENGTH_SHORT).show() }

        // --- EXPORT DATA LOGIC ---
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

    // ✅ NEW: Show language selection dialog
    private fun showLanguageDialog() {
        val languages = arrayOf(
            getString(R.string.english),      // Add this string to resources
            getString(R.string.afrikaans)     // Add this string to resources
        )
        val languageCodes = arrayOf("en", "af")

        val currentLanguage = LanguageHelper.loadLanguage(this)
        val currentIndex = languageCodes.indexOf(currentLanguage)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.switch_language_title))  // Add this string to resources
            .setSingleChoiceItems(languages, currentIndex) { dialog, which ->
                val selectedLanguage = languageCodes[which]

                LanguageHelper.setLanguage(this, selectedLanguage)

                Toast.makeText(this, getString(R.string.language_changed), Toast.LENGTH_SHORT).show() // ✅ CHANGED

                recreate()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel), null)  // Add this string to resources
            .show()
    }
    private fun exportHabitsToCSV() = activityScope.launch {
        try {
            Log.d("API_CALL", "Attempting to fetch habits from: $HABITS_API_URL")

            val habitList = withContext(Dispatchers.IO) {
                fetchHabitsFromApi()
            }

            if (habitList.isNullOrEmpty()) {
                Toast.makeText(
                    this@SettingsActivity,
                    getString(R.string.no_habits_export),  // ✅ CHANGED
                    Toast.LENGTH_LONG
                ).show()
                exportDataSetting.isEnabled = true
                exportDataSetting.text = getString(R.string.export_data)  // ✅ CHANGED
                return@launch
            }

            val csvContent = generateCsvContent(habitList)
            startSaveFileIntent(csvContent)

        } catch (e: Exception) {
            Toast.makeText(
                this@SettingsActivity,
                getString(R.string.export_failed, e.message),  // ✅ CHANGED
                Toast.LENGTH_LONG
            ).show()
            Log.e("Export", "API/File operation failed", e)
            exportDataSetting.isEnabled = true
            exportDataSetting.text = getString(R.string.export_data)  // ✅ CHANGED
        }
    }
    private fun fetchHabitsFromApi(): List<Habit>? {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(HABITS_API_URL)
            connection = url.openConnection() as HttpURLConnection

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

    private fun startSaveFileIntent(csvContent: String) {
        tempCsvContent = csvContent

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, "HabitMate_Export_${System.currentTimeMillis()}.csv")
        }

        createDocumentLauncher.launch(intent)
    }

    private fun writeCsvToUri(uri: Uri, csvContent: String) = activityScope.launch {
        try {
            withContext(Dispatchers.IO) {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(csvContent.toByteArray())
                }
            }
            runOnUiThread {
                Toast.makeText(
                    this@SettingsActivity,
                    getString(R.string.export_success),  // ✅ CHANGED
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Log.e("Export", "Writing to URI failed: ${e.message}", e)
            runOnUiThread {
                Toast.makeText(
                    this@SettingsActivity,
                    getString(R.string.file_save_failed, e.message),  // ✅ CHANGED
                    Toast.LENGTH_LONG
                ).show()
            }
        } finally {
            tempCsvContent = null
        }
    }


}