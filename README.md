# SIH26031 Onion Quality Assessment & Grading — Real V1 Backend

Real, production-grade Python CLI backend for **SIH2026 Problem Statement SIH26031: "Quality assessment and grading of onions are often subjective and vary across procurement centers, resulting in disputes and inconsistencies."**

This backend is built specifically for the **Procurement / Quality Inspection Officer** workflow. It establishes an AI-assisted visual inspection pipeline using Google's real Gemini Vision API, decoupled local metric computation, mathematical count verification, and configurable procurement grading rules.

---

## 1. Core Architecture & Responsibility Separation

The system follows a strict principle of decoupled responsibilities:

> **Gemini observes. Backend validates and calculates. Grading rules decide. Inspector verifies.**

```text
Inspector provides onion image + sample count (e.g. 50)
                        │
                        ▼
         Technical Image Validation (Pillow)
  (checks file existence, MIME type, dimensions, corruption)
                        │
                        ▼
        Real Gemini Vision API (google-genai)
  (is_onion_sample, is_sample_usable, counts, defects, confidence signal)
                        │
                        ▼
      Backend Math & Count Verification Engine
(verifies healthy + defects == visible_count; sets INCONSISTENT_AI_RESULT if mismatched)
                        │
                        ▼
       Backend Metric & Measurement Engine
  (healthy %, defect %, defect breakdowns, size_measurement_status)
                        │
                        ▼
          Grading Rules Evaluation Engine
   (evaluates rule_set or returns GRADING_RULES_NOT_VERIFIED)
                        │
                        ▼
          Human Inspector Review Contract
     (PENDING_INSPECTOR_VERIFICATION status)
                        │
                        ▼
          Human-Readable CLI / JSON Output
```

---

## 2. Responsibilities Breakdown

### 🤖 Gemini's Responsibility (Visual Observer)
- **Sample Validity**: Checks if image actually depicts onions (`is_onion_sample`).
- **Sample Usability**: Determines if lighting, clarity, and angle allow inspection (`is_sample_usable`).
- **Visual Counts**: Estimates visible onion count (`estimated_visible_count`).
- **Defect Extraction**: Identifies visible defects (`damaged`, `rotten`, `sprouted`, `undersized`, `diseased`, `other`).
- **Qualitative Notes**: Observes skin condition, neck tightness, rot, and discoloration.
- **AI Confidence Signal**: Returns `ai_confidence_signal` (0.0 to 1.0) indicating self-reported image clarity.

### ⚙️ Backend's Responsibility (Math & Validation Engine)
- **Technical Validation**: Validates image integrity locally via Pillow (NO remote network call if local file is corrupt/invalid).
- **Count Validation**: Verifies that `healthy_count + total_defects == estimated_visible_count`. Never silently "repairs" or alters AI count mismatches. Flags `INCONSISTENT_AI_RESULT` when AI counts mismatch.
- **Denominator Separation**: Keeps `user_provided_sample_count` (declared batch size) separate from `analyzable_count` (image-derived count). Metric percentages are strictly calculated against `analyzable_count`.
- **Measurement Status**: Marks physical size as `size_measurement_status = NOT_MEASURABLE` unless physical scale calibration is active.

### 📜 Grading Engine Responsibility (Rule Evaluator)
- Evaluates calculated backend metrics against registered procurement specifications.
- **Default Safety**: If no verified official procurement rule set is active, returns `status = GRADING_RULES_NOT_VERIFIED` and `grade = null`. Does **not** invent arbitrary Grade A/B/C letter grades.

### 👤 Human Inspector's Responsibility (Decision Authority)
- The system is an **AI-assisted inspection tool**.
- Final assessment status defaults to `inspector_status: PENDING_INSPECTOR_VERIFICATION`.
- Inspector retains final verification authority before lot acceptance/rejection.

---

## 3. Environment Setup & Gemini API Configuration

