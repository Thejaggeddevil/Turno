package com.mansi.focusway.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Format time in milliseconds to a readable string
 */
fun formatTime(timeInMillis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(timeInMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis) % 60
    
    return when {
        hours > 0 -> String.format("%dh %dm", hours, minutes)
        else -> String.format("%dm", minutes)
    }
}

/**
 * Format milliseconds to a human-readable duration string
 */
fun formatDuration(timeInMillis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(timeInMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis) % 60
    
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}m"
        else -> "<1m"
    }
}

/**
 * Format a date to a readable string in the specified format
 */
fun formatDate(timestamp: Long, pattern: String = "dd MMM yyyy"): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(date)
}

/**
 * Get current day of week (abbreviated)
 */
fun getCurrentDayOfWeek(): String {
    val calendar = Calendar.getInstance()
    val format = SimpleDateFormat("EEE", Locale.getDefault())
    return format.format(calendar.time)
}

/**
 * Get the start date of the current week
 */
fun getStartOfWeek(): Date {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}

/**
 * Get the end date of the current week
 */
fun getEndOfWeek(): Date {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek + 6)
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    return calendar.time
} 