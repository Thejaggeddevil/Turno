package com.mansi.focusway.ui.stats

import com.mansi.focusway.data.database.DailyStatsEntity
import com.mansi.focusway.data.database.FocusSessionEntity

/**
 * UI state for the Stats screen
 */
data class StatsUiState(
    // Today's stats
    val todayFocusTime: Long = 0L,
    val todaySessionsCount: Int = 0,
    val todayTasksCompleted: Int = 0,
    val todaySessions: List<FocusSessionEntity> = emptyList(),
    
    // Week stats
    val totalWeeklyFocusTime: Long = 0L,
    val avgDailyFocusTime: Long = 0L,
    val totalWeeklyTasksCompleted: Int = 0,
    
    // Year stats
    val totalYearlyFocusTime: Long = 0L,
    val yearlyStatsMap: Map<String, DailyStatsEntity> = emptyMap(),
    
    // Streaks
    val currentStreak: Int = 0,
    val longestStreak: Int = 0
) 