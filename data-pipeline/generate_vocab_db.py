#!/usr/bin/env python3
"""
VocabQuest Vocabulary Database Generator

Generates a SQLite database with 10,000 English words for the VocabQuest app.

Sources:
  - NLTK WordNet: definitions, examples, synonyms/antonyms
  - NLTK Brown Corpus: word frequency data
  - NLTK CMU Pronouncing Dictionary: phonetic transcription (ARPAbet -> IPA)
  - Optional: Free Dictionary API for enhanced definitions and audio URLs

Usage:
    cd data-pipeline
    source venv/bin/activate
    python generate_vocab_db.py
    python generate_vocab_db.py --output ../shared-core/src/commonMain/resources/vocabquest.db
    python generate_vocab_db.py --enrich-api    # Also fetch from Free Dictionary API
    python generate_vocab_db.py --count 5000    # Generate fewer words for testing

License: Internal (JWorks)
"""

import argparse
import json
import logging
import math
import os
import re
import sqlite3
import sys
import time
from collections import Counter, defaultdict
from pathlib import Path
from typing import Dict, List, Optional, Set, Tuple

import nltk
from nltk.corpus import brown, cmudict, wordnet

# ---------------------------------------------------------------------------
# Constants
# ---------------------------------------------------------------------------

DEFAULT_OUTPUT = "../shared-core/src/commonMain/resources/vocabquest.db"
DEFAULT_WORD_COUNT = 10000
MIN_WORD_LENGTH = 2
MAX_WORD_LENGTH = 25

# CEFR level boundaries (by frequency rank, 1-indexed)
# Target distribution: A1+A2 ~30%, B1+B2 ~40%, C1+C2 ~30%
CEFR_BOUNDARIES = [
    (1000, "A1"),    # ranks 1-1000
    (3000, "A2"),    # ranks 1001-3000
    (5000, "B1"),    # ranks 3001-5000
    (7000, "B2"),    # ranks 5001-7000
    (9000, "C1"),    # ranks 7001-9000
    (99999, "C2"),   # ranks 9001+
]

# Map WordNet POS tags to human-readable
WN_POS_MAP = {
    wordnet.NOUN: "noun",
    wordnet.VERB: "verb",
    wordnet.ADJ: "adj",
    wordnet.ADJ_SAT: "adj",
    wordnet.ADV: "adv",
}

# ARPAbet to IPA conversion
ARPABET_TO_IPA = {
    "AA": "\u0251\u02D0", "AE": "\u00E6", "AH": "\u028C", "AO": "\u0254\u02D0",
    "AW": "a\u028A", "AY": "a\u026A", "B": "b", "CH": "t\u0283",
    "D": "d", "DH": "\u00F0", "EH": "\u025B", "ER": "\u025C\u02D0r",
    "EY": "e\u026A", "F": "f", "G": "\u0261", "HH": "h",
    "IH": "\u026A", "IY": "i\u02D0", "JH": "d\u0292", "K": "k",
    "L": "l", "M": "m", "N": "n", "NG": "\u014B",
    "OW": "o\u028A", "OY": "\u0254\u026A", "P": "p", "R": "r",
    "S": "s", "SH": "\u0283", "T": "t", "TH": "\u03B8",
    "UH": "\u028A", "UW": "u\u02D0", "V": "v", "W": "w",
    "Y": "j", "Z": "z", "ZH": "\u0292",
}

# Words to always exclude (too basic/function or offensive)
EXCLUDE_WORDS = {
    "a", "an", "the", "i", "me", "my", "we", "us", "our", "you", "your",
    "he", "him", "his", "she", "her", "it", "its", "they", "them", "their",
    "am", "is", "are", "was", "were", "be", "been", "being",
    "do", "does", "did", "has", "have", "had", "shall", "will", "would",
    "should", "may", "might", "must", "can", "could",
    "not", "no", "nor", "so", "if", "or", "and", "but", "as", "at",
    "by", "for", "from", "in", "of", "on", "to", "up", "with",
    "that", "this", "these", "those", "what", "which", "who", "whom",
    "how", "when", "where", "why", "than", "then", "very", "just",
}

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    datefmt="%H:%M:%S",
)
log = logging.getLogger("vocabquest")


