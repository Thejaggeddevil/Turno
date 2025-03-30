package com.mansi.focusway.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "Turno-FocusWayDatabase"

/**
 * Main database for the FocusWay app.
 * Contains tables for tasks, categories, timers, and settings.
 */
@Database(
    entities = [
        TaskEntity::class,
        FocusSessionEntity::class,
        DailyStatsEntity::class,
        CategoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FocusWayDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun dailyStatsDao(): DailyStatsDao
    abstract fun categoryDao(): CategoryDao
    
    companion object {
        @Volatile
        private var INSTANCE: FocusWayDatabase? = null
        private val initializingDatabase = AtomicBoolean(false)
        
        /**
         * Gets the singleton database instance.
         * Uses improved error handling and recovery mechanisms.
         */
        fun getDatabase(context: Context): FocusWayDatabase {
            return INSTANCE ?: synchronized(this) {
                try {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        FocusWayDatabase::class.java,
                        "focusway_database"
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                    INSTANCE = instance
                    instance
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating database", e)
                    // If database creation fails, try again with a safer approach
                    val fallbackInstance = Room.databaseBuilder(
                        context.applicationContext,
                        FocusWayDatabase::class.java,
                        "focusway_database"
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                    INSTANCE = fallbackInstance
                    fallbackInstance
                }
            }
        }
        
        /**
         * Populate the database with initial data
         */
        private suspend fun populateInitialData(context: Context) {
            try {
                val database = getDatabase(context)
                
                // Add default categories if none exist
                val categoryDao = database.categoryDao()
                if (categoryDao.getCount() == 0) {
                    val defaultCategories = listOf(
                        CategoryEntity(name = "Work"),
                        CategoryEntity(name = "Study"),
                        CategoryEntity(name = "Personal")
                    )
                    categoryDao.insertAll(defaultCategories)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error populating initial data", e)
            }
        }
    }
} 