package com.kreation.onionquality.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kreation.onionquality.theme.PrimaryBackground
import com.kreation.onionquality.ui.components.SectionHeader
import com.kreation.onionquality.theme.*
import com.kreation.onionquality.ui.components.DefectCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedAnalysisScreen(onBack: () -> Unit) {
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
        ) {
            // Simplified for demonstration - standard tabs
            var selectedTabIndex = androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(0) }
            val tabs = listOf("Overview", "Size Dist.", "Defects")

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

            Spacer(modifier = Modifier.height(24.dp))

            when (selectedTabIndex.intValue) {
                0 -> { // Overview
                    SectionHeader("AI Confidence")
                    BreakdownRow("Detection Confidence", "96%", DarkPlum, 0.96f)
                    BreakdownRow("Classification", "94%", DarkPlum, 0.94f)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionHeader("Annotated Image")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .background(DarkPlum, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                    ) {
                        // Image Placeholder
                    }
                }
                1 -> { // Size Dist
                    SectionHeader("Size Distribution")
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            BreakdownRow("Reference Size & Above", "93%", SemanticGood, 0.93f)
                            BreakdownRow("Undersized (URS)", "7%", SemanticUrs, 0.07f)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("URS indicates onions below the configured reference size.", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                        }
                    }
                }
                2 -> { // Defects
                    SectionHeader("Detected Defects")
                    DefectCard(category = "Sprouted", count = 5, percentage = 5, color = SemanticSprouted, onClick = {})
                    Spacer(modifier = Modifier.height(12.dp))
                    DefectCard(category = "Damaged", count = 6, percentage = 6, color = SemanticDamaged, onClick = {})
                    Spacer(modifier = Modifier.height(12.dp))
                    DefectCard(category = "Rotten", count = 4, percentage = 4, color = SemanticRotten, onClick = {})
                }
            }
        }
    }
}