# ---------------------------------------------------------------------------
# ARPAbet -> IPA conversion
# ---------------------------------------------------------------------------

def arpabet_to_ipa(phones: List[str]) -> str:
    """Convert a CMU ARPAbet pronunciation to IPA."""
    ipa_parts: List[str] = []
    for phone in phones:
        # Strip stress digit (0=no stress, 1=primary, 2=secondary)
        base = phone.rstrip("012")
        stress = phone[-1] if phone[-1] in "012" else None

        ipa_char = ARPABET_TO_IPA.get(base, base.lower())

        # Handle stress markers
        if stress == "1":
            ipa_parts.append("\u02C8" + ipa_char)  # primary stress
        elif stress == "2":
            ipa_parts.append("\u02CC" + ipa_char)  # secondary stress
        else:
            # Unstressed AH -> schwa
            if base == "AH" and stress == "0":
                ipa_parts.append("\u0259")
            else:
                ipa_parts.append(ipa_char)

    return "/" + "".join(ipa_parts) + "/"


# ---------------------------------------------------------------------------
# Word selection and scoring
# ---------------------------------------------------------------------------

def compute_brown_frequencies() -> Tuple[Counter, Dict[str, str]]:
    """Get word frequencies and primary POS from the Brown corpus."""
    log.info("Computing Brown corpus frequencies and POS tags...")
    freq = Counter()
    pos_counts: Dict[str, Counter] = defaultdict(Counter)

    # Brown POS tag -> simplified POS mapping
    tag_map = {
        "NN": "noun", "NNS": "noun", "NP": "noun", "NPS": "noun",
        "VB": "verb", "VBD": "verb", "VBG": "verb", "VBN": "verb", "VBZ": "verb",
        "JJ": "adj", "JJR": "adj", "JJS": "adj", "JJT": "adj",
        "RB": "adv", "RBR": "adv", "RBT": "adv",
    }

    for word, tag in brown.tagged_words():
        w = word.lower()
        if w.isalpha() and len(w) >= MIN_WORD_LENGTH:
            freq[w] += 1
            simple_pos = tag_map.get(tag)
            if simple_pos:
                pos_counts[w][simple_pos] += 1

    # For each word, pick the most common POS
    primary_pos: Dict[str, str] = {}
    for w, counts in pos_counts.items():
        primary_pos[w] = counts.most_common(1)[0][0]

    log.info(f"  Found {len(freq)} unique words, {len(primary_pos)} with POS tags")
    return freq, primary_pos


def get_wordnet_words() -> Dict[str, dict]:
    """Get all single-word lemmas from WordNet with their metadata."""
    log.info("Extracting WordNet lemmas...")
    words: Dict[str, dict] = {}

    for lemma_name in wordnet.all_lemma_names():
        # Filter: single word, alphabetic, reasonable length
        if "_" in lemma_name or "-" in lemma_name:
            continue
        if not lemma_name.isalpha():
            continue
        if len(lemma_name) < MIN_WORD_LENGTH or len(lemma_name) > MAX_WORD_LENGTH:
            continue

        word = lemma_name.lower()
        if word in EXCLUDE_WORDS:
            continue

        synsets = wordnet.synsets(word)
        if not synsets:
            continue

        words[word] = {
            "synset_count": len(synsets),
            "synsets": synsets,
        }

    log.info(f"  Found {len(words)} candidate words in WordNet")
    return words


def filter_to_base_forms(candidates: List[dict]) -> List[dict]:
    """Remove inflected forms where the base form is also a candidate."""
    log.info("Filtering inflected forms...")
    word_set = {c["word"] for c in candidates}
    filtered = []
    removed = 0

    for c in candidates:
        word = c["word"]
        is_inflected = False
        for pos in [wordnet.VERB, wordnet.NOUN, wordnet.ADJ, wordnet.ADV]:
            base = wordnet.morphy(word, pos)
            if base and base != word and base in word_set:
                is_inflected = True
                break
        if not is_inflected:
            filtered.append(c)
        else:
            removed += 1

    log.info(f"  Removed {removed} inflected forms, kept {len(filtered)}")
    return filtered


