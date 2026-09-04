import logging
from typing import Optional
from PIL import Image
from google import genai
from google.genai import types
from google.genai.errors import APIError

from app.config import Config
from app.schemas import GeminiVisionObservation

logger = logging.getLogger("onion_backend.gemini_client")


class GeminiAPIError(Exception):
    """Custom exception for Gemini Vision API failures."""

    def __init__(self, code: str, message: str):
        self.code = code
        self.message = message
        super().__init__(f"[{code}] {message}")


GEMINI_INSPECTION_PROMPT = """
You are an expert agricultural quality inspection AI specializing in onion procurement and grading (SIH26031).
Analyze the provided image of an onion sample carefully and return a structured visual observation object.

1. SAMPLE VALIDITY & USABILITY:
   - Determine if this image actually contains an onion sample (`is_onion_sample`: true/false).
   - Determine if the photograph clarity, lighting, resolution, and angle are usable for visual defect assessment (`is_sample_usable`: true/false).
   - If either is false, provide a clear explanation in `rejection_reason` (e.g. "The image depicts a vehicle, not onions", or "Image is too blurry/dark for reliable inspection").

2. ONION COUNT & VISUAL DEFECT OBSERVATIONS:
   - Estimate the total count of onions clearly visible in the photograph (`estimated_visible_count`).
   - Categorize defect counts among the visible onions into:
     * `damaged`: physically cut, cracked, crushed, or bruised bulbs.
     * `rotten`: soft rot, wet rot, bacterial infection, or decayed bulbs.
     * `sprouted`: green sprouts emerging from the top neck.
     * `undersized`: visibly small/miniature bulbs relative to standard sample size.
     * `diseased`: black mold/smut, purple blotch, severe discoloration.
     * `other`: thick/bottle neck, double bulbs, seed stems, missing scales, rooting.

3. QUALITATIVE OBSERVATIONS & CONFIDENCE:
   - List distinct visual observations regarding skin condition, neck tightness, color, and defect severity.
   - Provide your self-assessed image clarity signal (`ai_confidence_signal`) as a float between 0.0 and 1.0 (where 1.0 means crystal-clear lighting and resolution).

CRITICAL REQUIREMENT: Return ONLY a valid JSON object matching the requested schema. Do not guess or fabricate counts if the image is unsuitable.
"""


class GeminiVisionClient:
    """
    Real Google Gemini Vision API Client.
    Communicates with Gemini API using the official google-genai SDK.
    Strictly NO mock fallbacks or hardcoded AI responses.
    """

    def __init__(self, api_key: Optional[str] = None, model: Optional[str] = None):
        self.api_key = api_key if api_key is not None else Config.GEMINI_API_KEY
        self.model_name = model or Config.GEMINI_MODEL

    def analyze_image(self, image: Image.Image) -> GeminiVisionObservation:
        """
        Sends PIL image payload to Gemini Vision API with Pydantic JSON schema enforcement.

        Returns:
            GeminiVisionObservation object with structured visual findings.

        Raises:
            GeminiAPIError on missing API key, network failure, or response validation error.
        """
        logger.info(f"[INFO] Initializing Gemini API request using model '{self.model_name}'...")

        # 1. API key check
        try:
            active_key = self.api_key if self.api_key is not None else Config.get_api_key()
            if not active_key or active_key.strip() == "" or active_key == "your_real_gemini_api_key_here":
                raise ValueError(
                    "GEMINI_API_KEY is not set or invalid. "
                    "Please set GEMINI_API_KEY in your environment or .env file."
                )
        except ValueError as e:
            logger.error(f"❌ Gemini API Key Error: {e}")
            raise GeminiAPIError("MISSING_API_KEY", str(e)) from e

        # 2. Instantiate GenAI client
        try:
            client = genai.Client(api_key=active_key)
        except Exception as e:
            logger.error(f"❌ Failed to instantiate GenAI Client: {e}")
            raise GeminiAPIError("CLIENT_INIT_FAILED", f"Failed to initialize Gemini API client: {e}") from e

        # 3. Request structured JSON inference
        logger.info("[INFO] Sending image to Gemini API...")
        try:
            response = client.models.generate_content(
                model=self.model_name,
                contents=[image, GEMINI_INSPECTION_PROMPT],
                config=types.GenerateContentConfig(
                    response_mime_type="application/json",
                    response_schema=GeminiVisionObservation,
                    temperature=0.1,
                ),
            )
            logger.info("[INFO] Gemini response received.")
        except APIError as e:
            logger.error(f"❌ Gemini API Error: {e}")
            raise GeminiAPIError("API_REQUEST_FAILED", f"Gemini API request failed: {e.message}") from e
        except Exception as e:
            logger.error(f"❌ Gemini API request unexpected failure: {e}")
            raise GeminiAPIError("API_REQUEST_FAILED", f"Gemini API call failed: {str(e)}") from e

        # 4. Parse & Validate Response
        if not response or not response.text:
            raise GeminiAPIError("EMPTY_RESPONSE", "Gemini API returned an empty response.")

        logger.info("[INFO] Validating Gemini response schema...")
        try:
            # Parse response text into Pydantic GeminiVisionObservation schema
            observation = GeminiVisionObservation.model_validate_json(response.text)
            logger.info(
                f"[INFO] Gemini Vision Observation validated: "
                f"visible={observation.estimated_visible_count}, "
                f"defects_total={observation.defects.total()}, "
                f"confidence={observation.ai_confidence_signal:.2f}"
            )
            return observation
        except Exception as e:
            logger.error(f"❌ Schema validation failed for Gemini response text: {response.text}")
            raise GeminiAPIError(
                "RESPONSE_VALIDATION_FAILED",
                f"Gemini API response did not match expected visual observation schema: {e}"
            ) from e
