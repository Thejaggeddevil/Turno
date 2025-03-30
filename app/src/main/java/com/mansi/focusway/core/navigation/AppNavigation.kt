package com.mansi.focusway.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mansi.focusway.features.groups.ui.GroupsScreen
import com.mansi.focusway.features.stats.ui.StatsScreen
import com.mansi.focusway.features.tasks.ui.TasksScreen
import com.mansi.focusway.features.timer.ui.TimerScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.TIMER,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Routes.TIMER) {
                TimerScreen(
                    onNavigateToTasks = { navController.navigate(Routes.TASKS) },
                    onNavigateToStats = { navController.navigate(Routes.STATS) },
                    onNavigateToGroups = { navController.navigate(Routes.GROUPS) }
                )
            }
            
            composable(Routes.TASKS) {
                TasksScreen(
                    onNavigateToTimer = { navController.navigate(Routes.TIMER) },
                    onNavigateToStats = { navController.navigate(Routes.STATS) },
                    onNavigateToGroups = { navController.navigate(Routes.GROUPS) }
                )
            }
            
            composable(Routes.STATS) {
                StatsScreen(
                    onNavigateToTimer = { navController.navigate(Routes.TIMER) },
                    onNavigateToTasks = { navController.navigate(Routes.TASKS) },
                    onNavigateToGroups = { navController.navigate(Routes.GROUPS) }
                )
            }
            
            composable(Routes.GROUPS) {
                GroupsScreen(
                    onNavigateToTimer = { navController.navigate(Routes.TIMER) },
                    onNavigateToTasks = { navController.navigate(Routes.TASKS) },
                    onNavigateToStats = { navController.navigate(Routes.STATS) }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Timer, contentDescription = "Timer") },
            label = { Text("Timer") },
            selected = currentRoute == Routes.TIMER,
            onClick = {
                navController.navigate(Routes.TIMER) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        
        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = "Tasks") },
            label = { Text("Tasks") },
            selected = currentRoute == Routes.TASKS,
            onClick = {
                navController.navigate(Routes.TASKS) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        
        NavigationBarItem(
            icon = { Icon(Icons.Default.ShowChart, contentDescription = "Stats") },
            label = { Text("Stats") },
            selected = currentRoute == Routes.STATS,
            onClick = {
                navController.navigate(Routes.STATS) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        
        NavigationBarItem(
            icon = { Icon(Icons.Default.Group, contentDescription = "Groups") },
            label = { Text("Groups") },
            selected = currentRoute == Routes.GROUPS,
            onClick = {
                navController.navigate(Routes.GROUPS) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
} 