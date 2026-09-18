package com.example.data.model

import java.util.Locale

enum class CycleType {
    WORK,
    BREAK
}

enum class TimerMode {
    IDLE,
    WORKING,
    BREAKING,
    FINISHED
}

data class CycleRecord(
    val cycleNumber: Int,
    val type: CycleType,
    val durationSeconds: Long,
    val plannedBreakSeconds: Long = 0L,
    val overtimeSeconds: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedDuration: String
        get() = formatSeconds(durationSeconds)

    val formattedPlanned: String
        get() = formatSeconds(plannedBreakSeconds)

    val formattedOvertime: String
        get() = "+${formatSeconds(overtimeSeconds)}"
}

data class RatioOption(
    val title: String,
    val description: String,
    val percentage: Int // break percentage of work time (e.g. 20 for 5:1)
) {
    companion object {
        val DEFAULT_PRESETS = listOf(
            RatioOption("5 : 1", "工作 5 分 / 休息 1 分 (20%)", 20),
            RatioOption("4 : 1", "工作 4 分 / 休息 1 分 (25%)", 25),
            RatioOption("3 : 1", "工作 3 分 / 休息 1 分 (33%)", 33),
            RatioOption("2 : 1", "工作 2 分 / 休息 1 分 (50%)", 50),
            RatioOption("1 : 1", "工作 1 分 / 休息 1 分 (100%)", 100)
        )
    }
}

fun formatSeconds(totalSeconds: Long): String {
    val s = if (totalSeconds < 0) 0 else totalSeconds
    val hours = s / 3600
    val minutes = (s % 3600) / 60
    val seconds = s % 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}

fun formatSecondsDetailed(totalSeconds: Long): String {
    val s = if (totalSeconds < 0) 0 else totalSeconds
    val hours = s / 3600
    val minutes = (s % 3600) / 60
    val seconds = s % 60
    return buildString {
        if (hours > 0) append("${hours}小時 ")
        if (minutes > 0 || hours > 0) append("${minutes}分 ")
        append("${seconds}秒")
    }
}
