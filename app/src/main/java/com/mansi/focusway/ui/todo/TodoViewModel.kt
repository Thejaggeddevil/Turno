package com.mansi.focusway.ui.todo

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mansi.focusway.data.database.TaskEntity
import com.mansi.focusway.data.repository.TaskRepository
import com.mansi.focusway.di.AppModule
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "Turno-TodoViewModel"

data class Task(
    val id: Long = 0,
    val title: String,
    val category: String,
    val color: Int,
    val startTime: String,
    val endTime: String,
    val repeatDays: List<Int>,
    val createdDate: Date = Date()
)

class TodoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()
    
    private var taskRepository: TaskRepository? = null
    
    private val _selectedCategory = MutableLiveData<String>("All")
    val selectedCategory: LiveData<String> = _selectedCategory
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()
    
    private val _errorState = MutableStateFlow<String?>(null)
    val errorState = _errorState.asStateFlow()
    
    private val _taskWithCategories = MutableStateFlow<List<TaskEntity>>(emptyList())
    val taskWithCategories = _taskWithCategories.asStateFlow()
    
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()
    
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Coroutine exception: ${throwable.message}", throwable)
        _errorState.value = "An error occurred: ${throwable.localizedMessage ?: "Unknown error"}"
        _uiState.value = _uiState.value.copy(isLoading = false)
    }
    
    fun initialize(context: Context) {
        viewModelScope.launch(exceptionHandler) {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                taskRepository = AppModule.provideTaskRepository(context)
                loadTasks()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize TodoViewModel", e)
                _errorState.value = "Failed to initialize: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    private fun loadTasks() {
        viewModelScope.launch(exceptionHandler) {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                taskRepository?.getAllTasks()
                    ?.onStart { _isRefreshing.value = true }
                    ?.onCompletion { _isRefreshing.value = false }
                    ?.catch { e -> 
                        Log.e(TAG, "Error loading tasks", e)
                        _errorState.value = "Failed to load tasks: ${e.localizedMessage ?: "Unknown error"}"
                        _taskWithCategories.value = emptyList()
                    }
                    ?.collectLatest { tasks ->
                        _taskWithCategories.value = tasks
                        val activeTasks = tasks.filter { !it.isCompleted }
                        val completedTasks = tasks.filter { it.isCompleted }
                        
                        _uiState.value = _uiState.value.copy(
                            tasks = tasks,
                            allTasks = tasks,
                            activeTasks = activeTasks,
                            completedTasks = completedTasks,
                            filteredTasks = if (_selectedCategory.value == "All") tasks else filterTasksByCategory(tasks, _selectedCategory.value ?: "All"),
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error in loadTasks", e)
                _errorState.value = "Failed to load tasks: ${e.localizedMessage ?: "Unknown error"}"
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    private fun filterTasksByCategory(tasks: List<TaskEntity>, category: String): List<TaskEntity> {
        return if (category == "All") tasks else tasks.filter { it.category == category }
    }
    
    fun getTaskById(taskId: Int, onTaskFound: (TaskEntity) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(exceptionHandler) {
            try {
                val task = taskRepository?.getTaskById(taskId)
                if (task != null) {
                    onTaskFound(task)
                } else {
                    onError("Task not found")
                    _errorState.value = "Task with ID $taskId not found"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in getTaskById", e)
                onError("Error getting task: ${e.localizedMessage ?: "Unknown error"}")
                _errorState.value = "Failed to get task: ${e.localizedMessage ?: "Unknown error"}"
            }
        }
    }
    
    fun toggleTaskCompletion(taskId: Int, isCompleted: Boolean = false) {
        viewModelScope.launch(exceptionHandler) {
            try {
                taskRepository?.updateTaskCompletion(taskId, isCompleted)
                refreshTasks()
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling task completion", e)
                _errorState.value = "Failed to update task: ${e.localizedMessage ?: "Unknown error"}"
            }
        }
    }
    
    fun refreshTasks() {
        loadTasks()
    }
    
    fun addTask(
        title: String,
        category: String,
        color: Int,
        startTime: String,
        endTime: String,
        repeatDays: List<Int>
    ) {
        val newTask = Task(
            id = System.currentTimeMillis(),
            title = title,
            category = category,
            color = color,
            startTime = startTime,
            endTime = endTime,
            repeatDays = repeatDays
        )
        
        val currentList = _tasks.value.toMutableList()
        currentList.add(newTask)
        _tasks.value = currentList
        
        // In a real app, save to database
        saveTask(newTask)
    }
    
    private fun saveTask(task: Task) {
        viewModelScope.launch(exceptionHandler) {
            // Save to database or repository
        }
    }
    
    fun addTaskWithAllFields(
        title: String,
        description: String = "",
        category: String = "",
        priority: Int = 0,
        dueDate: Long? = null,
        repeatDaily: Boolean = false,
        repeatWeekly: Boolean = false,
        repeatDays: String = "",
        isCompleted: Boolean = false
    ) {
        if (title.isBlank()) return
        
        viewModelScope.launch(exceptionHandler) {
            try {
                val task = TaskEntity(
                    title = title,
                    description = description,
                    category = category,
                    priority = priority,
                    dueDate = dueDate,
                    repeatDaily = repeatDaily,
                    repeatWeekly = repeatWeekly,
                    repeatDays = repeatDays,
                    isCompleted = isCompleted,
                    createdAt = System.currentTimeMillis()
                )
                taskRepository?.insertTask(task)
                refreshTasks()
            } catch (e: Exception) {
                Log.e(TAG, "Error adding task with all fields", e)
                _errorState.value = "Failed to add task: ${e.localizedMessage ?: "Unknown error"}"
            }
        }
    }
    
    fun updateTask(
        id: Int,
        title: String,
        description: String = "",
        category: String = "",
        priority: Int = 0,
        dueDate: Long? = null,
        repeatDaily: Boolean = false,
        repeatWeekly: Boolean = false,
        repeatDays: String = "",
        isCompleted: Boolean = false
    ) {
        if (title.isBlank()) return
        
        viewModelScope.launch(exceptionHandler) {
            try {
                val currentTask = taskRepository?.getTaskById(id)
                currentTask?.let {
                    val updatedTask = it.copy(
                        title = title,
                        description = description,
                        category = category,
                        priority = priority,
                        dueDate = dueDate,
                        repeatDaily = repeatDaily,
                        repeatWeekly = repeatWeekly,
                        repeatDays = repeatDays,
                        isCompleted = isCompleted
                    )
                    taskRepository?.updateTask(updatedTask)
                    refreshTasks()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating task", e)
                _errorState.value = "Failed to update task: ${e.localizedMessage ?: "Unknown error"}"
            }
        }
    }
    
    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch(exceptionHandler) {
            try {
                taskRepository?.deleteTask(task)
                refreshTasks()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting task", e)
                _errorState.value = "Failed to delete task: ${e.localizedMessage ?: "Unknown error"}"
            }
        }
    }
    
    fun deleteTaskById(taskId: Int, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch(exceptionHandler) {
            try {
                val task = taskRepository?.getTaskById(taskId)
                if (task != null) {
                    taskRepository?.deleteTask(task)
                    refreshTasks()
                    onSuccess()
                } else {
                    val errorMsg = "Failed to delete task with ID $taskId: task not found"
                    _errorState.value = errorMsg
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting task by ID", e)
                val errorMsg = "Error deleting task: ${e.localizedMessage ?: "Unknown error"}"
                _errorState.value = errorMsg
                onError(errorMsg)
            }
        }
    }
    
    fun clearError() {
        _errorState.value = null
    }
}

data class TodoUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val filteredTasks: List<TaskEntity> = emptyList(),
    val activeTasks: List<TaskEntity> = emptyList(),
    val completedTasks: List<TaskEntity> = emptyList(),
    val allTasks: List<TaskEntity> = emptyList(),
    val isLoading: Boolean = true
) 