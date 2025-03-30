package com.mansi.focusway

import android.app.Application
import com.mansi.focusway.di.AppModule

/**
 * Application class for the Turno app
 */
class TurnoApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize database early to avoid first-time lag
        try {
            AppModule.provideDatabase(applicationContext)
        } catch (e: Exception) {
            // Log error but don't crash
            e.printStackTrace()
        }
    }
} 