package com.kreation.onionquality.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kreation.onionquality.theme.*

@Composable
fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(value, style = MaterialTheme.typography.displaySmall, color = PrimaryMagenta)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, color = SecondaryText)
        }
    }
}

@Composable
fun InspectionCard(
    batchId: String,
    date: String,
    farmer: String,
    gradeA: Int,
    urs: Int,
    status: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(batchId, style = MaterialTheme.typography.titleMedium, color = DarkPlum)
                StatusBadge(status = status)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Person, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(farmer, style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Outlined.Event, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(date, style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = BorderColor)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Grade A", style = MaterialTheme.typography.labelMedium, color = SecondaryText)
                    Text("$gradeA%", style = MaterialTheme.typography.titleMedium, color = SemanticGood, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("URS", style = MaterialTheme.typography.labelMedium, color = SecondaryText)
                    Text("$urs%", style = MaterialTheme.typography.titleMedium, color = SemanticUrs, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DefectCard(
    category: String,
    count: Int,
    percentage: Int,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Warning, contentDescription = category, tint = color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(category, style = MaterialTheme.typography.titleMedium, color = DarkPlum)
                Text("$count onions detected", style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
            }
            Text("$percentage%", style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ReportCard(
    batchId: String,
    date: String,
    grade: String,
    onView: () -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(batchId, style = MaterialTheme.typography.titleMedium, color = DarkPlum)
                    Text(date, style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(2.dp, SemanticGood, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(grade, style = MaterialTheme.typography.titleLarge, color = SemanticGood, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = BorderColor)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = onView) {
                    Icon(Icons.Outlined.Visibility, contentDescription = "View", tint = PrimaryMagenta, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View", color = PrimaryMagenta)
                }
                TextButton(onClick = onShare) {
                    Icon(Icons.Outlined.Share, contentDescription = "Share", tint = SecondaryText, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", color = SecondaryText)
                }
                TextButton(onClick = onDownload) {
                    Icon(Icons.Outlined.Download, contentDescription = "Download", tint = SecondaryText, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Download", color = SecondaryText)
                }
            }
        }
    }
}
