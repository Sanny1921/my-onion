package com.kreation.onionquality.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kreation.onionquality.data.api.InspectionResultDto
import com.kreation.onionquality.theme.*
import com.kreation.onionquality.ui.components.DefectCard
import com.kreation.onionquality.ui.components.SectionHeader
import com.kreation.onionquality.ui.viewmodel.InspectionUiState
import com.kreation.onionquality.ui.viewmodel.InspectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedAnalysisScreen(
    viewModel: InspectionViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()
    val result: InspectionResultDto? = (uiState as? InspectionUiState.Success)?.result

    var selectedTabIndex = remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Size Dist.", "Defects")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detailed Analysis") },
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex.intValue,
                containerColor = PrimaryBackground,
                contentColor = PrimaryMagenta,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex.intValue]),
                        color = PrimaryMagenta
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex.intValue == index,
                        onClick = { selectedTabIndex.intValue = index },
                        text = { Text(title, color = if (selectedTabIndex.intValue == index) PrimaryMagenta else SecondaryText) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val aiObs = result?.aiObservation
            val metrics = result?.backendMetrics
            val defects = aiObs?.defects

            when (selectedTabIndex.intValue) {
                0 -> { // Overview
                    SectionHeader("AI Visual Signal & Inspector Status")

                    val confidencePct = ((aiObs?.aiConfidenceSignal ?: 0f) * 100).toInt()
                    BreakdownRow("AI Visual Clarity Signal", "$confidencePct%", DarkPlum, (aiObs?.aiConfidenceSignal ?: 0f))
                    BreakdownRow("Inspector Status", result?.finalAssessment?.inspectorStatus ?: "PENDING", PrimaryMagenta, 1.0f)

                    Spacer(modifier = Modifier.height(20.dp))
                    SectionHeader("Qualitative Visual Observations")

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (aiObs?.observations.isNullOrEmpty()) {
                                Text("No qualitative notes provided.", style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
                            } else {
                                aiObs?.observations?.forEach { obsNote ->
                                    Text("• $obsNote", style = MaterialTheme.typography.bodyMedium, color = DarkPlum, modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    SectionHeader("Captured Sample Photograph")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(12.dp))
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
                    }
                }
                1 -> { // Size Dist
                    SectionHeader("Physical Size Measurement Status")
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Measurement Status: ${metrics?.sizeMeasurementStatus ?: "NOT_MEASURABLE"}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DarkPlum
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Physical onion millimeter dimensions cannot be arbitrarily fabricated from a non-calibrated photograph without reference markers.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SecondaryText
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            val ursPct = metrics?.defectBreakdownPercentages?.get("undersized") ?: 0f
                            BreakdownRow("Visually Undersized (URS)", "${String.format("%.1f", ursPct)}%", SemanticUrs, ursPct / 100f)
                        }
                    }
                }
                2 -> { // Defects
                    SectionHeader("Categorized Visible Defects")
                    val totalAnalyzable = (metrics?.analyzableCount ?: 1).coerceAtLeast(1)

                    val damaged = defects?.damaged ?: 0
                    val rotten = defects?.rotten ?: 0
                    val sprouted = defects?.sprouted ?: 0
                    val undersized = defects?.undersized ?: 0
                    val diseased = defects?.diseased ?: 0
                    val other = defects?.other ?: 0

                    DefectCard(category = "Sprouted", count = sprouted, percentage = (sprouted * 100 / totalAnalyzable), color = SemanticSprouted, onClick = {})
                    Spacer(modifier = Modifier.height(10.dp))
                    DefectCard(category = "Damaged / Cut", count = damaged, percentage = (damaged * 100 / totalAnalyzable), color = SemanticDamaged, onClick = {})
                    Spacer(modifier = Modifier.height(10.dp))
                    DefectCard(category = "Rotten / Moldy", count = rotten, percentage = (rotten * 100 / totalAnalyzable), color = SemanticRotten, onClick = {})
                    Spacer(modifier = Modifier.height(10.dp))
                    DefectCard(category = "Diseased / Smut", count = diseased, percentage = (diseased * 100 / totalAnalyzable), color = PrimaryMagenta, onClick = {})
                    Spacer(modifier = Modifier.height(10.dp))
                    DefectCard(category = "Undersized (URS)", count = undersized, percentage = (undersized * 100 / totalAnalyzable), color = SemanticUrs, onClick = {})
                    Spacer(modifier = Modifier.height(10.dp))
                    DefectCard(category = "Other Defects", count = other, percentage = (other * 100 / totalAnalyzable), color = SecondaryText, onClick = {})
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
