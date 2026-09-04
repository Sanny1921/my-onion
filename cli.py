#!/usr/bin/env python3
import sys
import argparse
import json
import logging
from pathlib import Path

from app.service import InspectionService
from app.schemas import InspectionResult


def setup_logging(verbose: bool = False):
    """Configures logging for CLI execution."""
    level = logging.INFO if verbose else logging.WARNING
    logging.basicConfig(
        level=level,
        format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
        datefmt="%H:%M:%S",
    )


def print_human_readable_result(result: InspectionResult):
    """Prints beautiful formatted terminal inspection report."""
    print("\n" + "=" * 70)
    print("      SIH26031 REAL ONION QUALITY INSPECTION (V1 TERMINAL)")
    print("=" * 70)

    insp = result.inspection
    print(f"\nInspection ID : {insp.id}")
    print(f"Image File    : {insp.image_path}")
    print(f"Lot ID        : {insp.lot_id}")
    print(f"Timestamp     : {insp.timestamp}")

    if not result.success and result.error:
        print("\n" + "-" * 70)
        print("❌ INSPECTION FAILED / REJECTED")
        print("-" * 70)
        print(f"Error Code    : {result.error.get('code')}")
        print(f"Message       : {result.error.get('message')}")
        print(f"Analysis Status: {result.final_assessment.analysis_status.value}")
        print("=" * 70 + "\n")
        return

    print("\n" + "-" * 70)
    print("SAMPLE INFORMATION")
    print("-" * 70)
    declared_count = insp.user_provided_sample_count if insp.user_provided_sample_count is not None else "Not Specified"
    analyzable = result.backend_metrics.analyzable_count if result.backend_metrics else 0
    print(f"Declared Sample Count   : {declared_count}")
    print(f"Analyzable Visible Count: {analyzable} onions")

    if result.ai_observation:
        obs = result.ai_observation
        print("\n" + "-" * 70)
        print("AI VISUAL OBSERVATIONS")
        print("-" * 70)
        print(f"AI Confidence Signal    : {obs.ai_confidence_signal:.2f} (Visual Clarity Score)")
        print(f"Is Onion Sample         : {'Yes' if obs.is_onion_sample else 'No'}")
        print(f"Is Sample Usable        : {'Yes' if obs.is_sample_usable else 'No'}")
        
        print("\nVisual Defect Counts:")
        if result.backend_metrics:
            print(f"  Healthy (Calculated)  : {result.backend_metrics.healthy_count}")
        print(f"  Damaged               : {obs.defects.damaged}")
        print(f"  Rotten                : {obs.defects.rotten}")
        print(f"  Sprouted              : {obs.defects.sprouted}")
        print(f"  Undersized            : {obs.defects.undersized}")
        print(f"  Diseased              : {obs.defects.diseased}")
        print(f"  Other Visible Defects : {obs.defects.other}")

        if obs.observations:
            print("\nQualitative Observations:")
            for item in obs.observations:
                print(f"  - {item}")

    if result.backend_metrics:
        bm = result.backend_metrics
        print("\n" + "-" * 70)
        print("BACKEND QUALITY METRICS")
        print("-" * 70)
        print(f"Healthy Percentage      : {bm.healthy_percentage:.2f}%")
        print(f"Defect Percentage       : {bm.defect_percentage:.2f}%")
        print(f"Count Math Consistent   : {'Yes' if bm.count_consistent else 'NO (INCONSISTENT)'}")
        print(f"Physical Size Status    : {bm.size_measurement_status}")

        if bm.defect_breakdown_percentages:
            print("\nDefect Breakdown:")
            for defect_name, pct in bm.defect_breakdown_percentages.items():
                if pct > 0:
                    print(f"  {defect_name.capitalize():21s} : {pct:.2f}%")

    if result.grading:
        gr = result.grading
        print("\n" + "-" * 70)
        print("GRADING EVALUATION")
        print("-" * 70)
        print(f"Grading Status          : {gr.status.value}")
        print(f"Assigned Grade          : {gr.grade if gr.grade else 'None'}")
        print(f"Notes                   : {gr.notes}")

    fa = result.final_assessment
    print("\n" + "-" * 70)
    print("FINAL ASSESSMENT & VERIFICATION")
    print("-" * 70)
    print(f"Analysis Status         : {fa.analysis_status.value}")
    print(f"Grading Status          : {fa.grading_status.value}")
    print(f"Inspector Review Status : {fa.inspector_status.value}")
    print("=" * 70)
    print("Inspection Completed.")
    print("=" * 70 + "\n")


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="SIH26031 Real Onion Quality Inspection Backend CLI",
        formatter_class=argparse.RawTextHelpFormatter,
    )
    subparsers = parser.add_subparsers(dest="command", help="Available subcommands")

    # Command: inspect
    inspect_parser = subparsers.add_parser("inspect", help="Inspect an onion sample image")
    inspect_parser.add_argument("image_path", nargs="?", help="Path to onion sample image file")
    inspect_parser.add_argument("--image", "-i", dest="image_opt", help="Alternative flag for image path")
    inspect_parser.add_argument("--sample-count", "-s", type=int, default=None, help="User-declared sample count (e.g. 50)")
    inspect_parser.add_argument("--lot-id", "-l", type=str, default="LOT-DEFAULT", help="Lot or batch tracking ID")
    inspect_parser.add_argument("--json", "-j", action="store_true", help="Output machine-readable JSON to stdout")
    inspect_parser.add_argument("--output", "-o", type=str, help="Save JSON response output to specified file path")
    inspect_parser.add_argument("--verbose", "-v", action="store_true", help="Enable verbose step-by-step pipeline logging")

    # Allow direct execution: `python cli.py --image path/to/image.jpg`
    parser.add_argument("--image", "-i", dest="root_image", help="Image path for direct execution")
    parser.add_argument("--sample-count", "-s", dest="root_sample_count", type=int, default=None, help="User-declared sample count")
    parser.add_argument("--lot-id", "-l", dest="root_lot_id", type=str, default="LOT-DEFAULT", help="Lot tracking ID")
    parser.add_argument("--json", "-j", dest="root_json", action="store_true", help="Output machine-readable JSON")
    parser.add_argument("--output", "-o", dest="root_output", type=str, help="Save JSON response output to specified file path")
    parser.add_argument("--verbose", "-v", dest="root_verbose", action="store_true", help="Enable verbose logging")

    return parser


