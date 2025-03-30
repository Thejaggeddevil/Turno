package com.mansi.focusway.di

import android.content.Context
import com.mansi.focusway.data.repository.FocusSessionRepository
import com.mansi.focusway.data.repository.StatsRepository
import com.mansi.focusway.data.repository.TaskRepository

/**
 * Manager class to provide repository instances
 */
object RepositoryManager {
    fun getStatsRepository(context: Context): StatsRepository {
        return AppModule.provideStatsRepository(context)
    }
    
    fun getTaskRepository(context: Context): TaskRepository {
        return AppModule.provideTaskRepository(context)
    }
    
    fun getFocusSessionRepository(context: Context): FocusSessionRepository {
        return AppModule.provideFocusSessionRepository(context)
    }
} 