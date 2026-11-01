package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {
    @Query("SELECT * FROM check_ins ORDER BY date DESC")
    fun getAllCheckIns(): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins WHERE date = :date LIMIT 1")
    suspend fun getCheckInByDate(date: String): CheckInEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCheckIn(checkIn: CheckInEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(checkIns: List<CheckInEntity>)

    @Query("DELETE FROM check_ins WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("DELETE FROM check_ins")
    suspend fun deleteAll()
}
