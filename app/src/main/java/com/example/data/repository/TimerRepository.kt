package com.example.data.repository

import com.example.data.local.TimerSessionDao
import com.example.data.local.TimerSessionEntity
import kotlinx.coroutines.flow.Flow

class TimerRepository(private val timerSessionDao: TimerSessionDao) {
    val allSessions: Flow<List<TimerSessionEntity>> = timerSessionDao.getAllSessions()

    suspend fun insertSession(session: TimerSessionEntity): Long {
        return timerSessionDao.insertSession(session)
    }

    suspend fun deleteSession(id: Long) {
        timerSessionDao.deleteSession(id)
    }

    suspend fun clearAll() {
        timerSessionDao.clearAll()
    }
}
