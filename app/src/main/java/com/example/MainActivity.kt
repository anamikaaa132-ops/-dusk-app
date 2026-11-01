package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DuskTab
import com.example.ui.DuskViewModel
import com.example.ui.components.MoonPhaseIcon
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.screens.YourDataScreen
import com.example.ui.theme.DuskTheme

class MainActivity : ComponentActivity() {

    private val viewModel: DuskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DuskTheme {
                DuskApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun DuskApp(viewModel: DuskViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val allCheckIns by viewModel.allCheckIns.collectAsState()
    val draft by viewModel.draft.collectAsState()
    val availableTags by viewModel.availableTags.collectAsState()
    val insightRange by viewModel.insightRange.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    // Back handling: return to Today screen if currently on other tabs
    BackHandler(enabled = currentTab != DuskTab.TODAY) {
        viewModel.selectTab(DuskTab.TODAY)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 520.dp)
                    .padding(horizontal = 18.dp)
            ) {
                // Brand Header: "Dusk"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MoonPhaseIcon(mood = 4, size = 22.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dusk",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            fontSize = 20.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Segmented Tabs Pill Navigation
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        DuskTab.values().forEach { tab ->
                            val isSelected = currentTab == tab
                            val label = when (tab) {
                                DuskTab.TODAY -> "Today"
                                DuskTab.INSIGHTS -> "Insights"
                                DuskTab.DATA -> "Your data"
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.selectTab(tab) }
                                    .testTag("tab_${tab.name.lowercase()}")
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Tab Content with smooth Crossfade
                Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                    when (tab) {
                        DuskTab.TODAY -> {
                            TodayScreen(
                                draft = draft,
                                allCheckIns = allCheckIns,
                                availableTags = availableTags,
                                toastMessage = toastMessage,
                                onSelectMood = { viewModel.selectMood(it) },
                                onSetSleep = { viewModel.setSleep(it) },
                                onToggleTag = { viewModel.toggleTag(it) },
                                onAddCustomTag = { viewModel.addCustomTag(it) },
                                onSetNote = { viewModel.setNote(it) },
                                onSaveCheckIn = { viewModel.saveCheckIn() },
                                onResetToToday = { viewModel.resetToToday() },
                                onNavigateToInsights = { viewModel.selectTab(DuskTab.INSIGHTS) }
                            )
                        }

                        DuskTab.INSIGHTS -> {
                            InsightsScreen(
                                checkIns = allCheckIns,
                                rangeDays = insightRange,
                                onRangeChange = { viewModel.setInsightRange(it) },
                                onLoadSampleData = { viewModel.loadSampleData() },
                                onEditEntry = { viewModel.loadEntryForEdit(it) },
                                onDeleteEntry = { viewModel.deleteCheckIn(it) }
                            )
                        }

                        DuskTab.DATA -> {
                            YourDataScreen(
                                checkIns = allCheckIns,
                                csvContent = viewModel.generateCsvString(allCheckIns),
                                onLoadSampleData = { viewModel.loadSampleData() },
                                onClearAllData = { viewModel.clearAllData() }
                            )
                        }
                    }
                }
            }
        }
    }
}
