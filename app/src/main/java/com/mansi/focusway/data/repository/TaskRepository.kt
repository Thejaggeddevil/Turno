package com.mansi.focusway.data.repository

import com.mansi.focusway.data.database.TaskDao
import com.mansi.focusway.data.database.TaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for handling task operations
 */
class TaskRepository(private val taskDao: TaskDao) {
    
    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()
    
    fun getActiveTasks(): Flow<List<TaskEntity>> = taskDao.getActiveTasks()
    
    fun getCompletedTasks(): Flow<List<TaskEntity>> = taskDao.getCompletedTasks()
    
    suspend fun getTaskById(taskId: Int): TaskEntity? = taskDao.getTaskById(taskId)
    
    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)
    
    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)
    
    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)
    
    suspend fun updateTaskFocusTime(taskId: Int, additionalTime: Long) = 
        taskDao.updateTaskFocusTime(taskId, additionalTime)
    
    suspend fun updateTaskCompletion(taskId: Int, isCompleted: Boolean) = 
        taskDao.updateTaskCompletion(taskId, isCompleted)
        
    fun getTasksByCategory(category: String): Flow<List<TaskEntity>> = 
        taskDao.getTasksByCategory(category)
} 