package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sachinChatDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "sachinchat_user_preferences"
)

data class AppPreferencesState(
    val themeMode: String = "System", // System, Light, Dark
    val accentPreset: String = "Emerald", // Emerald, Ocean, Royal, Sunset
    val textScale: Float = 1.0f,
    val soundEffectsEnabled: Boolean = true,
    val backgroundMusicEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val pauseAllNotifications: Boolean = false,
    val reminderTime: String = "07:30 PM",
    val quietHoursRange: String = "10:00 PM – 07:00 AM",
    val notificationSound: Boolean = true,
    val notificationVibration: Boolean = true,
    val emailNotifications: Boolean = false,
    val dailyNotificationLimit: Int = 3,
    val enabledNotificationCategories: Set<String> = setOf(
        "Learning", "Achievements", "Friends", "AI Tutor", "Rewards", "System"
    ),
    val downloadedOfflineCourses: Set<String> = setOf("English"),
    val recentDictionarySearches: List<String> = listOf("Resilient", "Eloquent", "Collaborate")
)

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_PRESET = stringPreferencesKey("accent_preset")
        val TEXT_SCALE = floatPreferencesKey("text_scale")
        val SOUND_EFFECTS = booleanPreferencesKey("sound_effects")
        val BG_MUSIC = booleanPreferencesKey("bg_music")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val PAUSE_ALL_NOTIFICATIONS = booleanPreferencesKey("pause_all_notifications")
        val REMINDER_TIME = stringPreferencesKey("reminder_time")
        val QUIET_HOURS = stringPreferencesKey("quiet_hours")
        val NOTIF_SOUND = booleanPreferencesKey("notif_sound")
        val NOTIF_VIBRATION = booleanPreferencesKey("notif_vibration")
        val EMAIL_NOTIFS = booleanPreferencesKey("email_notifs")
        val DAILY_LIMIT = intPreferencesKey("daily_limit")
        val ENABLED_CATEGORIES = stringSetPreferencesKey("enabled_categories")
        val OFFLINE_COURSES = stringSetPreferencesKey("offline_courses")
        val RECENT_SEARCHES = stringPreferencesKey("recent_searches_csv")
    }

    val preferencesFlow: Flow<AppPreferencesState> = context.sachinChatDataStore.data.map { prefs ->
        val recentCsv = prefs[Keys.RECENT_SEARCHES] ?: "Resilient,Eloquent,Collaborate"
        AppPreferencesState(
            themeMode = prefs[Keys.THEME_MODE] ?: "System",
            accentPreset = prefs[Keys.ACCENT_PRESET] ?: "Emerald",
            textScale = prefs[Keys.TEXT_SCALE] ?: 1.0f,
            soundEffectsEnabled = prefs[Keys.SOUND_EFFECTS] ?: true,
            backgroundMusicEnabled = prefs[Keys.BG_MUSIC] ?: false,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            pauseAllNotifications = prefs[Keys.PAUSE_ALL_NOTIFICATIONS] ?: false,
            reminderTime = prefs[Keys.REMINDER_TIME] ?: "07:30 PM",
            quietHoursRange = prefs[Keys.QUIET_HOURS] ?: "10:00 PM – 07:00 AM",
            notificationSound = prefs[Keys.NOTIF_SOUND] ?: true,
            notificationVibration = prefs[Keys.NOTIF_VIBRATION] ?: true,
            emailNotifications = prefs[Keys.EMAIL_NOTIFS] ?: false,
            dailyNotificationLimit = prefs[Keys.DAILY_LIMIT] ?: 3,
            enabledNotificationCategories = prefs[Keys.ENABLED_CATEGORIES] ?: setOf(
                "Learning", "Achievements", "Friends", "AI Tutor", "Rewards", "System"
            ),
            downloadedOfflineCourses = prefs[Keys.OFFLINE_COURSES] ?: setOf("English"),
            recentDictionarySearches = recentCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
        )
    }

    suspend fun setThemeMode(mode: String) {
        context.sachinChatDataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun setAccentPreset(preset: String) {
        context.sachinChatDataStore.edit { it[Keys.ACCENT_PRESET] = preset }
    }

    suspend fun setTextScale(scale: Float) {
        context.sachinChatDataStore.edit { it[Keys.TEXT_SCALE] = scale.coerceIn(0.85f, 1.25f) }
    }

    suspend fun setSoundEffects(enabled: Boolean) {
        context.sachinChatDataStore.edit { it[Keys.SOUND_EFFECTS] = enabled }
    }

    suspend fun setBackgroundMusic(enabled: Boolean) {
        context.sachinChatDataStore.edit { it[Keys.BG_MUSIC] = enabled }
    }

    suspend fun updateNotificationSettings(
        enabled: Boolean? = null,
        pauseAll: Boolean? = null,
        reminderTime: String? = null,
        quietHours: String? = null,
        sound: Boolean? = null,
        vibration: Boolean? = null,
        email: Boolean? = null,
        dailyLimit: Int? = null,
        categories: Set<String>? = null
    ) {
        context.sachinChatDataStore.edit { prefs ->
            enabled?.let { prefs[Keys.NOTIFICATIONS_ENABLED] = it }
            pauseAll?.let { prefs[Keys.PAUSE_ALL_NOTIFICATIONS] = it }
            reminderTime?.let { prefs[Keys.REMINDER_TIME] = it }
            quietHours?.let { prefs[Keys.QUIET_HOURS] = it }
            sound?.let { prefs[Keys.NOTIF_SOUND] = it }
            vibration?.let { prefs[Keys.NOTIF_VIBRATION] = it }
            email?.let { prefs[Keys.EMAIL_NOTIFS] = it }
            dailyLimit?.let { prefs[Keys.DAILY_LIMIT] = it.coerceIn(1, 10) }
            categories?.let { prefs[Keys.ENABLED_CATEGORIES] = it }
        }
    }

    suspend fun toggleOfflineCourseDownload(languageName: String) {
        context.sachinChatDataStore.edit { prefs ->
            val current = prefs[Keys.OFFLINE_COURSES]?.toMutableSet() ?: mutableSetOf("English")
            if (current.contains(languageName)) {
                current.remove(languageName)
            } else {
                current.add(languageName)
            }
            prefs[Keys.OFFLINE_COURSES] = current
        }
    }

    suspend fun recordDictionarySearch(term: String) {
        val clean = term.trim().replace(",", "")
        if (clean.isBlank()) return
        context.sachinChatDataStore.edit { prefs ->
            val existing = (prefs[Keys.RECENT_SEARCHES] ?: "")
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() && !it.equals(clean, ignoreCase = true) }
            val updated = (listOf(clean) + existing).take(8)
            prefs[Keys.RECENT_SEARCHES] = updated.joinToString(",")
        }
    }
}
