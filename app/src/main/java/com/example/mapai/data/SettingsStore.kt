package com.example.mapai.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.compose.ui.graphics.Color

data class AppSettings(
    val serverUrl: String = "http://10.0.2.2:3000",
    val language: String = "en",
    val units: String = "metric",
    val mapSource: Int = MAP_SOURCE_OSM,
    val themeColor: Int = THEME_BLUE,
    val textColor: Int = TEXT_AUTO,
    val darkTheme: Boolean = false,
    val autoConfig: Boolean = true,
    val offlineOnly: Boolean = false,
    val sosContactName: String = "",
    val sosContactPhone: String = ""
) {
    companion object {
        const val MAP_SOURCE_OSM = 0
        const val MAP_SOURCE_TOPO = 1
        const val MAP_SOURCE_CYCLE = 2

        const val THEME_BLUE = 0
        const val THEME_GREEN = 1
        const val THEME_RED = 2
        const val THEME_PURPLE = 3
        const val THEME_ORANGE = 4
        const val THEME_TEAL = 5

        const val TEXT_AUTO = 0
        const val TEXT_WHITE = 1
        const val TEXT_BLACK = 2
        const val TEXT_YELLOW = 3

        fun themeColor(value: Int): Color = when (value) {
            THEME_GREEN -> Color(0xFF2E7D32)
            THEME_RED -> Color(0xFFC62828)
            THEME_PURPLE -> Color(0xFF6A1B9A)
            THEME_ORANGE -> Color(0xFFEF6C00)
            THEME_TEAL -> Color(0xFF00897B)
            else -> Color(0xFF1565C0)
        }

        fun textColor(value: Int): Color = when (value) {
            TEXT_WHITE -> Color.White
            TEXT_BLACK -> Color.Black
            TEXT_YELLOW -> Color(0xFFFFEB3B)
            else -> Color.Unspecified
        }
    }
}

object SettingsStore {
    private const val NAME = "mapai_settings"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }

    fun get(): AppSettings = AppSettings(
        serverUrl = prefs.getString("serverUrl", "http://10.0.2.2:3000")!!,
        language = prefs.getString("language", "en")!!,
        units = prefs.getString("units", "metric")!!,
        mapSource = prefs.getInt("mapSource", AppSettings.MAP_SOURCE_OSM),
        themeColor = prefs.getInt("themeColor", AppSettings.THEME_BLUE),
        textColor = prefs.getInt("textColor", AppSettings.TEXT_AUTO),
        darkTheme = prefs.getBoolean("darkTheme", false),
        autoConfig = prefs.getBoolean("autoConfig", true),
        offlineOnly = prefs.getBoolean("offlineOnly", false),
        sosContactName = prefs.getString("sosContactName", "")!!,
        sosContactPhone = prefs.getString("sosContactPhone", "")!!
    )

    fun save(settings: AppSettings) {
        prefs.edit {
            putString("serverUrl", settings.serverUrl)
            putString("language", settings.language)
            putString("units", settings.units)
            putInt("mapSource", settings.mapSource)
            putInt("themeColor", settings.themeColor)
            putInt("textColor", settings.textColor)
            putBoolean("darkTheme", settings.darkTheme)
            putBoolean("autoConfig", settings.autoConfig)
            putBoolean("offlineOnly", settings.offlineOnly)
            putString("sosContactName", settings.sosContactName)
            putString("sosContactPhone", settings.sosContactPhone)
        }
    }
}
