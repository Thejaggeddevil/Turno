package com.mansi.focusway.ui.study.flip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Data class representing the UI state for the FlipTimerScreen
 */
data class FlipTimerUiState(
    val isStudying: Boolean = false,
    val currentTime: String = "00:00:00",
    val sessionCount: Int = 0,
    val totalStudyTimeToday: Long = 0L,
    val totalTimeFormatted: String = "00:00:00"
)

/**
 * ViewModel for the flip timer feature
 */
class FlipTimerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FlipTimerUiState())
    val uiState: StateFlow<FlipTimerUiState> = _uiState.asStateFlow()
    
    private var timerJob: Job? = null
    private var startTime: Long = 0
    private var currentSessionTime: Long = 0
    private var totalStudyTime: Long = 0
    
    /**
     * Starts the study timer
     */
    fun startStudying() {
        if (_uiState.value.isStudying) return
        
        _uiState.update { it.copy(isStudying = true) }
        
        startTime = System.currentTimeMillis()
        timerJob?.cancel()
        timerJob = startTimer()
    }
    
    /**
     * Pauses the study timer
     */
    fun pauseStudying() {
        if (!_uiState.value.isStudying) return
        
        // Update total study time
        val currentTime = System.currentTimeMillis()
        currentSessionTime += (currentTime - startTime)
        totalStudyTime += (currentTime - startTime)
        
        // Format the total time
        val formattedTotalTime = formatTime(totalStudyTime)
        
        // Increment session count if it was a meaningful session (more than 1 minute)
        val sessionIncrement = if (currentTime - startTime > 60000) 1 else 0
        
        _uiState.update { 
            it.copy(
                isStudying = false,
                totalStudyTimeToday = totalStudyTime,
                totalTimeFormatted = formattedTotalTime,
                sessionCount = it.sessionCount + sessionIncrement
            ) 
        }
        
        timerJob?.cancel()
        timerJob = null
    }
    
    /**
     * Resets the study timer
     */
    fun resetTimer() {
        pauseStudying()
        currentSessionTime = 0
        _uiState.update { it.copy(currentTime = "00:00:00") }
    }
    
    /**
     * Starts a timer that updates the study time every second
     */
    private fun startTimer(): Job = viewModelScope.launch {
        while (true) {
            delay(1000) // Update every second
            
            val currentTime = System.currentTimeMillis()
            val elapsedTime = currentTime - startTime + currentSessionTime
            
            // Format the time as HH:MM:SS
            val formattedTime = formatTime(elapsedTime)
            
            _uiState.update { it.copy(currentTime = formattedTime) }
        }
    }
    
    /**
     * Formats a time in milliseconds as HH:MM:SS
     */
    private fun formatTime(timeInMillis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(timeInMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) % 60
        
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
    
    override fun onCleared() {
        super.onCleared()
        pauseStudying()
    }
} 