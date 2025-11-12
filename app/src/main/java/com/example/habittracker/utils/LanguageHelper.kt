package com.example.habittracker.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.*

object LanguageHelper {

    private const val SELECTED_LANGUAGE = "selected_language"

    fun setLanguage(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        // Save preference
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(SELECTED_LANGUAGE, languageCode).apply()
    }

    fun loadLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString(SELECTED_LANGUAGE, "en") ?: "en"
    }

    fun applyLanguage(context: Context) {
        val languageCode = loadLanguage(context)
        setLanguage(context, languageCode)
    }

    fun updateBaseContextLocale(context: Context): Context {
        val languageCode = loadLanguage(context)
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }



}