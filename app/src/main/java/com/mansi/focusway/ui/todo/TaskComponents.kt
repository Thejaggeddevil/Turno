package com.mansi.focusway.ui.todo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Reusable components for Task screens (Add, Edit)
 */

// Priorities to use across all task screens
val priorities = listOf(
    "Low" to Color(0xFF4CAF50),    // Green
    "Medium" to Color(0xFFFF9800),  // Orange
    "High" to Color(0xFFE91E63)     // Pink
)

// Categories to use across all task screens
val categories = listOf("Work", "Study", "Personal", "Health", "Shopping", "Other")

// Weekdays for repeat selection
val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

@Composable
fun CategoryChip(
    category: String,
    selected: Boolean,
    onSelected: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onSelected() },
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        contentColor = if (selected) Color.Black else Color.White,
        border = if (!selected) BorderStroke(1.dp, Color.Gray) else null
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun PriorityChip(
    label: String,
    color: Color,
    selected: Boolean,
    onSelected: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onSelected() },
        color = if (selected) color else Color.Transparent,
        contentColor = if (selected) Color.Black else Color.White,
        border = if (!selected) BorderStroke(1.dp, color) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun DayChip(
    day: String,
    selected: Boolean,
    onSelected: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = if (!selected) MaterialTheme.colorScheme.outline else Color.Transparent,
                shape = CircleShape
            )
            .clickable(onClick = onSelected),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.substring(0, 1),
            fontSize = 12.sp,
            color = if (selected) Color.Black else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
} 