### Prerequisites
- Python 3.10+
- Valid Gemini API key from [Google AI Studio](https://aistudio.google.com/)

### Installation
1. Clone / navigate to project repository:
   ```bash
   cd /home/turtle/my-onion
   ```

2. Install backend dependencies:
   ```bash
   python3 -m pip install -r requirements.txt
   ```

3. Configure Environment Variables:
   Copy `.env.example` to `.env` and insert your real Gemini API key:
   ```bash
   cp .env.example .env
   ```
   Edit `.env`:
   ```env
   GEMINI_API_KEY=AIzaSyYourRealKeyHere...
   GEMINI_MODEL=gemini-2.5-flash
   ```

---

## 4. CLI Usage

### Basic Inspection (Human-Readable Boxed Output)
```bash
python3 cli.py inspect ./data/samples/onions.jpg --sample-count 50
```
Or direct flag syntax:
```bash
python3 cli.py --image ./data/samples/onions.jpg --sample-count 50 --lot-id LOT-402
```

### Machine-Readable JSON Output
```bash
python3 cli.py inspect ./data/samples/onions.jpg --json
```

### Verbose Step-by-Step Pipeline Output
```bash
python3 cli.py inspect ./data/samples/onions.jpg --verbose
```

---

## 5. Structured Data Contract Schema

Sample JSON Output (`--json`):

```json
{
  "success": true,
  "inspection": {
    "id": "INS-20260904025000-a1b2c3",
    "lot_id": "LOT-DEFAULT",
    "timestamp": "2026-09-04T02:50:00.000000+00:00",
    "user_provided_sample_count": 50,
    "image_path": "/home/turtle/my-onion/data/samples/onions.jpg"
  },
  "ai_observation": {
    "is_onion_sample": true,
    "is_sample_usable": true,
    "rejection_reason": null,
    "estimated_visible_count": 43,
    "defects": {
      "damaged": 3,
      "rotten": 2,
      "sprouted": 1,
      "undersized": 0,
      "diseased": 0,
      "other": 0
    },
    "observations": [
      "Sprouting visible on 1 bulb top neck.",
      "Surface rot detected on 2 lower bulbs."
    ],
    "ai_confidence_signal": 0.88
  },
  "backend_metrics": {
    "analyzable_count": 43,
    "healthy_count": 37,
    "defect_count": 6,
    "healthy_percentage": 86.05,
    "defect_percentage": 13.95,
    "defect_breakdown_percentages": {
      "damaged": 6.98,
      "rotten": 4.65,
      "sprouted": 2.33
    },
    "count_consistent": true,
    "size_measurement_status": "NOT_MEASURABLE"
  },
  "grading": {
    "status": "GRADING_RULES_NOT_VERIFIED",
    "specification_name": null,
    "specification_version": null,
    "grade": null,
    "notes": "No official verified SIH26031 procurement rule set active. Grading rules must be formally configured."
  },
  "final_assessment": {
    "analysis_status": "COMPLETED",
    "grading_status": "GRADING_RULES_NOT_VERIFIED",
    "inspector_status": "PENDING_INSPECTOR_VERIFICATION",
    "inspector_notes": "Inspection complete. Awaiting procurement officer verification."
  },
  "error": null
}
```

---

## 6. Testing Strategy & Execution

Run unit and integration test suite:
```bash
PYTEST_DISABLE_PLUGIN_AUTOLOAD=1 python3 -m pytest tests/test_inspection.py -v
```

### Verified Test Scenarios
1. **Technical Image Validation**:
   - Missing file -> `FILE_NOT_FOUND`
   - Unsupported extension -> `UNSUPPORTED_FILE_TYPE`
   - Empty file -> `EMPTY_IMAGE_FILE`
   - Corrupt header -> `CORRUPT_IMAGE_FILE`
   - Low resolution -> `INVALID_DIMENSIONS`
2. **Count & Math Engine Verification**:
   - Valid math (`healthy + defects == visible`) -> `count_consistent = True`
   - Inconsistent AI count -> `count_consistent = False`, `INCONSISTENT_AI_RESULT` (zero silent count alteration)
3. **Grading Rule Engine**:
   - Unverified rules -> `GRADING_RULES_NOT_VERIFIED`, `grade = null`
4. **API Key Security**:
   - Unset/missing key -> `MISSING_API_KEY` failure
5. **Semantic Validation**:
   - Non-onion photo -> `NOT_AN_ONION_SAMPLE`
   - Blurry/dark photo -> `IMAGE_QUALITY_INSUFFICIENT`

---

## 7. Current Product Limitations & Guidelines

> [!WARNING]
> 1. **AI Assistance Only**: This system assists human procurement officers. It is **not** an autonomous replacement for human quality inspectors.
> 2. **Confidence Signal**: `ai_confidence_signal` represents Gemini's self-assessed image clarity score; it is **not** a ground-truth accuracy guarantee.
> 3. **Physical Size**: Physical millimetre dimensions require physical reference scale calibration. Without calibration, size is reported as `NOT_MEASURABLE`.
> 4. **Grading Verification**: Official procurement grading rules vary by season and agency (e.g. NAFED vs local mandis). Rules must be formally registered before automated letter grades are issued.
