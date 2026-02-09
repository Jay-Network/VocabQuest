#!/usr/bin/env python3
"""
VocabQuest Audio Generator

Generates OGG Vorbis pronunciation audio for all words in the vocabulary database
using Microsoft Edge TTS (neural voices, free).

Usage:
    cd data-pipeline
    source venv/bin/activate
    python generate_audio.py                         # Generate all
    python generate_audio.py --limit 500             # Generate first 500
    python generate_audio.py --voice en-US-EmmaNeural  # Use different voice
    python generate_audio.py --workers 5             # Fewer concurrent requests
    python generate_audio.py --report-only           # Just show stats

Prerequisites:
    pip install edge-tts
    ffmpeg must be installed (apt install ffmpeg)

License: Internal (JWorks)
"""

import argparse
import asyncio
import logging
import os
import sqlite3
import subprocess
import sys
import time
from pathlib import Path
from typing import List, Tuple

import edge_tts

# ---------------------------------------------------------------------------
# Constants
# ---------------------------------------------------------------------------

DEFAULT_DB = "../shared-core/src/commonMain/resources/vocabquest.db"
DEFAULT_OUTPUT_DIR = "../shared-core/src/commonMain/resources/audio"
DEFAULT_VOICE = "en-US-AndrewNeural"
DEFAULT_WORKERS = 10
OGG_QUALITY = "3"  # libvorbis quality (0-10, 3 ≈ 112kbps, good for speech)

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    datefmt="%H:%M:%S",
)
log = logging.getLogger("vocabquest-audio")


# ---------------------------------------------------------------------------
# Word loading
# ---------------------------------------------------------------------------

def load_words(db_path: str, limit: int = 0) -> List[Tuple[int, str]]:
    """Load words from the vocabulary database, ordered by frequency rank."""
    conn = sqlite3.connect(db_path)
    query = "SELECT id, word FROM word ORDER BY frequency_rank"
    if limit > 0:
        query += f" LIMIT {limit}"
    words = conn.execute(query).fetchall()
    conn.close()
    return words


# ---------------------------------------------------------------------------
# Audio generation
# ---------------------------------------------------------------------------

async def generate_one(
    word: str,
    output_dir: Path,
    voice: str,
    semaphore: asyncio.Semaphore,
) -> Tuple[str, bool, int]:
    """Generate audio for a single word. Returns (word, success, file_size)."""
    ogg_path = output_dir / f"{word}.ogg"

    # Skip if already exists (resume capability)
    if ogg_path.exists() and ogg_path.stat().st_size > 100:
        return word, True, ogg_path.stat().st_size

    mp3_path = output_dir / f"{word}.mp3"

    async with semaphore:
        try:
            # Generate MP3 with edge-tts
            communicate = edge_tts.Communicate(word, voice)
            await communicate.save(str(mp3_path))

            # Convert MP3 → OGG Vorbis
            result = subprocess.run(
                [
                    "ffmpeg", "-y", "-i", str(mp3_path),
                    "-c:a", "libvorbis", "-q:a", OGG_QUALITY,
                    str(ogg_path),
                ],
                capture_output=True,
                timeout=30,
            )

            # Clean up MP3
            if mp3_path.exists():
                mp3_path.unlink()

            if result.returncode != 0 or not ogg_path.exists():
                return word, False, 0

            return word, True, ogg_path.stat().st_size

        except Exception as e:
            # Clean up on error
            for p in [mp3_path, ogg_path]:
                if p.exists():
                    p.unlink()
            return word, False, 0


async def generate_batch(
    words: List[Tuple[int, str]],
    output_dir: Path,
    voice: str,
    workers: int,
) -> Tuple[int, int, int]:
    """Generate audio for a batch of words. Returns (success, failed, total_bytes)."""
    semaphore = asyncio.Semaphore(workers)
    total = len(words)
    success = 0
    failed = 0
    total_bytes = 0
    failed_words = []

    start_time = time.time()
    batch_size = 100

    for batch_start in range(0, total, batch_size):
        batch_end = min(batch_start + batch_size, total)
        batch = words[batch_start:batch_end]

        tasks = [
            generate_one(word, output_dir, voice, semaphore)
            for _, word in batch
        ]
        results = await asyncio.gather(*tasks)

        for word, ok, size in results:
            if ok:
                success += 1
                total_bytes += size
            else:
                failed += 1
                failed_words.append(word)

        elapsed = time.time() - start_time
        done = batch_end
        rate = done / elapsed if elapsed > 0 else 0
        eta = (total - done) / rate if rate > 0 else 0
        log.info(
            f"  Progress: {done}/{total} ({done/total*100:.1f}%) | "
            f"OK: {success} | Failed: {failed} | "
            f"Size: {total_bytes/1024/1024:.1f} MB | "
            f"Rate: {rate:.1f} words/s | ETA: {eta/60:.1f} min"
        )

    if failed_words:
        log.warning(f"  Failed words ({len(failed_words)}): {failed_words[:20]}...")

    return success, failed, total_bytes


