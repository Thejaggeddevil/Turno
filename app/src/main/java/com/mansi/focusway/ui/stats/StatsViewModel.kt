package com.mansi.focusway.ui.stats

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mansi.focusway.data.database.DailyStatsEntity
import com.mansi.focusway.data.database.FocusSessionEntity
import com.mansi.focusway.data.repository.FocusSessionRepository
import com.mansi.focusway.data.repository.StatsRepository
import com.mansi.focusway.di.RepositoryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

/**
 * ViewModel for the Stats screen
 */
class StatsViewModel : ViewModel() {
    private var repository: StatsRepository? = null
    private var sessionRepository: FocusSessionRepository? = null
    
    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState
    
    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear
    
    private val _yearlyStats = MutableStateFlow<Map<String, DailyStatsEntity>>(emptyMap())
    
    fun initialize(context: Context) {
        repository = RepositoryManager.getStatsRepository(context)
        sessionRepository = RepositoryManager.getFocusSessionRepository(context)
        
        // Initialize with current year stats
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            loadYearlyStats(calendar.get(Calendar.YEAR))
            calculateStreaks()
            loadTodayStats()
            loadWeeklyStats()
        }
    }
    
    fun setSelectedYear(year: Int) {
        viewModelScope.launch {
            _selectedYear.value = year
            loadYearlyStats(year)
        }
    }
    
    private fun loadYearlyStats(year: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val startCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, Calendar.JANUARY)
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                
                val endCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, Calendar.DECEMBER)
                    set(Calendar.DAY_OF_MONTH, 31)
                }
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val startDate = dateFormat.format(startCalendar.time)
                val endDate = dateFormat.format(endCalendar.time)
                
                // Get all stats for the year
                val yearStats = repository?.getStatsBetweenDates(startDate, endDate) ?: emptyList()
                
                // Convert to map with date as key
                val statsMap = yearStats.associateBy { it.date }
                _yearlyStats.value = statsMap
                
                // Calculate total focus time for the year
                var totalTime = 0L
                yearStats.forEach { stat ->
                    totalTime += stat.totalFocusTime
                }
                
                _uiState.update { currentState ->
                    currentState.copy(
                        totalYearlyFocusTime = totalTime,
                        yearlyStatsMap = statsMap
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // Get study time for a specific date
    fun getStudyTimeForDate(date: String): Long {
        return _yearlyStats.value[date]?.totalFocusTime ?: 0L
    }
    
    // Calculate current and longest streaks
    private fun calculateStreaks() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val allStats = repository?.getAllStats()?.first() ?: emptyList()
                
                if (allStats.isEmpty()) {
                    return@launch
                }
                
                // Sort stats by date
                val sortedStats = allStats.sortedBy { it.date }
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                var currentStreak = 0
                var longestStreak = 0
                
                // Calculate current streak (consecutive days with study time up to today)
                val today = dateFormat.format(Calendar.getInstance().time)
                val todayStat = allStats.find { it.date == today }
                
                // Start with today
                if (todayStat != null && todayStat.totalFocusTime > 0) {
                    currentStreak = 1
                    
                    // Check previous days
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                    
                    var continuousStreak = true
                    while (continuousStreak) {
                        val dateStr = dateFormat.format(cal.time)
                        val dayStat = allStats.find { it.date == dateStr }
                        
                        if (dayStat != null && dayStat.totalFocusTime > 0) {
                            currentStreak++
                            cal.add(Calendar.DAY_OF_YEAR, -1)
                        } else {
                            continuousStreak = false
                        }
                    }
                }
                
                // Calculate longest streak
                for (i in sortedStats.indices) {
                    if (sortedStats[i].totalFocusTime > 0) {
                        var streakCount = 1
                        
                        if (i < sortedStats.size - 1) {
                            val currentDate = dateFormat.parse(sortedStats[i].date)
                            val nextDate = dateFormat.parse(sortedStats[i + 1].date)
                            
                            val cal1 = Calendar.getInstance().apply { time = currentDate }
                            val cal2 = Calendar.getInstance().apply { time = nextDate }
                            
                            cal1.add(Calendar.DAY_OF_YEAR, 1)
                            
                            if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                                sortedStats[i + 1].totalFocusTime > 0) {
                                
                                var j = i + 1
                                while (j < sortedStats.size - 1) {
                                    val currentDate2 = dateFormat.parse(sortedStats[j].date)
                                    val nextDate2 = dateFormat.parse(sortedStats[j + 1].date)
                                    
                                    val cal3 = Calendar.getInstance().apply { time = currentDate2 }
                                    val cal4 = Calendar.getInstance().apply { time = nextDate2 }
                                    
                                    cal3.add(Calendar.DAY_OF_YEAR, 1)
                                    
                                    if (cal3.get(Calendar.YEAR) == cal4.get(Calendar.YEAR) &&
                                        cal3.get(Calendar.DAY_OF_YEAR) == cal4.get(Calendar.DAY_OF_YEAR) &&
                                        sortedStats[j + 1].totalFocusTime > 0) {
                                        streakCount++
                                        j++
                                    } else {
                                        break
                                    }
                                }
                                
                                streakCount++
                            }
                        }
                        
                        if (streakCount > longestStreak) {
                            longestStreak = streakCount
                        }
                    }
                }
                
                _uiState.update { currentState ->
                    currentState.copy(
                        currentStreak = currentStreak,
                        longestStreak = longestStreak
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun loadTodayStats() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val today = dateFormat.format(Calendar.getInstance().time)
                
                // Get today's stats
                val todayStats = repository?.getStatsByDate(today)?.first()
                
                // Get today's sessions
                val todaySessions = sessionRepository?.getSessionsByDate(today)?.first() ?: emptyList()
                
                _uiState.update { currentState ->
                    currentState.copy(
                        todayFocusTime = todayStats?.totalFocusTime ?: 0L,
                        todaySessionsCount = todayStats?.sessionsCount ?: 0,
                        todayTasksCompleted = todayStats?.tasksCompleted ?: 0,
                        todaySessions = todaySessions
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun loadWeeklyStats() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                
                val startDate = dateFormat.format(calendar.time)
                
                calendar.add(Calendar.DAY_OF_WEEK, 6)
                val endDate = dateFormat.format(calendar.time)
                
                // Get week stats
                val weekStats = repository?.getStatsBetweenDates(startDate, endDate) ?: emptyList()
                
                // Calculate totals
                var totalWeeklyFocusTime = 0L
                var totalWeeklyTasksCompleted = 0
                
                weekStats.forEach { stat ->
                    totalWeeklyFocusTime += stat.totalFocusTime
                    totalWeeklyTasksCompleted += stat.tasksCompleted
                }
                
                // Calculate average daily focus time
                val daysWithFocus = weekStats.count { it.totalFocusTime > 0 }
                val avgDailyFocusTime = if (daysWithFocus > 0) {
                    totalWeeklyFocusTime / daysWithFocus
                } else {
                    0L
                }
                
                _uiState.update { currentState ->
                    currentState.copy(
                        totalWeeklyFocusTime = totalWeeklyFocusTime,
                        avgDailyFocusTime = avgDailyFocusTime,
                        totalWeeklyTasksCompleted = totalWeeklyTasksCompleted
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
} 