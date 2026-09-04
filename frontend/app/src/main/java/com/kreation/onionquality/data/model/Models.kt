package com.kreation.onionquality.data.model

data class Inspection(
    val batchId: String,
    val farmerName: String,
    val date: String,
    val totalOnions: Int,
    val gradeA: Int, // Percentage
    val urs: Int, // Percentage
    val sprouted: Int,
    val damaged: Int,
    val rotten: Int,
    val status: String // "PASS", "REVIEW", "REJECTED"
)

data class Batch(
    val id: String,
    val vendorId: String,
    val variety: String,
    val sampleQuantity: Double
)

data class QualityResult(
    val overallGrade: String,
    val gradeAPercent: Int,
    val ursPercent: Int,
    val sproutedPercent: Int,
    val damagedPercent: Int,
    val rottenPercent: Int,
    val totalAnalyzed: Int,
    val defectiveCount: Int
)

data class DefectResult(
    val category: String,
    val count: Int,
    val percentage: Int
)

data class Report(
    val reportId: String,
    val batchId: String,
    val dateTime: String,
    val procurementCenter: String,
    val vendorId: String,
    val sampleQuantity: Double,
    val onionVariety: String,
    val qualityResult: QualityResult,
    val status: String,
    val aiConfidence: Int
)
