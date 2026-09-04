package com.kreation.onionquality.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kreation.onionquality.theme.DarkPlum

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onBack: () -> Unit,
    onPhotoCaptured: () -> Unit
) {
    // This is a placeholder for actual CameraX implementation
    // A real implementation would use AndroidView with a PreviewView
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Capture Sample", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Framing guides
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                        .border(2.dp, Color.White.copy(alpha = 0.5f))
                )
            }

            // Bottom controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(DarkPlum.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onBack) {
                        Text("Retake", color = Color.White)
                    }
                    
                    // Capture button
                    Button(
                        onClick = onPhotoCaptured,
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Box(modifier = Modifier.size(56.dp).background(Color.White, CircleShape).border(2.dp, Color.Black, CircleShape))
                    }
                    
                    TextButton(onClick = onPhotoCaptured) {
                        Text("Use Photo", color = Color.White)
                    }
                }
            }
        }
    }
}
