package com.mansi.focusway.data.repository

import com.mansi.focusway.data.database.DailyStatsDao
import com.mansi.focusway.data.database.DailyStatsEntity
import com.mansi.focusway.data.database.FocusSessionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repository for handling statistics operations
 */
class StatsRepository(
    private val dailyStatsDao: DailyStatsDao,
    private val focusSessionDao: FocusSessionDao
) {
    fun getAllStats(): Flow<List<DailyStatsEntity>> = dailyStatsDao.getAllStats()
    
    fun getStatsByDate(date: String): Flow<DailyStatsEntity?> = dailyStatsDao.getStatsByDateFlow(date)
    
    suspend fun getStatsForToday(): DailyStatsEntity {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date())
        
        return dailyStatsDao.getStatsByDate(todayDate) ?: DailyStatsEntity(date = todayDate)
    }
    
    fun getStatsByDateRange(startDate: String, endDate: String): Flow<List<DailyStatsEntity>> =
        dailyStatsDao.getStatsByDateRange(startDate, endDate)
        
    suspend fun getStatsBetweenDates(startDate: String, endDate: String): List<DailyStatsEntity> =
        dailyStatsDao.getStatsByDateRange(startDate, endDate).first()
    
    suspend fun updateDailyStats(date: String) {
        // Gather data for the specified date
        val totalFocusTime = focusSessionDao.getTotalFocusTimeForDate(date) ?: 0L
        val sessionsCount = focusSessionDao.getSessionCountForDate(date)
        val longestSession = focusSessionDao.getLongestSessionForDate(date) ?: 0L
        
        // Get or create stats entity
        val stats = dailyStatsDao.getStatsByDate(date) ?: DailyStatsEntity(date = date)
        
        // Update with new data
        val updatedStats = stats.copy(
            totalFocusTime = totalFocusTime,
            sessionsCount = sessionsCount,
            longestSession = longestSession
        )
        
        dailyStatsDao.insertStats(updatedStats)
    }
    
    suspend fun incrementTasksCompleted(date: String) {
        val stats = dailyStatsDao.getStatsByDate(date) ?: DailyStatsEntity(date = date)
        val updatedStats = stats.copy(tasksCompleted = stats.tasksCompleted + 1)
        dailyStatsDao.insertStats(updatedStats)
    }
    
    suspend fun getLastWeekStats(): List<DailyStatsEntity> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        // End date is today
        val endDate = dateFormat.format(calendar.time)
        
        // Start date is 7 days ago
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        val startDate = dateFormat.format(calendar.time)
        
        return dailyStatsDao.getStatsByDateRange(startDate, endDate)
            .first()
    }
    
    suspend fun getMostProductiveDay(): Long = dailyStatsDao.getMostProductiveDay() ?: 0L
    
    suspend fun getTotalFocusTimeInLastWeek(): Long {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        // End date is today
        val endDate = dateFormat.format(calendar.time)
        
        // Start date is 7 days ago
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        val startDate = dateFormat.format(calendar.time)
        
        return dailyStatsDao.getTotalFocusTimeInRange(startDate, endDate) ?: 0L
    }
    
    suspend fun getTotalTasksCompletedInLastWeek(): Int {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        // End date is today
        val endDate = dateFormat.format(calendar.time)
        
        // Start date is 7 days ago
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        val startDate = dateFormat.format(calendar.time)
        
        return dailyStatsDao.getTotalTasksCompletedInRange(startDate, endDate) ?: 0
    }
} 