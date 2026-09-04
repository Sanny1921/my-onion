import logging
from typing import Tuple, Dict, Any, Optional

from app.config import Config
from app.schemas import (
    GeminiVisionObservation,
    BackendMetrics,
    GradingAssessment,
    GradingStatus,
    AnalysisStatus,
)

logger = logging.getLogger("onion_backend.grading")


class GradingEngine:
    """
    Decoupled Backend Grading & Metrics Engine.
    
    Responsibilities:
      1. Performs strict mathematical count validation.
      2. Computes healthy %, defect %, and defect breakdown percentages strictly relative to analyzable_count.
      3. Never "repairs" bad AI counts. Sets count_consistent = False if math mismatches.
      4. Evaluates observations against rule sets (defaults safely to GRADING_RULES_NOT_VERIFIED).
    """

    def compute_metrics(self, observation: GeminiVisionObservation) -> Tuple[BackendMetrics, bool]:
        """
        Validates counts and calculates quality metrics from visual observations.

        Returns:
            Tuple of (BackendMetrics object, math_consistency_bool)
        """
        logger.info("[INFO] Calculating quality metrics...")
        estimated_count = observation.estimated_visible_count
        total_defects = observation.defects.total()

        # Analyzable count is the total visible onions reported by AI from the image
        analyzable_count = estimated_count

        # Math verification
        # healthy_count is the portion of visible onions without defects
        if total_defects <= estimated_count:
            healthy_count = estimated_count - total_defects
            count_consistent = True
        else:
            # Defect count exceeds reported visible count -> Inconsistent AI output!
            healthy_count = 0
            count_consistent = False
            logger.warning(
                f"⚠️ INCONSISTENT AI RESULT: Total defect count ({total_defects}) "
                f"exceeds estimated visible count ({estimated_count}). Flagging inconsistency."
            )

        # Calculate percentages strictly against analyzable_count
        if analyzable_count > 0:
            healthy_pct = round((healthy_count / analyzable_count) * 100.0, 2)
            defect_pct = round((total_defects / analyzable_count) * 100.0, 2)
            
            breakdowns: Dict[str, float] = {
                "damaged": round((observation.defects.damaged / analyzable_count) * 100.0, 2),
                "rotten": round((observation.defects.rotten / analyzable_count) * 100.0, 2),
                "sprouted": round((observation.defects.sprouted / analyzable_count) * 100.0, 2),
                "undersized": round((observation.defects.undersized / analyzable_count) * 100.0, 2),
                "diseased": round((observation.defects.diseased / analyzable_count) * 100.0, 2),
                "other": round((observation.defects.other / analyzable_count) * 100.0, 2),
            }
        else:
            healthy_pct = 0.0
            defect_pct = 0.0
            breakdowns = {
                "damaged": 0.0,
                "rotten": 0.0,
                "sprouted": 0.0,
                "undersized": 0.0,
                "diseased": 0.0,
                "other": 0.0,
            }

        metrics = BackendMetrics(
            analyzable_count=analyzable_count,
            healthy_count=healthy_count,
            defect_count=total_defects,
            healthy_percentage=healthy_pct,
            defect_percentage=defect_pct,
            defect_breakdown_percentages=breakdowns,
            count_consistent=count_consistent,
            size_measurement_status="NOT_MEASURABLE",
        )

        logger.info(
            f"[INFO] Metrics calculated: healthy={healthy_pct}% ({healthy_count}/{analyzable_count}), "
            f"defects={defect_pct}% ({total_defects}/{analyzable_count}), consistent={count_consistent}"
        )
        return metrics, count_consistent

    def evaluate_grading(
        self,
        metrics: BackendMetrics,
        rule_set_name: Optional[str] = None
    ) -> GradingAssessment:
        """
        Evaluates backend quality metrics against a registered procurement specification rule set.

        If no verified rule set is active or specified, returns GRADING_RULES_NOT_VERIFIED.
        Does NOT invent arbitrary Grade A/B/C letter grades.
        """
        logger.info("[INFO] Applying grading rules...")

        # 1. Check if a registered verified rule set is requested/active
        rule_set = Config.VERIFIED_RULESETS.get(rule_set_name) if rule_set_name else None

        if not rule_set:
            logger.info("ℹ️ No official verified grading rule set active. Returning GRADING_RULES_NOT_VERIFIED.")
            return GradingAssessment(
                status=GradingStatus.GRADING_RULES_NOT_VERIFIED,
                specification_name=rule_set_name,
                specification_version=None,
                grade=None,
                notes="No official verified SIH26031 procurement rule set active. Grading rules must be formally configured."
            )

        # 2. Execute custom rule set evaluation logic if provided
        try:
            grade_result, notes = self._apply_rule_set(metrics, rule_set)
            return GradingAssessment(
                status=GradingStatus.GRADED,
                specification_name=rule_set.get("name", "CUSTOM_PROCUREMENT_SPEC"),
                specification_version=rule_set.get("version", "1.0"),
                grade=grade_result,
                notes=notes
            )
        except Exception as e:
            logger.error(f"Failed to evaluate rule set: {e}")
            return GradingAssessment(
                status=GradingStatus.GRADING_RULES_NOT_VERIFIED,
                specification_name=rule_set_name,
                grade=None,
                notes=f"Error evaluating rule set: {str(e)}"
            )

    def _apply_rule_set(self, metrics: BackendMetrics, rule_set: Dict[str, Any]) -> Tuple[str, str]:
        """Helper to evaluate metrics against configured rule set thresholds."""
        max_rotten_pct = rule_set.get("max_rotten_pct", 0.0)
        max_defect_pct = rule_set.get("max_defect_pct", 15.0)

        rotten_pct = metrics.defect_breakdown_percentages.get("rotten", 0.0)

        if rotten_pct > max_rotten_pct:
            return "REJECT", f"Rotten percentage ({rotten_pct}%) exceeds zero-tolerance threshold ({max_rotten_pct}%)."
        
        if metrics.defect_percentage <= max_defect_pct:
            return "GRADE_A", f"Total defect percentage ({metrics.defect_percentage}%) meets Grade-A standard (<= {max_defect_pct}%)."
        
        return "GRADE_B", f"Total defect percentage ({metrics.defect_percentage}%) exceeds Grade-A limit."
