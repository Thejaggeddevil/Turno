package com.mansi.focusway.ui.timer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mansi.focusway.data.database.TaskEntity
import com.mansi.focusway.data.repository.FocusSessionRepository
import com.mansi.focusway.data.repository.StatsRepository
import com.mansi.focusway.data.repository.TaskRepository
import com.mansi.focusway.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * View model for the timer screen
 */
class TimerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()
    
    private val _tasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    val tasks: StateFlow<List<TaskEntity>> = _tasks.asStateFlow()
    
    private var taskRepository: TaskRepository? = null
    private var focusSessionRepository: FocusSessionRepository? = null
    private var statsRepository: StatsRepository? = null
    
    fun initialize(context: Context) {
        taskRepository = AppModule.provideTaskRepository(context)
        focusSessionRepository = AppModule.provideFocusSessionRepository(context)
        statsRepository = AppModule.provideStatsRepository(context)
        
        loadTasks()
    }
    
    private fun loadTasks() {
        viewModelScope.launch {
            taskRepository?.getActiveTasks()?.collect { tasks ->
                _tasks.value = tasks
            }
        }
    }
    
    fun startTimer() {
        _uiState.value = _uiState.value.copy(
            isTimerRunning = true,
            startTime = System.currentTimeMillis()
        )
    }
    
    fun stopTimer() {
        val currentTime = System.currentTimeMillis()
        val startTime = _uiState.value.startTime
        
        if (startTime > 0 && _uiState.value.isTimerRunning) {
            val sessionDuration = currentTime - startTime
            saveSession(startTime, currentTime, sessionDuration)
            
            // Update daily total time
            val newDailyTotalTime = _uiState.value.dailyTotalTime + sessionDuration
            _uiState.value = _uiState.value.copy(
                isTimerRunning = false,
                startTime = 0L,
                dailyTotalTime = newDailyTotalTime
            )
        } else {
            _uiState.value = _uiState.value.copy(
                isTimerRunning = false,
                startTime = 0L
            )
        }
    }
    
    private fun saveSession(startTime: Long, endTime: Long, duration: Long) {
        viewModelScope.launch {
            val selectedTaskId = _uiState.value.selectedTaskId
            
            // Save focus session to database
            focusSessionRepository?.createSession(
                taskId = selectedTaskId,
                startTime = startTime,
                endTime = endTime
            )
            
            // Update task focus time if a task is selected
            if (selectedTaskId != null) {
                taskRepository?.updateTaskFocusTime(selectedTaskId, duration)
            }
            
            // Update daily stats
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Date(startTime))
            statsRepository?.updateDailyStats(dateStr)
        }
    }
    
    fun resetDailyTotalIfNewDay() {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date())
        val lastDate = _uiState.value.lastSavedDate
        
        if (currentDate != lastDate) {
            _uiState.value = _uiState.value.copy(
                dailyTotalTime = 0L,
                lastSavedDate = currentDate
            )
        }
    }
    
    fun selectTask(taskId: Int?) {
        _uiState.value = _uiState.value.copy(selectedTaskId = taskId)
        
        // Update selected task name for display
        viewModelScope.launch {
            if (taskId != null) {
                val task = taskRepository?.getTaskById(taskId)
                _uiState.value = _uiState.value.copy(
                    selectedTaskName = task?.title ?: "No task selected"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    selectedTaskName = "No task selected"
                )
            }
        }
    }
    
    fun updateElapsedTime(elapsedTime: Long) {
        _uiState.value = _uiState.value.copy(elapsedTime = elapsedTime)
    }
    
    fun loadDailyStats() {
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Date())
            
            statsRepository?.getStatsForToday()?.let { stats ->
                _uiState.value = _uiState.value.copy(
                    dailyTotalTime = stats.totalFocusTime,
                    sessionsCount = stats.sessionsCount,
                    lastSavedDate = today
                )
            }
        }
    }
}

data class TimerUiState(
    val isTimerRunning: Boolean = false,
    val startTime: Long = 0L,
    val elapsedTime: Long = 0L,
    val dailyTotalTime: Long = 0L,
    val sessionsCount: Int = 0,
    val selectedTaskId: Int? = null,
    val selectedTaskName: String = "No task selected",
    val lastSavedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
) 