package com.mansi.focusway.data.repository

import com.mansi.focusway.data.database.FocusSessionDao
import com.mansi.focusway.data.database.FocusSessionEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repository for handling focus session operations
 */
class FocusSessionRepository(private val focusSessionDao: FocusSessionDao) {
    
    fun getAllSessions(): Flow<List<FocusSessionEntity>> = focusSessionDao.getAllSessions()
    
    fun getSessionsByDate(date: String): Flow<List<FocusSessionEntity>> = 
        focusSessionDao.getSessionsByDate(date)
    
    fun getSessionsByTask(taskId: Int): Flow<List<FocusSessionEntity>> = 
        focusSessionDao.getSessionsByTask(taskId)
    
    suspend fun getTotalFocusTimeForDate(date: String): Long = 
        focusSessionDao.getTotalFocusTimeForDate(date) ?: 0L
    
    suspend fun getTotalFocusTimeForDateRange(startDate: String, endDate: String): Long =
        focusSessionDao.getTotalFocusTimeForDateRange(startDate, endDate) ?: 0L
    
    suspend fun insertSession(session: FocusSessionEntity): Long = 
        focusSessionDao.insertSession(session)
    
    suspend fun updateSession(session: FocusSessionEntity) = 
        focusSessionDao.updateSession(session)
    
    suspend fun deleteSession(session: FocusSessionEntity) = 
        focusSessionDao.deleteSession(session)
    
    suspend fun getLongestSessionForDate(date: String): Long = 
        focusSessionDao.getLongestSessionForDate(date) ?: 0L
    
    suspend fun getSessionCountForDate(date: String): Int = 
        focusSessionDao.getSessionCountForDate(date)
    
    /**
     * Creates a new focus session from start and end times
     */
    suspend fun createSession(
        taskId: Int?,
        startTime: Long,
        endTime: Long
    ): Long {
        val duration = endTime - startTime
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date(startTime))
        
        val session = FocusSessionEntity(
            taskId = taskId,
            startTime = startTime,
            endTime = endTime,
            duration = duration,
            date = date
        )
        
        return insertSession(session)
    }
} 