package com.mansi.focusway

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShowChart
import androidx.core.graphics.drawable.DrawableCompat
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.mansi.focusway.ui.auth.AuthViewModel
import com.mansi.focusway.ui.auth.LoginScreen
import com.mansi.focusway.ui.auth.RegisterScreen
import com.mansi.focusway.ui.flip.FlipTimerScreen
import com.mansi.focusway.ui.navigation.Routes
import com.mansi.focusway.ui.settings.SettingsViewModel
import com.mansi.focusway.ui.settings.SettingsScreen
import com.mansi.focusway.ui.study.StudyModeSelectionScreen
import com.mansi.focusway.core.ui.TurnoTheme
import com.mansi.focusway.core.ui.NeonCyan
import com.mansi.focusway.ui.timer.TimerScreen
import com.mansi.focusway.ui.todo.AddTaskScreen
import com.mansi.focusway.ui.todo.TodoScreen
import com.mansi.focusway.ui.stats.StatsScreen
import com.mansi.focusway.ui.todo.EditTaskScreen
import com.mansi.focusway.ui.collaborative.GroupStudyScreen
import com.mansi.focusway.di.AppModule
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "Turno-MainActivity"
    }
    
    private var errorLogFile: File? = null
    // Track if we've shown an error message recently to avoid bombarding the user
    private val hasShownErrorRecently = AtomicBoolean(false)
    // List of common errors that don't require app restart
    private val nonFatalExceptions = listOf(
        "MediaPlayer", "Vibrator", "Sound", "NetworkOnMainThread", 
        "FrameHandler", "ResourceType", "NotFound", "Timeout",
        "animation", "Choreographer", "Lifecycle", "ViewRoot", 
        "Layout", "Canvas", "IllegalState", "IndexOutOfBounds"
    )
    
    private fun logError(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
        
        try {
            if (errorLogFile == null) {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                errorLogFile = File(cacheDir, "turno_error_log_$timestamp.txt")
            }
            
            errorLogFile?.let { file ->
                FileOutputStream(file, true).use { output ->
                    val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
                    val log = "$timestamp: $message\n"
                    output.write(log.toByteArray())
                    
                    throwable?.let {
                        output.write("${it.javaClass.name}: ${it.message}\n".toByteArray())
                        it.stackTrace.forEach { element ->
                            output.write("    at $element\n".toByteArray())
                        }
                    }
                    output.write("\n".toByteArray())
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write to error log", e)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set global exception handler
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            logError("Uncaught exception on thread ${thread.name}", exception)
            
            val isNonFatal = nonFatalExceptions.any { exceptionName ->
                exception.javaClass.name.contains(exceptionName, ignoreCase = true) ||
                (exception.message?.contains(exceptionName, ignoreCase = true) == true)
            }
            
            if (!isNonFatal && !hasShownErrorRecently.getAndSet(true)) {
                runOnUiThread {
                    Toast.makeText(this, "Koi error aa gaya hai. App ko restart karo.", Toast.LENGTH_LONG).show()
                    
                    // Reset the flag after some time
                    android.os.Handler(mainLooper).postDelayed({
                        hasShownErrorRecently.set(false)
                    }, 10000) // Don't show another error for 10 seconds
                }
            }
        }
        
        // Initialize database early to improve startup performance
        try {
            AppModule.provideDatabase(applicationContext)
        } catch (e: Exception) {
            logError("Database initialization failed", e)
            Toast.makeText(this, "Database initialization issue. Please restart the app.", Toast.LENGTH_LONG).show()
        }
        
        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val coroutineScope = rememberCoroutineScope()
            
            // Get dark mode setting
            val darkModeEnabled = try {
                SettingsViewModel.isDarkModeEnabled(this)
            } catch (e: Exception) {
                logError("Failed to get dark mode setting", e)
                false // Default to light mode if there's an error
            }
            
            TurnoTheme(darkTheme = darkModeEnabled) {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainApp(
                            onError = { message, exception ->
                                logError(message, exception)
                                
                                // Only show non-duplicate errors as snackbars
                                if (!hasShownErrorRecently.getAndSet(true)) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Error: $message")
                                        // Reset the flag after the snackbar is dismissed
                                        hasShownErrorRecently.set(false)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(onError: (String, Exception?) -> Unit = { _, _ -> }) {
    val navController = rememberNavController()
    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Timer, contentDescription = "Timer") },
                    label = { Text("Timer") },
                    selected = currentRoute == Routes.TIMER,
                    onClick = { 
                        try {
                            navController.navigate(Routes.TIMER) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        } catch (e: Exception) {
                            onError("Failed to navigate to Timer", e as? Exception)
                        }
                    }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Filled.List, contentDescription = "Tasks") },
                    label = { Text("Tasks") },
                    selected = currentRoute == Routes.TODO,
                    onClick = { 
                        try {
                            navController.navigate(Routes.TODO) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        } catch (e: Exception) {
                            onError("Failed to navigate to Tasks", e as? Exception)
                        }
                    }
                )
                
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.ShowChart, contentDescription = "Stats") },
                    label = { Text("Stats") },
                    selected = currentRoute == Routes.STATS,
                    onClick = { 
                        try {
                            navController.navigate(Routes.STATS) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        } catch (e: Exception) {
                            onError("Failed to navigate to Stats", e as? Exception)
                        }
                    }
                )
                
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Group, contentDescription = "Groups") },
                    label = { Text("Groups") },
                    selected = currentRoute == Routes.GROUP_STUDY,
                    onClick = { 
                        try {
                            navController.navigate(Routes.GROUP_STUDY) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        } catch (e: Exception) {
                            onError("Failed to navigate to Group Study", e as? Exception)
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.TIMER,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Authentication routes
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.TIMER) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Routes.REGISTER)
                    }
                )
            }
            
            composable(Routes.REGISTER) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Routes.TIMER) {
                            popUpTo(Routes.REGISTER) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Routes.LOGIN)
                    }
                )
            }
            
            // Main routes
            composable(Routes.TIMER) {
                TimerScreen(
                    onNavigateToTodo = { navController.navigate(Routes.TODO) },
                    onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
                )
            }
            
            composable(Routes.TODO) {
                TodoScreen(
                    onNavigateToTimer = { navController.navigate(Routes.TIMER) },
                    onAddTask = {
                        navController.navigate(Routes.ADD_TASK)
                    },
                    onEditTask = { taskId ->
                        navController.navigate(Routes.buildEditTaskRoute(taskId))
                    }
                )
            }
            
            composable(Routes.ADD_TASK) {
                AddTaskScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(
                Routes.EDIT_TASK,
                arguments = listOf(
                    navArgument("taskId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getInt("taskId") ?: -1
                EditTaskScreen(
                    taskId = taskId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Routes.STATS) {
                StatsScreen()
            }
            
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onLogOut = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
            
            composable(Routes.GROUP_STUDY) {
                GroupStudyScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}