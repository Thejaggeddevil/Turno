package com.mansi.focusway.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity class representing a Task in the database
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val category: String = "",
    val color: Int = 0xFF4CAF50.toInt(), // Default color (Green)
    val priority: Int = 0, // 0: Low, 1: Medium, 2: High
    val dueDate: Long? = null,
    val repeatDaily: Boolean = false,
    val repeatWeekly: Boolean = false,
    val repeatDays: String = "", // Comma-separated list of days to repeat (e.g., "Mon,Wed,Fri")
    val repeatUntil: Long? = null,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val totalFocusTime: Long = 0 // Total time spent focusing on this task in milliseconds
) 