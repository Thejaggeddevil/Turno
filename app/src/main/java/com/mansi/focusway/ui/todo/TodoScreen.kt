package com.mansi.focusway.ui.todo

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mansi.focusway.data.database.TaskEntity
import com.mansi.focusway.ui.navigation.Routes
import com.mansi.focusway.ui.theme.FocusWayTheme
import com.mansi.focusway.ui.theme.NeonCyan
import com.mansi.focusway.utils.formatDate
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
                title = { Text("Tasks") },
                actions = {
                    IconButton(onClick = { viewModel.refreshTasks() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                    IconButton(onClick = { /* Sort tasks */ }) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort Tasks"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTask,
                containerColor = NeonCyan
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task",
                    tint = Color.Black
                )
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeonCyan)
            }
        } else if (uiState.allTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.Gray.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No tasks yet",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.Gray.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap + to add a new task",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Active Tasks",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                if (uiState.activeTasks.isEmpty()) {
                    item {
                        Text(
                            text = "No active tasks",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                } else {
                    items(uiState.activeTasks) { task ->
                        TaskItem(
                            task = task,
                            onToggleCompletion = { viewModel.toggleTaskCompletion(task.id, !task.isCompleted) },
                            onDelete = { viewModel.deleteTask(task) },
                            onClick = { onEditTask(task.id) }
                        )
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Completed Tasks",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                if (uiState.completedTasks.isEmpty()) {
                    item {
                        Text(
                            text = "No completed tasks",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                } else {
                    items(uiState.completedTasks) { task ->
                        TaskItem(
                            task = task,
                            onToggleCompletion = { viewModel.toggleTaskCompletion(task.id, !task.isCompleted) },
                            onDelete = { viewModel.deleteTask(task) },
                            onClick = { onEditTask(task.id) }
                        )
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp)) // For FAB spacing
                }
            }
        }
    }
}

@Composable
private fun TaskItem(
    task: TaskEntity,
    onToggleCompletion: () -> Unit = {},
    onDelete: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    // Determine color based on priority
    val priorityColor = when (task.priority) {
        0 -> Color(0xFF4CAF50) // Low - Green
        1 -> Color(0xFFFF9800) // Medium - Orange
        else -> Color(0xFFE91E63) // High - Pink
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority color indicator
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(priorityColor)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Checkbox for completion
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleCompletion() },
                colors = CheckboxDefaults.colors(
                    checkedColor = NeonCyan,
                    uncheckedColor = Color.Gray
                )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Task details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                    color = if (task.isCompleted) Color.Gray else Color.White
                )
                
                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    // Format and display creation date
                    Text(
                        text = formatDate(task.createdAt, "MMM dd"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    // Show due date if available
                    if (task.dueDate != null) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Due: ${formatDate(task.dueDate, "MMM dd")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (task.dueDate < System.currentTimeMillis() && !task.isCompleted)
                                Color.Red.copy(alpha = 0.8f)
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    
                    // Show focus time if any
                    if (task.totalFocusTime > 0) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = NeonCyan.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatFocusTime(task.totalFocusTime),
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonCyan.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Task",
                    tint = Color.Gray
                )
            }
        }
    }
}

// Helper function to format focus time
private fun formatFocusTime(timeInMillis: Long): String {
    val hours = timeInMillis / (1000 * 60 * 60)
    val minutes = (timeInMillis / (1000 * 60)) % 60
    
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "<1m"
    }
}

@Preview(showBackground = true)
@Composable
private fun TodoScreenPreview() {
    FocusWayTheme(darkTheme = true) {
        TodoScreen()
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskItemPreview() {
    FocusWayTheme(darkTheme = true) {
        TaskItem(
            task = TaskEntity(
                id = 1,
                title = "Complete Math Assignment",
                description = "Finish Chapter 5 exercises",
                isCompleted = false,
                priority = 1,
                dueDate = System.currentTimeMillis() + 86400000 // tomorrow
            )
        )
    }
} 