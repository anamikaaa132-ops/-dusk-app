package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CheckInRepository(private val dao: CheckInDao) {

    val allCheckIns: Flow<List<CheckInEntity>> = dao.getAllCheckIns()

    suspend fun getCheckIn(date: String): CheckInEntity? {
        return dao.getCheckInByDate(date)
    }

    suspend fun saveCheckIn(checkIn: CheckInEntity) {
        dao.upsertCheckIn(checkIn)
    }

    suspend fun insertAll(checkIns: List<CheckInEntity>) {
        dao.insertAll(checkIns)
    }

    suspend fun deleteCheckIn(date: String) {
        dao.deleteByDate(date)
    }

    suspend fun clearAll() {
        dao.deleteAll()
    }

    suspend fun loadSampleData() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        var s = 7
        val r = {
            s = (s * 9301 + 49297) % 233280
            s.toFloat() / 233280f
        }

        val sampleList = mutableListOf<CheckInEntity>()
        // Generate 21 days of realistic sample check-ins
        for (i in 20 downTo 0) {
            val entryCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val dateStr = dateFormat.format(entryCal.time)
            val rawSleep = 5f + r() * 4f
            val sleepHours = Math.round(rawSleep * 2f) / 2f
            val rawMood = 1.2f + sleepHours * 0.38f + (r() - 0.5f) * 1.6f
            val mood = Math.max(1, Math.min(5, Math.round(rawMood)))
            val tags = when {
                mood >= 4 -> if (sleepHours >= 7.5f) "Calm;Grateful;Energized" else "Calm;Grateful"
                mood == 3 -> if (r() > 0.5f) "Social" else "Tired"
                else -> if (sleepHours < 6.5f) "Tired;Stressed" else "Anxious"
            }
            val note = when {
                mood == 5 -> "Felt fully rested and present."
                mood == 4 -> "Solid sleep, clear focus today."
                mood == 3 -> "Normal busy day."
                mood == 2 -> "Woke up groggy, slow morning."
                else -> "Tossing and turning last night."
            }
            sampleList.add(
                CheckInEntity(
                    date = dateStr,
                    mood = mood,
                    sleepHours = sleepHours,
                    tags = tags,
                    note = note
                )
            )
        }
        dao.insertAll(sampleList)
    }

    companion object {
        fun getTodayIsoDate(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return sdf.format(Date())
        }

        fun formatDisplayDate(isoDate: String): String {
            return try {
                val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
                val parsed = parser.parse(isoDate)
                if (parsed != null) formatter.format(parsed) else isoDate
            } catch (e: Exception) {
                isoDate
            }
        }

        fun formatLongDate(isoDate: String): String {
            return try {
                val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val formatter = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
                val parsed = parser.parse(isoDate)
                if (parsed != null) formatter.format(parsed) else isoDate
            } catch (e: Exception) {
                isoDate
            }
        }
    }
}
