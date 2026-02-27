# EigoQuest Development Tracker

> **Updated by**: jworks:45 (EigoQuest agent)
> **Last updated**: 2026-02-27

---

## Current Status

- **Version**: 0.1.0 (versionCode 1)
- **Platform**: Android (Kotlin + Jetpack Compose, KMP structure)
- **Package**: com.jworks.vocabquest (display name: EigoQuest)
- **Build**: Passing
- **Branch**: main (clean — all committed)
- **Stage**: Pre-beta

---

## Feature Matrix

| Feature | Status |
|---------|:------:|
| **Core Gameplay** | |
| Flashcard mode (SM-2 spaced repetition) | DONE |
| Quiz mode (multiple choice, premium-gated) | DONE |
| Word of the Day | DONE |
| Collection feature (gacha + encounter engine) | DONE |
| **Content** | |
| 10,000 vocabulary words (CEFR A1-C2) | DONE |
| 17,000+ example sentences | DONE |
| Audio pronunciation (OGG Vorbis) | DONE |
| Pre-built SQLite database | DONE |
| **Gamification** | |
| J Coin system (16 earn triggers, spend, sync) | DONE |
| Shop with power-ups | DONE |
| Progress tracking (streaks, stats, levels) | DONE |
| Weekly activity display | DONE |
| **User System** | |
| Subscription tiers (FREE/PREMIUM) | DONE |
| Feature gating | DONE |
| Feedback system (FAB + FCM) | DONE |
| **Cross-App** | |
| Received words (EigoLens → EigoQuest) | IN PROGRESS |
| Supabase pull sync worker | DONE |
| Word mastery J Coin trigger | DONE |
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

- **Current work**: Supabase pull sync worker built (uncommitted) — coordinating device ID sharing with jworks:46
- **Just completed (Phase 2)**:
  - received_words SQLDelight schema + ReceivedWordsRepository (f2889c3)
  - CheckWordMasteryUseCase — fires eigoquest_word_mastered on SRS graduation (f2889c3)
  - ReceivedWordsSyncWorker — pulls from eq_received_words every 15 min via WorkManager
  - DeviceIdProvider — UUID in SharedPreferences for cross-app identity
  - VocabRepository.findByWord() — case-insensitive lookup for linking received words
- **Phase 1 completed**:
  - Fixed source_business 'vocabquest' → 'eigoquests' in JCoin.sq (a3a9d42)
  - Committed Collection feature: gacha, encounter engine, 3-col UI (a9cdf32)
  - Wired 16 J Coin earn triggers with EarnTriggers constants (b5c7643)
- **Next**: Settle device ID sharing with EigoLens, commit sync worker, visual assets, device testing
- **Blockers**:
  - Feature graphic + screenshots (waiting on jayhub:31 Vision agent)
  - APK size investigation (3.2MB seems too small — audio may not be bundled)
  - Device ID sharing approach (coordinating with jworks:46)
- **Resolved**:
  - ~~Privacy policy~~ — deployed at jworks-ai.com/apps/vocabquest/privacy (Feb 20)
  - ~~4 critical bugs~~ — fixed in commit f03afd6 (Feb 20)
  - ~~Collection feature~~ — committed a9cdf32 (Feb 27)
  - ~~J Coin source_business~~ — fixed a3a9d42 (Feb 27)
  - ~~Earn triggers~~ — 16 triggers wired b5c7643 (Feb 27)
  - ~~received_words schema~~ — committed f2889c3 (Feb 27)
  - ~~CheckWordMasteryUseCase~~ — committed f2889c3 (Feb 27)

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

1. ~~Collection feature~~ — DONE (a9cdf32)
2. ~~J Coin earn triggers~~ — DONE (b5c7643)
3. Cross-app received_words (EigoLens integration)
4. Visual assets delivery
5. Device testing on real hardware
6. v0.2.0 closed beta
7. v1.0.0 production release
