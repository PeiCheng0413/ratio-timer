package com.example.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CycleRecord
import com.example.data.model.CycleType
import com.example.data.model.RatioOption
import com.example.data.model.TimerMode
import com.example.data.model.formatSeconds
import com.example.data.model.formatSecondsDetailed
import com.example.ui.components.HistoryBottomSheet
import com.example.ui.components.RatioSettingDialog
import com.example.ui.components.SessionPieChart
import com.example.ui.viewmodel.TimerUiState
import com.example.ui.viewmodel.TimerViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TimerScreen(
    viewModel: TimerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val historySessions by viewModel.historySessions.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // When in landscape mode, automatically enter OLED pure black mode with light gray clock & screen kept awake
    if (isLandscape) {
        OledLandscapeScreen(
            viewModel = viewModel,
            uiState = uiState,
            modifier = modifier
        )
        return
    }

    var showRatioDialog by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "工休比例計時器",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "動態換算 • 超時累計 • 圓餅圖統計",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showRatioDialog = true },
                        modifier = Modifier.testTag("ratio_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "設定比例",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { showHistorySheet = true },
                        modifier = Modifier.testTag("history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "歷史紀錄",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Break Finished Alert Banner
                AnimatedVisibility(
                    visible = uiState.showBreakEndReminder,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    BreakFinishedReminderBanner(
                        overtimeSeconds = uiState.overtimeSeconds,
                        onResumeWork = { viewModel.resumeWork() },
                        onDismiss = { viewModel.dismissReminder() }
                    )
                }

                when (uiState.timerMode) {
                    TimerMode.IDLE -> {
                        IdleContent(
                            ratioPercentage = uiState.ratioPercentage,
                            onStartClick = { viewModel.startTimer() },
                            onSelectRatio = { viewModel.setRatio(it) },
                            onOpenRatioDialog = { showRatioDialog = true }
                        )
                    }

                    TimerMode.WORKING -> {
                        WorkingContent(
                            uiState = uiState,
                            onTakeBreak = { viewModel.takeBreak() },
                            onTogglePause = { viewModel.togglePause() },
                            onFinish = { viewModel.finishSession() }
                        )
                    }

                    TimerMode.BREAKING -> {
                        BreakingContent(
                            uiState = uiState,
                            onResumeWork = { viewModel.resumeWork() },
                            onFinish = { viewModel.finishSession() }
                        )
                    }

                    TimerMode.FINISHED -> {
                        FinishedSummaryContent(
                            uiState = uiState,
                            onStartNew = { viewModel.resetToIdle() },
                            onOpenHistory = { showHistorySheet = true }
                        )
                    }
                }

                // Completed Cycles in this session (if any)
                if (uiState.completedCycles.isNotEmpty() && uiState.timerMode != TimerMode.FINISHED) {
                    CycleHistorySection(completedCycles = uiState.completedCycles)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showRatioDialog) {
        RatioSettingDialog(
            currentPercentage = uiState.ratioPercentage,
            onRatioSelected = { viewModel.setRatio(it) },
            onDismiss = { showRatioDialog = false }
        )
    }

    if (showHistorySheet) {
        HistoryBottomSheet(
            sessions = historySessions,
            onDeleteSession = { viewModel.deleteHistorySession(it) },
            onDismiss = { showHistorySheet = false }
        )
    }
}

// ----------------------------------------------------------------------------
// Banner: Break Finished Reminder
// ----------------------------------------------------------------------------
@Composable
fun BreakFinishedReminderBanner(
    overtimeSeconds: Long,
    onResumeWork: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("break_reminder_banner"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFEF2F2) // Soft warm red
        ),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFEF4444))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "休息時間已結束！",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "關閉提醒",
                        tint = Color(0xFF991B1B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (overtimeSeconds > 0) {
                    "目前超時：+${formatSeconds(overtimeSeconds)}。依規定，超時時間將完整計入休息總時間中。"
                } else {
                    "預定休息時間已倒數完畢，請點擊下方按鈕開始下一圈工作。"
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF7F1D1D)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onResumeWork,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("banner_resume_button")
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("立即開始工作 (第 ${2} 圈)")
            }
        }
    }
}

