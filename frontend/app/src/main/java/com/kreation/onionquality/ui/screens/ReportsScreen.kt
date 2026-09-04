package com.kreation.onionquality.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kreation.onionquality.theme.PrimaryBackground
import com.kreation.onionquality.ui.components.ReportCard

@Composable
fun ReportsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Quality Reports", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(5) { index ->
                ReportCard(
                    batchId = "ON-2026-000${24 - index}",
                    date = "${31 - index} Aug 2026",
                    grade = if (index % 3 == 0) "B" else "A",
                    onView = { /* TODO */ },
                    onShare = { /* TODO */ },
                    onDownload = { /* TODO */ }
                )
            }
        }
    }
}