def select_words(
    wn_words: Dict[str, dict],
    brown_freq: Counter,
    cmu_dict: Dict[str, list],
    count: int,
) -> List[dict]:
    """Score, filter, and select the top N words."""
    log.info(f"Scoring and selecting top {count} words...")

    # Use log-frequency to compress range (avoids top words dominating)
    max_log = math.log(max(brown_freq.values()) + 1) if brown_freq else 1.0

    scored = []
    for word, meta in wn_words.items():
        bf = brown_freq.get(word, 0)
        bf_norm = math.log(bf + 1) / max_log

        # Polysemy score (more meanings = more common)
        poly_norm = min(meta["synset_count"] / 30.0, 1.0)

        # Bonus for having pronunciation
        cmu_bonus = 0.03 if word in cmu_dict else 0.0

        # Length preference: slight bonus for 4-12 char words
        length = len(word)
        if length <= 3:
            len_bonus = -0.02
        elif 4 <= length <= 12:
            len_bonus = 0.01
        else:
            len_bonus = -0.01

        score = bf_norm * 0.60 + poly_norm * 0.35 + cmu_bonus + len_bonus

        scored.append({
            "word": word,
            "score": score,
            "brown_freq": bf,
            "synset_count": meta["synset_count"],
            "synsets": meta["synsets"],
        })

    # Sort by score descending
    scored.sort(key=lambda x: x["score"], reverse=True)

    # Take extra candidates then filter inflected forms
    overselect = min(int(count * 1.4), len(scored))
    candidates = scored[:overselect]
    filtered = filter_to_base_forms(candidates)

    # If still not enough after filtering, add more from remaining
    if len(filtered) < count:
        existing = {c["word"] for c in filtered}
        for c in scored[overselect:]:
            if c["word"] not in existing:
                # Quick base-form check
                is_inflected = False
                for pos in [wordnet.VERB, wordnet.NOUN, wordnet.ADJ]:
                    base = wordnet.morphy(c["word"], pos)
                    if base and base != c["word"] and base in existing:
                        is_inflected = True
                        break
                if not is_inflected:
                    filtered.append(c)
                    existing.add(c["word"])
            if len(filtered) >= count:
                break

    selected = filtered[:count]
    log.info(f"  Selected {len(selected)} words (score range: "
             f"{selected[0]['score']:.4f} - {selected[-1]['score']:.4f})")
    return selected


# ---------------------------------------------------------------------------
# CEFR assignment
# ---------------------------------------------------------------------------

def assign_cefr(rank: int) -> str:
    """Assign CEFR level based on frequency rank (1-indexed)."""
    for boundary, level in CEFR_BOUNDARIES:
        if rank <= boundary:
            return level
    return "C2"


# ---------------------------------------------------------------------------
# Word enrichment
# ---------------------------------------------------------------------------

