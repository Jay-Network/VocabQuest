# EigoJourney Development Tracker

> **Updated by**: jworks:45 (EigoJourney agent)
> **Last updated**: 2026-04-09

---

## Current Status

- **Version**: v0.2.2 (versionCode 4)
- **Platform**: Android (Kotlin + Jetpack Compose, KMP structure)
- **Package**: com.jworks.eigojourney (display name: EigoJourney)
- **Build**: Passing
- **Branch**: main
- **Stage**: Alpha (v0.x.x)

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
| Received words (EigoLens → EigoJourney) | IN PROGRESS |
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
| Settings screen (daily goal, notifications, account) | DONE |
| Word Detail screen (info, SRS stats, examples) | DONE |
| Feature graphic + screenshots | - |
| Privacy policy deployed | DONE |

**Legend**: DONE | IN PROGRESS | - (not started) | N/A

---

## Current Sprint

- **Current work**: All 8 medium-priority bugs fixed (v0.2.2)
- **Just completed**:
  - Fixed 8 medium-priority bugs: observeBalance error handling, SubscriptionViewModel reactive Flow, purchaseItem TOCTOU race, sync metadata forwarding, Settings/WordDetail nav routes, WeeklyActivityCard responsive layout, user-friendly offline error, FAB overlap fix
  - Brand rename VocabQuest → EigoJourney committed (350bc37)
- **Next**: Word Context Generator (Gemini dual-agent), Study Recommendations engine, cross-app word transfer
- **Blockers**:
  - Feature graphic + screenshots (waiting on jayhub:31 Vision agent)
  - APK size investigation (3.2MB seems too small — audio may not be bundled)
  - Cross-app word transfer (coordinating device ID with jworks:46)
- **Resolved**:
  - ~~8 medium-priority bugs~~ — all fixed v0.2.2 (Apr 9)
  - ~~Brand rename~~ — VocabQuest → EigoJourney + glass UI (350bc37, Apr 6)
  - ~~Privacy policy~~ — deployed at jworks-ai.com/apps/eigojourney/privacy (Feb 20)
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

## Version History

| Version | Date | Highlights |
|---------|------|-----------|
| v0.2.2 | 2026-04-09 | 8 medium-priority bug fixes (beta readiness) |
| v0.2.1 | 2026-03-01 | EigoQuest → EigoJourney rename, glass UI theme |
| v0.2.0 | 2026-02-27 | Collection system, 16 J Coin triggers, cross-app received_words |
| v0.1.2 | 2026-02-22 | VocabQuest → EigoQuest display rebrand |
| v0.1.1 | 2026-02-20 | 4 critical bug fixes, feedback system, privacy policy |
| v0.1.0 | 2026-02-09 | Full app infrastructure, subscription, Stripe, signing |
| v0.0.1 | 2026-02-08 | Initial project structure, 10k vocab DB, audio files |

See `CHANGELOG.md` for full details.

---

## Milestones

1. ~~Collection feature~~ — DONE (a9cdf32)
2. ~~J Coin earn triggers~~ — DONE (b5c7643)
3. ~~EigoJourney rename + glass UI~~ — DONE (v0.2.1)
4. ~~8 medium-priority bug fixes~~ — DONE (v0.2.2)
5. Cross-app received_words (EigoSage integration)
6. Visual assets delivery
7. Device testing on real hardware
8. v1.0.0 beta release
9. v2.0.0 store release
