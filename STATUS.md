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

- **Current work**: Phase 2 planning — cross-app integration (EigoLens → EigoQuest received_words)
- **Just completed (Phase 1)**:
  - Fixed source_business 'vocabquest' → 'eigoquests' in JCoin.sq (a3a9d42)
  - Committed Collection feature: gacha, encounter engine, 3-col UI (a9cdf32)
  - Wired 16 J Coin earn triggers with EarnTriggers constants (b5c7643)
  - Rarity colors verified matching KanjiQuest (#9E9E9E, #4CAF50, #2196F3, #9C27B0, #FFD700)
- **Next**: received_words table for EigoLens integration, visual assets, device testing
- **Blockers**:
  - Feature graphic + screenshots (waiting on jayhub:31 Vision agent)
  - APK size investigation (3.2MB seems too small — audio may not be bundled)
- **Resolved**:
  - ~~Privacy policy~~ — deployed at jworks-ai.com/apps/vocabquest/privacy (Feb 20)
  - ~~4 critical bugs~~ — fixed in commit f03afd6 (Feb 20)
  - ~~Collection feature~~ — committed a9cdf32 (Feb 27)
  - ~~J Coin source_business~~ — fixed a3a9d42 (Feb 27)
  - ~~Earn triggers~~ — 16 triggers wired b5c7643 (Feb 27)

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
