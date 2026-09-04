import uuid
import logging
from datetime import datetime, timezone
from typing import Optional

from app.image_validator import validate_image_file, TechnicalImageValidationError
from app.gemini_client import GeminiVisionClient, GeminiAPIError
from app.grading import GradingEngine
from app.schemas import (
    InspectionResult,
    InspectionMetadata,
    FinalAssessment,
    GradingAssessment,
    AnalysisStatus,
    GradingStatus,
    InspectorStatus,
)

logger = logging.getLogger("onion_backend.service")


class InspectionService:
    """
    Central Inspection Pipeline Orchestrator.

    Workflow:
      Image -> Technical Validation -> Real Gemini Vision -> Semantic Validation ->
      Backend Math & Metric Verification -> Grading Engine -> Inspector Status Contract
    """

    def __init__(
        self,
        gemini_client: Optional[GeminiVisionClient] = None,
        grading_engine: Optional[GradingEngine] = None,
    ):
        self.gemini_client = gemini_client or GeminiVisionClient()
        self.grading_engine = grading_engine or GradingEngine()

    def run_inspection(
        self,
        image_path: str,
        user_provided_sample_count: Optional[int] = None,
        lot_id: str = "LOT-DEFAULT",
        rule_set_name: Optional[str] = None,
    ) -> InspectionResult:
        """
        Executes end-to-end onion quality inspection pipeline.
        """
        # Generate inspection ID & metadata
        inspection_id = f"INS-{datetime.now(timezone.utc).strftime('%Y%m%d%H%M%S')}-{uuid.uuid4().hex[:6]}"
        metadata = InspectionMetadata(
            id=inspection_id,
            lot_id=lot_id,
            timestamp=datetime.now(timezone.utc).isoformat(),
            user_provided_sample_count=user_provided_sample_count,
            image_path=image_path,
        )

        logger.info(f"==================================================")
        logger.info(f"[INFO] Starting Inspection [{inspection_id}] for image: {image_path}")
        logger.info(f"==================================================")

        # ------------------------------------------------------------------
        # Step 1: Technical Image Validation (Local Pillow)
        # ------------------------------------------------------------------
        try:
            logger.info("[INFO] Step 1: Technical Image Validation...")
            pil_image, resolved_path = validate_image_file(image_path)
            metadata.image_path = resolved_path
        except TechnicalImageValidationError as e:
            logger.error(f"❌ Step 1 FAILED: Technical Image Validation Error [{e.code}]: {e.message}")
            return InspectionResult(
                success=False,
                inspection=metadata,
                ai_observation=None,
                backend_metrics=None,
                grading=GradingAssessment(status=GradingStatus.NOT_APPLICABLE),
                final_assessment=FinalAssessment(
                    analysis_status=AnalysisStatus.FAILED,
                    grading_status=GradingStatus.NOT_APPLICABLE,
                    inspector_status=InspectorStatus.PENDING_INSPECTOR_VERIFICATION,
                ),
                error={"code": e.code, "message": e.message},
            )

        # ------------------------------------------------------------------
        # Step 2: Real Gemini Vision Analysis
        # ------------------------------------------------------------------
        try:
            logger.info("[INFO] Step 2: Sending image to Gemini Vision API...")
            observation = self.gemini_client.analyze_image(pil_image)
        except GeminiAPIError as e:
            logger.error(f"❌ Step 2 FAILED: Gemini API Error [{e.code}]: {e.message}")
            return InspectionResult(
                success=False,
                inspection=metadata,
                ai_observation=None,
                backend_metrics=None,
                grading=GradingAssessment(status=GradingStatus.NOT_APPLICABLE),
                final_assessment=FinalAssessment(
                    analysis_status=AnalysisStatus.FAILED,
                    grading_status=GradingStatus.NOT_APPLICABLE,
                    inspector_status=InspectorStatus.PENDING_INSPECTOR_VERIFICATION,
                ),
                error={"code": e.code, "message": e.message},
            )

        # ------------------------------------------------------------------
        # Step 3: Semantic Validation (Is Onion? Is Usable?)
        # ------------------------------------------------------------------
        logger.info("[INFO] Step 3: Validating Semantic Requirements...")
        if not observation.is_onion_sample:
            logger.warning("⚠️ Step 3 REJECTED: Image is NOT an onion sample.")
            return InspectionResult(
                success=False,
                inspection=metadata,
                ai_observation=observation,
                backend_metrics=None,
                grading=GradingAssessment(status=GradingStatus.NOT_APPLICABLE),
                final_assessment=FinalAssessment(
                    analysis_status=AnalysisStatus.NOT_AN_ONION_SAMPLE,
                    grading_status=GradingStatus.NOT_APPLICABLE,
                    inspector_status=InspectorStatus.PENDING_INSPECTOR_VERIFICATION,
                ),
                error={
                    "code": "NOT_AN_ONION_SAMPLE",
                    "message": observation.rejection_reason or "The provided image does not contain an onion sample.",
                },
            )

        if not observation.is_sample_usable:
            logger.warning("⚠️ Step 3 REJECTED: Image quality/clarity is insufficient for inspection.")
            return InspectionResult(
                success=False,
                inspection=metadata,
                ai_observation=observation,
                backend_metrics=None,
                grading=GradingAssessment(status=GradingStatus.NOT_APPLICABLE),
                final_assessment=FinalAssessment(
                    analysis_status=AnalysisStatus.INSUFFICIENT_IMAGE,
                    grading_status=GradingStatus.NOT_APPLICABLE,
                    inspector_status=InspectorStatus.PENDING_INSPECTOR_VERIFICATION,
                ),
                error={
                    "code": "IMAGE_QUALITY_INSUFFICIENT",
                    "message": observation.rejection_reason or "Image clarity or resolution is insufficient for reliable defect inspection.",
                },
            )

        # ------------------------------------------------------------------
        # Step 4: Backend Count Verification & Metric Calculation
        # ------------------------------------------------------------------
        logger.info("[INFO] Step 4: Calculating Backend Quality Metrics & Verifying Counts...")
        metrics, count_consistent = self.grading_engine.compute_metrics(observation)

        analysis_status = (
            AnalysisStatus.COMPLETED
            if count_consistent
            else AnalysisStatus.INCONSISTENT_AI_RESULT
        )

        # ------------------------------------------------------------------
        # Step 5: Grading Engine Evaluation
        # ------------------------------------------------------------------
        logger.info("[INFO] Step 5: Evaluating Grading Rules...")
        grading_assessment = self.grading_engine.evaluate_grading(metrics, rule_set_name)

        # ------------------------------------------------------------------
        # Step 6: Construct Final Inspection Outcome
        # ------------------------------------------------------------------
        logger.info(f"[INFO] Step 6: Inspection Pipeline Completed. Status: {analysis_status.value}")
        return InspectionResult(
            success=True if analysis_status == AnalysisStatus.COMPLETED else False,
            inspection=metadata,
            ai_observation=observation,
            backend_metrics=metrics,
            grading=grading_assessment,
            final_assessment=FinalAssessment(
                analysis_status=analysis_status,
                grading_status=grading_assessment.status,
                inspector_status=InspectorStatus.PENDING_INSPECTOR_VERIFICATION,
                inspector_notes="Inspection complete. Awaiting procurement officer verification.",
            ),
            error=None if count_consistent else {
                "code": "INCONSISTENT_AI_RESULT",
                "message": "AI defect counts exceeded reported visible count. Inspection flagged for inspector review."
            },
        )
