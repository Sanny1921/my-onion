package com.kreation.onionquality.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kreation.onionquality.theme.*
import kotlinx.coroutines.delay

@Composable
fun AIAnalysisScreen(onAnalysisComplete: () -> Unit) {
    var progress by remember { mutableFloatStateOf(0f) }
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500, easing = LinearEasing), label = "progress"
    )

    // Scanning animation
    val infiniteTransition = rememberInfiniteTransition(label = "scanning")
    val scanPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scan"
    )

    LaunchedEffect(Unit) {
        while (progress < 1f) {
            delay(500)
            progress += 0.15f
        }
        progress = 1f
        delay(800)
        onAnalysisComplete()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Icon(Icons.Outlined.DocumentScanner, contentDescription = null, modifier = Modifier.size(48.dp), tint = PrimaryMagenta)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Analyzing onions...", style = MaterialTheme.typography.headlineMedium, color = DarkPlum)
        Spacer(modifier = Modifier.height(32.dp))
        
        // Image with scanning line overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkPlum)
        ) {
            // Simulated scanning line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .offset(y = (scanPosition * 280).dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, PrimaryMagenta.copy(alpha = 0.5f), PrimaryMagenta)
                        )
                    )
            )
            // Simulated border glow
            Box(modifier = Modifier.fillMaxSize().border(2.dp, PrimaryMagenta.copy(alpha = 0.3f), RoundedCornerShape(16.dp)))
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = PrimaryMagenta,
            trackColor = SoftPink
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("${(animatedProgress * 100).toInt()}% • 78 / 100 detected", style = MaterialTheme.typography.bodyMedium, color = SecondaryText, fontWeight = FontWeight.Medium)
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Text("DETECTION SUMMARY", style = MaterialTheme.typography.labelLarge, color = SecondaryText, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SummaryItem(color = SemanticGood, label = "Good", count = "52")
                    SummaryItem(color = SemanticSprouted, label = "Sprouted", count = "12")
                    SummaryItem(color = SemanticDamaged, label = "Damaged", count = "8")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SummaryItem(color = SemanticRotten, label = "Rotten", count = "6")
                    SummaryItem(color = SemanticUrs, label = "URS", count = "0")
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Text("AI model analyzing spatial and visual defects...", color = PrimaryMagenta, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun RowScope.SummaryItem(color: Color, label: String, count: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
    ) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(count, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DarkPlum)
            Text(label, style = MaterialTheme.typography.labelMedium, color = SecondaryText)
        }
    }
}
