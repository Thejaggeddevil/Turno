package com.mansi.focusway.features.stats.viewmodel

import androidx.lifecycle.ViewModel
import com.mansi.focusway.features.stats.model.DailyStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

class StatsViewModel : ViewModel() {
    
    private val _statsState = MutableStateFlow(StatsState())
    val statsState: StateFlow<StatsState> = _statsState.asStateFlow()
    
    fun loadStats() {
        // In a real app, this would load stats from a repository
        val mockDailyStats = listOf(
            DailyStats(date = Date(), totalFocusTime = 120, completedTasks = 5),
            DailyStats(date = Date(System.currentTimeMillis() - 86400000), totalFocusTime = 90, completedTasks = 3),
            DailyStats(date = Date(System.currentTimeMillis() - 172800000), totalFocusTime = 150, completedTasks = 7)
        )
        _statsState.value = _statsState.value.copy(dailyStats = mockDailyStats)
    }
}

data class StatsState(
    val dailyStats: List<DailyStats> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) 