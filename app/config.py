import os
import logging
from pathlib import Path
from typing import Optional, Dict, Any
from dotenv import load_dotenv

# Load .env file from working directory or parent if present
env_path = Path.cwd() / ".env"
if env_path.exists():
    load_dotenv(dotenv_path=env_path)
else:
    load_dotenv()

logger = logging.getLogger("onion_backend.config")


class Config:
    """Centralized Backend Configuration"""
    
    # Gemini API Settings
    GEMINI_API_KEY: Optional[str] = os.getenv("GEMINI_API_KEY")
    GEMINI_MODEL: str = os.getenv("GEMINI_MODEL", "gemini-3.5-flash")
    
    # Technical Image Validation Rules
    MAX_FILE_SIZE_BYTES: int = 20 * 1024 * 1024  # 20 MB
    MIN_IMAGE_WIDTH: int = 50
    MIN_IMAGE_HEIGHT: int = 50
    ALLOWED_EXTENSIONS: set = {".jpg", ".jpeg", ".png", ".webp"}
    ALLOWED_MIME_TYPES: set = {"image/jpeg", "image/png", "image/webp"}
    
    # Rule Set Registry (Provisional / Verified specs registry)
    # Default behavior is unverified ruleset
    VERIFIED_RULESETS: Dict[str, Any] = {}

    @classmethod
    def get_api_key(cls) -> str:
        """Returns API key or raises explicit error if missing."""
        key = cls.GEMINI_API_KEY or os.getenv("GEMINI_API_KEY")
        if not key or key.strip() == "" or key == "your_real_gemini_api_key_here":
            raise ValueError(
                "GEMINI_API_KEY is not set or invalid. "
                "Please set GEMINI_API_KEY in your environment or .env file."
            )
        return key.strip()
