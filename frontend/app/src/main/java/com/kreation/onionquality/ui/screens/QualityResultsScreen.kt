package com.kreation.onionquality.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kreation.onionquality.data.api.InspectionResultDto
import com.kreation.onionquality.theme.*
import com.kreation.onionquality.ui.components.PrimaryButton
import com.kreation.onionquality.ui.components.SecondaryButton
import com.kreation.onionquality.ui.components.SectionHeader
import com.kreation.onionquality.ui.viewmodel.InspectionUiState
import com.kreation.onionquality.ui.viewmodel.InspectionViewModel

@Composable
fun QualityResultsScreen(
    viewModel: InspectionViewModel,
    onViewDetailed: () -> Unit,
    onGenerateReport: () -> Unit,
    onBackToDashboard: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val result: InspectionResultDto? = (uiState as? InspectionUiState.Success)?.result

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text("Quality Results", style = MaterialTheme.typography.headlineMedium, color = DarkPlum)
        Spacer(modifier = Modifier.height(24.dp))

        if (result == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No inspection result available.", style = MaterialTheme.typography.bodyLarge, color = SecondaryText)
                }
            }
        } else {
            val analysisStatus = result.finalAssessment.analysisStatus
            val gradingStatus = result.grading.status
            val gradeText = result.grading.grade ?: "UNVERIFIED"
            val backendMetrics = result.backendMetrics
            val aiObs = result.aiObservation

            // Special Banner for non-onion or unrated images
            if (analysisStatus == "NOT_AN_ONION_SAMPLE" || analysisStatus == "INSUFFICIENT_IMAGE") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SemanticRotten.copy(alpha = 0.1f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SemanticRotten)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = SemanticRotten, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (analysisStatus == "NOT_AN_ONION_SAMPLE") "Image Rejected: Not an Onion Sample" else "Image Rejected: Low Clarity",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SemanticRotten
                            )
                            Text(
                                text = aiObs?.rejectionReason ?: "Provided photo cannot be processed for onion inspection.",
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkPlum
                            )
                        }
                    }
                }
            } else if (analysisStatus == "INCONSISTENT_AI_RESULT") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SemanticSprouted.copy(alpha = 0.15f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SemanticSprouted)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = SemanticSprouted, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Count Inconsistency Detected",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DarkPlum
                            )
                            Text(
                                text = result.error?.get("message") ?: "AI defect count exceeded visible sample count. Pending verification.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SecondaryText
                            )
                        }
                    }
                }
            }

            // Overall Grade Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (gradingStatus == "GRADED") SemanticGood.copy(alpha = 0.1f) else SoftPink.copy(alpha = 0.5f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    2.dp,
                    if (gradingStatus == "GRADED") SemanticGood.copy(alpha = 0.3f) else PrimaryMagenta.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "OVERALL GRADE",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (gradingStatus == "GRADED") SemanticGood else PrimaryMagenta
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = gradeText,
                        fontSize = if (gradeText.length > 3) 32.sp else 64.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (gradingStatus == "GRADED") SemanticGood else DarkPlum
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (gradingStatus == "GRADED") "Official Quality Grade" else "Grading Rules Unverified",
                        style = MaterialTheme.typography.titleMedium,
                        color = SecondaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader("QUALITY BREAKDOWN")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    val healthyPct = backendMetrics?.healthyPercentage ?: 0f
                    val breakdown = backendMetrics?.defectBreakdownPercentages ?: emptyMap()

                    BreakdownRow("Healthy Onions", "${String.format("%.1f", healthyPct)}%", SemanticGood, healthyPct / 100f)
                    BreakdownRow("Diseased", "${String.format("%.1f", breakdown["diseased"] ?: 0f)}%", SemanticRotten, (breakdown["diseased"] ?: 0f) / 100f)
                    BreakdownRow("Sprouted", "${String.format("%.1f", breakdown["sprouted"] ?: 0f)}%", SemanticSprouted, (breakdown["sprouted"] ?: 0f) / 100f)
                    BreakdownRow("Damaged", "${String.format("%.1f", breakdown["damaged"] ?: 0f)}%", SemanticDamaged, (breakdown["damaged"] ?: 0f) / 100f)
                    BreakdownRow("Rotten", "${String.format("%.1f", breakdown["rotten"] ?: 0f)}%", SemanticRotten, (breakdown["rotten"] ?: 0f) / 100f)
                    BreakdownRow("Undersized (URS)", "${String.format("%.1f", breakdown["undersized"] ?: 0f)}%", SemanticUrs, (breakdown["undersized"] ?: 0f) / 100f)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sample Counts Overview Card
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("DECLARED SAMPLE", style = MaterialTheme.typography.labelSmall, color = SecondaryText)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${result.inspection.userProvidedSampleCount ?: "N/A"}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = DarkPlum,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("VISIBLE ANALYZABLE", style = MaterialTheme.typography.labelSmall, color = SecondaryText)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${backendMetrics?.analyzableCount ?: 0}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = DarkPlum,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("DEFECTIVE", style = MaterialTheme.typography.labelSmall, color = SecondaryText)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${backendMetrics?.defectCount ?: 0}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = SemanticRotten,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        PrimaryButton(text = "View Detailed Analysis", onClick = onViewDetailed)
        Spacer(modifier = Modifier.height(12.dp))
        SecondaryButton(text = "Generate Report", onClick = onGenerateReport)
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onBackToDashboard, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Dashboard", color = SecondaryText, style = MaterialTheme.typography.labelLarge)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun BreakdownRow(label: String, value: String, color: androidx.compose.ui.graphics.Color, progress: Float) {
    val safeProgress = progress.coerceIn(0f, 1f)
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = safeProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}
