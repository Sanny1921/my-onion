package com.kreation.onionquality.data.api

import com.google.gson.annotations.SerializedName

data class HealthResponse(
    val status: String,
    val service: String,
    @SerializedName("gemini_configured") val geminiConfigured: Boolean,
    val model: String,
    val timestamp: String
)

data class InspectionMetadataDto(
    val id: String,
    @SerializedName("lot_id") val lotId: String,
    val timestamp: String,
    @SerializedName("user_provided_sample_count") val userProvidedSampleCount: Int?,
    @SerializedName("image_path") val imagePath: String
)

data class DefectCountsDto(
    val damaged: Int = 0,
    val rotten: Int = 0,
    val sprouted: Int = 0,
    val undersized: Int = 0,
    val diseased: Int = 0,
    val other: Int = 0
)

data class GeminiVisionObservationDto(
    @SerializedName("is_onion_sample") val isOnionSample: Boolean,
    @SerializedName("is_sample_usable") val isSampleUsable: Boolean,
    @SerializedName("rejection_reason") val rejectionReason: String?,
    @SerializedName("estimated_visible_count") val estimatedVisibleCount: Int = 0,
    val defects: DefectCountsDto = DefectCountsDto(),
    val observations: List<String> = emptyList(),
    @SerializedName("ai_confidence_signal") val aiConfidenceSignal: Float = 0f
)

data class BackendMetricsDto(
    @SerializedName("analyzable_count") val analyzableCount: Int = 0,
    @SerializedName("healthy_count") val healthyCount: Int = 0,
    @SerializedName("defect_count") val defectCount: Int = 0,
    @SerializedName("healthy_percentage") val healthyPercentage: Float = 0f,
    @SerializedName("defect_percentage") val defectPercentage: Float = 0f,
    @SerializedName("defect_breakdown_percentages") val defectBreakdownPercentages: Map<String, Float> = emptyMap(),
    @SerializedName("count_consistent") val countConsistent: Boolean = true,
    @SerializedName("size_measurement_status") val sizeMeasurementStatus: String = "NOT_MEASURABLE"
)

data class GradingAssessmentDto(
    val status: String,
    @SerializedName("specification_name") val specificationName: String?,
    @SerializedName("specification_version") val specificationVersion: String?,
    val grade: String?,
    val notes: String?
)

data class FinalAssessmentDto(
    @SerializedName("analysis_status") val analysisStatus: String,
    @SerializedName("grading_status") val gradingStatus: String,
    @SerializedName("inspector_status") val inspectorStatus: String = "PENDING_INSPECTOR_VERIFICATION",
    @SerializedName("inspector_notes") val inspectorNotes: String? = null
)

data class InspectionResultDto(
    val success: Boolean,
    val inspection: InspectionMetadataDto,
    @SerializedName("ai_observation") val aiObservation: GeminiVisionObservationDto?,
    @SerializedName("backend_metrics") val backendMetrics: BackendMetricsDto?,
    val grading: GradingAssessmentDto,
    @SerializedName("final_assessment") val finalAssessment: FinalAssessmentDto,
    val error: Map<String, String>? = null
)
