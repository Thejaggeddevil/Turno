package com.mansi.focusway.ui.todo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mansi.focusway.ui.theme.FocusWayTheme
import com.mansi.focusway.ui.theme.NeonCyan
import com.mansi.focusway.ui.todo.priorities
import com.mansi.focusway.ui.todo.categories
import com.mansi.focusway.ui.todo.weekDays
import com.mansi.focusway.ui.todo.CategoryChip
import com.mansi.focusway.ui.todo.PriorityChip
import com.mansi.focusway.ui.todo.DayChip
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    taskId: Int,
    onNavigateBack: () -> Unit = {},
    onTaskDeleted: () -> Unit = {},
    onError: (String, Throwable?) -> Unit = { _, _ -> },
    viewModel: TodoViewModel = viewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // State for task fields
    var taskTitle by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(0) } 
    var selectedCategory by remember { mutableStateOf("") }
    var repeatDaily by remember { mutableStateOf(false) }
    var repeatWeekly by remember { mutableStateOf(false) }
    var selectedDays by remember { mutableStateOf(emptySet<String>()) }
    var everyDaySelected by remember { mutableStateOf(false) }
    var dueDate by remember { mutableStateOf<Long?>(null) }
    var isCompleted by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Initialize ViewModel and load task data
    LaunchedEffect(key1 = true) {
        try {
            viewModel.initialize(context)
            viewModel.getTaskById(
                taskId = taskId,
                onTaskFound = { task ->
                    taskTitle = task.title
                    taskDescription = task.description
                    selectedPriority = task.priority
                    selectedCategory = task.category
                    repeatDaily = task.repeatDaily
                    repeatWeekly = task.repeatWeekly
                    selectedDays = if (task.repeatDays.isNotEmpty()) {
                        task.repeatDays.split(",").toSet()
                    } else {
                        emptySet()
                    }
                    everyDaySelected = selectedDays.size == weekDays.size
                    dueDate = task.dueDate
                    isCompleted = task.isCompleted
                },
                onError = { error ->
                    onError(error, null)
                    onNavigateBack()
                }
            )
        } catch (e: Exception) {
            onError("Failed to load task", e)
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Task") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Delete button
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Task",
                            tint = Color.Red.copy(alpha = 0.8f)
                        )
                    }
                    
                    // Save button
                    IconButton(onClick = {
                        if (taskTitle.isNotBlank()) {
                            try {
                                viewModel.updateTask(
                                    id = taskId,
                                    title = taskTitle,
                                    description = taskDescription,
                                    priority = selectedPriority,
                                    category = selectedCategory,
                                    repeatDaily = repeatDaily,
                                    repeatWeekly = repeatWeekly,
                                    repeatDays = selectedDays.joinToString(","),
                                    dueDate = dueDate,
                                    isCompleted = isCompleted
                                )
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Task updated")
                                }
                                onNavigateBack()
                            } catch (e: Exception) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Failed to update task")
                                }
                                onError("Failed to update task", e)
                            }
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Task title cannot be empty")
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save Task"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Completion Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isCompleted,
                    onCheckedChange = { isCompleted = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = NeonCyan,
                        uncheckedColor = Color.Gray
                    )
                )
                
                Text(
                    text = if (isCompleted) "Mark as incomplete" else "Mark as complete",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Task Title
            OutlinedTextField(
                value = taskTitle,
                onValueChange = { taskTitle = it },
                label = { Text("Task Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = priorities[selectedPriority].second,
                    cursorColor = priorities[selectedPriority].second,
                    focusedLabelColor = priorities[selectedPriority].second
                )
            )

            // Task Description
            OutlinedTextField(
                value = taskDescription,
                onValueChange = { taskDescription = it },
                label = { Text("Description (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = priorities[selectedPriority].second,
                    cursorColor = priorities[selectedPriority].second,
                    focusedLabelColor = priorities[selectedPriority].second
                )
            )

            // Priority Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Priority",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Priority selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        priorities.forEachIndexed { index, (label, color) ->
                            PriorityChip(
                                label = label,
                                color = color,
                                selected = selectedPriority == index,
                                onSelected = { selectedPriority = index }
                            )
                        }
                    }
                }
            }

            // Date & Time Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Due Date (Optional)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (dueDate != null)
                                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(dueDate!!))
                            else 
                                "No due date set",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        // In a real app, this would show a DatePickerDialog
                        // For this example, we'll just set it to tomorrow
                        Button(
                            onClick = {
                                val calendar = Calendar.getInstance()
                                calendar.add(Calendar.DAY_OF_YEAR, 1)
                                calendar.set(Calendar.HOUR_OF_DAY, 23)
                                calendar.set(Calendar.MINUTE, 59)
                                dueDate = calendar.timeInMillis
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan,
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Set Due Date")
                        }
                    }
                }
            }

            // Category Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Category",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Categories as chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.take(3).forEach { category ->
                            CategoryChip(
                                category = category,
                                selected = selectedCategory == category,
                                onSelected = { selectedCategory = category }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.drop(3).forEach { category ->
                            CategoryChip(
                                category = category,
                                selected = selectedCategory == category,
                                onSelected = { selectedCategory = category }
                            )
                        }
                    }
                }
            }

            // Repeat Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Repeat",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Repeat Options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Daily")
                        Switch(
                            checked = repeatDaily,
                            onCheckedChange = { repeatDaily = it }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Weekly")
                        Switch(
                            checked = repeatWeekly,
                            onCheckedChange = { repeatWeekly = it }
                        )
                    }

                    // Day selection (if weekly is selected)
                    if (repeatWeekly) {
                        Text(
                            text = "Select Days",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        // Everyday checkbox
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = everyDaySelected,
                                onCheckedChange = { checked ->
                                    everyDaySelected = checked
                                    selectedDays = if (checked) {
                                        weekDays.toSet()
                                    } else {
                                        emptySet()
                                    }
                                }
                            )
                            Text(
                                text = "Everyday",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        // Day chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            weekDays.forEach { day ->
                                DayChip(
                                    day = day,
                                    selected = selectedDays.contains(day),
                                    onSelected = {
                                        selectedDays = if (selectedDays.contains(day)) {
                                            everyDaySelected = false
                                            selectedDays - day
                                        } else {
                                            val newSet = selectedDays + day
                                            everyDaySelected = newSet.size == weekDays.size
                                            newSet
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Update Button
            Button(
                onClick = {
                    if (taskTitle.isNotBlank()) {
                        viewModel.updateTask(
                            id = taskId,
                            title = taskTitle,
                            description = taskDescription,
                            priority = selectedPriority,
                            category = selectedCategory,
                            repeatDaily = repeatDaily,
                            repeatWeekly = repeatWeekly,
                            repeatDays = selectedDays.joinToString(","),
                            dueDate = dueDate,
                            isCompleted = isCompleted
                        )
                        onNavigateBack()
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Task title cannot be empty")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = Color.Black
                )
            ) {
                Text("Update Task", fontWeight = FontWeight.Bold)
            }

            // Delete Button
            Button(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red.copy(alpha = 0.8f),
                    contentColor = Color.White
                )
            ) {
                Text("Delete Task", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete this task?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            viewModel.deleteTaskById(taskId)
                            showDeleteConfirmation = false
                            onTaskDeleted()
                        } catch (e: Exception) {
                            showDeleteConfirmation = false
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Failed to delete task")
                            }
                            onError("Failed to delete task", e)
                        }
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditTaskScreenPreview() {
    FocusWayTheme(darkTheme = true) {
        EditTaskScreen(taskId = 1)
    }
} 