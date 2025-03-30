package com.mansi.focusway.features.stats.model

import java.util.Date

data class DailyStats(
    val date: Date = Date(),
    val totalFocusTime: Int = 0, // in minutes
    val completedTasks: Int = 0,
    val startedTasks: Int = 0,
    val totalBreakTime: Int = 0 // in minutes
) 