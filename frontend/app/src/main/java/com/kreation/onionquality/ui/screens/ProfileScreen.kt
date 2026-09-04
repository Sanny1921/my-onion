package com.kreation.onionquality.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kreation.onionquality.theme.*
import com.kreation.onionquality.ui.components.PrimaryButton
import com.kreation.onionquality.ui.components.SecondaryButton

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Profile", style = MaterialTheme.typography.headlineMedium, color = DarkPlum)
        Spacer(modifier = Modifier.height(24.dp))

        // Officer Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(SoftPink),
                    contentAlignment = Alignment.Center
                ) {
                    Text("O", style = MaterialTheme.typography.headlineMedium, color = PrimaryMagenta)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Priya Sharma", style = MaterialTheme.typography.titleLarge, color = DarkPlum)
                    Text("Officer ID: INSP-2026-99", style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Nashik Procurement Center", style = MaterialTheme.typography.labelMedium, color = PrimaryMagenta)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("Settings", style = MaterialTheme.typography.titleMedium, color = DarkPlum)
        Spacer(modifier = Modifier.height(16.dp))

        SettingsItem(icon = Icons.Outlined.Notifications, title = "Notifications")
        SettingsItem(icon = Icons.Outlined.Language, title = "Language", subtitle = "English")
        SettingsItem(icon = Icons.Outlined.Sync, title = "Data Sync", subtitle = "Last synced 10 mins ago")
        SettingsItem(icon = Icons.Outlined.Security, title = "Privacy")
        SettingsItem(icon = Icons.Outlined.HelpOutline, title = "Help & Support")

        Spacer(modifier = Modifier.weight(1f))
        
        SecondaryButton(
            text = "Logout",
            onClick = onLogout
        )
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(White),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = PrimaryMagenta, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = DarkPlum)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SecondaryText)
            }
        }
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = SecondaryText)
    }
}
