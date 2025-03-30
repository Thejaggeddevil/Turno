package com.mansi.focusway.ui.flip

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mansi.focusway.ui.theme.FocusWayTheme
import com.mansi.focusway.ui.theme.NeonCyan
import com.mansi.focusway.ui.theme.NeonBlue
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlipTimerScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var isTimerRunning by remember { mutableStateOf(false) }
    var lastFlipTime by remember { mutableStateOf(0L) }
    var elapsedTime by remember { mutableStateOf(0L) }
    var startTime by remember { mutableStateOf(0L) }
    var showInstructions by remember { mutableStateOf(false) }
    
    // Format current time
    var time by remember { mutableStateOf(System.currentTimeMillis()) }
    val dateFormat = remember { SimpleDateFormat("EEE, MMM d", Locale.getDefault()) }
    val formattedDate = remember(time) { dateFormat.format(Date(time)) }
    
    LaunchedEffect(key1 = true) {
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
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator 
    }
    
    // Update elapsed time
    LaunchedEffect(isTimerRunning) {
        while(isTimerRunning) {
            delay(1000) // Update every second
            if (startTime > 0) {
                elapsedTime = System.currentTimeMillis() - startTime
            }
        }
    }
    
    // Sensor event listener
    val sensorEventListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val zValue = event.values[2]
                    val currentTime = System.currentTimeMillis()
                    
                    // Prevent multiple triggers within 500ms
                    if (currentTime - lastFlipTime > 500) {
                        // Phone is face down (z < -9)
                        if (zValue < -9 && !isTimerRunning) {
                            isTimerRunning = true
                            startTime = System.currentTimeMillis()
                            vibrator?.let { vibratePhone(it) }
                            lastFlipTime = currentTime
                        }
                        // Phone is face up (z > 9)
                        else if (zValue > 9 && isTimerRunning) {
                            isTimerRunning = false
                            vibrator?.let { vibratePhone(it) }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flip Timer") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showInstructions = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Instructions"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Date
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Circular timer
                Box(
                    modifier = Modifier.size(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularTimerAnimation(
                        isRunning = isTimerRunning,
                        elapsedTime = elapsedTime
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Format elapsed time for display
                        val hours = (elapsedTime / (1000 * 60 * 60)) % 24
                        val minutes = (elapsedTime / (1000 * 60)) % 60
                        val seconds = (elapsedTime / 1000) % 60
                        val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                        
                        Text(
                            text = timeString,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        PulsatingText(
                            text = if (isTimerRunning) 
                                "Flip Face Up to Pause" 
                            else 
                                "Flip Face Down to Start"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Status text
                Text(
                    text = if (isTimerRunning) "Focus Mode: Active" else "Focus Mode: Inactive",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isTimerRunning) NeonCyan else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = if (isTimerRunning) "Keep your phone face down to stay focused" else "Flip your phone to start focusing",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
    
    // Instructions dialog
    if (showInstructions) {
        AlertDialog(
            onDismissRequest = { showInstructions = false },
            title = { Text("How to Use Flip Timer") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("1. Place your phone face down on a flat surface to start the timer.")
                    Text("2. When your phone is face down, the timer will start and track your focus time.")
                    Text("3. If you need to check your phone, simply flip it face up and the timer will pause.")
                    Text("4. Flip it back face down to continue your focus session.")
                    Text("5. Your phone will vibrate briefly when the timer starts or stops.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showInstructions = false }) {
                    Text("Got it")
                }
            }
        )
    }
}

@Composable
private fun CircularTimerAnimation(
    isRunning: Boolean,
    elapsedTime: Long
) {
    val animatedSweepAngle by animateFloatAsState(
        targetValue = if (isRunning) 360f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
    )
    
    Canvas(
        modifier = Modifier.size(280.dp)
    ) {
        // Background circle
        drawCircle(
            color = Color.DarkGray.copy(alpha = 0.2f),
            style = Stroke(width = 24f, cap = StrokeCap.Round),
            radius = size.minDimension / 2 - 12f
        )

        // Progress arc
        drawArc(
            brush = Brush.linearGradient(
                colors = listOf(NeonCyan, NeonBlue)
            ),
            startAngle = -90f,
            sweepAngle = animatedSweepAngle,
            useCenter = false,
            style = Stroke(width = 24f, cap = StrokeCap.Round),
            size = Size(size.width, size.height)
        )

        // Draw tick marks
        for (i in 0 until 60) {
            val angle = (i * 6f) * (Math.PI / 180f)
            val radius = size.minDimension / 2
            val start = Offset(
                x = (center.x + (radius - 40f) * cos(angle)).toFloat(),
                y = (center.y + (radius - 40f) * sin(angle)).toFloat()
            )
            val end = Offset(
                x = (center.x + (radius - 20f) * cos(angle)).toFloat(),
                y = (center.y + (radius - 20f) * sin(angle)).toFloat()
            )
            
            drawLine(
                color = if (isRunning) NeonCyan.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.3f),
                start = start,
                end = end,
                strokeWidth = if (i % 5 == 0) 3f else 1f
            )
        }
    }
}

@Composable
private fun PulsatingText(text: String) {
    var pulsate by remember { mutableStateOf(false) }
    
    val color by animateColorAsState(
        targetValue = if (pulsate) NeonCyan else Color.Gray,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
    )
    
    LaunchedEffect(Unit) {
        while (true) {
            pulsate = !pulsate
            delay(1000)
        }
    }
    
    Text(
        text = text,
        color = color,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center
    )
}

private fun vibratePhone(vibrator: Vibrator) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(200)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun FlipTimerScreenPreview() {
    FocusWayTheme(darkTheme = true) {
        FlipTimerScreen()
    }
} 