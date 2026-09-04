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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kreation.onionquality.theme.*
import com.kreation.onionquality.ui.components.PrimaryButton
import com.kreation.onionquality.ui.components.SecondaryButton
import com.kreation.onionquality.ui.viewmodel.InspectionUiState
import com.kreation.onionquality.ui.viewmodel.InspectionViewModel

@Composable
fun AIAnalysisScreen(
    viewModel: InspectionViewModel,
    onAnalysisComplete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()

    var progress by remember { mutableFloatStateOf(0.1f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500, easing = LinearEasing),
        label = "progress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "scanning")
    val scanPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan"
    )

    // Trigger API call on launch
    LaunchedEffect(Unit) {
        viewModel.startInspection(context)
    }

    // Handle state transitions
    LaunchedEffect(uiState) {
        when (uiState) {
            is InspectionUiState.Loading -> {
                progress = 0.5f
            }
            is InspectionUiState.Success -> {
                progress = 1.0f
                onAnalysisComplete()
            }
            is InspectionUiState.Error -> {
                progress = 0.0f
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Icon(Icons.Outlined.DocumentScanner, contentDescription = null, modifier = Modifier.size(48.dp), tint = PrimaryMagenta)
        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState) {
            is InspectionUiState.Error -> {
                Text("Inspection Error", style = MaterialTheme.typography.headlineMedium, color = DarkPlum)
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SemanticRotten.copy(alpha = 0.1f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SemanticRotten)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = SemanticRotten, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = DarkPlum,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                PrimaryButton(text = "Retry Inspection", onClick = { viewModel.startInspection(context) })
                Spacer(modifier = Modifier.height(12.dp))
                SecondaryButton(text = "Back to New Inspection", onClick = onBack)
                Spacer(modifier = Modifier.height(16.dp))
            }
            else -> {
                Text("Analyzing sample with Gemini...", style = MaterialTheme.typography.headlineMedium, color = DarkPlum)
                Spacer(modifier = Modifier.height(24.dp))

                // Image with scanning line overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkPlum)
                ) {
                    selectedImageUri?.let { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "Sample Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Scanning line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .offset(y = (scanPosition * 260).dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, PrimaryMagenta.copy(alpha = 0.5f), PrimaryMagenta)
                                )
                            )
                    )
                    Box(modifier = Modifier.fillMaxSize().border(2.dp, PrimaryMagenta.copy(alpha = 0.3f), RoundedCornerShape(16.dp)))
                }

                Spacer(modifier = Modifier.height(32.dp))

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
                Text(
                    text = if (uiState is InspectionUiState.Loading) (uiState as InspectionUiState.Loading).message else "Processing AI models...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.weight(1f))
                Text("Real Gemini 3.5 Vision API executing defect analysis...", color = PrimaryMagenta, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
