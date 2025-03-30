package com.mansi.focusway.ui.todo

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: TodoViewModel = viewModel()
) {
    // State for task details
    var taskTitle by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Study") }
    var selectedColor by remember { mutableStateOf(Color(0xFF9B51E0)) } // Default purple
    var startTime by remember { mutableStateOf("05:00") }
    var endTime by remember { mutableStateOf("06:00") }
    var selectedDate by remember { mutableStateOf("03/30 Sun") }
    var isEveryDay by remember { mutableStateOf(true) }
    val selectedDays = remember { mutableStateListOf(0, 1, 2, 3, 4, 5, 6) } // All days selected by default
    
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // Category options
    val categories = remember { mutableStateListOf("Study", "Exercise", "Daily") }
    
    // Color options for first row
    val colorOptionsRow1 = remember {
        listOf(
            Color(0xFF9B51E0), // Purple
            Color(0xFFEF5DA8), // Pink
            Color(0xFFF44336), // Red
            Color(0xFFFF9800), // Orange
            Color(0xFFFFEB3B), // Yellow
        )
    }
    
    // Color options for second row
    val colorOptionsRow2 = remember {
        listOf(
            Color(0xFF4CAF50), // Green
            Color(0xFF2196F3), // Blue
            Color(0xFF03A9F4), // Light Blue
            Color(0xFF009688), // Teal
            Color(0xFF607D8B), // Bluish Grey
        )
    }
    
    // Day names
    val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val dayShortNames = listOf("S", "M", "T", "W", "T", "F", "S")
    
    // Time picker dialog
    fun showTimePickerDialog(isStartTime: Boolean) {
        // Parse the current time string
        val timeParts = (if (isStartTime) startTime else endTime).split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()
        
        val timePickerDialog = TimePickerDialog(
            context,
            { _, hourOfDay, minuteOfHour ->
                val formattedTime = String.format("%02d:%02d", hourOfDay, minuteOfHour)
                if (isStartTime) {
                    startTime = formattedTime
                } else {
                    endTime = formattedTime
                }
            },
            hour,
            minute,
            true
        )
        
        timePickerDialog.show()
    }
    
    // Background and UI content
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add task", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
        ) {
            // Title section
            Text(
                text = "Title",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            
            Text(
                text = "required (0)",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Title input
            OutlinedTextField(
                value = taskTitle,
                onValueChange = { taskTitle = it },
                placeholder = { Text("Please enter title", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.DarkGray,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                shape = RoundedCornerShape(4.dp)
            )
            
            // Category section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "Edit",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { /* Edit categories */ }
                )
            }
            
            // Category buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    CategoryPill(
                        text = category,
                        isSelected = selectedCategory == category,
                        onClick = { selectedCategory = category }
                    )
                }
                
                // Add category button
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .width(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2C2C2C))
                        .clickable { categories.add("New") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Category",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Color section
            Text(
                text = "Color",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Color options tabs
            var selectedColorTab by remember { mutableStateOf("Primary") }
            val colorTabs = listOf("Primary", "Custom", "Pastel", "Theme1", "Theme2")
            
            // Color tabs
            ScrollableTabRow(
                selectedTabIndex = colorTabs.indexOf(selectedColorTab),
                containerColor = Color(0xFF121212),
                contentColor = Color.White,
                edgePadding = 0.dp,
                divider = { /* No divider */ },
                indicator = { /* No indicator */ },
            ) {
                colorTabs.forEach { tab ->
                    Tab(
                        selected = selectedColorTab == tab,
                        onClick = { selectedColorTab = tab },
                        text = { Text(tab, fontSize = 12.sp) },
                        selectedContentColor = Color.White,
                        unselectedContentColor = Color.Gray
                    )
                }
            }
            
            // Color picker first row
            LazyRow(
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(colorOptionsRow1) { color ->
                    ColorOption(
                        colorValue = color,
                        isSelected = selectedColor == color,
                        onClick = { selectedColor = color }
                    )
                }
            }
            
            // Color picker second row
            LazyRow(
                modifier = Modifier.padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(colorOptionsRow2) { color ->
                    ColorOption(
                        colorValue = color,
                        isSelected = selectedColor == color,
                        onClick = { selectedColor = color }
                    )
                }
            }
            
            // Time section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Manage time",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Time",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { /* Add time */ }
                )
            }
            
            // Time picker row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Start time
                Column {
                    Text(
                        text = "Start at",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    TimePickerButton(
                        time = startTime,
                        onClick = { showTimePickerDialog(true) }
                    )
                }
                
                // End time
                Column {
                    Text(
                        text = "End at",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    TimePickerButton(
                        time = endTime,
                        onClick = { showTimePickerDialog(false) }
                    )
                }
            }
            
            // Date section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Date",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Previous Date",
                        tint = Color.Gray,
                        modifier = Modifier.clickable { /* Previous date */ }
                    )
                    
                    Text(
                        text = selectedDate,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = "Next Date",
                        tint = Color.Gray,
                        modifier = Modifier.clickable { /* Next date */ }
                    )
                }
            }
            
            // Repeat section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "repeat",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Everyday",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isEveryDay) selectedColor else Color(0xFF2C2C2C))
                            .clickable { 
                                isEveryDay = !isEveryDay
                                if (isEveryDay) {
                                    selectedDays.clear()
                                    selectedDays.addAll(listOf(0, 1, 2, 3, 4, 5, 6))
                                } else {
                                    selectedDays.clear()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isEveryDay) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Checked",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            
            // Day selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayShortNames.forEachIndexed { index, day ->
                    DayButton(
                        text = day,
                        isSelected = selectedDays.contains(index),
                        selectedColor = selectedColor,
                        onClick = {
                            if (isEveryDay) {
                                isEveryDay = false
                                selectedDays.clear()
                                selectedDays.add(index)
                            } else {
                                if (selectedDays.contains(index)) {
                                    selectedDays.remove(index)
                                } else {
                                    selectedDays.add(index)
                                }
                                
                                // If all days are selected, set everyday to true
                                if (selectedDays.size == 7) {
                                    isEveryDay = true
                                }
                            }
                        }
                    )
                }
            }
            
            Text(
                text = "Set the repeat end date",
                color = selectedColor,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 16.dp)
                    .clickable { /* Set end date */ }
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Empty spacer to push following content to the right
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = "not set",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            
            // Save and Cancel buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cancel button
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(4.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp,
                        brush = SolidColor(Color.DarkGray)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFF2C2C2C),
                        contentColor = Color.White
                    )
                ) {
                    Text("Cancel")
                }
                
                // Save button
                Button(
                    onClick = {
                        // Create task and save
                        viewModel.addTask(
                            title = taskTitle,
                            category = selectedCategory,
                            color = selectedColor.value.toInt(),
                            startTime = startTime,
                            endTime = endTime,
                            repeatDays = selectedDays.toList()
                        )
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = selectedColor,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CategoryPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Color.White else Color(0xFF2C2C2C))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black else Color.White,
            fontSize = 14.sp
        )
    }
}

@Composable
fun ColorOption(
    colorValue: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(colorValue)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = Color.White,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    )
}

@Composable
fun TimePickerButton(
    time: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(120.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF2C2C2C))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = time,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = "Select Time",
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun DayButton(
    text: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (isSelected) selectedColor else Color(0xFF2C2C2C))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
} 