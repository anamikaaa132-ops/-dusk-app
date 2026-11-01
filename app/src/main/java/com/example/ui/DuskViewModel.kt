package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CheckInEntity
import com.example.data.CheckInRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class DuskTab {
    TODAY,
    INSIGHTS,
    DATA
}

data class CheckInDraft(
    val date: String = CheckInRepository.getTodayIsoDate(),
    val mood: Int = 0,
    val sleep: Float = 7.0f,
    val tags: Set<String> = emptySet(),
    val note: String = ""
)

class DuskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CheckInRepository
    init {
        val db = AppDatabase.getInstance(application)
        repository = CheckInRepository(db.checkInDao())
    }

    val allCheckIns: StateFlow<List<CheckInEntity>> = repository.allCheckIns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentTab = MutableStateFlow(DuskTab.TODAY)
    val currentTab: StateFlow<DuskTab> = _currentTab.asStateFlow()

    private val _draft = MutableStateFlow(CheckInDraft())
    val draft: StateFlow<CheckInDraft> = _draft.asStateFlow()

    private val _insightRange = MutableStateFlow(7)
    val insightRange: StateFlow<Int> = _insightRange.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _availableTags = MutableStateFlow(
        listOf("Calm", "Stressed", "Anxious", "Energized", "Tired", "Social", "Grateful")
    )
    val availableTags: StateFlow<List<String>> = _availableTags.asStateFlow()

    init {
        // Check if there is an existing entry for today and load it
        viewModelScope.launch {
            val today = CheckInRepository.getTodayIsoDate()
            val existing = repository.getCheckIn(today)
            if (existing != null) {
                _draft.value = CheckInDraft(
                    date = existing.date,
                    mood = existing.mood,
                    sleep = existing.sleepHours,
                    tags = existing.getTagList().toSet(),
                    note = existing.note
                )
            }
        }
    }

    fun selectTab(tab: DuskTab) {
        _currentTab.value = tab
        _toastMessage.value = null
    }

    fun selectMood(mood: Int) {
        _draft.value = _draft.value.copy(mood = mood)
        _toastMessage.value = null
    }

    fun setSleep(hours: Float) {
        val rounded = Math.round(hours * 2f) / 2f
        _draft.value = _draft.value.copy(sleep = rounded.coerceIn(0f, 12f))
    }

    fun toggleTag(tag: String) {
        val current = _draft.value.tags.toMutableSet()
        if (current.contains(tag)) {
            current.remove(tag)
        } else {
            current.add(tag)
        }
        _draft.value = _draft.value.copy(tags = current)
    }

    fun addCustomTag(newTag: String) {
        val trimmed = newTag.trim()
        if (trimmed.isNotEmpty() && !_availableTags.value.contains(trimmed)) {
            _availableTags.value = _availableTags.value + trimmed
            toggleTag(trimmed)
        }
    }

    fun setNote(note: String) {
        if (note.length <= 200) {
            _draft.value = _draft.value.copy(note = note)
        }
    }

    fun saveCheckIn() {
        val currentDraft = _draft.value
        if (currentDraft.mood == 0) {
            _toastMessage.value = "Pick a moon first."
            return
        }

        viewModelScope.launch {
            val entity = CheckInEntity(
                date = currentDraft.date,
                mood = currentDraft.mood,
                sleepHours = currentDraft.sleep,
                tags = currentDraft.tags.joinToString(";"),
                note = currentDraft.note.trim(),
                updatedAt = System.currentTimeMillis()
            )
            repository.saveCheckIn(entity)
            _toastMessage.value = "Saved. See Insights for your trends."
        }
    }

    fun loadEntryForEdit(entry: CheckInEntity) {
        _draft.value = CheckInDraft(
            date = entry.date,
            mood = entry.mood,
            sleep = entry.sleepHours,
            tags = entry.getTagList().toSet(),
            note = entry.note
        )
        _currentTab.value = DuskTab.TODAY
        _toastMessage.value = "Loaded check-in for ${CheckInRepository.formatDisplayDate(entry.date)}"
    }

    fun resetToToday() {
        val today = CheckInRepository.getTodayIsoDate()
        viewModelScope.launch {
            val existing = repository.getCheckIn(today)
            if (existing != null) {
                _draft.value = CheckInDraft(
                    date = existing.date,
                    mood = existing.mood,
                    sleep = existing.sleepHours,
                    tags = existing.getTagList().toSet(),
                    note = existing.note
                )
            } else {
                _draft.value = CheckInDraft(date = today)
            }
        }
    }

    fun deleteCheckIn(date: String) {
        viewModelScope.launch {
            repository.deleteCheckIn(date)
            if (_draft.value.date == date) {
                resetToToday()
            }
            _toastMessage.value = "Deleted entry for ${CheckInRepository.formatDisplayDate(date)}"
        }
    }

    fun setInsightRange(range: Int) {
        _insightRange.value = range
    }

    fun loadSampleData() {
        viewModelScope.launch {
            repository.loadSampleData()
            _toastMessage.value = "Loaded 21 days of sample check-ins."
            resetToToday()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
            _draft.value = CheckInDraft(date = CheckInRepository.getTodayIsoDate())
            _toastMessage.value = "All check-ins deleted."
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun generateCsvString(entries: List<CheckInEntity>): String {
        val sorted = entries.sortedBy { it.date }
        val sb = StringBuilder()
        sb.append("date,mood,sleep_hours,tags,note\n")
        for (e in sorted) {
            val escapedNote = "\"" + e.note.replace("\"", "\"\"") + "\""
            val escapedTags = "\"" + e.tags.replace("\"", "\"\"") + "\""
            sb.append("${e.date},${e.mood},${e.sleepHours},$escapedTags,$escapedNote\n")
        }
        return sb.toString()
    }
}
