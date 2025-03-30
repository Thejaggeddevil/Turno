package com.mansi.focusway.ui.navigation

object Routes {
    // Auth routes
    const val LOGIN = "login"
    const val REGISTER = "register"
    
    // Main routes
    const val TIMER = "timer"
    const val TODO = "todo"
    const val ADD_TASK = "add_task"
    const val EDIT_TASK = "edit_task/{taskId}"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val GROUP_STUDY = "group_study"
    
    // Route builders
    fun buildEditTaskRoute(taskId: Int): String = "edit_task/$taskId"
} 