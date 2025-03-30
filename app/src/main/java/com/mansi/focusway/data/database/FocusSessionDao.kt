package com.mansi.focusway.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for FocusSession entities
 */
@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>
    
    @Query("SELECT * FROM focus_sessions WHERE date = :date ORDER BY startTime DESC")
    fun getSessionsByDate(date: String): Flow<List<FocusSessionEntity>>
    
    @Query("SELECT * FROM focus_sessions WHERE taskId = :taskId ORDER BY startTime DESC")
    fun getSessionsByTask(taskId: Int): Flow<List<FocusSessionEntity>>
    
    @Query("SELECT sum(duration) FROM focus_sessions WHERE date = :date")
    suspend fun getTotalFocusTimeForDate(date: String): Long?
    
    @Query("SELECT sum(duration) FROM focus_sessions WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalFocusTimeForDateRange(startDate: String, endDate: String): Long?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity): Long
    
    @Update
    suspend fun updateSession(session: FocusSessionEntity)
    
    @Delete
    suspend fun deleteSession(session: FocusSessionEntity)
    
    @Query("SELECT MAX(duration) FROM focus_sessions WHERE date = :date")
    suspend fun getLongestSessionForDate(date: String): Long?
    
    @Query("SELECT COUNT(*) FROM focus_sessions WHERE date = :date")
    suspend fun getSessionCountForDate(date: String): Int
} 