package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CheckInEntity
import com.example.data.CheckInRepository
import com.example.ui.components.MOOD_LABELS
import com.example.ui.components.MoonPhaseIcon
import com.example.ui.components.SleepMoodChart
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun InsightsScreen(
    checkIns: List<CheckInEntity>,
    rangeDays: Int,
    onRangeChange: (Int) -> Unit,
    onLoadSampleData: () -> Unit,
    onEditEntry: (CheckInEntity) -> Unit,
    onDeleteEntry: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var entryToDelete by remember { mutableStateOf<CheckInEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Insights",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        if (checkIns.size < 3) {
            // Less than 3 check-ins state
            Text(
                text = "Log three days to see how your sleep and mood move together.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "You have ${checkIns.size} of 3 check-ins so far.",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Consistent daily reflections unlock trends comparing high sleep versus low sleep moods.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedButton(
                onClick = onLoadSampleData,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("preview_sample_button")
            ) {
                Text(
                    text = "Preview with sample data",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            // 3 or more check-ins: Full insights view!
            Text(
                text = "How sleep and mood line up.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
            )

            // Segmented range buttons (7 days vs 30 days)
            Row(
                modifier = Modifier
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(7 to "7 days", 30 to "30 days").forEach { (days, label) ->
                    val isSelected = rangeDays == days
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        border = BorderStroke(
                            1.5.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onRangeChange(days) }
                            .testTag("range_toggle_$days")
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // Dual chart card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SleepMoodChart(
                        checkIns = checkIns,
                        rangeDays = rangeDays
                    )
                }
            }

            // Calculation of correlation insights
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -(rangeDays - 1))
            val fromDate = sdf.format(cal.time)

            val inRangeEntries = checkIns.filter { it.date >= fromDate && it.mood > 0 }
            val longSleep = inRangeEntries.filter { it.sleepHours >= 7.0f }
            val shortSleep = inRangeEntries.filter { it.sleepHours < 7.0f }

            val insightText = remember(inRangeEntries) {
                if (longSleep.size >= 3 && shortSleep.size >= 3) {
                    val avgLong = longSleep.map { it.mood }.average()
                    val avgShort = shortSleep.map { it.mood }.average()
                    val diff = avgLong - avgShort
                    when {
                        diff > 0.3 -> "Your mood averages %.1f after 7+ hours of sleep and %.1f after less. That’s %.1f points higher."
                            .format(avgLong, avgShort, diff)
                        diff < -0.3 -> "Your mood is actually higher after shorter nights (%.1f vs %.1f). Sleep may not be your main driver right now."
                            .format(avgShort, avgLong)
                        else -> "No clear link between sleep and mood yet (%.1f vs %.1f). Other factors may matter more."
                            .format(avgLong, avgShort)
                    }
                } else {
                    "Keep logging. A clearer pattern shows up after about two weeks of consistent check-ins."
                }
            }

            // Insight Narrative Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MoonPhaseIcon(mood = 4, size = 30.dp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = insightText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            lineHeight = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Quick Stats summary card
            val avgMoodStr = if (inRangeEntries.isNotEmpty()) {
                "%.1f".format(inRangeEntries.map { it.mood }.average())
            } else "–"
            val avgSleepStr = if (inRangeEntries.isNotEmpty()) {
                "%.1f h".format(inRangeEntries.map { it.sleepHours.toDouble() }.average())
            } else "–"

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Stat 1
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${inRangeEntries.size}",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "check-ins",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Stat 2
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = avgMoodStr,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "avg mood",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Stat 3
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = avgSleepStr,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "avg sleep",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Recent Check-ins List Card
            val recentEntries = remember(checkIns) {
                checkIns.sortedByDescending { it.date }.take(8)
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Recent Check-ins",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    recentEntries.forEachIndexed { index, entry ->
                        if (index > 0) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                thickness = 1.dp
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEditEntry(entry) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            MoonPhaseIcon(
                                mood = entry.mood,
                                size = 28.dp,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${CheckInRepository.formatDisplayDate(entry.date)} · ${MOOD_LABELS.getOrNull(entry.mood - 1) ?: "Mood ${entry.mood}"}, ${entry.sleepHours} h",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                val tagsList = entry.getTagList()
                                val tagText = tagsList.joinToString(", ")
                                val noteText = entry.note.trim()

                                if (tagText.isNotEmpty() || noteText.isNotEmpty()) {
                                    val fullDetail = buildString {
                                        if (tagText.isNotEmpty()) append(tagText)
                                        if (tagText.isNotEmpty() && noteText.isNotEmpty()) append(" – ")
                                        if (noteText.isNotEmpty()) append(noteText)
                                    }
                                    Text(
                                        text = fullDetail,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }

                            Row {
                                IconButton(
                                    onClick = { onEditEntry(entry) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit check-in",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { entryToDelete = entry },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete check-in",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete single entry confirmation dialog
    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Delete check-in?") },
            text = { Text("Are you sure you want to remove the check-in for ${CheckInRepository.formatDisplayDate(entry.date)}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteEntry(entry.date)
                        entryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
