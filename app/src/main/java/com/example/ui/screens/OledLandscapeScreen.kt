package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TimerMode
import com.example.data.model.formatSeconds
import com.example.ui.components.SessionPieChart
import com.example.ui.viewmodel.TimerUiState
import com.example.ui.viewmodel.TimerViewModel

fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

/**
 * OLED Pure Black Landscape Screen (OLED 全黑省電橫向模式)
 * - Pure #000000 background (OLED pixels completely turned off for max energy saving)
 * - Muted light gray typography (low glare, completely battery-efficient)
 * - Automatically keeps the screen awake without entering standby (FLAG_KEEP_SCREEN_ON)
 */
@Composable
fun OledLandscapeScreen(
    viewModel: TimerViewModel,
    uiState: TimerUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Keep screen awake (不會進入待機) while in OLED landscape mode
    DisposableEffect(Unit) {
        val activity = context.findActivity()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Color definitions for OLED pure black theme
    val oledBlack = Color(0xFF000000) // True black #000000
    val oledDimGray = Color(0xFF64748B) // Subtle dim text
    val oledLightGray = Color(0xFF94A3B8) // Primary soft light gray for timer
    val oledBorder = Color(0xFF1E293B) // Dark subtle border
    val oledSurface = Color(0xFF0A0A0A) // Almost-black dark surface for subtle buttons

    val isOvertime = uiState.timerMode == TimerMode.BREAKING && uiState.currentCycleSeconds == 0L

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(oledBlack)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .testTag("oled_landscape_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Status Bar in OLED landscape
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: OLED Mode Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF38BDF8).copy(alpha = 0.6f))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "OLED 全黑省電模式",
                        style = MaterialTheme.typography.labelSmall,
                        color = oledDimGray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = when (uiState.timerMode) {
                            TimerMode.IDLE -> "待命中"
                            TimerMode.WORKING -> if (uiState.isPaused) "工作中 (已暫停)" else "第 ${uiState.currentCycleNumber} 圈 工作中"
                            TimerMode.BREAKING -> if (isOvertime) "第 ${uiState.currentCycleNumber} 圈 休息 (超時)" else "第 ${uiState.currentCycleNumber} 圈 休息倒數中"
                            TimerMode.FINISHED -> "已完成"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOvertime) Color(0xFFF59E0B).copy(alpha = 0.8f) else oledLightGray,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Right: Anti-Standby Indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = oledDimGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "螢幕常亮（不待機）",
                        style = MaterialTheme.typography.labelSmall,
                        color = oledDimGray
                    )
                }
            }

            // Center Content: Large Dim Light-Gray Digital Clock & Status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                when (uiState.timerMode) {
                    TimerMode.IDLE -> {
                        Text(
                            text = "00:00",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Light,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 86.sp
                            ),
                            color = oledLightGray.copy(alpha = 0.7f),
                            modifier = Modifier.testTag("timer_display_text")
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "比例設定：工作 : 休息 = ${uiState.ratioPercentage}% (約 ${100 / uiState.ratioPercentage.coerceAtLeast(1)}:1)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = oledDimGray
                        )
                    }

                    TimerMode.WORKING -> {
                        Text(
                            text = formatSeconds(uiState.currentCycleSeconds),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 92.sp,
                                letterSpacing = 2.sp
                            ),
                            color = oledLightGray,
                            modifier = Modifier.testTag("timer_display_text")
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "第 ${uiState.currentCycleNumber} 圈工作 • 現在休息享有 ${formatSeconds(uiState.estimatedBreakForCurrentWork)} 倒數",
                            style = MaterialTheme.typography.bodyMedium,
                            color = oledDimGray
                        )
                    }

                    TimerMode.BREAKING -> {
                        if (!isOvertime) {
                            Text(
                                text = formatSeconds(uiState.currentCycleSeconds),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 92.sp,
                                    letterSpacing = 2.sp
                                ),
                                color = oledLightGray,
                                modifier = Modifier.testTag("break_countdown_text")
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "休息倒數中 • 預定 ${formatSeconds(uiState.targetBreakSeconds)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = oledDimGray
                            )
                        } else {
                            Text(
                                text = "+${formatSeconds(uiState.overtimeSeconds)}",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 88.sp,
                                    letterSpacing = 2.sp
                                ),
                                color = Color(0xFFF59E0B).copy(alpha = 0.85f),
                                modifier = Modifier.testTag("overtime_text")
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "⚠️ 休息已超時，超時時間累計至休息總時間 (${formatSeconds(uiState.targetBreakSeconds + uiState.overtimeSeconds)})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFD97706).copy(alpha = 0.8f)
                            )
                        }
                    }

                    TimerMode.FINISHED -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(32.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "工作總時間",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = oledDimGray
                                )
                                Text(
                                    text = formatSeconds(uiState.totalWorkSeconds),
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 44.sp
                                    ),
                                    color = oledLightGray
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(60.dp)
                                    .background(oledBorder)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "休息總時間 (含超時)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = oledDimGray
                                )
                                Text(
                                    text = formatSeconds(uiState.totalBreakSeconds),
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 44.sp
                                    ),
                                    color = oledLightGray
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "總耗時 ${formatSeconds(uiState.totalSessionSeconds)} • 完成 ${uiState.currentCycleNumber} 圈",
                            style = MaterialTheme.typography.bodySmall,
                            color = oledDimGray
                        )
                    }
                }
            }

            // Break Finished High-contrast Alert in OLED mode
            if (uiState.showBreakEndReminder) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E1010),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Color(0xFFF87171),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "休息時間已結束！請點擊右方按鈕繼續工作",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFCA5A5)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.dismissReminder() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "我知道了",
                                tint = Color(0xFFFCA5A5)
                            )
                        }
                    }
                }
            }

            // Bottom Minimal Low-Glare Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (uiState.timerMode) {
                    TimerMode.IDLE -> {
                        OutlinedButton(
                            onClick = { viewModel.startTimer() },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("start_timer_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = oledLightGray,
                                containerColor = oledSurface
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, oledBorder)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("開始第 1 圈工作", fontWeight = FontWeight.Medium)
                        }
                    }

                    TimerMode.WORKING -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Break Button
                            OutlinedButton(
                                onClick = { viewModel.takeBreak() },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("break_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF34D399), // Soft mint
                                    containerColor = oledSurface
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF065F46).copy(alpha = 0.7f))
                            ) {
                                Icon(imageVector = Icons.Default.Coffee, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("進入休息 (倒數 ${formatSeconds(uiState.estimatedBreakForCurrentWork)})", fontWeight = FontWeight.Medium)
                            }

                            // Pause Button
                            OutlinedButton(
                                onClick = { viewModel.togglePause() },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("pause_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = oledLightGray,
                                    containerColor = oledSurface
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, oledBorder)
                            ) {
                                Icon(
                                    imageVector = if (uiState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (uiState.isPaused) "繼續" else "暫停")
                            }

                            // End Button
                            OutlinedButton(
                                onClick = { viewModel.finishSession() },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("end_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFF87171),
                                    containerColor = oledSurface
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7F1D1D).copy(alpha = 0.7f))
                            ) {
                                Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("結束")
                            }
                        }
                    }

                    TimerMode.BREAKING -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Resume Work Button
                            OutlinedButton(
                                onClick = { viewModel.resumeWork() },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("resume_work_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF38BDF8),
                                    containerColor = oledSurface
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0369A1).copy(alpha = 0.7f))
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("繼續工作 (第 ${uiState.currentCycleNumber + 1} 圈)", fontWeight = FontWeight.Medium)
                            }

                            // End Button
                            OutlinedButton(
                                onClick = { viewModel.finishSession() },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("end_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFF87171),
                                    containerColor = oledSurface
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7F1D1D).copy(alpha = 0.7f))
                            ) {
                                Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("結束")
                            }
                        }
                    }

                    TimerMode.FINISHED -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.resetToIdle() },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("new_session_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = oledLightGray,
                                    containerColor = oledSurface
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, oledBorder)
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("開始新計時")
                            }
                        }
                    }
                }
            }
        }
    }
}
