package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timer_sessions")
data class TimerSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val totalWorkSeconds: Long,
    val totalBreakSeconds: Long,
    val totalOvertimeSeconds: Long,
    val ratioPercentage: Int,
    val cycleCount: Int,
    val cyclesSummary: String
)
