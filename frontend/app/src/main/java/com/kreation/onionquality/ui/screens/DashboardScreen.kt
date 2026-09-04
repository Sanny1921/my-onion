package com.kreation.onionquality.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kreation.onionquality.theme.*
import com.kreation.onionquality.ui.components.InspectionCard
import com.kreation.onionquality.ui.components.MetricCard
import com.kreation.onionquality.ui.components.SectionHeader

@Composable
fun DashboardScreen(onNewInspection: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good Morning, Officer",
                        style = MaterialTheme.typography.headlineMedium,
                        color = DarkPlum
                    )
                    Text(
                        text = "Ready for today's inspections?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText
                    )
                }
                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = DarkPlum)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            // Primary Action
            Button(
                onClick = onNewInspection,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryMagenta),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text("New Inspection", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            Text("Today's Overview", style = MaterialTheme.typography.titleLarge, color = DarkPlum)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(title = "Total Inspections", value = "24", modifier = Modifier.weight(1f))
                MetricCard(title = "Avg Grade A", value = "78.4%", modifier = Modifier.weight(1f))
                MetricCard(title = "Avg URS", value = "7.2%", modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            SectionHeader(
                title = "RECENT INSPECTIONS",
                actionText = "View All",
                onAction = { /* TODO: Navigate to history */ }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(3) { index ->
            InspectionCard(
                batchId = "ON-2026-000${24 - index}",
                date = "31 Aug 2026",
                farmer = listOf("Ramesh Yadav", "Suresh Kumar", "Amit Patel")[index],
                gradeA = listOf(78, 65, 92)[index],
                urs = listOf(7, 12, 3)[index],
                status = listOf("PASS", "REVIEW", "PASS")[index],
                onClick = { /* Navigate */ }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        item {
            Spacer(modifier = Modifier.height(100.dp)) // Padding for bottom nav
        }
    }
}
