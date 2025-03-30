package com.mansi.focusway.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity class representing a focus session in the database
 */
@Entity(
    tableName = "focus_sessions",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("taskId")]
)
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val taskId: Int? = null,  // Can be null for general focus sessions
    val startTime: Long,
    val endTime: Long,
    val duration: Long,  // Duration in milliseconds
    val date: String     // YYYY-MM-DD format for easy querying
) 