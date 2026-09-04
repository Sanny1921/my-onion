package com.kreation.onionquality.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kreation.onionquality.theme.*
import com.kreation.onionquality.ui.components.PrimaryButton
import com.kreation.onionquality.ui.components.SecondaryButton
import com.kreation.onionquality.ui.components.SectionHeader

@Composable
fun QualityResultsScreen(
    onViewDetailed: () -> Unit,
    onGenerateReport: () -> Unit,
    onBackToDashboard: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text("Quality Results", style = MaterialTheme.typography.headlineMedium, color = DarkPlum)
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SemanticGood.copy(alpha = 0.1f)),
            border = androidx.compose.foundation.BorderStroke(2.dp, SemanticGood.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("OVERALL GRADE", style = MaterialTheme.typography.labelLarge, color = SemanticGood.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(8.dp))
                Text("A", fontSize = 72.sp, fontWeight = FontWeight.Bold, color = SemanticGood)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Good Quality", style = MaterialTheme.typography.titleLarge, color = SemanticGood)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        SectionHeader("QUALITY BREAKDOWN")
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                BreakdownRow("Grade A", "78%", SemanticGood, 0.78f)
                BreakdownRow("URS", "7%", SemanticUrs, 0.07f)
                BreakdownRow("Sprouted", "5%", SemanticSprouted, 0.05f)
                BreakdownRow("Damaged", "6%", SemanticDamaged, 0.06f)
                BreakdownRow("Rotten", "4%", SemanticRotten, 0.04f)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("TOTAL ONIONS", style = MaterialTheme.typography.labelMedium, color = SecondaryText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("100", style = MaterialTheme.typography.displaySmall, color = DarkPlum)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("DEFECTIVE", style = MaterialTheme.typography.labelMedium, color = SecondaryText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("22", style = MaterialTheme.typography.displaySmall, color = SemanticRotten)
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        PrimaryButton(text = "View Detailed Analysis", onClick = onViewDetailed)
        Spacer(modifier = Modifier.height(16.dp))
        SecondaryButton(text = "Generate Report", onClick = onGenerateReport)
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onBackToDashboard, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Dashboard", color = SecondaryText, style = MaterialTheme.typography.labelLarge)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun BreakdownRow(label: String, value: String, color: androidx.compose.ui.graphics.Color, progress: Float) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}
