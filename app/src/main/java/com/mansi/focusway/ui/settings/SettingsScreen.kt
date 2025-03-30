package com.mansi.focusway.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.tooling.preview.Preview
import com.mansi.focusway.ui.theme.FocusWayTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogOut: () -> Unit,
    viewModel: SettingsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    // Dialogs state
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showAppInfoDialog by remember { mutableStateOf(false) }
    
    // Initialize viewModel
    LaunchedEffect(Unit) {
        viewModel.initialize(context)
    }
    
    // App info
    val appVersion = "1.0.0"
    val buildVersion = "1"
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        // Top app bar
        TopAppBar(
            title = { Text("Settings") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF1E1E1E),
                titleContentColor = Color.White
            )
        )
        
        // Settings content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile / Manage My Info
            SettingsCategory(title = "Account")
            
            SettingsItem(
                icon = Icons.Default.Person,
                title = "Manage My Info",
                subtitle = "View and edit your profile information",
                onClick = { /* Will be implemented in a future update */ }
            )
            
            SettingsItem(
                icon = Icons.Default.Logout,
                title = "Log Out",
                subtitle = "Sign out from your account",
                onClick = onLogOut
            )
            
            HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
            
            // Social
            SettingsCategory(title = "Social")
            
            SettingsItem(
                icon = Icons.Default.PersonAdd,
                title = "Invite Friends",
                subtitle = "Share this app with friends",
                onClick = { shareApp(context) }
            )
            
            HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
            
            // Preferences
            SettingsCategory(title = "Preferences")
            
            // Notifications
            SettingsToggleItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = "Enable or disable notifications",
                isChecked = uiState.notificationsEnabled,
                onCheckedChange = { enabled -> viewModel.toggleNotifications(context, enabled) }
            )
            
            // Appearance
            SettingsToggleItem(
                icon = Icons.Default.DarkMode,
                title = "Dark Mode",
                subtitle = "Toggle between light and dark theme",
                isChecked = uiState.darkMode,
                onCheckedChange = { enabled -> viewModel.toggleDarkMode(context, enabled) }
            )
            
            // Vibration
            SettingsToggleItem(
                icon = Icons.Default.Vibration,
                title = "Vibration",
                subtitle = "Toggle vibration when flipping",
                isChecked = uiState.vibrationEnabled,
                onCheckedChange = { enabled -> viewModel.toggleVibration(context, enabled) }
            )
            
            HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
            
            // App Info
            SettingsCategory(title = "About")
            
            SettingsItem(
                icon = Icons.Default.Info,
                title = "App Info",
                subtitle = "Version $appVersion (Build $buildVersion)",
                onClick = { showAppInfoDialog = true }
            )
            
            SettingsItem(
                icon = Icons.Default.PrivacyTip,
                title = "Privacy Policy",
                subtitle = "Read our privacy policy",
                onClick = { showPrivacyPolicyDialog = true }
            )
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
    
    // App Info Dialog
    if (showAppInfoDialog) {
        AlertDialog(
            onDismissRequest = { showAppInfoDialog = false },
            title = { Text("App Info") },
            text = {
                Column {
                    Text("FocusWay")
                    Text("Version: $appVersion")
                    Text("Build: $buildVersion")
                    Text("© 2023 FocusWay Team")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("A productivity app designed to help you focus and manage your time effectively.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showAppInfoDialog = false }) {
                    Text("OK")
                }
            },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }
    
    // Privacy Policy Dialog
    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = { Text("Privacy Policy") },
            text = {
                Column {
                    Text("FocusWay Privacy Policy")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("We value your privacy and are committed to protecting your personal data. This Privacy Policy explains how we collect, use, and safeguard your information when you use our application.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Data Collection: We collect minimal data necessary for the app's functionality. This includes your tasks, timers, and settings preferences, which are stored locally on your device.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Would you like to read the full privacy policy online?")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPrivacyPolicyDialog = false
                        // Open a browser with privacy policy URL
                        openPrivacyPolicyUrl(context)
                    }
                ) {
                    Text("Read Full Policy")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrivacyPolicyDialog = false }) {
                    Text("Close")
                }
            },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }
}

// Function to share the app
private fun shareApp(context: Context) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Check out FocusWay")
        putExtra(Intent.EXTRA_TEXT, 
            "Hey! I've been using FocusWay to boost my productivity. " +
            "It helps me stay focused and manage my time effectively. " +
            "Check it out: https://play.google.com/store/apps/details?id=com.mansi.focusway")
    }
    context.startActivity(Intent.createChooser(intent, "Share via"))
}

// Function to open privacy policy URL
private fun openPrivacyPolicyUrl(context: Context) {
    val url = "https://www.focusway.com/privacy-policy"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

@Composable
fun SettingsCategory(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF03DAC5), // Neon cyan color
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = Color.White
            )
            
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun SettingsToggleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = Color.White
            )
            
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF03DAC5),
                checkedTrackColor = Color(0xFF03DAC5).copy(alpha = 0.5f),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.DarkGray
            )
        )
    }
}

// Add these preview functions at the end of the file
@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun SettingsScreenPreview() {
    FocusWayTheme(darkTheme = true) {
        SettingsScreen(
            onNavigateBack = {},
            onLogOut = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun SettingsCategoryPreview() {
    FocusWayTheme(darkTheme = true) {
        Column(Modifier.background(Color(0xFF121212))) {
            SettingsCategory(title = "Account")
            SettingsItem(
                icon = Icons.Default.Person,
                title = "Manage My Info",
                subtitle = "View and edit your profile information",
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun SettingsItemPreview() {
    FocusWayTheme(darkTheme = true) {
        Column(Modifier.background(Color(0xFF121212))) {
            SettingsItem(
                icon = Icons.Default.Info,
                title = "App Info",
                subtitle = "Version 1.0.0 (Build 1)",
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun SettingsToggleItemPreview() {
    FocusWayTheme(darkTheme = true) {
        Column(Modifier.background(Color(0xFF121212))) {
            SettingsToggleItem(
                icon = Icons.Default.DarkMode,
                title = "Dark Mode",
                subtitle = "Toggle between light and dark theme",
                isChecked = true,
                onCheckedChange = {}
            )
        }
    }
} 