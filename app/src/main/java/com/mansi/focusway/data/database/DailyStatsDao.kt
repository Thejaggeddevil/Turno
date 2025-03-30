package com.mansi.focusway.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for DailyStats entities
 */
@Dao
interface DailyStatsDao {
    @Query("SELECT * FROM daily_stats ORDER BY date DESC")
    fun getAllStats(): Flow<List<DailyStatsEntity>>
    
    @Query("SELECT * FROM daily_stats WHERE date = :date")
    suspend fun getStatsByDate(date: String): DailyStatsEntity?
    
    @Query("SELECT * FROM daily_stats WHERE date = :date")
    fun getStatsByDateFlow(date: String): Flow<DailyStatsEntity?>
    
    @Query("SELECT * FROM daily_stats WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    fun getStatsByDateRange(startDate: String, endDate: String): Flow<List<DailyStatsEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: DailyStatsEntity)
    
    @Update
    suspend fun updateStats(stats: DailyStatsEntity)
    
    @Query("SELECT SUM(totalFocusTime) FROM daily_stats WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalFocusTimeInRange(startDate: String, endDate: String): Long?
    
    @Query("SELECT SUM(tasksCompleted) FROM daily_stats WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalTasksCompletedInRange(startDate: String, endDate: String): Int?
    
    @Query("SELECT MAX(totalFocusTime) FROM daily_stats")
    suspend fun getMostProductiveDay(): Long?
    
    @Query("SELECT AVG(totalFocusTime) FROM daily_stats WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getAverageFocusTimeInRange(startDate: String, endDate: String): Float?
} 