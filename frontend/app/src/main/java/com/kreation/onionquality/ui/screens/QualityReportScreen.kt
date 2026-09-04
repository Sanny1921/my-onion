package com.kreation.onionquality.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kreation.onionquality.theme.*
import com.kreation.onionquality.ui.components.PrimaryButton
import com.kreation.onionquality.ui.components.SecondaryButton
import com.kreation.onionquality.ui.components.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QualityReportScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quality Report") },
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Government Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.QrCode2, contentDescription = "Seal", modifier = Modifier.size(48.dp), tint = SecondaryText)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Government of India",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkPlum
                    )
                    Text(
                        "Ministry of Consumer Affairs,\nFood & Public Distribution",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(color = BorderColor)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "ONION QUALITY\nASSESSMENT REPORT",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryMagenta,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                    ReportRow("Report ID", "REP-2026-88901")
                    ReportRow("Batch ID", "ON-2026-00024")
                    ReportRow("Date & Time", "31 Aug 2026, 10:30 AM")
                    ReportRow("Center", "Nashik Procurement Hub")
                    ReportRow("Farmer ID", "FMR-4492")
                    ReportRow("Quantity", "5000 kg")
                    ReportRow("Variety", "Nashik Red")

                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(color = BorderColor)
                    Spacer(modifier = Modifier.height(24.dp))

                    Text("QUALITY SUMMARY", style = MaterialTheme.typography.labelLarge, color = SecondaryText, modifier = Modifier.align(Alignment.Start))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ReportRow("Grade A", "78%", SemanticGood)
                    ReportRow("URS", "7%", SemanticUrs)
                    ReportRow("Sprouted", "5%", SemanticSprouted)
                    ReportRow("Damaged", "6%", SemanticDamaged)
                    ReportRow("Rotten", "4%", SemanticRotten)

                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(color = BorderColor)
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("FINAL ASSESSMENT", style = MaterialTheme.typography.titleMedium, color = DarkPlum)
                        StatusBadge("PASS")
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("AI Detection Confidence", style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
                        Text("96%", style = MaterialTheme.typography.labelLarge, color = DarkPlum)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            PrimaryButton(text = "Download PDF", onClick = { /* TODO */ })
            Spacer(modifier = Modifier.height(16.dp))
            SecondaryButton(text = "Share Report", onClick = { /* TODO */ })
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ReportRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color = DarkPlum) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
        Text(value, style = MaterialTheme.typography.labelLarge, color = valueColor, fontWeight = FontWeight.SemiBold)
    }
}
