package com.mansi.focusway.ui.timer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mansi.focusway.ui.theme.FocusWayTheme
import com.mansi.focusway.ui.theme.NeonCyan
import com.mansi.focusway.ui.theme.NeonBlue
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mansi.focusway.data.database.TaskEntity
import com.mansi.focusway.ui.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    onNavigateToTodo: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: TimerViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    
    var isTimerRunning by remember { mutableStateOf(false) }
    var lastFlipTime by remember { mutableStateOf(0L) }
    var elapsedTime by remember { mutableStateOf(0L) }
    var startTime by remember { mutableStateOf(0L) }
    
    // Task selection and tracking
    var selectedTaskName by remember { mutableStateOf("Select a task") }
    var isTaskSelectionOpen by remember { mutableStateOf(false) }
    val taskTimers = remember { mutableStateMapOf<String, Long>() }
    var currentTaskStartTime by remember { mutableStateOf(0L) }
    var currentTaskTime by remember { mutableStateOf(0L) }
    
    // Mode selection
    var selectedMode by remember { mutableStateOf("individual") } // "individual" or "collaborative"
    
    // Format current time and date
    var time by remember { mutableStateOf(System.currentTimeMillis()) }
    val dateFormat = remember { SimpleDateFormat("yyyy.MM.dd EEE", Locale.getDefault()) }
    val formattedDate = remember(time) { dateFormat.format(Date(time)) }
    
    // Load total accumulated time for the day
    val prefs = remember { context.getSharedPreferences("timer_prefs", Context.MODE_PRIVATE) }
    val todayKey = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var dailyTotalTime by remember { mutableStateOf(prefs.getLong(todayKey, 0L)) }
    
    // Rotation animation for the circular border
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    LaunchedEffect(key1 = true) {
        viewModel.initialize(context)
        viewModel.loadDailyStats()
        viewModel.resetDailyTotalIfNewDay()
        while (true) {
            delay(1000)
            time = System.currentTimeMillis()
        }
    }
    
    // Initialize sensor manager and check if sensor is available
    val sensorManager = remember { 
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager 
    }
    
    val accelerometer = remember { 
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) 
    }
    
    // Check if accelerometer is available
    val isSensorAvailable = remember { accelerometer != null }
    
    // Vibrator service with null check
    val vibrator = remember { 
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
    
    // Update elapsed time and save daily total
    LaunchedEffect(isTimerRunning) {
        while(isTimerRunning) {
            delay(1000) // Update every second
            if (startTime > 0) {
                val newElapsedTime = System.currentTimeMillis() - startTime
                elapsedTime = newElapsedTime
                
                // Update daily total and save to preferences
                dailyTotalTime += 1000 // Add one second
                prefs.edit().putLong(todayKey, dailyTotalTime).apply()
                
                // Update current task time - only update the display value, don't add to the total yet
                if (selectedTaskName != "Select a task" && currentTaskStartTime > 0) {
                    currentTaskTime = System.currentTimeMillis() - currentTaskStartTime
                }
            }
        }
    }
    
    // Check for midnight reset
    LaunchedEffect(time) {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(time))
        if (currentDate != todayKey) {
            // It's a new day, reset the daily total
            dailyTotalTime = 0L
            prefs.edit().putLong(currentDate, 0L).apply()
            // Reset task timers
            taskTimers.clear()
        }
    }
    
    // Modify the sensor event listener to use view model and check vibration settings
    val sensorEventListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val zValue = event.values[2]
                    val currentTime = System.currentTimeMillis()
                    
                    // Prevent multiple triggers within 500ms
                    if (currentTime - lastFlipTime > 500) {
                        // Phone is face down (z < -9)
                        if (zValue < -9 && !uiState.isTimerRunning) {
                            viewModel.startTimer()
                            isTimerRunning = true
                            startTime = System.currentTimeMillis()
                            if (selectedTaskName != "Select a task") {
                                currentTaskStartTime = System.currentTimeMillis()
                                currentTaskTime = 0 // Reset current session time
                            }
                            
                            // Check vibration setting before vibrating
                            if (SettingsViewModel.isVibrationEnabled(context)) {
                                vibrator?.let { vibratePhone(it) }
                            }
                            lastFlipTime = currentTime
                        }
                        // Phone is face up (z > 9)
                        else if (zValue > 9 && uiState.isTimerRunning) {
                            viewModel.stopTimer()
                            isTimerRunning = false
                            if (selectedTaskName != "Select a task") {
                                // Save the task time - add current session to the total
                                val previousTaskTime = taskTimers[selectedTaskName] ?: 0L
                                taskTimers[selectedTaskName] = previousTaskTime + currentTaskTime
                                currentTaskTime = 0
                            }
                            
                            // Check vibration setting before vibrating
                            if (SettingsViewModel.isVibrationEnabled(context)) {
                                vibrator?.let { vibratePhone(it) }
                            }
                            lastFlipTime = currentTime
                        }
                    }
                }
            }
            
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }
    
    // Register/unregister sensor listener only if sensor is available
    DisposableEffect(Unit) {
        if (isSensorAvailable) {
            sensorManager.registerListener(
                sensorEventListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            
            onDispose {
                sensorManager.unregisterListener(sensorEventListener)
            }
        } else {
            onDispose { }
        }
    }

    // Show snackbar if sensor is not available
    if (!isSensorAvailable) {
        LaunchedEffect(Unit) {
            Toast.makeText(
                context,
                "Accelerometer sensor not available on this device",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar with navigation icons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Timer",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                
                Row {
                    // Premium/Star icon
                    IconButton(onClick = { /* Premium feature */ }) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Premium",
                            tint = Color.Yellow
                        )
                    }
                    
                    // ToDo list icon
                    IconButton(onClick = { onNavigateToTodo() }) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "View Tasks",
                            tint = Color.White
                        )
                    }
                    
                    // Add task icon
                    IconButton(onClick = { /* Navigate to Add Task */ }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Task",
                            tint = Color.White
                        )
                    }
                    
                    // Settings icon
                    IconButton(onClick = { onNavigateToSettings() }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mode selection (Individual/Collaborative)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 80.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Individual mode
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { selectedMode = "individual" }
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(if (selectedMode == "individual") Color.White else Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Individual Mode",
                            tint = if (selectedMode == "individual") Color.Black else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                // Collaborative mode
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { 
                            selectedMode = "collaborative"
                            onNavigateToTodo()
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(if (selectedMode == "collaborative") Color.White else Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Collaborative Mode",
                            tint = if (selectedMode == "collaborative") Color.Black else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Circular timer with rotating border
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Rotating border animation
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    rotate(rotation) {
                        // Outer rotating gradient circle - THINNER AND MORE NEON
                        drawCircle(
                            brush = Brush.sweepGradient(
                                0.0f to NeonCyan.copy(alpha = 0.9f),
                                0.3f to NeonBlue.copy(alpha = 0.95f),
                                0.6f to Color.Transparent,
                                1.0f to NeonCyan.copy(alpha = 0.9f)
                            ),
                            radius = size.minDimension / 2,
                            style = Stroke(width = 3.dp.toPx()) // Thinner border
                        )
                    }
                    
                    // Static inner circle for timer background
                    drawCircle(
                        color = Color.DarkGray.copy(alpha = 0.1f),
                        radius = size.minDimension / 2 - 10.dp.toPx(),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
                
                // Timer display
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Date moved inside the circle
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Always show the actual time, not 00:00:00 when not running
                    Text(
                        text = formatElapsedTime(dailyTotalTime),
                        style = MaterialTheme.typography.displayMedium,
                        fontSize = 45.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTimerRunning) NeonCyan else Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Show current task and its timer - moved outside isTimerRunning check
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "• $selectedTaskName •",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isTimerRunning) Color.Green else Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.clickable { isTaskSelectionOpen = true }
                        )
                        
                        // Show the total accumulated time for the selected task
                        val taskTotalTime = if (selectedTaskName != "Select a task") {
                            taskTimers[selectedTaskName] ?: 0L
                        } else 0L
                        
                        // If timer is running, show current session time + previous total
                        // If timer is not running, just show the total saved time
                        val displayTime = if (isTimerRunning && selectedTaskName != "Select a task") {
                            taskTotalTime + currentTaskTime
                        } else {
                            taskTotalTime
                        }
                        
                        // Show task time clearly
                        Text(
                            text = "Task time: ${formatElapsedTime(displayTime)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isTimerRunning && selectedTaskName != "Select a task") 
                                   Color.Green.copy(alpha = 0.8f) else Color.Gray
                        )
                    }
                    
                    // Task selection dropdown
                    DropdownMenu(
                        expanded = isTaskSelectionOpen,
                        onDismissRequest = { isTaskSelectionOpen = false },
                        modifier = Modifier
                            .width(280.dp)
                            .background(Color(0xFF121212))
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "Select task",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            
                            // Add button at the top right
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.TopEnd
                            ) {
                                IconButton(
                                    onClick = { /* Add new task */ },
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add task",
                                        tint = Color.White
                                    )
                                }
                            }
                            
                            // Organize tasks into categories based on title prefixes or other logic
                            // For simplicity, we'll divide tasks into three categories based on task list position
                            val studyTasks = tasks.take(tasks.size / 3)
                            val exerciseTasks = tasks.drop(tasks.size / 3).take(tasks.size / 3)
                            val dailyTasks = tasks.drop(2 * tasks.size / 3)
                            
                            // No tasks option
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        selectedTaskName = "No task selected"
                                        viewModel.selectTask(null)
                                        isTaskSelectionOpen = false
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "No Task", 
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            HorizontalDivider(
                                color = Color.DarkGray.copy(alpha = 0.5f)
                            )
                            
                            // Categories with tasks
                            if (studyTasks.isNotEmpty()) {
                                TaskCategory(
                                    title = "Study",
                                    tasks = studyTasks,
                                    selectedTaskName = selectedTaskName,
                                    onTaskSelected = { task ->
                                        selectedTaskName = task.title
                                        viewModel.selectTask(task.id)
                                        isTaskSelectionOpen = false
                                    }
                                )
                            }
                            
                            if (exerciseTasks.isNotEmpty()) {
                                TaskCategory(
                                    title = "Exercise",
                                    tasks = exerciseTasks,
                                    selectedTaskName = selectedTaskName,
                                    onTaskSelected = { task ->
                                        selectedTaskName = task.title
                                        viewModel.selectTask(task.id)
                                        isTaskSelectionOpen = false
                                    }
                                )
                            }
                            
                            if (dailyTasks.isNotEmpty()) {
                                TaskCategory(
                                    title = "Daily",
                                    tasks = dailyTasks,
                                    selectedTaskName = selectedTaskName,
                                    onTaskSelected = { task ->
                                        selectedTaskName = task.title
                                        viewModel.selectTask(task.id)
                                        isTaskSelectionOpen = false
                                    }
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Cancel button
                            TextButton(
                                onClick = { isTaskSelectionOpen = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = "Cancel",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
        }
    }
}

@Composable
private fun ModeSelector(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (isSelected) Color.Yellow else Color.DarkGray,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.Black else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) Color.Yellow else Color.Gray
        )
    }
}

