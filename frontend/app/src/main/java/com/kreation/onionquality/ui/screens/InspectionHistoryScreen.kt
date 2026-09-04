package com.kreation.onionquality.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kreation.onionquality.data.model.Inspection
import com.kreation.onionquality.theme.PrimaryBackground
import com.kreation.onionquality.ui.components.CustomFilterChip
import com.kreation.onionquality.ui.components.CustomTextField
import com.kreation.onionquality.ui.components.InspectionCard

@Composable
fun InspectionHistoryScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Pass", "Review", "Rejected")

    // Mock data
    val inspections = listOf(
        Inspection("ON-2026-00024", "Ramesh Yadav", "31 Aug 2026", 100, 78, 7, 5, 6, 4, "PASS"),
        Inspection("ON-2026-00025", "Suresh Kumar", "31 Aug 2026", 120, 65, 12, 10, 8, 5, "REVIEW"),
        Inspection("ON-2026-00026", "Amit Patel", "30 Aug 2026", 90, 40, 25, 15, 12, 8, "REJECTED"),
        Inspection("ON-2026-00027", "Vikas Singh", "30 Aug 2026", 110, 82, 5, 4, 6, 3, "PASS")
    )

    val filteredInspections = inspections.filter {
        (selectedFilter == "All" || it.status.equals(selectedFilter, ignoreCase = true)) &&
        it.batchId.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Inspection History", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = "Search batch ID...",
            leadingIcon = Icons.Filled.Search
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { filter ->
                CustomFilterChip(
                    selected = selectedFilter == filter,
                    label = filter,
                    onClick = { selectedFilter = filter }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(filteredInspections) { inspection ->
                InspectionCard(
                    batchId = inspection.batchId,
                    date = inspection.date,
                    farmer = inspection.farmerName,
                    gradeA = inspection.gradeA,
                    urs = inspection.urs,
                    status = inspection.status,
                    onClick = { /* Navigate to detail */ }
                )
            }
        }
    }
}
