package com.mansi.focusway.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    private val PREFS_NAME = "focusway_settings"
    private val KEY_DARK_MODE = "dark_mode"
    private val KEY_VIBRATION = "vibration_enabled"
    private val KEY_NOTIFICATIONS = "notifications_enabled"
    
    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        viewModelScope.launch {
            _uiState.value = SettingsUiState(
                darkMode = prefs.getBoolean(KEY_DARK_MODE, true),
                vibrationEnabled = prefs.getBoolean(KEY_VIBRATION, true),
                notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS, true)
            )
        }
    }
    
    fun toggleDarkMode(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DARK_MODE, enabled)
            .apply()
        
        _uiState.value = _uiState.value.copy(darkMode = enabled)
    }
    
    fun toggleVibration(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_VIBRATION, enabled)
            .apply()
        
        _uiState.value = _uiState.value.copy(vibrationEnabled = enabled)
    }
    
    fun toggleNotifications(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_NOTIFICATIONS, enabled)
            .apply()
        
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
    }
    
    companion object {
        // Method to check vibration setting from any part of the app
        fun isVibrationEnabled(context: Context): Boolean {
            return context.getSharedPreferences("focusway_settings", Context.MODE_PRIVATE)
                .getBoolean("vibration_enabled", true)
        }
        
        // Method to check notification setting from any part of the app
        fun areNotificationsEnabled(context: Context): Boolean {
            return context.getSharedPreferences("focusway_settings", Context.MODE_PRIVATE)
                .getBoolean("notifications_enabled", true)
        }
        
        // Method to check dark mode setting from any part of the app
        fun isDarkModeEnabled(context: Context): Boolean {
            return context.getSharedPreferences("focusway_settings", Context.MODE_PRIVATE)
                .getBoolean("dark_mode", true)
        }
    }
}

data class SettingsUiState(
    val darkMode: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true
) 