// Helper function to format elapsed time
private fun formatElapsedTime(elapsedTime: Long): String {
    val hours = (elapsedTime / (1000 * 60 * 60)) % 24
    val minutes = (elapsedTime / (1000 * 60)) % 60
    val seconds = (elapsedTime / 1000) % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

// Helper function to vibrate the phone
private fun vibratePhone(vibrator: Vibrator) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(500)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun TimerScreenPreview() {
    FocusWayTheme(darkTheme = true) {
        TimerScreen(
            onNavigateToTodo = {},
            onNavigateToSettings = {}
        )
    }
}

@Preview(
    name = "Timer Screen - Full",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun TimerScreenFullPreview() {
    FocusWayTheme(darkTheme = true) {
        TimerScreen(
            onNavigateToTodo = {},
            onNavigateToSettings = {}
        )
    }
}

@Preview(
    name = "Circular Timer Component",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 360,
    heightDp = 400
)
@Composable
private fun CircularTimerPreview() {
    FocusWayTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularTimer()
        }
    }
}

@Preview(
    name = "Navigation Bar",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 360
)
@Composable
private fun NavigationBarPreview() {
    FocusWayTheme(darkTheme = true) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.height(64.dp)
        ) {
            NavigationBarItem(
                icon = { Icon(Icons.Default.Timer, contentDescription = "Timer") },
                label = { Text("Timer") },
                selected = true,
                onClick = { }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.List, contentDescription = "Tasks") },
                label = { Text("Tasks") },
                selected = false,
                onClick = { }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.ShowChart, contentDescription = "Stats") },
                label = { Text("Stats") },
                selected = false,
                onClick = { }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Group, contentDescription = "Groups") },
                label = { Text("Groups") },
                selected = false,
                onClick = { }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    name = "Top App Bar",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 360
)
@Composable
private fun TopAppBarPreview() {
    FocusWayTheme(darkTheme = true) {
        TopAppBar(
            title = { Text("Timer") },
            actions = {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.Star, 
                        contentDescription = "Premium",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.Settings, 
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )
    }
}

@Composable
fun CircularTimer(
    isRunning: Boolean = false,
    elapsedTime: Long = 0L
) {
    val currentDateTime = SimpleDateFormat("yyyy.MM.dd. EEE", Locale.getDefault()).format(Date())
    
    // Format elapsed time
    val hours = (elapsedTime / (1000 * 60 * 60)) % 24
    val minutes = (elapsedTime / (1000 * 60)) % 60
    val seconds = (elapsedTime / 1000) % 60
    val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    Box(
        modifier = Modifier
            .size(300.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Circular progress background
        Canvas(
            modifier = Modifier
                .size(280.dp)
        ) {
            // Draw outer circle
            drawCircle(
                color = Color.DarkGray.copy(alpha = 0.3f),
                style = Stroke(width = 25f, cap = StrokeCap.Round),
                radius = size.minDimension / 2
            )

            // Draw progress arc
            drawArc(
                brush = Brush.linearGradient(
                    colors = listOf(NeonCyan, NeonBlue)
                ),
                startAngle = -90f,
                sweepAngle = if (isRunning) 360f else 0f,
                useCenter = false,
                style = Stroke(width = 25f, cap = StrokeCap.Round),
                size = Size(size.width, size.height)
            )

            // Draw tick marks
            for (i in 0 until 60) {
                val angle = (i * 6f) * (Math.PI / 180f)
                val radius = size.minDimension / 2
                val start = Offset(
                    x = (center.x + (radius - 40f) * kotlin.math.cos(angle)).toFloat(),
                    y = (center.y + (radius - 40f) * kotlin.math.sin(angle)).toFloat()
                )
                val end = Offset(
                    x = (center.x + (radius - 20f) * kotlin.math.cos(angle)).toFloat(),
                    y = (center.y + (radius - 20f) * kotlin.math.sin(angle)).toFloat()
                )
                drawLine(
                    color = if (isRunning) NeonCyan.copy(alpha = 0.5f) else Color.DarkGray,
                    start = start,
                    end = end,
                    strokeWidth = if (i % 5 == 0) 3f else 1f
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentDateTime,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp
            )
            
            Text(
                text = timeString,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Text(
                text = if (isRunning) "Timer Running" else "Timer Paused",
                color = if (isRunning) NeonCyan else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
            
            Text(
                text = if (isRunning) "Flip face up to pause" else "Flip face down to start",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

// Add this new composable for displaying task categories
@Composable
private fun TaskCategory(
    title: String,
    tasks: List<TaskEntity>,
    selectedTaskName: String,
    onTaskSelected: (TaskEntity) -> Unit
) {
    if (tasks.isEmpty()) return
    
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        // Category header
        Text(
            text = title,
            color = when (title) {
                "Study" -> Color(0xFFF57C00) // Orange
                "Exercise" -> Color(0xFF2196F3) // Blue
                "Daily" -> Color(0xFF4CAF50) // Green
                else -> Color.White
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        
        // Tasks in this category
        tasks.forEach { task ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTaskSelected(task) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Task indicator and name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Colored indicator based on category
                    Box(
                        modifier = Modifier
                            .size(4.dp, 16.dp)
                            .background(
                                when (title) {
                                    "Study" -> Color(0xFFF57C00) // Orange
                                    "Exercise" -> Color(0xFF2196F3) // Blue
                                    "Daily" -> Color(0xFF4CAF50) // Green
                                    else -> Color.Gray
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = task.title,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Right: Time spent on task
                Text(
                    text = formatTaskTime(task.totalFocusTime),
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// Helper function to format task time
private fun formatTaskTime(timeInMillis: Long): String {
    val hours = (timeInMillis / (1000 * 60 * 60))
    val minutes = (timeInMillis / (1000 * 60)) % 60
    return "${hours}h ${minutes.toString().padStart(2, '0')}m"
} 