package com.mansi.focusway.ui.stats

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mansi.focusway.core.ui.NeonCyan
import com.mansi.focusway.utils.formatTime
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Define progression class for heatmap color generation
class ChartProgression(val start: Float, val end: Float, var current: Float = start) {
    companion object {
        fun fromClosedRange(start: Float, end: Float, step: Float): ChartProgression {
            return ChartProgression(start, end, start)
        }
    }
    
    // For the operator to work with steps
    operator fun plus(increment: Float): ChartProgression {
        return ChartProgression(start, end, current + increment)
    }
    
    // Custom step function for ChartProgression that returns a new instance
    infix fun step(step: Float): ChartProgression {
        return fromClosedRange(start, end, step)
    }
    
    // For iteration
    fun forEach(action: (Float) -> Unit) {
        var current = start
        val stepValue = 0.2f // Default step size
        while (current <= end) {
            action(current)
            current += stepValue
        }
    }
}

// Standard progression step functions
public infix fun IntProgression.step(step: Int): IntProgression {
    return IntProgression.fromClosedRange(first, last, if (step > 0) step else -step)
}

public infix fun LongProgression.step(step: Long): LongProgression {
    return LongProgression.fromClosedRange(first, last, if (step > 0) step else -step)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: StatsViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // Initialize the view model
    LaunchedEffect(Unit) {
        viewModel.initialize(context)
    }
    
    val uiState by viewModel.uiState.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    var selectedDate by remember { mutableStateOf<String?>(null) }
    
    // Colors - matching exactly with screenshot
    val darkBackground = Color(0xFF000000) // Pure black
    val lightGreen = Color(0xFF8EE28C) // Lighter green from screenshot
    
    val dayOfWeekLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                actions = {
                    // Gold star
                    IconButton(onClick = { /* Mark as favorite */ }) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Favorite",
                            tint = Color(0xFFFFD700)
                        )
                    }
                    
                    // List view icon
                    IconButton(onClick = { /* Show list view */ }) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = "List View",
                            tint = Color.White
                        )
                    }
                    
                    // Settings icon
                    IconButton(onClick = { /* Open settings */ }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = darkBackground,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Show insights */ },
                containerColor = lightGreen,
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.BarChart,
                    contentDescription = "Insights"
                )
            }
        },
        containerColor = darkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Year selector with large centered text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            ) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous year button
                    IconButton(
                        onClick = { viewModel.setSelectedYear(selectedYear - 1) }
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Previous Year",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Year text
                    AnimatedContent(
                        targetState = selectedYear,
                        transitionSpec = {
                            fadeIn() + scaleIn() togetherWith fadeOut()
                        },
                        label = "YearAnimation"
                    ) { year ->
                        Text(
                            text = year.toString(),
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Next year button
                    IconButton(
                        onClick = { viewModel.setSelectedYear(selectedYear + 1) }
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = "Next Year",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            
            // Total study time for the selected year
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TOTAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontSize = 10.sp
                )
                
                Text(
                    text = formatTotalHours(uiState.totalYearlyFocusTime),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Day of week headers - as seen in screenshot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp, start = 22.dp, end = 16.dp)
            ) {
                // Color bar legend
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "less",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    
                    // Color boxes for the legend
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF1A1A1A))
                    )
                    
                    // Define and use color steps without the step operator
                    val colorSteps = listOf(0.3f, 0.5f, 0.7f, 0.9f)
                    colorSteps.forEach { alpha ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(lightGreen.copy(alpha = alpha))
                        )
                    }
                    
                    Text(
                        text = "more",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
            
            // Week day letters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, end = 16.dp)
            ) {
                dayOfWeekLabels.forEach { day ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.take(1),
                            color = Color.Gray,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            // Heatmap grid
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedYear)
                set(Calendar.MONTH, 0) // January
                set(Calendar.DAY_OF_MONTH, 1)
                
                // Adjust to start with Monday
                val firstDayOfWeek = get(Calendar.DAY_OF_WEEK)
                if (firstDayOfWeek != Calendar.MONDAY) {
                    add(Calendar.DAY_OF_YEAR, -(((firstDayOfWeek - Calendar.MONDAY + 7) % 7)))
                }
            }
            
            // Calculate number of weeks in this view
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            
            // Create a list of dates for the grid
            val allDates = mutableListOf<Date>()
            val tempCalendar = calendar.clone() as Calendar
            
            // Generate dates for the entire year with padding for complete weeks
            while (tempCalendar.get(Calendar.YEAR) <= selectedYear) {
                allDates.add(tempCalendar.time)
                tempCalendar.add(Calendar.DAY_OF_YEAR, 1)
                
                // Stop if we're past the current year and reached a Monday
                if (tempCalendar.get(Calendar.YEAR) > selectedYear && 
                    tempCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
                    break
                }
            }
            
            // Month labels on left side
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Month labels on left side
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(end = 4.dp)
                ) {
                    // Position month names at appropriate intervals as in screenshot
                    val monthLabels = listOf("Mar", "Jun", "Sep", "Dec")
                    val cellHeight = 40.dp // Match cell size from screenshot
                    
                    monthLabels.forEachIndexed { index, month ->
                        Spacer(modifier = Modifier.height(if (index == 0) 12.dp else cellHeight * 6.2f))
                        Text(
                            text = month,
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                }
                
                // Grid for heatmap - matches screenshot exactly
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    contentPadding = PaddingValues(start = 22.dp, bottom = 16.dp),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    itemsIndexed(allDates) { index, date ->
                        val dateString = dateFormat.format(date)
                        val studyTime = viewModel.getStudyTimeForDate(dateString)
                        val isCurrentYear = Calendar.getInstance().apply { time = date }.get(Calendar.YEAR) == selectedYear
                        
                        // Calculate color based on study time - match screenshot colors
                        val squareColor = when {
                            !isCurrentYear -> Color.Transparent
                            studyTime <= 0 -> Color(0xFF1A1A1A) // Darker gray as in screenshot
                            studyTime < 30 * 60 * 1000 -> lightGreen.copy(alpha = 0.3f) 
                            studyTime < 60 * 60 * 1000 -> lightGreen.copy(alpha = 0.5f)
                            studyTime < 120 * 60 * 1000 -> lightGreen.copy(alpha = 0.7f)
                            else -> lightGreen
                        }

                        // Is this the selected date?
                        val isSelected = dateString == selectedDate
                        
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(1.dp))
                                .background(squareColor)
                                .clickable(enabled = isCurrentYear && studyTime > 0) {
                                    selectedDate = if (isSelected) null else dateString
                                }
                                .border(
                                    width = if (isSelected) 0.5.dp else 0.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(1.dp)
                                )
                        )
                    }
                }
            }
            
            // Bottom section with promotion text - exact match with screenshot
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A1A)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Text(
                        text = "Share Turno on social media to win a gift",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StreakItem(count: Int, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Whatshot,
                contentDescription = null,
                tint = if (count > 0) Color(0xFFFF5722) else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleLarge,
                color = if (count > 0) Color.White else Color.Gray,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = " days",
                style = MaterialTheme.typography.titleSmall,
                color = Color.Gray
            )
        }
        
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun DetailItem(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = title,
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun formatTotalHours(timeInMillis: Long): String {
    val hours = timeInMillis / (1000 * 60 * 60)
    val minutes = (timeInMillis % (1000 * 60 * 60)) / (1000 * 60)
    
    return "${hours}h ${minutes}m"
} 