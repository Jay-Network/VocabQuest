# EigoJourney Development Tracker

> **Updated by**: jworks:45 (EigoJourney agent)
> **Last updated**: 2026-04-17

---

## Current Status

- **Version**: v0.3.0 (versionCode 5)
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

- **Current work**: v0.3.0 — unit tests, build hygiene, APK verification
- **Just completed**:
  - 32 unit tests added: Sm2Algorithm (8), WordRarityCalculator (11), ScoringEngine (13)
  - Deprecated Icons.Filled.Send → Icons.AutoMirrored.Filled.Send
  - expect/actual beta warnings silenced via compiler flag
  - Debug APK verified: 84MB with 10k audio + 5.6MB SQLite bundled
  - Settings screen + Word Detail screen (v0.4.0 roadmap, committed 9e462e6)
- **Next**: Word Context Generator (Gemini dual-agent), Study Recommendations engine, cross-app word transfer
- **Blockers**:
  - `entire` CLI missing from PATH — blocking git commits (commit-msg hook)
  - Feature graphic + screenshots (waiting on jayhub:31 Vision agent)
  - Cross-app word transfer (coordinating device ID with jworks:46)
- **Resolved**:
  - ~~APK size investigation~~ — debug APK verified at 84MB (Apr 13)
  - ~~8 medium-priority bugs~~ — all fixed v0.2.2 (Apr 9)
  - ~~Brand rename~~ — VocabQuest → EigoJourney + glass UI (350bc37, Apr 6)

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
| v0.3.0 | 2026-04-17 | Settings + Word Detail screens, 32 unit tests, build hygiene, APK verified |
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
