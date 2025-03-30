package com.mansi.focusway.ui.collaborative

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mansi.focusway.R
import com.mansi.focusway.core.ui.NeonCyan
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

// Study group data model
data class StudyGroup(
    val id: String,
    val name: String,
    val memberCount: Int,
    val isJoined: Boolean = false
)

// Study member data model
data class StudyMember(
    val id: String,
    val name: String,
    val studyTime: Long = 0, // in seconds
    val isActive: Boolean = false
)

// Screen states
sealed class CollaborativeScreenState {
    object GroupList : CollaborativeScreenState()
    data class GroupDetail(val groupId: String) : CollaborativeScreenState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupStudyScreen(
    onNavigateBack: () -> Unit = {}
) {
    // State
    var screenState by remember { mutableStateOf<CollaborativeScreenState>(CollaborativeScreenState.GroupList) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Sample data
    val allGroups = remember {
        listOf(
            StudyGroup("g1", "WE CAN DO IT", 4, true),
            StudyGroup("g2", "ASSIGNMENT GROUP", 3, false),
            StudyGroup("g3", "EXAM PREP", 7, true),
            StudyGroup("g4", "NIGHT OWLS", 5, false),
            StudyGroup("g5", "CODING BUDDIES", 4, false)
        )
    }
    
    val groupMembers = remember {
        mapOf(
            "g1" to listOf(
                StudyMember("m1", "Mansi Sharma", 3600, true),
                StudyMember("m2", "Himanshu S", 2500, true),
                StudyMember("m3", "Priya Verma", 1800, false),
                StudyMember("m4", "Anil Kumar", 4200, true)
            ),
            "g3" to listOf(
                StudyMember("m5", "Mansi Sharma", 1800, true),
                StudyMember("m6", "Raj Patel", 3600, false),
                StudyMember("m7", "Shruti Gupta", 2400, true),
                StudyMember("m8", "Vikram S", 900, true),
                StudyMember("m9", "Ananya K", 1200, false),
                StudyMember("m10", "Deepak M", 3000, true),
                StudyMember("m11", "Lakshmi R", 1500, true)
            )
        )
    }
    
    var activeMembers by remember { mutableStateOf(groupMembers) }
    
    // Background gradient
    val darkNavy = Color(0xFF121420)
    val darkPurple = Color(0xFF1E1B2C)
    
    // Function to play sounds
    fun playTimerSound(beepCount: Int) {
        try {
            // Vibrate for haptic feedback
            try {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vibratorManager?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }
                
                vibrator?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        it.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 250, 100, 250), -1))
                    } else {
                        @Suppress("DEPRECATION")
                        it.vibrate(longArrayOf(0, 250, 100, 250), -1)
                    }
                }
            } catch (e: Exception) {
                // Silently ignore vibration errors
                Log.e("GroupStudyScreen", "Vibration failed", e)
            }
            
            // Create sound player only when needed
            try {
                val soundResId = if (beepCount == 1) R.raw.single_beep else R.raw.double_beep
                val mediaPlayer = MediaPlayer.create(context, soundResId)
                mediaPlayer?.let { player ->
                    player.setOnCompletionListener { mp ->
                        try {
                            mp.release()
                        } catch (e: Exception) {
                            // Ignore release errors
                        }
                    }
                    player.start()
                }
            } catch (e: Exception) {
                Log.e("GroupStudyScreen", "Sound playback failed", e)
            }
        } catch (e: Exception) {
            // Catch any unexpected errors in the entire sound playing process
            Log.e("GroupStudyScreen", "Error in playTimerSound", e)
        }
    }
    
    // Timer effect for active members
    LaunchedEffect(screenState) {
        if (screenState is CollaborativeScreenState.GroupDetail) {
            try {
                val groupId = (screenState as CollaborativeScreenState.GroupDetail).groupId
                val lastFifteenMinNotification = mutableMapOf<String, Long>()
                val lastThirtyMinNotification = mutableMapOf<String, Long>()
                // Track last notification time to limit frequency
                var lastGlobalNotificationTime = 0L
                
                while (true) {
                    delay(1000)
                    try {
                        // Check if we should check for notifications (not too frequent)
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastGlobalNotificationTime < 5000) { // Minimum 5 seconds between notifications
                            continue
                        }
                        
                        groupMembers[groupId]?.let { members ->
                            val updatedMembers = members.map { member ->
                                if (member.isActive) {
                                    val newStudyTime = member.studyTime + 1
                                    
                                    // Check for 15-minute intervals - only for the active user's session
                                    if (newStudyTime % 900L == 0L && newStudyTime > 0) { // 15 minutes = 900 seconds
                                        val lastMemberNotificationTime = lastFifteenMinNotification[member.id] ?: 0L
                                        if (newStudyTime - lastMemberNotificationTime >= 900L) {
                                            lastFifteenMinNotification[member.id] = newStudyTime
                                            
                                            // Calculate if this is a 15 or 30 minute interval
                                            val isThirtyMinInterval = newStudyTime % 1800L == 0L // 30 minutes = 1800 seconds
                                            
                                            // Update last notification time
                                            lastGlobalNotificationTime = currentTime
                                            
                                            // Delay slightly to prevent UI jank
                                            delay(100)
                                            
                                            if (isThirtyMinInterval) {
                                                lastThirtyMinNotification[member.id] = newStudyTime
                                                playTimerSound(2) // Double beep
                                            } else {
                                                playTimerSound(1) // Single beep
                                            }
                                        }
                                    }
                                    
                                    member.copy(studyTime = newStudyTime)
                                } else {
                                    member
                                }
                            }
                            activeMembers = activeMembers.toMutableMap().apply {
                                put(groupId, updatedMembers)
                            }
                        }
                    } catch (e: Exception) {
                        // Log error but continue the loop
                        Log.e("GroupStudyScreen", "Error in timer update loop", e)
                    }
                }
            } catch (e: Exception) {
                // Handle any exceptions
                Log.e("GroupStudyScreen", "Error in timer LaunchedEffect", e)
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            when (screenState) {
                                is CollaborativeScreenState.GroupList -> "Study Groups"
                                is CollaborativeScreenState.GroupDetail -> {
                                    val groupId = (screenState as CollaborativeScreenState.GroupDetail).groupId
                                    allGroups.find { it.id == groupId }?.name ?: "Study Session"
                                }
                            },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (screenState is CollaborativeScreenState.GroupDetail) {
                                    screenState = CollaborativeScreenState.GroupList
                                } else {
                                    onNavigateBack()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = NeonCyan
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = darkNavy
                    )
                )
            }
        ) { paddingValues ->
            // Main content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(darkNavy, darkPurple)
                        )
                    )
            ) {
                when (screenState) {
                    is CollaborativeScreenState.GroupList -> {
                        GroupListScreen(
                            allGroups = allGroups,
                            onGroupSelected = { groupId ->
                                screenState = CollaborativeScreenState.GroupDetail(groupId)
                            }
                        )
                    }
                    is CollaborativeScreenState.GroupDetail -> {
                        val groupId = (screenState as CollaborativeScreenState.GroupDetail).groupId
                        GroupDetailScreen(
                            groupId = groupId,
                            members = activeMembers[groupId] ?: emptyList(),
                            isJoined = allGroups.find { it.id == groupId }?.isJoined ?: false
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GroupListScreen(
    allGroups: List<StudyGroup>,
    onGroupSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tabs for All Groups vs Joined Groups
        var selectedTab by remember { mutableStateOf(0) }
        
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = NeonCyan,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier,
                    height = 2.dp,
                    color = NeonCyan
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("All Groups") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Joined Groups") }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Group list
        val displayGroups = if (selectedTab == 0) {
            allGroups
        } else {
            allGroups.filter { it.isJoined }
        }
        
        if (displayGroups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No groups found. Join a study group to get started!",
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayGroups) { group ->
                    GroupCard(
                        group = group,
                        onClick = { onGroupSelected(group.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun GroupCard(
    group: StudyGroup,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A2035)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Group icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                NeonCyan.copy(alpha = 0.8f),
                                NeonCyan.copy(alpha = 0.3f)
                            )
                        )
                    )
                    .border(
                        width = if (group.isJoined) 2.dp else 0.dp,
                        color = NeonCyan,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${group.memberCount} members",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
            
            if (group.isJoined) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(NeonCyan.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Joined",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun GroupDetailScreen(
    groupId: String,
    members: List<StudyMember>,
    isJoined: Boolean
) {
    var joinedState by remember { mutableStateOf(isJoined) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Stats summary
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E273E)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = members.size.toString(),
                    label = "Members"
                )
                
                Divider(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp),
                    color = Color.White.copy(alpha = 0.2f)
                )
                
                StatItem(
                    value = members.count { it.isActive }.toString(),
                    label = "Active"
                )
                
                Divider(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp),
                    color = Color.White.copy(alpha = 0.2f)
                )
                
                StatItem(
                    value = formatTotalTime(members.sumOf { it.studyTime }),
                    label = "Total Time"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Members list
        Text(
            text = "Study Members",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = NeonCyan
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(members) { member ->
                MemberCard(member = member)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Join/Leave button
        Button(
            onClick = { joinedState = !joinedState },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (joinedState) Color.Red else NeonCyan,
                contentColor = if (joinedState) Color.White else Color.Black
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = if (joinedState) "Leave Session" else "Join Session",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun MemberCard(member: StudyMember) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale = if (member.isActive) {
        infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        ).value
    } else {
        0.6f
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (member.isActive) 
                Color(0xFF1A3A60) else Color(0xFF272727)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with timer circle
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .drawBehind {
                        if (member.isActive) {
                            // Pulsing circle
                            drawCircle(
                                color = NeonCyan.copy(alpha = 0.3f * pulseScale),
                                radius = size.maxDimension/1.7f * pulseScale,
                                center = center
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (member.isActive) NeonCyan else Color.Gray)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.name.first().toString(),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (member.isActive) Color.Green else Color.Red)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = if (member.isActive) "Studying now" else "Taking a break",
                        color = if (member.isActive) NeonCyan else Color.Gray,
                        fontSize = 14.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Study progress
                LinearProgressIndicator(
                    progress = { member.studyTime.toFloat() / 7200f }, // 2 hour max
                    modifier = Modifier.fillMaxWidth(),
                    color = NeonCyan,
                    trackColor = Color(0xFF1E1E1E)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Time display
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF1A2A3A))
                        .padding(8.dp)
                ) {
                    val hours = TimeUnit.SECONDS.toHours(member.studyTime)
                    val minutes = TimeUnit.SECONDS.toMinutes(member.studyTime) % 60
                    
                    Text(
                        text = String.format("%02d:%02d", hours, minutes),
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// Helper function to format total time
private fun formatTotalTime(totalSeconds: Long): String {
    val hours = TimeUnit.SECONDS.toHours(totalSeconds)
    return "${hours}h"
} 