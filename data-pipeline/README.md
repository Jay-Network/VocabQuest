# EigoQuest Data Pipeline

Generates a SQLite database with 10,000 English vocabulary words for the EigoQuest app.

## Data Sources

| Source | Words | Coverage | License |
|--------|-------|----------|---------|
| NLTK WordNet | ~147,000 lemmas | Definitions, examples, synonyms/antonyms | WordNet License (permissive) |
| NLTK Brown Corpus | ~40,000 unique words | Word frequency, POS tags | Free for research |
| NLTK CMU Pronouncing Dict | ~123,000 entries | IPA phonetic transcription | Public domain |
| Free Dictionary API | On-demand | Enhanced definitions, audio URLs | Free tier |

## Setup

```bash
cd data-pipeline
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

NLTK data is downloaded automatically on first run if not present:
- `wordnet`, `omw-1.4`, `brown`, `cmudict`, `averaged_perceptron_tagger`

## Usage

### Generate full database (10,000 words)

```bash
source venv/bin/activate
python generate_vocab_db.py
```

Output: `../shared-core/src/commonMain/resources/vocabquest.db`

### Custom output path

```bash
python generate_vocab_db.py --output /path/to/vocabquest.db
```

### Generate smaller database (for testing)

```bash
python generate_vocab_db.py --count 1000 --output /tmp/test.db
```

### Enrich with Free Dictionary API (optional, slow)

```bash
python generate_vocab_db.py --enrich-api
```

This fetches additional data (better IPA, audio URLs, richer definitions) from the free
[dictionaryapi.dev](https://dictionaryapi.dev/) API. Takes ~25 minutes for 10k words due
to rate limiting. Responses are cached in `cache/` for re-runs.

### View verification report only

```bash
python generate_vocab_db.py --report-only
```

## Database Schema

```sql
CREATE TABLE word (
    id INTEGER PRIMARY KEY,
    word TEXT NOT NULL UNIQUE,
    definition TEXT NOT NULL,
    pos TEXT NOT NULL,              -- noun, verb, adj, adv
    cefr_level TEXT,               -- A1, A2, B1, B2, C1, C2
    frequency_rank INTEGER,
    phonetic TEXT,                  -- IPA pronunciation (e.g., /bjˈuːtəfəl/)
    audio_url TEXT,
    etymology TEXT,
    metadata TEXT                   -- JSON: synonyms, antonyms, alt_definitions
);

CREATE TABLE word_example (
    id INTEGER PRIMARY KEY,
    word_id INTEGER NOT NULL REFERENCES word(id),
    sentence TEXT NOT NULL,
    context TEXT,                   -- general, formal, informal, academic
    difficulty INTEGER             -- 1-5
);
```

### Metadata JSON format

```json
{
  "synonyms": ["happy", "cheerful", "content"],
  "antonyms": ["sad", "unhappy"],
  "alt_definitions": [
    {"pos": "noun", "definition": "alternative meaning..."}
  ],
  "all_pos": ["adj", "noun"]
}
```

## Pipeline Architecture

```
Phase 1: Load Data Sources
  ├── Brown Corpus → word frequencies + POS tags
  ├── CMU Dict → pronunciation data
  └── WordNet → 77k candidate words

Phase 2: Select Words
  ├── Score by: log(frequency) × 0.6 + polysemy × 0.35 + bonuses
  ├── Filter inflected forms (morphy base-form check)
  └── Select top 10,000

Phase 3: Enrich
  ├── Definition from WordNet (POS-aware synset selection)
  ├── IPA from CMU Dict (ARPAbet → IPA conversion)
  ├── Examples from WordNet synsets (up to 3 per word)
  ├── Synonyms/antonyms from WordNet relations
  └── (Optional) Free Dictionary API enrichment

Phase 4: Generate SQLite Database

Phase 5: Verification Report
```

## Word Selection Algorithm

Words are scored using:
- **Frequency (60%)**: Log-normalized Brown Corpus frequency
- **Polysemy (35%)**: Number of WordNet synsets (more meanings = more useful)
- **Pronunciation (3%)**: Bonus for words in CMU Dict
- **Length (2%)**: Slight preference for 4-12 character words

Inflected forms (past tense, gerunds, etc.) are filtered when the base form
is also a candidate, using WordNet's morphy function.

## CEFR Level Assignment

| Level | Rank Range | Count | Description |
|-------|-----------|-------|-------------|
| A1 | 1-1000 | 1,000 | Beginner |
| A2 | 1001-3000 | 2,000 | Elementary |
| B1 | 3001-5000 | 2,000 | Intermediate |
| B2 | 5001-7000 | 2,000 | Upper-intermediate |
| C1 | 7001-9000 | 2,000 | Advanced |
| C2 | 9001-10000 | 1,000 | Proficiency |

**Distribution**: A1+A2 30%, B1+B2 40%, C1+C2 30%

## Output Statistics (Latest Run)

- **Total words**: 10,000
- **Total examples**: 17,901
- **Example coverage**: 73.3%
- **Phonetic coverage**: 99.3%
- **Database size**: 5.59 MB
- **Generation time**: ~9 seconds (offline mode)

## Regeneration

To regenerate the database from scratch:

```bash
cd data-pipeline
source venv/bin/activate
python generate_vocab_db.py
```

The script is fully deterministic - same NLTK data produces the same output.
