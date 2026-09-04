from datetime import datetime, timezone
from enum import Enum
from typing import Dict, List, Optional
from pydantic import BaseModel, Field


class AnalysisStatus(str, Enum):
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"
    INSUFFICIENT_IMAGE = "INSUFFICIENT_IMAGE"
    NOT_AN_ONION_SAMPLE = "NOT_AN_ONION_SAMPLE"
    INCONSISTENT_AI_RESULT = "INCONSISTENT_AI_RESULT"


class GradingStatus(str, Enum):
    NOT_APPLICABLE = "NOT_APPLICABLE"
    GRADING_RULES_NOT_VERIFIED = "GRADING_RULES_NOT_VERIFIED"
    GRADED = "GRADED"


class InspectorStatus(str, Enum):
    PENDING_INSPECTOR_VERIFICATION = "PENDING_INSPECTOR_VERIFICATION"
    VERIFIED = "VERIFIED"
    CORRECTED = "CORRECTED"


class DefectCounts(BaseModel):
    damaged: int = Field(default=0, ge=0, description="Count of physically damaged onions (cuts, cracks, mechanical injury)")
    rotten: int = Field(default=0, ge=0, description="Count of rotten onions (soft rot, bacterial, fungal infection)")
    sprouted: int = Field(default=0, ge=0, description="Count of sprouted onions")
    undersized: int = Field(default=0, ge=0, description="Count of visually undersized onions")
    diseased: int = Field(default=0, ge=0, description="Count of diseased/smut/discolored onions")
    other: int = Field(default=0, ge=0, description="Count of other visible defects (thick neck, double bulb, seed stem)")

    def total(self) -> int:
        return self.damaged + self.rotten + self.sprouted + self.undersized + self.diseased + self.other


class GeminiVisionObservation(BaseModel):
    """Raw structured visual observation provided by Gemini Vision API."""
    is_onion_sample: bool = Field(description="True if the image contains an onion sample")
    is_sample_usable: bool = Field(description="True if the image clarity, lighting, and coverage allow visual analysis")
    rejection_reason: Optional[str] = Field(default=None, description="Reason if image is not an onion sample or not usable")
    estimated_visible_count: int = Field(default=0, ge=0, description="Estimated number of onions visible in the photograph")
    defects: DefectCounts = Field(default_factory=DefectCounts, description="Categorized defect counts for visible onions")
    observations: List[str] = Field(default_factory=list, description="Qualitative observations regarding skin, neck, color, condition")
    ai_confidence_signal: float = Field(default=0.0, ge=0.0, le=1.0, description="AI self-assessed image clarity signal (0.0 to 1.0). NOT an accuracy guarantee.")


class BackendMetrics(BaseModel):
    """Calculated backend quality metrics based strictly on analyzable image counts."""
    analyzable_count: int = Field(ge=0, description="Total count of analyzable onions derived from image")
    healthy_count: int = Field(ge=0, description="Calculated healthy onion count (analyzable_count - total_defects)")
    defect_count: int = Field(ge=0, description="Sum of all defect counts")
    healthy_percentage: float = Field(description="(healthy_count / analyzable_count) * 100")
    defect_percentage: float = Field(description="(defect_count / analyzable_count) * 100")
    defect_breakdown_percentages: Dict[str, float] = Field(default_factory=dict, description="Percentage of each defect type relative to analyzable_count")
    count_consistent: bool = Field(description="True if healthy_count + sum(defect_counts) == estimated_visible_count")
    size_measurement_status: str = Field(default="NOT_MEASURABLE", description="Measurement status for physical size/diameter")


class GradingAssessment(BaseModel):
    """Grading rule evaluation engine result."""
    status: GradingStatus = Field(default=GradingStatus.GRADING_RULES_NOT_VERIFIED)
    specification_name: Optional[str] = None
    specification_version: Optional[str] = None
    grade: Optional[str] = None
    notes: Optional[str] = "No official verified SIH26031 procurement rule set active."


class FinalAssessment(BaseModel):
    """Multi-status summary of the inspection run."""
    analysis_status: AnalysisStatus
    grading_status: GradingStatus
    inspector_status: InspectorStatus = Field(default=InspectorStatus.PENDING_INSPECTOR_VERIFICATION)
    inspector_notes: Optional[str] = None


class InspectionMetadata(BaseModel):
    id: str
    lot_id: str = "LOT-DEFAULT"
    timestamp: str = Field(default_factory=lambda: datetime.now(timezone.utc).isoformat())
    user_provided_sample_count: Optional[int] = None
    image_path: str


class InspectionResult(BaseModel):
    """Complete structured output data contract."""
    success: bool
    inspection: InspectionMetadata
    ai_observation: Optional[GeminiVisionObservation] = None
    backend_metrics: Optional[BackendMetrics] = None
    grading: GradingAssessment
    final_assessment: FinalAssessment
    error: Optional[Dict[str, str]] = None
