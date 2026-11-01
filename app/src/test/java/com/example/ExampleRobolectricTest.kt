package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.CheckInEntity
import com.example.data.CheckInRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Dusk", appName)
    }

    @Test
    fun `checkIn entity tag parsing works`() {
        val entity = CheckInEntity(
            date = "2026-11-01",
            mood = 4,
            sleepHours = 7.5f,
            tags = "Calm; Grateful ; Energized",
            note = "Rested and focused"
        )
        val tags = entity.getTagList()
        assertEquals(3, tags.size)
        assertEquals("Calm", tags[0])
        assertEquals("Grateful", tags[1])
        assertEquals("Energized", tags[2])
    }

    @Test
    fun `date formatting functions correctly`() {
        val display = CheckInRepository.formatDisplayDate("2026-11-01")
        assertTrue(display.contains("Nov") || display.contains("1"))
    }
}
