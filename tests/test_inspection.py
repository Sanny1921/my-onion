import os
import tempfile
import pytest
from pathlib import Path
from PIL import Image

from app.image_validator import validate_image_file, TechnicalImageValidationError
from app.schemas import (
    GeminiVisionObservation,
    DefectCounts,
    AnalysisStatus,
    GradingStatus,
    InspectorStatus,
)
from app.grading import GradingEngine
from app.service import InspectionService
from app.gemini_client import GeminiVisionClient, GeminiAPIError


@pytest.fixture
def temp_dir():
    with tempfile.TemporaryDirectory() as tmp:
        yield Path(tmp)


@pytest.fixture
def valid_test_image(temp_dir):
    """Creates a valid 200x200 RGB JPEG image."""
    img_path = temp_dir / "valid_onion.jpg"
    img = Image.new("RGB", (200, 200), color=(200, 100, 50))
    img.save(img_path, format="JPEG")
    return str(img_path)


# ============================================================================
# 1. Technical Image Validation Tests
# ============================================================================

def test_missing_file_failure():
    with pytest.raises(TechnicalImageValidationError) as exc_info:
        validate_image_file("non_existent_file_path_123.jpg")
    assert exc_info.value.code == "FILE_NOT_FOUND"


def test_unsupported_file_extension(temp_dir):
    txt_file = temp_dir / "test.txt"
    txt_file.write_text("not an image")
    with pytest.raises(TechnicalImageValidationError) as exc_info:
        validate_image_file(str(txt_file))
    assert exc_info.value.code == "UNSUPPORTED_FILE_TYPE"


def test_empty_image_file(temp_dir):
    empty_file = temp_dir / "empty.jpg"
    empty_file.write_bytes(b"")
    with pytest.raises(TechnicalImageValidationError) as exc_info:
        validate_image_file(str(empty_file))
    assert exc_info.value.code == "EMPTY_IMAGE_FILE"


def test_corrupt_image_file(temp_dir):
    corrupt_file = temp_dir / "corrupt.jpg"
    corrupt_file.write_bytes(b"\xFF\xD8\xFF\xE0\x00\x10JFIF\x00corruptdata")
    with pytest.raises(TechnicalImageValidationError) as exc_info:
        validate_image_file(str(corrupt_file))
    assert exc_info.value.code == "CORRUPT_IMAGE_FILE"


def test_invalid_dimensions(temp_dir):
    tiny_file = temp_dir / "tiny.jpg"
    img = Image.new("RGB", (10, 10), color="red")
    img.save(tiny_file, format="JPEG")
    with pytest.raises(TechnicalImageValidationError) as exc_info:
        validate_image_file(str(tiny_file))
    assert exc_info.value.code == "INVALID_DIMENSIONS"


# ============================================================================
# 2. Count Validation & Math Metric Engine Tests
# ============================================================================

def test_count_consistency_check_passed():
    engine = GradingEngine()
    obs = GeminiVisionObservation(
        is_onion_sample=True,
        is_sample_usable=True,
        estimated_visible_count=50,
        defects=DefectCounts(damaged=3, rotten=2, sprouted=1, undersized=0, diseased=0, other=0),
        ai_confidence_signal=0.9,
    )
    metrics, consistent = engine.compute_metrics(obs)
    
    assert consistent is True
    assert metrics.analyzable_count == 50
    assert metrics.defect_count == 6
    assert metrics.healthy_count == 44
    assert metrics.healthy_percentage == 88.0
    assert metrics.defect_percentage == 12.0
    assert metrics.defect_breakdown_percentages["damaged"] == 6.0
    assert metrics.defect_breakdown_percentages["rotten"] == 4.0
    assert metrics.defect_breakdown_percentages["sprouted"] == 2.0


