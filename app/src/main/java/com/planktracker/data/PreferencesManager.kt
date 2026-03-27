package com.planktracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "plank_settings")

class PreferencesManager(private val context: Context) {

    companion object {
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val DAILY_INCREMENT = intPreferencesKey("daily_increment")
        val BASE_TARGET = intPreferencesKey("base_target")
        val START_DATE = longPreferencesKey("start_date")
    }

    val reminderHour: Flow<Int> = context.dataStore.data.map { it[REMINDER_HOUR] ?: 8 }
    val reminderMinute: Flow<Int> = context.dataStore.data.map { it[REMINDER_MINUTE] ?: 0 }
    val reminderEnabled: Flow<Boolean> = context.dataStore.data.map { it[REMINDER_ENABLED] ?: false }
    val dailyIncrement: Flow<Int> = context.dataStore.data.map { it[DAILY_INCREMENT] ?: 5 }
    val baseTarget: Flow<Int> = context.dataStore.data.map { it[BASE_TARGET] ?: 30 }
    val startDate: Flow<Long> = context.dataStore.data.map { it[START_DATE] ?: System.currentTimeMillis() }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[REMINDER_HOUR] = hour
            prefs[REMINDER_MINUTE] = minute
        }
    }

    suspend fun setReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { it[REMINDER_ENABLED] = enabled }
    }

    suspend fun setDailyIncrement(seconds: Int) {
        context.dataStore.edit { it[DAILY_INCREMENT] = seconds }
    }

    suspend fun setBaseTarget(seconds: Int) {
        context.dataStore.edit { it[BASE_TARGET] = seconds }
    }

    suspend fun setStartDate(millis: Long) {
        context.dataStore.edit { it[START_DATE] = millis }
    }

    suspend fun resetProgress() {
        context.dataStore.edit { prefs ->
            prefs[START_DATE] = System.currentTimeMillis()
            prefs[BASE_TARGET] = 30
        }
    }
}
