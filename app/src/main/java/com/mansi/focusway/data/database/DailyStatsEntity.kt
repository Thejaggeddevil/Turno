package com.mansi.focusway.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity class representing daily focus statistics
 */
@Entity(tableName = "daily_stats")
data class DailyStatsEntity(
    @PrimaryKey
    val date: String,  // YYYY-MM-DD format
    val totalFocusTime: Long = 0,  // Total focus time in milliseconds
    val sessionsCount: Int = 0,    // Number of focus sessions
    val tasksCompleted: Int = 0,   // Number of tasks completed
    val longestSession: Long = 0   // Longest session duration in milliseconds
) 