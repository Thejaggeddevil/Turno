package com.mansi.focusway.features.tasks.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun TasksScreen(
    onNavigateToTimer: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    onNavigateToGroups: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Tasks Screen")
    }
} 