def main():
    parser = build_parser()
    args = parser.parse_args()

    # Determine execution arguments whether invoked as `inspect` subcommand or direct flags
    image_path = getattr(args, "image_path", None) or getattr(args, "image_opt", None) or getattr(args, "root_image", None)
    sample_count = getattr(args, "sample_count", None) if getattr(args, "sample_count", None) is not None else getattr(args, "root_sample_count", None)
    lot_id = getattr(args, "lot_id", "LOT-DEFAULT") if getattr(args, "lot_id", "LOT-DEFAULT") != "LOT-DEFAULT" else getattr(args, "root_lot_id", "LOT-DEFAULT")
    is_json = getattr(args, "json", False) or getattr(args, "root_json", False)
    output_file = getattr(args, "output", None) or getattr(args, "root_output", None)
    is_verbose = getattr(args, "verbose", False) or getattr(args, "root_verbose", False)

    if not image_path:
        parser.print_help()
        print("\n❌ Error: Please provide an image file path (e.g. `python cli.py inspect path/to/image.jpg` or `python cli.py --image path/to/image.jpg`)")
        sys.exit(1)

    setup_logging(verbose=is_verbose)

    # Initialize inspection service & run pipeline
    service = InspectionService()
    result = service.run_inspection(
        image_path=image_path,
        user_provided_sample_count=sample_count,
        lot_id=lot_id,
    )

    json_str = json.dumps(result.model_dump(), indent=2)

    if output_file:
        out_path = Path(output_file)
        out_path.parent.mkdir(parents=True, exist_ok=True)
        out_path.write_text(json_str)
        print(f"✅ Saved inspection JSON response to: {out_path}")

    if is_json or not output_file:
        if is_json:
            print(json_str)
        else:
            print_human_readable_result(result)

    # Exit with code 0 if successful or completed, non-zero if technical error
    if not result.success and result.error and result.error.get("code") in ["FILE_NOT_FOUND", "CORRUPT_IMAGE_FILE", "MISSING_API_KEY", "CLIENT_INIT_FAILED"]:
        sys.exit(2)
    sys.exit(0)


if __name__ == "__main__":
    main()
