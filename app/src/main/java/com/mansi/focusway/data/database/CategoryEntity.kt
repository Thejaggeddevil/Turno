package com.mansi.focusway.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a task category in the database
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
) 