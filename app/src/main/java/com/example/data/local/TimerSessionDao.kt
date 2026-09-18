package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerSessionDao {
    @Query("SELECT * FROM timer_sessions ORDER BY endTime DESC")
    fun getAllSessions(): Flow<List<TimerSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TimerSessionEntity): Long

    @Query("DELETE FROM timer_sessions WHERE id = :id")
    suspend fun deleteSession(id: Long)

    @Query("DELETE FROM timer_sessions")
    suspend fun clearAll()
}
