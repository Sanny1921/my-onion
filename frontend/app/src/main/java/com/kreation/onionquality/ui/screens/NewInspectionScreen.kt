package com.kreation.onionquality.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kreation.onionquality.theme.*
import com.kreation.onionquality.ui.components.CustomTextField
import com.kreation.onionquality.ui.components.PrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewInspectionScreen(
    onBack: () -> Unit,
    onCaptureImage: () -> Unit,
    onStartAnalysis: () -> Unit
) {
    var batchId by remember { mutableStateOf("ON-2026-") }
    var vendorId by remember { mutableStateOf("") }
    var variety by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Inspection") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBackground)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PrimaryBackground)
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Inspection Details", style = MaterialTheme.typography.titleMedium, color = DarkPlum)
                    Spacer(modifier = Modifier.height(16.dp))
                    CustomTextField(value = batchId, onValueChange = { batchId = it }, label = "Batch ID")
                    Spacer(modifier = Modifier.height(16.dp))
                    CustomTextField(value = vendorId, onValueChange = { vendorId = it }, label = "Farmer / Vendor ID")
                    Spacer(modifier = Modifier.height(16.dp))
                    CustomTextField(value = variety, onValueChange = { variety = it }, label = "Onion Variety")
                    Spacer(modifier = Modifier.height(16.dp))
                    CustomTextField(value = quantity, onValueChange = { quantity = it }, label = "Sample Quantity (kg)")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("CAPTURE ONION SAMPLE", style = MaterialTheme.typography.labelLarge, color = SecondaryText)
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SoftPink.copy(alpha = 0.5f))
                    .clickable { onCaptureImage() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.PhotoCamera,
                            contentDescription = "Camera",
                            modifier = Modifier.size(32.dp),
                            tint = PrimaryMagenta
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Tap to capture image", color = PrimaryMagenta, style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Ensure good lighting and place onions on a clean surface.",
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = "Start AI Analysis",
                onClick = onStartAnalysis
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
