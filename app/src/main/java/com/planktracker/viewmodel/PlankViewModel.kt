package com.planktracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.planktracker.data.PlankDao
import com.planktracker.data.PlankDatabase
import com.planktracker.data.PlankRecord
import com.planktracker.data.PreferencesManager
import com.planktracker.notification.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class TimerState { IDLE, RUNNING, PAUSED, STOPPED }

data class PlankUiState(
    val todayTarget: Int = 30,
    val todayRecord: PlankRecord? = null,
    val streak: Int = 0,
    val bestDuration: Int = 0,
    val totalPlanks: Int = 0,
    val elapsedSeconds: Int = 0,
    val timerState: TimerState = TimerState.IDLE,
    val allRecords: List<PlankRecord> = emptyList(),
    // settings
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val reminderEnabled: Boolean = false,
    val dailyIncrement: Int = 5,
    val baseTarget: Int = 30,
)

class PlankViewModel(application: Application) : AndroidViewModel(application) {

    private val db: PlankDao = PlankDatabase.getInstance(application).plankDao()
    private val prefs = PreferencesManager(application)

    private val _uiState = MutableStateFlow(PlankUiState())
    val uiState: StateFlow<PlankUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private val today get() = LocalDate.now().toString()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                prefs.baseTarget,
                prefs.dailyIncrement,
                prefs.startDate,
                prefs.reminderHour,
                prefs.reminderMinute,
                prefs.reminderEnabled,
                db.getAllRecords(),
                db.observeRecordForDate(today)
            ) { values ->
                val baseTarget = values[0] as Int
                val dailyIncrement = values[1] as Int
                val startDate = values[2] as Long
                val reminderHour = values[3] as Int
                val reminderMinute = values[4] as Int
                val reminderEnabled = values[5] as Boolean
                @Suppress("UNCHECKED_CAST")
                val allRecords = values[6] as List<PlankRecord>
                val todayRecord = values[7] as PlankRecord?

                val streak = calculateStreak(allRecords)
                val todayTarget = baseTarget + (streak * dailyIncrement)
                val bestDuration = allRecords.maxOfOrNull { it.durationSeconds } ?: 0

                PlankUiState(
                    todayTarget = todayTarget,
                    todayRecord = todayRecord,
                    streak = streak,
                    bestDuration = bestDuration,
                    totalPlanks = allRecords.size,
                    elapsedSeconds = _uiState.value.elapsedSeconds,
                    timerState = _uiState.value.timerState,
                    allRecords = allRecords,
                    reminderHour = reminderHour,
                    reminderMinute = reminderMinute,
                    reminderEnabled = reminderEnabled,
                    dailyIncrement = dailyIncrement,
                    baseTarget = baseTarget,
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun calculateStreak(records: List<PlankRecord>): Int {
        if (records.isEmpty()) return 0
        val recordDates = records.map { LocalDate.parse(it.date) }.toSortedSet()
        var streak = 0
        var checkDate = LocalDate.now()

        // if today is not done, start checking from yesterday
        if (!recordDates.contains(checkDate)) {
            checkDate = checkDate.minusDays(1)
        }

        while (recordDates.contains(checkDate)) {
            streak++
            checkDate = checkDate.minusDays(1)
        }
        return streak
    }

    fun startTimer() {
        if (_uiState.value.timerState == TimerState.RUNNING) return
        _uiState.update { it.copy(timerState = TimerState.RUNNING) }
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(timerState = TimerState.PAUSED) }
    }

    fun resumeTimer() {
        _uiState.update { it.copy(timerState = TimerState.RUNNING) }
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(timerState = TimerState.STOPPED) }
    }

    fun resetTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(timerState = TimerState.IDLE, elapsedSeconds = 0) }
    }

    fun savePlank() {
        val state = _uiState.value
        if (state.elapsedSeconds == 0) return
        viewModelScope.launch {
            val record = PlankRecord(
                date = today,
                durationSeconds = state.elapsedSeconds,
                targetSeconds = state.todayTarget,
            )
            db.insert(record)
            _uiState.update { it.copy(timerState = TimerState.IDLE, elapsedSeconds = 0) }
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            prefs.setReminderTime(hour, minute)
            if (_uiState.value.reminderEnabled) {
                NotificationHelper.scheduleDailyReminder(getApplication(), hour, minute)
            }
        }
    }

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setReminderEnabled(enabled)
            val state = _uiState.value
            if (enabled) {
                NotificationHelper.scheduleDailyReminder(getApplication(), state.reminderHour, state.reminderMinute)
            } else {
                NotificationHelper.cancelReminder(getApplication())
            }
        }
    }

    fun setDailyIncrement(seconds: Int) {
        viewModelScope.launch { prefs.setDailyIncrement(seconds) }
    }

    fun setBaseTarget(seconds: Int) {
        viewModelScope.launch { prefs.setBaseTarget(seconds) }
    }

    fun resetProgress() {
        viewModelScope.launch {
            db.deleteAll()
            prefs.resetProgress()
            _uiState.update { it.copy(timerState = TimerState.IDLE, elapsedSeconds = 0) }
        }
    }
}