def enrich_word(
    word_data: dict,
    cmu_dict: Dict[str, list],
    brown_pos: Dict[str, str],
) -> dict:
    """Enrich a word with definition, POS, phonetic, examples, synonyms."""
    word = word_data["word"]
    synsets = word_data["synsets"]

    # --- Determine primary POS from Brown corpus, fallback to WordNet ---
    pos = brown_pos.get(word)
    pos_to_wn = {"noun": wordnet.NOUN, "verb": wordnet.VERB, "adj": wordnet.ADJ, "adv": wordnet.ADV}

    # Reorder synsets: prefer synsets matching the Brown POS
    if pos and pos in pos_to_wn:
        target_wn = pos_to_wn[pos]
        pos_synsets = [ss for ss in synsets if ss.pos() in (target_wn, wordnet.ADJ_SAT) or
                       (target_wn == wordnet.ADJ and ss.pos() == wordnet.ADJ_SAT)]
        other = [ss for ss in synsets if ss not in pos_synsets]
        synsets = pos_synsets + other if pos_synsets else synsets

    best_ss = synsets[0]
    definition = best_ss.definition()

    # POS: use Brown if available, else WordNet
    if not pos:
        pos = WN_POS_MAP.get(best_ss.pos(), "noun")

    # --- Examples from all synsets (deduplicated) ---
    examples = []
    seen = set()
    for ss in synsets[:5]:  # check top 5 senses
        for ex in ss.examples():
            if ex not in seen:
                examples.append(ex)
                seen.add(ex)
            if len(examples) >= 3:
                break
        if len(examples) >= 3:
            break

    # --- IPA pronunciation from CMU dict ---
    phonetic = None
    if word in cmu_dict:
        phones = cmu_dict[word][0]  # first pronunciation variant
        phonetic = arpabet_to_ipa(phones)

    # --- Synonyms and antonyms from WordNet ---
    synonyms = set()
    antonyms = set()
    for ss in synsets[:5]:
        for lemma in ss.lemmas():
            name = lemma.name().replace("_", " ")
            if name.lower() != word:
                synonyms.add(name)
            for ant in lemma.antonyms():
                antonyms.add(ant.name().replace("_", " "))
        if len(synonyms) >= 5:
            break

    # --- Additional definitions for metadata ---
    alt_definitions = []
    alt_pos_set = set()
    for ss in synsets[1:4]:
        p = WN_POS_MAP.get(ss.pos(), "noun")
        alt_pos_set.add(p)
        alt_definitions.append({"pos": p, "definition": ss.definition()})

    metadata = {
        "synonyms": sorted(synonyms)[:5],
        "antonyms": sorted(antonyms)[:3],
        "alt_definitions": alt_definitions,
        "all_pos": sorted(set([pos]) | alt_pos_set),
    }

    return {
        "word": word,
        "definition": definition,
        "pos": pos,
        "phonetic": phonetic,
        "examples": examples,
        "metadata": json.dumps(metadata, ensure_ascii=False),
    }


# ---------------------------------------------------------------------------
# Optional: Free Dictionary API enrichment
# ---------------------------------------------------------------------------

def enrich_from_api(word: str, cache_dir: Path) -> Optional[dict]:
    """Fetch additional data from Free Dictionary API (dictionaryapi.dev)."""
    import requests

    cache_file = cache_dir / f"{word}.json"
    if cache_file.exists():
        try:
            return json.loads(cache_file.read_text())
        except json.JSONDecodeError:
            pass

    url = f"https://api.dictionaryapi.dev/api/v2/entries/en/{word}"
    try:
        resp = requests.get(url, timeout=10)
        if resp.status_code == 200:
            data = resp.json()
            cache_file.write_text(json.dumps(data, ensure_ascii=False))
            return data
        elif resp.status_code == 429:
            log.warning(f"  Rate limited on '{word}', sleeping 5s...")
            time.sleep(5)
            return None
        else:
            return None
    except Exception:
        return None


def apply_api_enrichment(enriched: dict, api_data: list) -> dict:
    """Merge Free Dictionary API data into enriched word data."""
    if not api_data or not isinstance(api_data, list):
        return enriched

    entry = api_data[0]

    # Better phonetic (IPA) from API
    if "phonetic" in entry and entry["phonetic"]:
        enriched["phonetic"] = entry["phonetic"]
    elif "phonetics" in entry:
        for ph in entry["phonetics"]:
            if ph.get("text"):
                enriched["phonetic"] = ph["text"]
                break

    # Audio URL
    if "phonetics" in entry:
        for ph in entry["phonetics"]:
            if ph.get("audio"):
                enriched["audio_url"] = ph["audio"]
                break

    # Better definition if available
    if "meanings" in entry and entry["meanings"]:
        meaning = entry["meanings"][0]
        if meaning.get("definitions"):
            api_def = meaning["definitions"][0].get("definition", "")
            if len(api_def) > len(enriched.get("definition", "")):
                enriched["definition"] = api_def

            # API example
            api_example = meaning["definitions"][0].get("example")
            if api_example and api_example not in enriched.get("examples", []):
                enriched.setdefault("examples", []).insert(0, api_example)

    return enriched


