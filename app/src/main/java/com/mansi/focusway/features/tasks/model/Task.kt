package com.mansi.focusway.features.tasks.model

import java.util.Date

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val createdAt: Date = Date(),
    val dueDate: Date? = null,
    val completed: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val category: String? = null
)

enum class Priority {
    LOW, MEDIUM, HIGH
} 