// ----------------------------------------------------------------------------
// Screen State: IDLE
// ----------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IdleContent(
    ratioPercentage: Int,
    onStartClick: () -> Unit,
    onSelectRatio: (Int) -> Unit,
    onOpenRatioDialog: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "準備開始專注",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "計時器第一圈會記錄您的工作時間。當您按下「休息」時，會根據設定的比例自動換算成休息倒數！",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }

    // Ratio Selection Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "工休比例設定",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "目前設定：休息佔工作 ${ratioPercentage}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                FilledTonalButton(
                    onClick = onOpenRatioDialog,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("自訂")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RatioOption.DEFAULT_PRESETS.forEach { preset ->
                    val isSelected = ratioPercentage == preset.percentage
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectRatio(preset.percentage) }
                    ) {
                        Text(
                            text = preset.title,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "推薦 5:1 (20%)：每專注工作 25 分鐘，可獲得 5 分鐘休息倒數。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // Start Button
    Button(
        onClick = onStartClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag("start_timer_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "開始計時 (第 1 圈工作)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
    }

    // OLED Landscape tip
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ScreenRotation,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "橫放手機將自動進入 OLED 全黑省電時鐘，以淡灰色顯示且螢幕常亮不待機。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ----------------------------------------------------------------------------
// Screen State: WORKING
// ----------------------------------------------------------------------------
@Composable
fun WorkingContent(
    uiState: TimerUiState,
    onTakeBreak: () -> Unit,
    onTogglePause: () -> Unit,
    onFinish: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("working_timer_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            if (uiState.isPaused) MaterialTheme.colorScheme.outlineVariant else Color(0xFF38BDF8)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = if (uiState.isPaused) Color(0xFFF1F5F9) else Color(0xFFE0F2FE)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (uiState.isPaused) Color(0xFF64748B) else Color(0xFF0284C7))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (uiState.isPaused) "已暫停 • 第 ${uiState.currentCycleNumber} 圈" else "專注工作進行中 • 第 ${uiState.currentCycleNumber} 圈",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (uiState.isPaused) Color(0xFF64748B) else Color(0xFF0369A1)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Large Digital Clock
            Text(
                text = formatSeconds(uiState.currentCycleSeconds),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 58.sp
                ),
                color = if (uiState.isPaused) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF0F172A),
                modifier = Modifier.testTag("timer_display_text")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Real-time calculated break preview
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFECFDF5),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Coffee,
                        contentDescription = null,
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "若現在休息：將享有 ${formatSeconds(uiState.estimatedBreakForCurrentWork)} 休息時間 (${uiState.ratioPercentage}%)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF065F46)
                    )
                }
            }

            if (uiState.totalWorkSeconds > uiState.currentCycleSeconds) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "本日累計工作：${formatSeconds(uiState.totalWorkSeconds)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Action Buttons
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Primary Break Button
        Button(
            onClick = onTakeBreak,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("break_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF059669) // Emerald green
            )
        ) {
            Icon(imageVector = Icons.Default.Coffee, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "進入休息 (倒數 ${formatSeconds(uiState.estimatedBreakForCurrentWork)})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilledTonalButton(
                onClick = onTogglePause,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("pause_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = if (uiState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (uiState.isPaused) "繼續" else "暫停")
            }

            OutlinedButton(
                onClick = onFinish,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("end_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("結束計時")
            }
        }
    }
}

