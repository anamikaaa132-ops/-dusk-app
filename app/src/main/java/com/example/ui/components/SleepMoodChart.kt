package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CheckInEntity
import com.example.data.CheckInRepository
import com.example.ui.theme.DuskDarkSleep
import com.example.ui.theme.DuskLightSleep
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun SleepMoodChart(
    checkIns: List<CheckInEntity>,
    rangeDays: Int = 7,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val sleepBarColor = if (isDark) DuskDarkSleep else DuskLightSleep
    val moodLineColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
    val textMuteColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()

    // Generate list of ISO dates for the range [today - (rangeDays - 1) .. today]
    val (dates, entriesByDate) = remember(checkIns, rangeDays) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -(rangeDays - 1))
        for (i in 0 until rangeDays) {
            list.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        val map = checkIns.associateBy { it.date }
        Pair(list, map)
    }

    var selectedIndex by remember(rangeDays) { mutableStateOf<Int?>(null) }
    val selectedEntry = selectedIndex?.let { idx ->
        if (idx in dates.indices) entriesByDate[dates[idx]] else null
    }
    val selectedDate = selectedIndex?.let { idx ->
        if (idx in dates.indices) dates[idx] else null
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Inspection tooltip if user touches a day
        AnimatedVisibility(
            visible = selectedIndex != null && selectedDate != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = selectedDate?.let { CheckInRepository.formatDisplayDate(it) } ?: "",
                            style = MaterialTheme.typography.labelMedium,
                            color = textMuteColor
                        )
                        if (selectedEntry != null) {
                            Text(
                                text = "${MOOD_LABELS.getOrNull(selectedEntry.mood - 1) ?: "Mood ${selectedEntry.mood}"} · ${selectedEntry.sleepHours}h sleep",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Text(
                                text = "No check-in for this day",
                                style = MaterialTheme.typography.bodySmall,
                                color = textMuteColor
                            )
                        }
                    }

                    if (selectedEntry != null) {
                        MoonPhaseIcon(mood = selectedEntry.mood, size = 26.dp)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .testTag("chart_canvas_box")
                .pointerInput(dates) {
                    detectTapGestures(
                        onTap = { tapOffset ->
                            val paddingLeft = 32.dp.toPx()
                            val paddingRight = 10.dp.toPx()
                            val chartWidth = size.width - paddingLeft - paddingRight
                            val slotWidth = chartWidth / dates.size
                            val relativeX = tapOffset.x - paddingLeft
                            if (relativeX in 0f..chartWidth) {
                                val clickedIdx = (relativeX / slotWidth).toInt().coerceIn(0, dates.size - 1)
                                selectedIndex = if (selectedIndex == clickedIdx) null else clickedIdx
                            } else {
                                selectedIndex = null
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                val paddingLeft = 32.dp.toPx()
                val paddingRight = 10.dp.toPx()
                val paddingTop = 12.dp.toPx()
                val paddingBottom = 26.dp.toPx()

                val innerW = size.width - paddingLeft - paddingRight
                val innerH = size.height - paddingTop - paddingBottom
                val n = dates.size
                val slotWidth = innerW / n

                // Mood level gridlines (1 to 5)
                fun yForMood(m: Int): Float {
                    return paddingTop + innerH - ((m - 1) / 4f) * innerH
                }

                // Sleep hours to Y coordinate (up to 10 hours)
                fun yForSleep(hours: Float): Float {
                    val clamped = hours.coerceIn(0f, 10f)
                    return paddingTop + innerH - (clamped / 10f) * innerH
                }

                fun xForIndex(i: Int): Float {
                    return paddingLeft + slotWidth * i + slotWidth / 2f
                }

                // 1. Draw horizontal gridlines and mood numbers 1..5
                for (m in 1..5) {
                    val gy = yForMood(m)
                    drawLine(
                        color = gridColor,
                        start = Offset(paddingLeft, gy),
                        end = Offset(size.width - paddingRight, gy),
                        strokeWidth = 1f
                    )
                    val labelResult = textMeasurer.measure(
                        text = "$m",
                        style = TextStyle(color = textMuteColor, fontSize = 10.sp)
                    )
                    drawText(
                        textLayoutResult = labelResult,
                        topLeft = Offset(paddingLeft - labelResult.size.width - 6.dp.toPx(), gy - labelResult.size.height / 2f)
                    )
                }

                // 2. Draw sleep bars
                dates.forEachIndexed { i, d ->
                    val entry = entriesByDate[d]
                    if (entry != null && entry.sleepHours > 0f) {
                        val barTop = yForSleep(entry.sleepHours)
                        val barH = (paddingTop + innerH) - barTop
                        val barW = (slotWidth * 0.65f).coerceAtLeast(3f)
                        val barX = xForIndex(i) - barW / 2f
                        val isDaySelected = selectedIndex == i

                        drawRoundRect(
                            color = if (isDaySelected) sleepBarColor else sleepBarColor.copy(alpha = 0.85f),
                            topLeft = Offset(barX, barTop),
                            size = Size(barW, barH),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }

                // 3. Draw mood line & dots
                val moodPath = Path()
                var hasPrevPoint = false
                val moodPoints = mutableListOf<Pair<Int, Offset>>()

                dates.forEachIndexed { i, d ->
                    val entry = entriesByDate[d]
                    if (entry != null && entry.mood in 1..5) {
                        val pt = Offset(xForIndex(i), yForMood(entry.mood))
                        moodPoints.add(Pair(i, pt))
                        if (!hasPrevPoint) {
                            moodPath.moveTo(pt.x, pt.y)
                            hasPrevPoint = true
                        } else {
                            moodPath.lineTo(pt.x, pt.y)
                        }
                    } else {
                        // Skip gap or keep line continuous?
                        // If gap, line ends
                        hasPrevPoint = false
                    }
                }

                // Stroke the mood path
                drawPath(
                    path = moodPath,
                    color = moodLineColor,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Draw dots on each recorded mood entry
                moodPoints.forEach { (idx, pt) ->
                    val isDaySelected = selectedIndex == idx
                    val radius = if (isDaySelected) 6.dp.toPx() else (if (n > 14) 3.5f.dp.toPx() else 4.5f.dp.toPx())
                    drawCircle(
                        color = surfaceColor,
                        radius = radius + 2.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = moodLineColor,
                        radius = radius,
                        center = pt
                    )
                }

                // 4. X-axis date labels: first date and Today
                val firstDateLabel = CheckInRepository.formatDisplayDate(dates.first())
                val lastDateLabel = "Today"

                val firstResult = textMeasurer.measure(
                    text = firstDateLabel,
                    style = TextStyle(color = textMuteColor, fontSize = 11.sp)
                )
                drawText(
                    textLayoutResult = firstResult,
                    topLeft = Offset(paddingLeft, size.height - firstResult.size.height)
                )

                val lastResult = textMeasurer.measure(
                    text = lastDateLabel,
                    style = TextStyle(color = textMuteColor, fontSize = 11.sp)
                )
                drawText(
                    textLayoutResult = lastResult,
                    topLeft = Offset(size.width - paddingRight - lastResult.size.width, size.height - lastResult.size.height)
                )
            }
        }

        // Legend below chart
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(moodLineColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Mood (1–5)",
                    style = MaterialTheme.typography.bodySmall,
                    color = textMuteColor
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(sleepBarColor, RoundedCornerShape(3.dp))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sleep (up to 10 h)",
                    style = MaterialTheme.typography.bodySmall,
                    color = textMuteColor
                )
            }
        }
    }
}