# ---------------------------------------------------------------------------
# Database generation
# ---------------------------------------------------------------------------

SCHEMA_SQL = """
CREATE TABLE IF NOT EXISTS word (
    id INTEGER PRIMARY KEY,
    word TEXT NOT NULL UNIQUE,
    definition TEXT NOT NULL,
    pos TEXT NOT NULL,
    cefr_level TEXT,
    frequency_rank INTEGER,
    phonetic TEXT,
    audio_url TEXT,
    etymology TEXT,
    metadata TEXT
);

CREATE TABLE IF NOT EXISTS word_example (
    id INTEGER PRIMARY KEY,
    word_id INTEGER NOT NULL REFERENCES word(id),
    sentence TEXT NOT NULL,
    context TEXT,
    difficulty INTEGER
);

CREATE INDEX IF NOT EXISTS idx_word_cefr ON word(cefr_level);
CREATE INDEX IF NOT EXISTS idx_word_frequency ON word(frequency_rank);
CREATE INDEX IF NOT EXISTS idx_word_pos ON word(pos);
CREATE INDEX IF NOT EXISTS idx_example_word ON word_example(word_id);
"""


def create_database(
    output_path: Path,
    words: List[dict],
) -> Tuple[int, int]:
    """Write enriched words to SQLite database. Returns (word_count, example_count)."""
    log.info(f"Creating database at {output_path}...")

    output_path.parent.mkdir(parents=True, exist_ok=True)
    if output_path.exists():
        output_path.unlink()

    conn = sqlite3.connect(str(output_path))
    conn.execute("PRAGMA journal_mode=WAL")
    conn.execute("PRAGMA synchronous=NORMAL")
    conn.executescript(SCHEMA_SQL)

    word_count = 0
    example_count = 0

    for i, w in enumerate(words, 1):
        # Determine context difficulty from CEFR
        cefr = w.get("cefr_level", "B1")
        cefr_difficulty = {"A1": 1, "A2": 2, "B1": 3, "B2": 4, "C1": 5, "C2": 5}

        conn.execute(
            """INSERT INTO word (id, word, definition, pos, cefr_level,
               frequency_rank, phonetic, audio_url, etymology, metadata)
               VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
            (
                i,
                w["word"],
                w["definition"],
                w["pos"],
                cefr,
                w.get("frequency_rank", i),
                w.get("phonetic"),
                w.get("audio_url"),
                w.get("etymology"),
                w.get("metadata"),
            ),
        )
        word_count += 1

        # Insert example sentences
        for ex in w.get("examples", []):
            conn.execute(
                """INSERT INTO word_example (word_id, sentence, context, difficulty)
                   VALUES (?, ?, ?, ?)""",
                (i, ex, "general", cefr_difficulty.get(cefr, 3)),
            )
            example_count += 1

        if i % 1000 == 0:
            conn.commit()
            log.info(f"  Inserted {i}/{len(words)} words...")

    conn.commit()
    conn.execute("PRAGMA optimize")
    conn.close()

    log.info(f"  Database created: {word_count} words, {example_count} examples")
    return word_count, example_count


# ---------------------------------------------------------------------------
# Verification
# ---------------------------------------------------------------------------

def verify_database(db_path: Path) -> dict:
    """Run verification queries and return statistics."""
    conn = sqlite3.connect(str(db_path))

    stats = {}

    # Total words
    stats["total_words"] = conn.execute("SELECT COUNT(*) FROM word").fetchone()[0]

    # CEFR distribution
    rows = conn.execute(
        "SELECT cefr_level, COUNT(*) FROM word GROUP BY cefr_level ORDER BY cefr_level"
    ).fetchall()
    stats["cefr_distribution"] = {r[0]: r[1] for r in rows}

    # POS distribution
    rows = conn.execute(
        "SELECT pos, COUNT(*) FROM word GROUP BY pos ORDER BY COUNT(*) DESC"
    ).fetchall()
    stats["pos_distribution"] = {r[0]: r[1] for r in rows}

    # Example coverage
    stats["total_examples"] = conn.execute(
        "SELECT COUNT(*) FROM word_example"
    ).fetchone()[0]
    stats["words_with_examples"] = conn.execute(
        "SELECT COUNT(DISTINCT word_id) FROM word_example"
    ).fetchone()[0]
    stats["example_coverage_pct"] = round(
        stats["words_with_examples"] / max(stats["total_words"], 1) * 100, 1
    )

    # Phonetic coverage
    stats["words_with_phonetic"] = conn.execute(
        "SELECT COUNT(*) FROM word WHERE phonetic IS NOT NULL"
    ).fetchone()[0]
    stats["phonetic_coverage_pct"] = round(
        stats["words_with_phonetic"] / max(stats["total_words"], 1) * 100, 1
    )

    # File size
    stats["file_size_mb"] = round(db_path.stat().st_size / (1024 * 1024), 2)

    # Sample words per CEFR level
    stats["samples"] = {}
    for level in ["A1", "A2", "B1", "B2", "C1", "C2"]:
        rows = conn.execute(
            "SELECT word, definition, pos FROM word WHERE cefr_level = ? "
            "ORDER BY frequency_rank LIMIT 5",
            (level,),
        ).fetchall()
        stats["samples"][level] = [
            {"word": r[0], "definition": r[1], "pos": r[2]} for r in rows
        ]

    conn.close()
    return stats


def print_report(stats: dict):
    """Print a formatted verification report."""
    print("\n" + "=" * 70)
    print("VocabQuest Database Verification Report")
    print("=" * 70)

    print(f"\nTotal words:           {stats['total_words']:,}")
    print(f"Total examples:        {stats['total_examples']:,}")
    print(f"Example coverage:      {stats['example_coverage_pct']}%")
    print(f"Phonetic coverage:     {stats['phonetic_coverage_pct']}%")
    print(f"Database size:         {stats['file_size_mb']} MB")

    print("\nCEFR Level Distribution:")
    total = stats["total_words"]
    for level in ["A1", "A2", "B1", "B2", "C1", "C2"]:
        count = stats["cefr_distribution"].get(level, 0)
        pct = count / total * 100 if total else 0
        bar = "#" * int(pct / 2)
        print(f"  {level}: {count:>5} ({pct:>5.1f}%) {bar}")

    print("\nPOS Distribution:")
    for pos, count in stats["pos_distribution"].items():
        pct = count / total * 100 if total else 0
        print(f"  {pos:<6}: {count:>5} ({pct:>5.1f}%)")

    print("\nSample Words:")
    for level in ["A1", "A2", "B1", "B2", "C1", "C2"]:
        samples = stats["samples"].get(level, [])
        print(f"\n  [{level}]")
        for s in samples[:3]:
            defn = s["definition"][:60] + "..." if len(s["definition"]) > 60 else s["definition"]
            print(f"    {s['word']:<20} ({s['pos']}) - {defn}")

    print("\n" + "=" * 70)

    # Check pass/fail criteria
    checks = []
    checks.append(("Word count >= 10,000", stats["total_words"] >= 10000))
    checks.append(("Example coverage >= 60%", stats["example_coverage_pct"] >= 60))
    checks.append(("Database size < 10 MB", stats["file_size_mb"] < 10))
    checks.append(("All CEFR levels present", len(stats["cefr_distribution"]) == 6))

    a1a2 = stats["cefr_distribution"].get("A1", 0) + stats["cefr_distribution"].get("A2", 0)
    b1b2 = stats["cefr_distribution"].get("B1", 0) + stats["cefr_distribution"].get("B2", 0)
    c1c2 = stats["cefr_distribution"].get("C1", 0) + stats["cefr_distribution"].get("C2", 0)
    checks.append(("A1+A2 ~30%", 20 <= a1a2 / total * 100 <= 40 if total else False))
    checks.append(("B1+B2 ~40%", 30 <= b1b2 / total * 100 <= 50 if total else False))
    checks.append(("C1+C2 ~30%", 20 <= c1c2 / total * 100 <= 40 if total else False))

    print("\nValidation Checks:")
    all_pass = True
    for name, passed in checks:
        status = "PASS" if passed else "FAIL"
        symbol = "+" if passed else "!"
        print(f"  [{symbol}] {name}: {status}")
        if not passed:
            all_pass = False

    print("\n" + ("ALL CHECKS PASSED" if all_pass else "SOME CHECKS FAILED"))
    print("=" * 70)

    return all_pass


# ---------------------------------------------------------------------------
# Main pipeline
# ---------------------------------------------------------------------------

def main():
    parser = argparse.ArgumentParser(description="Generate VocabQuest vocabulary database")
    parser.add_argument(
        "--output", "-o",
        default=DEFAULT_OUTPUT,
        help=f"Output database path (default: {DEFAULT_OUTPUT})",
    )
    parser.add_argument(
        "--count", "-n",
        type=int,
        default=DEFAULT_WORD_COUNT,
        help=f"Number of words to generate (default: {DEFAULT_WORD_COUNT})",
    )
    parser.add_argument(
        "--enrich-api",
        action="store_true",
        help="Also fetch data from Free Dictionary API (slow, ~3 hours for 10k)",
    )
    parser.add_argument(
        "--api-batch",
        type=int,
        default=0,
        help="Only enrich words in this rank range batch (e.g., 1000 = words 1-1000)",
    )
    parser.add_argument(
        "--report-only",
        action="store_true",
        help="Only run verification on existing database",
    )
    args = parser.parse_args()

    output_path = Path(args.output)

    # Report-only mode
    if args.report_only:
        if not output_path.exists():
            log.error(f"Database not found at {output_path}")
            sys.exit(1)
        stats = verify_database(output_path)
        print_report(stats)
        sys.exit(0)

    start_time = time.time()
    log.info("=" * 50)
    log.info("VocabQuest Vocabulary Database Generator")
    log.info("=" * 50)

    # Phase 1: Load data sources
    log.info("\n--- Phase 1: Loading data sources ---")
    brown_freq, brown_pos = compute_brown_frequencies()

    cmu_entries = cmudict.dict()
    log.info(f"CMU Pronouncing Dictionary: {len(cmu_entries)} entries")

    wn_words = get_wordnet_words()

    # Phase 2: Select and rank words
    log.info("\n--- Phase 2: Selecting words ---")
    selected = select_words(wn_words, brown_freq, cmu_entries, args.count)

    # Phase 3: Enrich words
    log.info("\n--- Phase 3: Enriching words ---")
    enriched_words = []
    cache_dir = Path("cache")
    cache_dir.mkdir(exist_ok=True)

    for i, word_data in enumerate(selected, 1):
        rank = i
        cefr = assign_cefr(rank)

        enriched = enrich_word(word_data, cmu_entries, brown_pos)
        enriched["cefr_level"] = cefr
        enriched["frequency_rank"] = rank

        # Optional API enrichment
        if args.enrich_api:
            if args.api_batch == 0 or rank <= args.api_batch:
                api_data = enrich_from_api(word_data["word"], cache_dir)
                if api_data:
                    enriched = apply_api_enrichment(enriched, api_data)
                time.sleep(0.15)  # rate limit (~7 requests/sec)

        enriched_words.append(enriched)

        if i % 1000 == 0:
            elapsed = time.time() - start_time
            log.info(f"  Enriched {i}/{len(selected)} words ({elapsed:.0f}s elapsed)")

    # Phase 4: Generate database
    log.info("\n--- Phase 4: Generating database ---")
    word_count, example_count = create_database(output_path, enriched_words)

    # Phase 5: Verification
    log.info("\n--- Phase 5: Verification ---")
    stats = verify_database(output_path)
    all_pass = print_report(stats)

    elapsed = time.time() - start_time
    log.info(f"\nTotal time: {elapsed:.1f}s")

    # Save verification report as JSON
    report_path = Path("verification_report.json")
    with open(report_path, "w") as f:
        json.dump(stats, f, indent=2, ensure_ascii=False)
    log.info(f"Verification report saved to {report_path}")

    sys.exit(0 if all_pass else 1)


if __name__ == "__main__":
    main()
