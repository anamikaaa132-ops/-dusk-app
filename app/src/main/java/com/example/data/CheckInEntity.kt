package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "check_ins")
data class CheckInEntity(
    @PrimaryKey
    val date: String, // ISO date "yyyy-MM-dd"
    val mood: Int, // 1: Rough, 2: Low, 3: Okay, 4: Good, 5: Great
    val sleepHours: Float, // 0.0 - 12.0
    val tags: String = "", // Semicolon or comma delimited
    val note: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getTagList(): List<String> {
        if (tags.isBlank()) return emptyList()
        return tags.split(";").map { it.trim() }.filter { it.isNotEmpty() }
    }
}
