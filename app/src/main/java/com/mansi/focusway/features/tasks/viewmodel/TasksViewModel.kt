package com.mansi.focusway.features.tasks.viewmodel

import androidx.lifecycle.ViewModel
import com.mansi.focusway.features.tasks.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TasksViewModel : ViewModel() {
    
    private val _tasksState = MutableStateFlow(TasksState())
    val tasksState: StateFlow<TasksState> = _tasksState.asStateFlow()
    
    fun addTask(task: Task) {
        val updatedTasks = _tasksState.value.tasks.toMutableList()
        updatedTasks.add(task)
        _tasksState.value = _tasksState.value.copy(tasks = updatedTasks)
    }
    
    fun deleteTask(taskId: String) {
        val updatedTasks = _tasksState.value.tasks.filter { it.id != taskId }
        _tasksState.value = _tasksState.value.copy(tasks = updatedTasks)
    }
    
    fun toggleTaskCompletion(taskId: String) {
        val updatedTasks = _tasksState.value.tasks.map { task ->
            if (task.id == taskId) {
                task.copy(completed = !task.completed)
            } else {
                task
            }
        }
        _tasksState.value = _tasksState.value.copy(tasks = updatedTasks)
    }
}

data class TasksState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) 