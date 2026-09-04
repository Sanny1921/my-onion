import os
import logging
from pathlib import Path
from typing import Tuple
from PIL import Image, UnidentifiedImageError

from app.config import Config

logger = logging.getLogger("onion_backend.image_validator")


class TechnicalImageValidationError(Exception):
    """Custom exception raised when an image fails technical validation."""

    def __init__(self, code: str, message: str):
        self.code = code
        self.message = message
        super().__init__(f"[{code}] {message}")


def validate_image_file(file_path: str) -> Tuple[Image.Image, str]:
    """
    Performs local technical image validation using PIL.

    Checks:
      1. File existence & path type
      2. File non-emptiness & file size limit
      3. Extension & MIME format compatibility
      4. PIL image decoding & corruption verification
      5. Minimum resolution requirements

    Returns:
      Tuple of (PIL.Image loaded in RGB mode, resolved absolute file path)
    
    Raises:
      TechnicalImageValidationError if any technical check fails.
    """
    logger.info(f"[INFO] Loading image for technical validation: {file_path}")
    path = Path(file_path).resolve()

    # 1. Existence check
    if not path.exists():
        raise TechnicalImageValidationError(
            code="FILE_NOT_FOUND",
            message=f"Image file does not exist at path: '{file_path}'"
        )

    if not path.is_file():
        raise TechnicalImageValidationError(
            code="INVALID_FILE_PATH",
            message=f"Target path is a directory, not a file: '{file_path}'"
        )

    # 2. Extension check
    ext = path.suffix.lower()
    if ext not in Config.ALLOWED_EXTENSIONS:
        allowed_str = ", ".join(sorted(Config.ALLOWED_EXTENSIONS))
        raise TechnicalImageValidationError(
            code="UNSUPPORTED_FILE_TYPE",
            message=f"Unsupported file extension '{ext}'. Allowed extensions are: {allowed_str}"
        )

    # 3. File size check
    file_size = path.stat().st_size
    if file_size == 0:
        raise TechnicalImageValidationError(
            code="EMPTY_IMAGE_FILE",
            message=f"Image file is empty (0 bytes): '{file_path}'"
        )

    if file_size > Config.MAX_FILE_SIZE_BYTES:
        max_mb = Config.MAX_FILE_SIZE_BYTES / (1024 * 1024)
        actual_mb = file_size / (1024 * 1024)
        raise TechnicalImageValidationError(
            code="FILE_TOO_LARGE",
            message=f"Image file size ({actual_mb:.2f} MB) exceeds maximum allowed size ({max_mb:.2f} MB)."
        )

    # 4. PIL open & corruption check
    try:
        with Image.open(path) as img:
            img.verify()  # Verifies file header and integrity
    except (UnidentifiedImageError, SyntaxError, OSError, ValueError) as e:
        logger.error(f"Image integrity verification failed for '{file_path}': {e}")
        raise TechnicalImageValidationError(
            code="CORRUPT_IMAGE_FILE",
            message=f"Image file is corrupt or unreadable: {str(e)}"
        )

    # 5. Load PIL Image into RGB memory
    try:
        image = Image.open(path)
        image.load()  # Force reading pixel data
        if image.mode != "RGB":
            image = image.convert("RGB")
    except Exception as e:
        raise TechnicalImageValidationError(
            code="IMAGE_DECODE_FAILED",
            message=f"Failed to decode image data into RGB memory: {str(e)}"
        )

    # 6. Dimensions check
    width, height = image.size
    if width < Config.MIN_IMAGE_WIDTH or height < Config.MIN_IMAGE_HEIGHT:
        raise TechnicalImageValidationError(
            code="INVALID_DIMENSIONS",
            message=f"Image dimensions ({width}x{height}) are below minimum required resolution ({Config.MIN_IMAGE_WIDTH}x{Config.MIN_IMAGE_HEIGHT})."
        )

    logger.info(f"[INFO] Technical image validation PASSED ({width}x{height}, {file_size} bytes)")
    return image, str(path)
