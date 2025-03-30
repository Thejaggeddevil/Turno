package com.mansi.focusway.di

import android.content.Context
import android.util.Log
import com.mansi.focusway.data.database.FocusWayDatabase
import com.mansi.focusway.data.repository.FocusSessionRepository
import com.mansi.focusway.data.repository.StatsRepository
import com.mansi.focusway.data.repository.TaskRepository

private const val TAG = "Turno-AppModule"

/**
 * Dependency injection module for the app
 */
object AppModule {
    @Volatile
    private var database: FocusWayDatabase? = null
    private var taskRepository: TaskRepository? = null
    private var focusSessionRepository: FocusSessionRepository? = null
    private var statsRepository: StatsRepository? = null

    fun provideDatabase(context: Context): FocusWayDatabase {
        return database ?: synchronized(this) {
            try {
                val instance = FocusWayDatabase.getDatabase(context)
                database = instance
                instance
            } catch (e: Exception) {
                Log.e(TAG, "Error creating database", e)
                // If database creation fails, try again with a safer approach
                val fallbackInstance = FocusWayDatabase.getDatabase(context.applicationContext)
                database = fallbackInstance
                fallbackInstance
            }
        }
    }

    fun provideTaskRepository(context: Context): TaskRepository {
        return taskRepository ?: synchronized(this) {
            try {
                val database = provideDatabase(context)
                val newRepo = TaskRepository(database.taskDao())
                taskRepository = newRepo
                newRepo
            } catch (e: Exception) {
                Log.e(TAG, "Error creating TaskRepository", e)
                
                // Try to recover by recreating the database
                try {
                    Log.w(TAG, "Attempting to recreate database and repository")
                    database = null // Force recreation
                    val db = provideDatabase(context)
                    val recoveryRepo = TaskRepository(db.taskDao())
                    taskRepository = recoveryRepo
                    recoveryRepo
                } catch (e2: Exception) {
                    Log.e(TAG, "Fatal error: Failed to create TaskRepository", e2)
                    throw RuntimeException("Cannot create TaskRepository: ${e.message}, recovery also failed: ${e2.message}", e)
                }
            }
        }
    }

    fun provideFocusSessionRepository(context: Context): FocusSessionRepository {
        return focusSessionRepository ?: synchronized(this) {
            try {
                val database = provideDatabase(context)
                val newRepo = FocusSessionRepository(database.focusSessionDao())
                focusSessionRepository = newRepo
                newRepo
            } catch (e: Exception) {
                Log.e(TAG, "Error creating FocusSessionRepository", e)
                
                // Try to recover by recreating the database
                try {
                    Log.w(TAG, "Attempting to recreate database and repository")
                    database = null // Force recreation
                    val db = provideDatabase(context)
                    val recoveryRepo = FocusSessionRepository(db.focusSessionDao())
                    focusSessionRepository = recoveryRepo
                    recoveryRepo
                } catch (e2: Exception) {
                    Log.e(TAG, "Fatal error: Failed to create FocusSessionRepository", e2)
                    throw RuntimeException("Cannot create FocusSessionRepository: ${e.message}, recovery also failed: ${e2.message}", e)
                }
            }
        }
    }

    fun provideStatsRepository(context: Context): StatsRepository {
        return statsRepository ?: synchronized(this) {
            try {
                val database = provideDatabase(context)
                val newRepo = StatsRepository(
                    database.dailyStatsDao(),
                    database.focusSessionDao()
                )
                statsRepository = newRepo
                newRepo
            } catch (e: Exception) {
                Log.e(TAG, "Error creating StatsRepository", e)
                
                // Try to recover by recreating the database
                try {
                    Log.w(TAG, "Attempting to recreate database and repository")
                    database = null // Force recreation
                    val db = provideDatabase(context)
                    val recoveryRepo = StatsRepository(
                        db.dailyStatsDao(),
                        db.focusSessionDao()
                    )
                    statsRepository = recoveryRepo
                    recoveryRepo
                } catch (e2: Exception) {
                    Log.e(TAG, "Fatal error: Failed to create StatsRepository", e2)
                    throw RuntimeException("Cannot create StatsRepository: ${e.message}, recovery also failed: ${e2.message}", e)
                }
            }
        }
    }
} 