def test_count_consistency_check_failed_no_repair():
    """Verify that inconsistent AI defect counts are NOT repaired into fake numbers."""
    engine = GradingEngine()
    # Defects total = 55 (exceeds estimated visible count of 50)
    obs = GeminiVisionObservation(
        is_onion_sample=True,
        is_sample_usable=True,
        estimated_visible_count=50,
        defects=DefectCounts(damaged=30, rotten=20, sprouted=5, undersized=0, diseased=0, other=0),
        ai_confidence_signal=0.85,
    )
    metrics, consistent = engine.compute_metrics(obs)

    assert consistent is False
    assert metrics.healthy_count == 0
    assert metrics.defect_count == 55
    # Verify numbers were NOT silently repaired to force 50
    assert metrics.count_consistent is False


# ============================================================================
# 3. Grading Rule Engine Tests
# ============================================================================

def test_unverified_grading_rules_default():
    """Verify that when no verified rule set is loaded, grading status is GRADING_RULES_NOT_VERIFIED and grade is None."""
    engine = GradingEngine()
    obs = GeminiVisionObservation(
        is_onion_sample=True,
        is_sample_usable=True,
        estimated_visible_count=20,
        defects=DefectCounts(damaged=1, rotten=0, sprouted=0, undersized=0, diseased=0, other=0),
        ai_confidence_signal=0.95,
    )
    metrics, _ = engine.compute_metrics(obs)
    grading = engine.evaluate_grading(metrics)

    assert grading.status == GradingStatus.GRADING_RULES_NOT_VERIFIED
    assert grading.grade is None
    assert "No official verified SIH26031 procurement rule set active" in grading.notes


# ============================================================================
# 4. Gemini API Error Handling Tests
# ============================================================================

def test_missing_api_key_failure(valid_test_image):
    """Verify clean failure when GEMINI_API_KEY is missing/empty."""
    client = GeminiVisionClient(api_key="")
    service = InspectionService(gemini_client=client)

    result = service.run_inspection(image_path=valid_test_image)

    assert result.success is False
    assert result.error is not None
    assert result.error.get("code") == "MISSING_API_KEY"
    assert result.final_assessment.analysis_status == AnalysisStatus.FAILED


# ============================================================================
# 5. Semantic Validation Tests (Mock Client Injection for Unit Test)
# ============================================================================

class MockSemanticClient:
    def __init__(self, observation: GeminiVisionObservation):
        self.observation = observation

    def analyze_image(self, image):
        return self.observation


def test_non_onion_image_semantic_rejection(valid_test_image):
    mock_obs = GeminiVisionObservation(
        is_onion_sample=False,
        is_sample_usable=False,
        rejection_reason="The image depicts a laptop keyboard, not onions.",
        estimated_visible_count=0,
        defects=DefectCounts(),
        ai_confidence_signal=0.98,
    )
    service = InspectionService(gemini_client=MockSemanticClient(mock_obs))
    result = service.run_inspection(image_path=valid_test_image)

    assert result.success is False
    assert result.final_assessment.analysis_status == AnalysisStatus.NOT_AN_ONION_SAMPLE
    assert result.error.get("code") == "NOT_AN_ONION_SAMPLE"
    assert "laptop keyboard" in result.error.get("message")


def test_unusable_image_semantic_rejection(valid_test_image):
    mock_obs = GeminiVisionObservation(
        is_onion_sample=True,
        is_sample_usable=False,
        rejection_reason="The photograph is severely out of focus and under-exposed.",
        estimated_visible_count=5,
        defects=DefectCounts(),
        ai_confidence_signal=0.2,
    )
    service = InspectionService(gemini_client=MockSemanticClient(mock_obs))
    result = service.run_inspection(image_path=valid_test_image)

    assert result.success is False
    assert result.final_assessment.analysis_status == AnalysisStatus.INSUFFICIENT_IMAGE
    assert result.error.get("code") == "IMAGE_QUALITY_INSUFFICIENT"
    assert "out of focus" in result.error.get("message")