# ---------------------------------------------------------------------------
# Verification
# ---------------------------------------------------------------------------

def verify_audio(output_dir: Path, expected_count: int) -> dict:
    """Verify generated audio files."""
    ogg_files = list(output_dir.glob("*.ogg"))
    total_size = sum(f.stat().st_size for f in ogg_files)

    sizes = [f.stat().st_size for f in ogg_files]
    avg_size = sum(sizes) / len(sizes) if sizes else 0
    min_size = min(sizes) if sizes else 0
    max_size = max(sizes) if sizes else 0

    # Check for suspiciously small files (likely errors)
    tiny_files = [f.name for f in ogg_files if f.stat().st_size < 500]

    stats = {
        "total_files": len(ogg_files),
        "expected_files": expected_count,
        "coverage_pct": round(len(ogg_files) / max(expected_count, 1) * 100, 1),
        "total_size_mb": round(total_size / 1024 / 1024, 1),
        "avg_size_kb": round(avg_size / 1024, 1),
        "min_size_kb": round(min_size / 1024, 1),
        "max_size_kb": round(max_size / 1024, 1),
        "tiny_files": tiny_files[:10],
    }
    return stats


def print_report(stats: dict):
    """Print audio verification report."""
    print("\n" + "=" * 60)
    print("VocabQuest Audio Verification Report")
    print("=" * 60)
    print(f"\nTotal audio files:   {stats['total_files']:,}")
    print(f"Expected files:      {stats['expected_files']:,}")
    print(f"Coverage:            {stats['coverage_pct']}%")
    print(f"Total size:          {stats['total_size_mb']} MB")
    print(f"Avg file size:       {stats['avg_size_kb']} KB")
    print(f"Min file size:       {stats['min_size_kb']} KB")
    print(f"Max file size:       {stats['max_size_kb']} KB")

    if stats["tiny_files"]:
        print(f"\nSuspiciously small files: {stats['tiny_files']}")

    checks = [
        ("Coverage >= 99%", stats["coverage_pct"] >= 99),
        ("Total size < 200 MB", stats["total_size_mb"] < 200),
        ("No tiny files", len(stats["tiny_files"]) == 0),
    ]

    print("\nValidation Checks:")
    all_pass = True
    for name, passed in checks:
        symbol = "+" if passed else "!"
        status = "PASS" if passed else "FAIL"
        print(f"  [{symbol}] {name}: {status}")
        if not passed:
            all_pass = False

    print("\n" + ("ALL CHECKS PASSED" if all_pass else "SOME CHECKS FAILED"))
    print("=" * 60)
    return all_pass


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main():
    parser = argparse.ArgumentParser(description="Generate pronunciation audio for VocabQuest")
    parser.add_argument("--db", default=DEFAULT_DB, help="Path to vocabquest.db")
    parser.add_argument("--output", "-o", default=DEFAULT_OUTPUT_DIR, help="Output directory for audio files")
    parser.add_argument("--voice", default=DEFAULT_VOICE, help=f"Edge TTS voice (default: {DEFAULT_VOICE})")
    parser.add_argument("--workers", "-w", type=int, default=DEFAULT_WORKERS, help="Concurrent workers")
    parser.add_argument("--limit", type=int, default=0, help="Limit number of words (0 = all)")
    parser.add_argument("--report-only", action="store_true", help="Only verify existing audio")
    args = parser.parse_args()

    output_dir = Path(args.output)
    output_dir.mkdir(parents=True, exist_ok=True)

    # Load words
    words = load_words(args.db, args.limit)
    log.info(f"Loaded {len(words)} words from database")

    if args.report_only:
        stats = verify_audio(output_dir, len(words))
        print_report(stats)
        return

    # Check how many already exist (resume)
    existing = sum(1 for _, w in words if (output_dir / f"{w}.ogg").exists())
    if existing > 0:
        log.info(f"  {existing} already generated (will skip)")

    # Generate
    log.info(f"Generating audio: voice={args.voice}, workers={args.workers}")
    start = time.time()

    success, failed, total_bytes = asyncio.run(
        generate_batch(words, output_dir, args.voice, args.workers)
    )

    elapsed = time.time() - start
    log.info(f"\nGeneration complete in {elapsed/60:.1f} minutes")
    log.info(f"  Success: {success}, Failed: {failed}, Size: {total_bytes/1024/1024:.1f} MB")

    # Verify
    stats = verify_audio(output_dir, len(words))
    all_pass = print_report(stats)

    sys.exit(0 if all_pass else 1)


if __name__ == "__main__":
    main()
