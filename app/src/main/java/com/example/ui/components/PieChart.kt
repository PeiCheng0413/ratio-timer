package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.formatSeconds
import java.util.Locale
import kotlin.math.atan2

data class PieSlice(
    val id: String,
    val label: String,
    val value: Long, // in seconds
    val color: Color,
    val description: String = ""
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SessionPieChart(
    workSeconds: Long,
    plannedBreakSeconds: Long,
    overtimeSeconds: Long,
    modifier: Modifier = Modifier,
    detailedOvertimeSplit: Boolean = true
) {
    val totalBreakSeconds = plannedBreakSeconds + overtimeSeconds
    val totalSeconds = workSeconds + totalBreakSeconds

    if (totalSeconds <= 0L) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "尚未有計時統計資料",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    // Prepare slices
    val slices = remember(workSeconds, plannedBreakSeconds, overtimeSeconds, detailedOvertimeSplit) {
        buildList {
            add(
                PieSlice(
                    id = "work",
                    label = "工作時間",
                    value = workSeconds,
                    color = Color(0xFF0284C7), // Sky blue
                    description = "全神貫注的工作時長"
                )
            )
            if (detailedOvertimeSplit && overtimeSeconds > 0L) {
                add(
                    PieSlice(
                        id = "break_planned",
                        label = "標準休息",
                        value = plannedBreakSeconds,
                        color = Color(0xFF10B981), // Emerald green
                        description = "按比例設定計算的倒數休息"
                    )
                )
                add(
                    PieSlice(
                        id = "break_overtime",
                        label = "超時休息",
                        value = overtimeSeconds,
                        color = Color(0xFFF59E0B), // Warm Amber
                        description = "倒數結束後額外休息 (已計入休息總時間)"
                    )
                )
            } else {
                add(
                    PieSlice(
                        id = "break_total",
                        label = "休息時間 (含超時)",
                        value = totalBreakSeconds,
                        color = Color(0xFF10B981),
                        description = if (overtimeSeconds > 0) "包含倒數時間與超時 ${formatSeconds(overtimeSeconds)}" else "倒數休息時間"
                    )
                )
            }
        }
    }

    var selectedSliceId by remember { mutableStateOf<String?>(null) }
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(workSeconds, totalBreakSeconds) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pie_chart_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "工休比例圓餅圖",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "超時時間已完整計入休息總時間中",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Donut Chart Canvas with Center Details
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .testTag("pie_chart_canvas"),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(220.dp)
                        .pointerInput(slices) {
                            detectTapGestures { tapOffset ->
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val dx = tapOffset.x - center.x
                                val dy = tapOffset.y - center.y
                                var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                if (angle < 0) angle += 360f

                                // Standard start angle is -90 degrees (12 o'clock)
                                val normalizedAngle = (angle + 90f) % 360f

                                var accumulated = 0f
                                for (slice in slices) {
                                    val sweep = (slice.value.toFloat() / totalSeconds.toFloat()) * 360f
                                    if (normalizedAngle >= accumulated && normalizedAngle < accumulated + sweep) {
                                        selectedSliceId = if (selectedSliceId == slice.id) null else slice.id
                                        break
                                    }
                                    accumulated += sweep
                                }
                            }
                        }
                ) {
                    val strokeWidth = 38.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                    val arcSize = Size(diameter, diameter)

                    var startAngle = -90f
                    val totalSweep = 360f * animationProgress.value

                    slices.forEach { slice ->
                        val sliceSweep = (slice.value.toFloat() / totalSeconds.toFloat()) * totalSweep
                        val isSelected = selectedSliceId == slice.id

                        // Gap separation between slices if multiple slices exist
                        val gap = if (slices.size > 1) 2.5f else 0f
                        val actualSweep = (sliceSweep - gap).coerceAtLeast(0.1f)

                        drawArc(
                            color = if (isSelected) slice.color else slice.color.copy(alpha = if (selectedSliceId != null) 0.4f else 0.95f),
                            startAngle = startAngle + gap / 2f,
                            sweepAngle = actualSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(
                                width = if (isSelected) strokeWidth + 6.dp.toPx() else strokeWidth,
                                cap = StrokeCap.Round
                            )
                        )

                        startAngle += sliceSweep
                    }
                }

                // Center Label
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    val activeSlice = slices.find { it.id == selectedSliceId }
                    if (activeSlice != null) {
                        Text(
                            text = activeSlice.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = activeSlice.color,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = formatSeconds(activeSlice.value),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val pct = (activeSlice.value.toDouble() / totalSeconds.toDouble()) * 100.0
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f%%", pct),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "總耗時",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatSeconds(totalSeconds),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val workPct = (workSeconds.toDouble() / totalSeconds.toDouble()) * 100.0
                        Text(
                            text = String.format(Locale.getDefault(), "工作佔 %.1f%%", workPct),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF0284C7),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Legend Badges
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                slices.forEach { slice ->
                    val isSelected = selectedSliceId == slice.id
                    val pct = (slice.value.toDouble() / totalSeconds.toDouble()) * 100.0
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) slice.color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, slice.color) else null,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedSliceId = if (selectedSliceId == slice.id) null else slice.id
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(slice.color)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = slice.label,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${formatSeconds(slice.value)} (${String.format(Locale.getDefault(), "%.1f%%", pct)})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (overtimeSeconds > 0L) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF3C7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "休息包含超時時間 ${formatSeconds(overtimeSeconds)}，已如實計入休息總時間 (${formatSeconds(totalBreakSeconds)}) 中。",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }
        }
    }
}
