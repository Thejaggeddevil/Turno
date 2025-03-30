package com.mansi.focusway.ui.todo

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mansi.focusway.data.database.TaskEntity
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    onNavigateToTimer: () -> Unit = {},
    onAddTask: () -> Unit = {},
    onEditTask: (Int) -> Unit = {},
    viewModel: TodoViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    // Initialize the view model
    LaunchedEffect(key1 = true) {
        viewModel.initialize(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("To-do list", fontWeight = FontWeight.Medium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212),
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { /* Navigate back */ }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            Column {
                // Category + FAB
                FloatingActionButton(
                    onClick = { /* Add Category */ },
                    containerColor = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Category", color = Color.Black, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add Category",
                            tint = Color.Black
                        )
                    }
                }
                
                // To-do list + FAB
            FloatingActionButton(
                    onClick = { 
                        try {
                            onAddTask()
                        } catch (e: Exception) {
                            Log.e("TodoScreen", "Failed to navigate to Add Task screen", e)
                            Toast.makeText(context, "Could not open Add Task screen. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    containerColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("To-do list", color = Color.Black, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.width(8.dp))
                Icon(
                            imageVector = Icons.Rounded.Add,
                    contentDescription = "Add Task",
                    tint = Color.Black
                )
            }
        }
            }
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Time info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "00:00 - 00:00",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Text(
                    text = "Sun, Mon, Wed, Fri",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
            }
        } else {
            LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // DAILY CATEGORY
                item {
                        CategoryHeader(name = "Daily", onShowMore = {})
                    }
                    
                    // Daily tasks
                    item {
                        TaskItem(
                            color = Color(0xFF4CAF50),
                            title = "Reading every day",
                            timeSpent = "5h 30m",
                            onToggleCompletion = {},
                            onMoreClick = {}
                        )
                    }
                    
                    item {
                        TaskItem(
                            color = Color(0xFF4CAF50),
                            title = "Do cleaning",
                            days = "Mon, Wed, Thu",
                            time = "09:00 - 10:00",
                            timeSpent = "5h 30m",
                            onToggleCompletion = {},
                            onMoreClick = {}
                        )
                    }
                    
                    // STUDY CATEGORY
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        CategoryHeader(name = "Upcoming", onShowMore = {})
                }
                
                item {
                        CategoryHeader(name = "Study", onShowMore = {})
                    }
                    
                    // Study tasks
                    item {
                        TaskItem(
                            color = Color(0xFFFF9800),
                            title = "Foreign language",
                            days = "Mon, Thu",
                            time = "13:00 - 15:00",
                            timeSpent = "5h 30m",
                            onToggleCompletion = {},
                            onMoreClick = {}
                        )
                    }
                    
                    item {
                        TaskItem(
                            color = Color(0xFFFF9800),
                            title = "Grammar",
                            days = "Mon, Wed, Fri",
                            time = "13:00 - 14:00",
                            timeSpent = "5h 30m",
                            onToggleCompletion = {},
                            onMoreClick = {}
                        )
                    }
                    
                    // EXERCISE CATEGORY
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        CategoryHeader(name = "Exercise", onShowMore = {})
                    }
                    
                    // Exercise tasks
                    item {
                        TaskItem(
                            color = Color(0xFF2196F3),
                            title = "Weight Training",
                            days = "Tue, Thu, Sat",
                            time = "20:00 - 21:00",
                            timeSpent = "5h 30m",
                            onToggleCompletion = {},
                            onMoreClick = {}
                        )
                    }
                    
                    // Add spacer at bottom for FAB
                item {
                        Spacer(modifier = Modifier.height(140.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(
    name: String,
    onShowMore: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        
        IconButton(onClick = onShowMore) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More Options",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun TaskItem(
    color: Color,
    title: String,
    days: String = "Everyday",
    time: String = "",
    timeSpent: String,
    onToggleCompletion: () -> Unit,
    onMoreClick: () -> Unit,
    isCompleted: Boolean = false
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
            .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
        // Color indicator
            Box(
                modifier = Modifier
                .size(4.dp, 24.dp)
                .background(color)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Task content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
            )
            
            if (time.isNotEmpty()) {
                Row {
                    Text(
                        text = time,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = days,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            } else {
                Text(
                    text = days,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
        
        // Total time
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "TOTAL: $timeSpent",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // More options
        IconButton(onClick = onMoreClick) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More Options",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun TodoScreenPreview() {
        TodoScreen()
} 