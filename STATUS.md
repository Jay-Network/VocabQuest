# EigoQuest Development Tracker

> **Updated by**: jworks:45 (EigoQuest agent)
> **Last updated**: 2026-02-25 (reviewed & corrected by jworks:45)

---

## Current Status

- **Version**: 0.1.0 (versionCode 1)
- **Platform**: Android (Kotlin + Jetpack Compose, KMP structure)
- **Package**: com.jworks.vocabquest (display name: EigoQuest)
- **Build**: Passing
- **Branch**: main (uncommitted Collection feature in progress)
- **Stage**: Pre-beta

---

## Feature Matrix

| Feature | Status |
|---------|:------:|
| **Core Gameplay** | |
| Flashcard mode (SM-2 spaced repetition) | DONE |
| Quiz mode (multiple choice, premium-gated) | DONE |
| Word of the Day | DONE |
| Collection feature | IN PROGRESS |
| **Content** | |
| 10,000 vocabulary words (CEFR A1-C2) | DONE |
| 17,000+ example sentences | DONE |
| Audio pronunciation (OGG Vorbis) | DONE |
| Pre-built SQLite database | DONE |
| **Gamification** | |
| J Coin system (earn + spend) | DONE |
| Shop with power-ups | DONE |
| Progress tracking (streaks, stats, levels) | DONE |
| Weekly activity display | DONE |
| **User System** | |
| Subscription tiers (FREE/PREMIUM) | DONE |
| Feature gating | DONE |
| Feedback system (FAB + FCM) | DONE |
| **Backend** | |
| Supabase edge functions | DONE |
| Stripe integration (live mode) | DONE |
| CI workflow (GitHub Actions) | DONE |
| **Store Readiness** | |
| Signed release APK | DONE |
| ProGuard/R8 minification | DONE |
| Store listing text (descriptions, keywords) | DONE |
| Content rating questionnaire | DONE |
| Feature graphic + screenshots | - |
| Privacy policy deployed | DONE |

**Legend**: DONE | IN PROGRESS | - (not started) | N/A

---

## Current Sprint

- **Current work**: Collection feature — UI done (CollectionScreen, CollectionViewModel), backend done (CollectionRepository, Collection.sq, WordEncounterEngine, WordLevelEngine, WordRarityCalculator), integrated into FlashcardVM + QuizVM. 6 modified + 8 new files uncommitted.
- **Next**: Commit collection feature, visual assets, device testing, store submission
- **Blockers**:
  - Feature graphic + screenshots (waiting on jayhub:31 Vision agent)
  - APK size investigation (3.2MB seems too small — audio may not be bundled)
  - 8 medium-priority issues in STORE_READINESS.md
- **Resolved**:
  - ~~Privacy policy~~ — deployed at jworks-ai.com/apps/vocabquest/privacy (Feb 20)
  - ~~4 critical bugs~~ — fixed in commit f03afd6 (Feb 20)

---

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | KMP (commonMain/androidMain/jvmMain) |
| Database | SQLDelight |
| DI | Hilt |
| Backend | Supabase + Stripe |
| Notifications | Firebase Cloud Messaging |
| Background | WorkManager + Hilt |

---

## Data Pipeline

| Metric | Value |
|--------|-------|
| Word count | 10,000 |
| Source | NLTK WordNet + Brown Corpus + CMU Dict |
| CEFR levels | A1-C2 (frequency-based) |
| Database size | 5.59 MB SQLite |
| Generation time | ~9 seconds |
| Enrichment | Free Dictionary API (optional) |

---

## Milestones

1. Collection feature completion
2. Visual assets delivery
3. Privacy policy deployment
4. Device testing on real hardware
5. v0.2.0 closed beta
6. v1.0.0 production release
