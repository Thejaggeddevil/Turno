package com.mansi.focusway.features.timer.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TimerViewModel : ViewModel() {
    
    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()
    
    fun startTimer() {
        _timerState.value = _timerState.value.copy(isRunning = true)
    }
    
    fun stopTimer() {
        _timerState.value = _timerState.value.copy(isRunning = false)
    }
}

data class TimerState(
    val isRunning: Boolean = false,
    val elapsedTime: Long = 0,
    val selectedTask: String? = null
) 