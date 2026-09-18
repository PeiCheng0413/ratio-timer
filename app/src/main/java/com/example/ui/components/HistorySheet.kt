package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TimerSessionEntity
import com.example.data.model.formatSeconds
import com.example.data.model.formatSecondsDetailed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryBottomSheet(
    sessions: List<TimerSessionEntity>,
    onDeleteSession: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var inspectingSession by remember { mutableStateOf<TimerSessionEntity?>(null) }
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (inspectingSession != null) "歷史紀錄圖表詳情" else "歷史計時紀錄",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                if (inspectingSession != null) {
                    IconButton(onClick = { inspectingSession = null }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "關閉詳情")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (inspectingSession != null) {
                val s = inspectingSession!!
                val plannedBreak = (s.totalBreakSeconds - s.totalOvertimeSeconds).coerceAtLeast(0L)

                Column {
                    SessionPieChart(
                        workSeconds = s.totalWorkSeconds,
                        plannedBreakSeconds = plannedBreak,
                        overtimeSeconds = s.totalOvertimeSeconds,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "計時資訊",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "結束時間：${dateFormat.format(Date(s.endTime))}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "工作總計：${formatSecondsDetailed(s.totalWorkSeconds)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF0284C7),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "休息總計：${formatSecondsDetailed(s.totalBreakSeconds)} (含超時 ${formatSeconds(s.totalOvertimeSeconds)})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "設定比例：${s.ratioPercentage}% (約 ${100 / s.ratioPercentage.coerceAtLeast(1)} : 1)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "完成循環：${s.cycleCount} 圈",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else if (sessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "目前尚無歷史紀錄，完成計時後會自動儲存於此",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sessions, key = { it.id }) { item ->
                        val dateStr = dateFormat.format(Date(item.endTime))
                        val totalSec = item.totalWorkSeconds + item.totalBreakSeconds
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { inspectingSession = item },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dateStr,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "工作 ${formatSeconds(item.totalWorkSeconds)}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF0284C7)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "休息 ${formatSeconds(item.totalBreakSeconds)}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                    if (item.totalOvertimeSeconds > 0) {
                                        Text(
                                            text = "超時 ${formatSeconds(item.totalOvertimeSeconds)} (已計入休息)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFD97706)
                                        )
                                    }
                                    Text(
                                        text = "共 ${item.cycleCount} 圈 • 比例 ${item.ratioPercentage}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { inspectingSession = item }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PieChart,
                                            contentDescription = "查看圓餅圖",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteSession(item.id) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "刪除",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