// ----------------------------------------------------------------------------
// Screen State: BREAKING
// ----------------------------------------------------------------------------
@Composable
fun BreakingContent(
    uiState: TimerUiState,
    onResumeWork: () -> Unit,
    onFinish: () -> Unit
) {
    val isOvertime = uiState.currentCycleSeconds == 0L
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isOvertime) 1.04f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(pulseScale)
            .testTag("breaking_timer_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOvertime) Color(0xFFFFFBEB) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            if (isOvertime) Color(0xFFF59E0B) else Color(0xFF10B981)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = if (isOvertime) Color(0xFFFEF3C7) else Color(0xFFD1FAE5)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (isOvertime) Icons.Default.NotificationsActive else Icons.Default.Coffee,
                        contentDescription = null,
                        tint = if (isOvertime) Color(0xFFD97706) else Color(0xFF059669),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOvertime) "⚠️ 休息已超時 • 計入休息總時間" else "休息倒數中 • 第 ${uiState.currentCycleNumber} 圈",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isOvertime) Color(0xFF92400E) else Color(0xFF065F46)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progress or countdown display
            if (!isOvertime) {
                val progress = if (uiState.targetBreakSeconds > 0) {
                    uiState.currentCycleSeconds.toFloat() / uiState.targetBreakSeconds.toFloat()
                } else 0f

                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(180.dp),
                        strokeWidth = 12.dp,
                        color = Color(0xFF10B981),
                        trackColor = Color(0xFFE2E8F0)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formatSeconds(uiState.currentCycleSeconds),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 42.sp
                            ),
                            color = Color(0xFF0F172A),
                            modifier = Modifier.testTag("break_countdown_text")
                        )
                        Text(
                            text = "原預定 ${formatSeconds(uiState.targetBreakSeconds)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Overtime Display
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "+${formatSeconds(uiState.overtimeSeconds)}",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 54.sp
                        ),
                        color = Color(0xFFD97706),
                        modifier = Modifier.testTag("overtime_text")
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "本圈休息已耗費：${formatSeconds(uiState.targetBreakSeconds + uiState.overtimeSeconds)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF92400E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "提示：休息結束後若未立即開始，超時時間也會如實算進休息時間之中。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(10.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // Action Buttons
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Resume Work Button
        Button(
            onClick = onResumeWork,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("resume_work_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "繼續計時 (開始第 ${uiState.currentCycleNumber + 1} 圈工作)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        OutlinedButton(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("end_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(imageVector = Icons.Default.Stop, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("結束計時並查看圓餅圖統計")
        }
    }
}

// ----------------------------------------------------------------------------
// Screen State: FINISHED (Pie Chart + Statistics)
// ----------------------------------------------------------------------------
@Composable
fun FinishedSummaryContent(
    uiState: TimerUiState,
    onStartNew: () -> Unit,
    onOpenHistory: () -> Unit
) {
    val plannedBreakTotal = (uiState.totalBreakSeconds - uiState.totalOvertimeSeconds).coerceAtLeast(0L)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎉 本次計時已完成！",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "共完成 ${uiState.currentCycleNumber} 圈循環 • 數據已自動儲存",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Summary Metric Tiles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Work Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBAE6FD))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "工作總時間",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF0369A1)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = formatSeconds(uiState.totalWorkSeconds),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = Color(0xFF0C4A6E)
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "佔比 %.1f%%", uiState.actualWorkRatioPct),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF0284C7)
                    )
                }
            }

            // Break Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "休息總時間 (含超時)",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF047857)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = formatSeconds(uiState.totalBreakSeconds),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = Color(0xFF064E3B)
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "佔比 %.1f%%", uiState.actualBreakRatioPct),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF059669)
                    )
                }
            }
        }

        // The Required Pie Chart (圓餅圖)
        SessionPieChart(
            workSeconds = uiState.totalWorkSeconds,
            plannedBreakSeconds = plannedBreakTotal,
            overtimeSeconds = uiState.totalOvertimeSeconds,
            modifier = Modifier.fillMaxWidth()
        )

        // Detailed Cycle Breakdown Card
        if (uiState.completedCycles.isNotEmpty()) {
            CycleHistorySection(completedCycles = uiState.completedCycles)
        }

        // Action Buttons
        Button(
            onClick = onStartNew,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("new_session_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "開始新計時",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        OutlinedButton(
            onClick = onOpenHistory,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(imageVector = Icons.Default.History, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("查看歷史紀錄與過去圖表")
        }
    }
}

// ----------------------------------------------------------------------------
// Section: Cycle Breakdown List
// ----------------------------------------------------------------------------
@Composable
fun CycleHistorySection(completedCycles: List<CycleRecord>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "每圈詳細記錄",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "共 ${completedCycles.size} 項",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                completedCycles.forEach { cycle ->
                    val isWork = cycle.type == CycleType.WORK
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (isWork) Color(0xFF0284C7) else Color(0xFF10B981))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isWork) "第 ${cycle.cycleNumber} 圈 工作" else "第 ${cycle.cycleNumber} 圈 休息",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!isWork && cycle.overtimeSeconds > 0) {
                                        Text(
                                            text = "含超時 ${formatSeconds(cycle.overtimeSeconds)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFD97706)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = cycle.formattedDuration,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = if (isWork) Color(0xFF0284C7) else Color(0xFF10B981)
                            )
                        }
                    }
                }
            }
        }
    }
}
