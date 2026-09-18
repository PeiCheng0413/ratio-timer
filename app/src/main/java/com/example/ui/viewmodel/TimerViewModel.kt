package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.TimerSessionEntity
import com.example.data.model.CycleRecord
import com.example.data.model.CycleType
import com.example.data.model.RatioOption
import com.example.data.model.TimerMode
import com.example.data.repository.TimerRepository
import com.example.util.ReminderManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class TimerUiState(
    val timerMode: TimerMode = TimerMode.IDLE,
    val currentCycleNumber: Int = 1,
    val currentCycleSeconds: Long = 0L,
    val targetBreakSeconds: Long = 0L,
    val overtimeSeconds: Long = 0L,
    val ratioPercentage: Int = 20, // Default 5:1 (20%)
    val isPaused: Boolean = false,
    val totalWorkSeconds: Long = 0L,
    val totalBreakSeconds: Long = 0L,
    val totalOvertimeSeconds: Long = 0L,
    val completedCycles: List<CycleRecord> = emptyList(),
    val showBreakEndReminder: Boolean = false,
    val sessionStartTime: Long = 0L,
    val sessionEndTime: Long = 0L
) {
    val estimatedBreakForCurrentWork: Long
        get() {
            if (currentCycleSeconds <= 0L) return 0L
            return ((currentCycleSeconds * ratioPercentage) / 100).coerceAtLeast(1L)
        }

    val totalSessionSeconds: Long
        get() = totalWorkSeconds + totalBreakSeconds

    val actualWorkRatioPct: Double
        get() {
            val total = totalSessionSeconds
            return if (total > 0) (totalWorkSeconds.toDouble() / total.toDouble()) * 100.0 else 0.0
        }

    val actualBreakRatioPct: Double
        get() {
            val total = totalSessionSeconds
            return if (total > 0) (totalBreakSeconds.toDouble() / total.toDouble()) * 100.0 else 0.0
        }
}

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TimerRepository
    val historySessions: StateFlow<List<TimerSessionEntity>>

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        val db = AppDatabase.getDatabase(application)
        repository = TimerRepository(db.timerSessionDao())
        historySessions = repository.allSessions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun setRatio(percentage: Int) {
        _uiState.update { it.copy(ratioPercentage = percentage.coerceIn(5, 200)) }
    }

    fun startTimer() {
        _uiState.update {
            it.copy(
                timerMode = TimerMode.WORKING,
                currentCycleNumber = 1,
                currentCycleSeconds = 0L,
                targetBreakSeconds = 0L,
                overtimeSeconds = 0L,
                totalWorkSeconds = 0L,
                totalBreakSeconds = 0L,
                totalOvertimeSeconds = 0L,
                completedCycles = emptyList(),
                showBreakEndReminder = false,
                isPaused = false,
                sessionStartTime = System.currentTimeMillis(),
                sessionEndTime = 0L
            )
        }
        startTicker()
    }

    fun takeBreak() {
        val current = _uiState.value
        if (current.timerMode != TimerMode.WORKING) return

        val workSec = current.currentCycleSeconds
        val plannedBreak = ((workSec * current.ratioPercentage) / 100).coerceAtLeast(1L)

        val workCycle = CycleRecord(
            cycleNumber = current.currentCycleNumber,
            type = CycleType.WORK,
            durationSeconds = workSec
        )

        _uiState.update {
            it.copy(
                timerMode = TimerMode.BREAKING,
                targetBreakSeconds = plannedBreak,
                currentCycleSeconds = plannedBreak,
                overtimeSeconds = 0L,
                completedCycles = it.completedCycles + workCycle,
                showBreakEndReminder = false,
                isPaused = false
            )
        }
    }

    fun resumeWork() {
        val current = _uiState.value
        if (current.timerMode != TimerMode.BREAKING) return

        // Calculate actual break duration = targetBreakSeconds + overtimeSeconds
        val actualBreakSec = current.targetBreakSeconds + current.overtimeSeconds

        val breakCycle = CycleRecord(
            cycleNumber = current.currentCycleNumber,
            type = CycleType.BREAK,
            durationSeconds = actualBreakSec,
            plannedBreakSeconds = current.targetBreakSeconds,
            overtimeSeconds = current.overtimeSeconds
        )

        _uiState.update {
            it.copy(
                timerMode = TimerMode.WORKING,
                currentCycleNumber = it.currentCycleNumber + 1,
                currentCycleSeconds = 0L,
                targetBreakSeconds = 0L,
                overtimeSeconds = 0L,
                completedCycles = it.completedCycles + breakCycle,
                showBreakEndReminder = false,
                isPaused = false
            )
        }
    }

    fun finishSession() {
        stopTicker()
        val current = _uiState.value
        val updatedCompleted = current.completedCycles.toMutableList()

        var finalWork = current.totalWorkSeconds
        var finalBreak = current.totalBreakSeconds
        var finalOvertime = current.totalOvertimeSeconds

        if (current.timerMode == TimerMode.WORKING && current.currentCycleSeconds > 0) {
            updatedCompleted.add(
                CycleRecord(
                    cycleNumber = current.currentCycleNumber,
                    type = CycleType.WORK,
                    durationSeconds = current.currentCycleSeconds
                )
            )
        } else if (current.timerMode == TimerMode.BREAKING) {
            val actualBreak = current.targetBreakSeconds + current.overtimeSeconds - current.currentCycleSeconds.coerceAtLeast(0)
            updatedCompleted.add(
                CycleRecord(
                    cycleNumber = current.currentCycleNumber,
                    type = CycleType.BREAK,
                    durationSeconds = actualBreak,
                    plannedBreakSeconds = current.targetBreakSeconds,
                    overtimeSeconds = current.overtimeSeconds
                )
            )
        }

        val endTime = System.currentTimeMillis()

        _uiState.update {
            it.copy(
                timerMode = TimerMode.FINISHED,
                completedCycles = updatedCompleted,
                totalWorkSeconds = finalWork,
                totalBreakSeconds = finalBreak,
                totalOvertimeSeconds = finalOvertime,
                showBreakEndReminder = false,
                sessionEndTime = endTime
            )
        }

        // Save session summary to Room DB
        viewModelScope.launch {
            if (finalWork > 0L || finalBreak > 0L) {
                val entity = TimerSessionEntity(
                    startTime = current.sessionStartTime,
                    endTime = endTime,
                    totalWorkSeconds = finalWork,
                    totalBreakSeconds = finalBreak,
                    totalOvertimeSeconds = finalOvertime,
                    ratioPercentage = current.ratioPercentage,
                    cycleCount = current.currentCycleNumber,
                    cyclesSummary = "${updatedCompleted.size} 個週期"
                )
                repository.insertSession(entity)
            }
        }
    }

    fun togglePause() {
        _uiState.update { it.copy(isPaused = !it.isPaused) }
    }

    fun dismissReminder() {
        _uiState.update { it.copy(showBreakEndReminder = false) }
    }

    fun resetToIdle() {
        stopTicker()
        _uiState.update {
            TimerUiState(ratioPercentage = it.ratioPercentage)
        }
    }

    fun deleteHistorySession(id: Long) {
        viewModelScope.launch {
            repository.deleteSession(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    private fun startTicker() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val current = _uiState.value
                if (current.isPaused) continue

                when (current.timerMode) {
                    TimerMode.WORKING -> {
                        _uiState.update {
                            it.copy(
                                currentCycleSeconds = it.currentCycleSeconds + 1,
                                totalWorkSeconds = it.totalWorkSeconds + 1
                            )
                        }
                    }

                    TimerMode.BREAKING -> {
                        if (current.currentCycleSeconds > 0) {
                            val nextSec = current.currentCycleSeconds - 1
                            val reminderNow = (nextSec == 0L)
                            _uiState.update {
                                it.copy(
                                    currentCycleSeconds = nextSec,
                                    totalBreakSeconds = it.totalBreakSeconds + 1,
                                    showBreakEndReminder = if (reminderNow) true else it.showBreakEndReminder
                                )
                            }
                            if (reminderNow) {
                                ReminderManager.triggerBreakFinishedReminder(getApplication())
                            }
                        } else {
                            // Overtime countdown: counts up and adds to totalBreakSeconds and totalOvertimeSeconds!
                            _uiState.update {
                                it.copy(
                                    overtimeSeconds = it.overtimeSeconds + 1,
                                    totalOvertimeSeconds = it.totalOvertimeSeconds + 1,
                                    totalBreakSeconds = it.totalBreakSeconds + 1
                                )
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    private fun stopTicker() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopTicker()
    }
}
