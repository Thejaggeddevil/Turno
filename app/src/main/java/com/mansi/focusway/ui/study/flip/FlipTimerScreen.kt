package com.mansi.focusway.ui.study.flip

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mansi.focusway.ui.theme.FocusWayTheme
import kotlinx.coroutines.delay

@Composable
fun FlipTimerScreen(
    viewModel: FlipTimerViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Set up accelerometer sensor for flip detection
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val z = event.values[2]
                    
                    // Phone is face down when z is negative
                    val isFaceDown = z < -9.0f
                    
                    if (isFaceDown && !uiState.isStudying) {
                        viewModel.startStudying()
                    } else if (!isFaceDown && uiState.isStudying) {
                        viewModel.pauseStudying()
                    }
                }
            }
            
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Not needed
            }
        }
        
        // Register the sensor listener
        sensorManager.registerListener(
            sensorListener, 
            accelerometer, 
            SensorManager.SENSOR_DELAY_NORMAL
        )
        
        onDispose {
            // Unregister when leaving the screen
            sensorManager.unregisterListener(sensorListener)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
                
                Text(
                    "Flip Timer",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium
                )
                
                // Empty box for alignment
                Box(modifier = Modifier.size(48.dp))
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Timer Display
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = uiState.currentTime,
                    color = if (uiState.isStudying) Color(0xff87fefe) else Color.White,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.isStudying) Color(0xFF1E4E2C) else Color(0xFF1E1E1E)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (uiState.isStudying) "Currently Studying" else "Timer Paused",
                            color = if (uiState.isStudying) Color.Green else Color.Gray,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = if (uiState.isStudying) 
                                "Your phone is face down. Flip it face up to pause." 
                            else 
                                "Place your phone face down to start studying.",
                            color = Color.White,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Manual Controls
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Manual Controls",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { 
                        if (uiState.isStudying) {
                            viewModel.pauseStudying()
                        } else {
                            viewModel.startStudying()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isStudying) Color.Red else Color(0xff87fefe)
                    ),
                    modifier = Modifier
                        .height(56.dp)
                        .width(200.dp)
                ) {
                    if (uiState.isStudying) {
                        Icon(Icons.Default.Pause, contentDescription = "Pause")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pause")
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start")
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Session Stats
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Today's Focus Sessions",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SessionStatCard(
                        title = "Sessions",
                        value = uiState.sessionCount.toString()
                    )
                    
                    SessionStatCard(
                        title = "Total Time",
                        value = uiState.totalTimeFormatted
                    )
                }
            }
        }
    }
}

@Composable
fun SessionStatCard(title: String, value: String) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                color = Color(0xff87fefe),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = title,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun FlipTimerScreenPreview() {
    FocusWayTheme(darkTheme = true) {
        FlipTimerScreen()
    }
} 