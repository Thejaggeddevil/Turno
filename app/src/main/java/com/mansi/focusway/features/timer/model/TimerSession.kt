package com.mansi.focusway.features.timer.model

import java.util.Date

data class TimerSession(
    val id: String = "",
    val startTime: Date = Date(),
    val endTime: Date? = null,
    val duration: Long = 0, // in milliseconds
    val taskId: String? = null,
    val completed: Boolean = false
) 