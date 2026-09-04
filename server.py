import os
import shutil
import tempfile
import logging
from datetime import datetime, timezone
from typing import Optional

from fastapi import FastAPI, UploadFile, File, Form, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from app.config import Config
from app.service import InspectionService
from app.schemas import InspectionResult

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
    datefmt="%H:%M:%S",
)
logger = logging.getLogger("onion_backend.server")

app = FastAPI(
    title="SIH26031 Real Onion Quality Inspection Backend API",
    description="REST API for Android and Web Clients powered by Gemini Vision & Rules Engine",
    version="1.0.0",
)

# Configure CORS Middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Global InspectionService instance
inspection_service = InspectionService()


@app.get("/api/v1/health")
async def health_check():
    """
    Health check endpoint returning system status and Gemini API key configuration check.
    """
    gemini_key = os.getenv("GEMINI_API_KEY")
    key_configured = bool(gemini_key and gemini_key.strip() and gemini_key != "your_real_gemini_api_key_here")
    
    return {
        "status": "healthy",
        "service": "Onion Quality Inspection API",
        "gemini_configured": key_configured,
        "model": Config.GEMINI_MODEL,
        "timestamp": datetime.now(timezone.utc).isoformat(),
    }


@app.post("/api/v1/inspect", response_model=InspectionResult)
async def inspect_onion(
    image: UploadFile = File(..., description="Captured onion sample image file"),
    sample_count: Optional[int] = Form(None, description="Declared physical sample count (e.g. 50)"),
    lot_id: Optional[str] = Form("LOT-DEFAULT", description="Lot or batch tracking ID"),
):
    """
    End-to-end onion sample inspection endpoint.
    Accepts multipart/form-data with actual image file bytes.
    """
    logger.info(f"Received inspection request: file={image.filename}, sample_count={sample_count}, lot_id={lot_id}")

    if not image.filename:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="No image file provided in request.",
        )

    # Save uploaded file temporarily to disk
    suffix = os.path.splitext(image.filename)[1] or ".jpg"
    with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as temp_file:
        temp_path = temp_file.name
        shutil.copyfileobj(image.file, temp_file)

    try:
        # Execute inspection pipeline
        result = inspection_service.run_inspection(
            image_path=temp_path,
            user_provided_sample_count=sample_count,
            lot_id=lot_id or "LOT-DEFAULT",
        )
        return result
    except Exception as e:
        logger.exception("Unexpected error during inspection execution:")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Backend inspection error: {str(e)}",
        )
    finally:
        # Clean up temp file
        if os.path.exists(temp_path):
            try:
                os.remove(temp_path)
            except Exception as clean_err:
                logger.warning(f"Failed to remove temp file {temp_path}: {clean_err}")


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
