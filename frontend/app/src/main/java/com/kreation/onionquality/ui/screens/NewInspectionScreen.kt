package com.kreation.onionquality.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kreation.onionquality.theme.*
import com.kreation.onionquality.ui.components.CustomTextField
import com.kreation.onionquality.ui.components.PrimaryButton
import com.kreation.onionquality.ui.viewmodel.InspectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewInspectionScreen(
    viewModel: InspectionViewModel,
    onBack: () -> Unit,
    onCaptureImage: () -> Unit,
    onStartAnalysis: () -> Unit
) {
    val context = LocalContext.current

    val batchId by viewModel.batchId.collectAsState()
    val vendorId by viewModel.vendorId.collectAsState()
    val variety by viewModel.variety.collectAsState()
    val sampleCount by viewModel.sampleCount.collectAsState()
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Gallery Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setImageUri(uri)
            errorMessage = null
        }
    }

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
                    CustomTextField(
                        value = batchId,
                        onValueChange = { viewModel.setBatchId(it) },
                        label = "Batch / Lot ID"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CustomTextField(
                        value = vendorId,
                        onValueChange = { viewModel.setVendorId(it) },
                        label = "Farmer / Vendor ID"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CustomTextField(
                        value = variety,
                        onValueChange = { viewModel.setVariety(it) },
                        label = "Onion Variety"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CustomTextField(
                        value = sampleCount,
                        onValueChange = { viewModel.setSampleCount(it) },
                        label = "Declared Sample Count (e.g. 50)"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("CAPTURE ONION SAMPLE", style = MaterialTheme.typography.labelLarge, color = SecondaryText)
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SoftPink.copy(alpha = 0.5f))
                    .clickable { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected Sample Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
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
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Tap to pick image from gallery / camera", color = PrimaryMagenta, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { galleryLauncher.launch("image/*") }) {
                    Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp), tint = PrimaryMagenta)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Select from Gallery", color = PrimaryMagenta)
                }
                TextButton(onClick = onCaptureImage) {
                    Icon(Icons.Outlined.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp), tint = PrimaryMagenta)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Use Camera", color = PrimaryMagenta)
                }
            }

            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = "Start AI Analysis",
                onClick = {
                    if (selectedImageUri == null) {
                        errorMessage = "Please capture or select an onion sample image before proceeding."
                    } else {
                        errorMessage = null
                        onStartAnalysis()
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
