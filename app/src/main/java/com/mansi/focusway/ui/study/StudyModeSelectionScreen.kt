package com.mansi.focusway.ui.study

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mansi.focusway.ui.theme.FocusWayTheme
import com.mansi.focusway.ui.theme.NeonBlue
import com.mansi.focusway.ui.theme.NeonCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyModeSelectionScreen(
    onNavigateBack: () -> Unit = {},
    onSelectFlipTimer: () -> Unit = {},
    onSelectCollaborativeStudy: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Study Modes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Select a Study Mode",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // Flip Timer Mode Card
            StudyModeCard(
                title = "Flip Timer Mode",
                description = "Flip your phone to start and stop the timer. Helps maintain focus by creating a physical barrier to checking your phone.",
                icon = Icons.Default.FlipCameraAndroid,
                onClick = onSelectFlipTimer,
                color = NeonCyan
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Collaborative Study Mode Card
            StudyModeCard(
                title = "Collaborative Study",
                description = "Study with friends or classmates. Set shared goals and track progress together.",
                icon = Icons.Default.Groups,
                onClick = onSelectCollaborativeStudy,
                color = NeonBlue
            )
        }
    }
}

@Composable
private fun StudyModeCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, color)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon section
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(40.dp),
                    tint = color
                )
            }
            
            // Text section
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StudyModeSelectionScreenPreview() {
    FocusWayTheme(darkTheme = true) {
        StudyModeSelectionScreen()
    }
}

@Preview(showBackground = true)
@Composable
private fun StudyModeCardPreview() {
    FocusWayTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            StudyModeCard(
                title = "Flip Timer Mode",
                description = "Flip your phone to start and stop the timer. Helps maintain focus by creating a physical barrier to checking your phone.",
                icon = Icons.Default.FlipCameraAndroid,
                onClick = {},
                color = NeonCyan
            )
        }
    }
} 