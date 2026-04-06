# EigoJourney - Claude Code Agent Instructions

**Project**: EigoJourney (formerly EigoQuest, originally VocabQuest) - English Vocabulary Learning App
**Owner**: JWorks (Jay's Technology & Manufacturing Business)
**Created**: 2026-02-08
**Current Version**: v0.2.0 (Alpha)
**Status**: Active Development

---

## Project Overview

EigoJourney ("Eigo" = English in Japanese) is an English vocabulary learning app designed for ESL students and language learners. It provides gamified vocabulary building with spaced repetition, context-based learning, and progress tracking.

**Core Value**: Unlike traditional flashcard apps, EigoJourney uses game mechanics and real-world context to make vocabulary acquisition engaging and effective.

**Brand Family** (Jay Network App Ecosystem):
- **SAGE** = scan + AI dialog apps (replaces "Lens"): KanjiSage, EigoSage
- **JOURNEY** = mastery + gamification apps (replaces "Quest"): KanjiJourney, EigoJourney
- All connected via J Coin cross-app economy

---

## Key Documents

**Development Resources**:
1. `/home/takuma/1_jworks/A_ai/4_Apps/EigoJourney/README.md` - Project overview and status (if exists)
2. Source code: `/home/takuma/Data_ubuntu/GitHub/Jay-Network/apps/EigoJourney/`

---

## Development Phases

### Current Phase: Setup & Planning
**Goal**: Define MVP scope and establish project structure

**Key Questions to Address**:
- Target audience (ESL students, TOEFL prep, general learners?)
- Vocabulary sources (custom lists, TOEFL vocab, academic word lists?)
- Gamification mechanics (points, levels, achievements?)
- Learning methods (flashcards, sentence context, games?)
- Spaced repetition algorithm (SM-2, Anki-style?)

---

## Technology Stack

| Component | Technology | Notes |
|-----------|-----------|-------|
| Language | Kotlin | Modern Android standard |
| UI | Jetpack Compose | Declarative UI |
| Database | Room + SQLite | Local vocabulary storage |
| Backend | TBD | User progress sync (future) |
| DI | Hilt | Dependency injection |
| Testing | JUnit + Espresso | Unit and UI tests |

---

## Directory Structure

### Agent Workspace (This Directory)
**Location**: `/home/takuma/1_jworks/A_ai/4_Apps/EigoJourney/`

```
~/1_jworks/A_ai/4_Apps/EigoJourney/
├── README.md              # Project overview
├── CLAUDE.md              # This file (agent instructions)
├── STATUS.md              # Project status tracker
├── docs/                  # Documentation
└── planning/              # Project planning
```

### Android App Project (GitHub Repository)
**Location**: `/home/takuma/Data_ubuntu/GitHub/Jay-Network/apps/EigoJourney/`

```
~/Data_ubuntu/GitHub/Jay-Network/apps/EigoJourney/
├── app/                   # Main Android module
│   ├── src/main/
│   │   ├── java/com/jworks/eigojourney/
│   │   │   ├── MainActivity.kt
│   │   │   ├── domain/    # Business logic
│   │   │   ├── data/      # Data layer
│   │   │   └── ui/        # UI components
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts       # Root build file
└── settings.gradle.kts    # Gradle settings
```

**IMPORTANT**: When working on Android code, change to the GitHub directory:
```bash
cd ~/Data_ubuntu/GitHub/Jay-Network/apps/EigoJourney/
```

---

## Development Guidelines

### Code Quality Standards

1. **Kotlin Best Practices**:
   - Use data classes for vocabulary models
   - Leverage sealed classes for learning states
   - Use coroutines for async operations
   - Prefer immutability

2. **Jetpack Compose**:
   - Keep composables small and focused
   - Use ViewModels for state management
   - Follow unidirectional data flow
   - Extract reusable components

3. **Testing**:
   - Unit tests for business logic (80%+ coverage)
   - Integration tests for critical paths
   - UI tests for key user flows
   - Manual testing on real devices

### Git Workflow

1. Work on feature branches
2. Commit frequently with clear messages
3. Use conventional commit format:
   - `feat: Add vocabulary quiz`
   - `fix: Fix progress tracking bug`
   - `docs: Update README`
   - `test: Add spaced repetition tests`

---

## Versioning (MANDATORY)

Format: **vMAJOR.MINOR.PATCH** (e.g. v1.2.3)

- **PATCH** (last digit): Every deployed change (bug fix, tweak, any update)
- **MINOR** (middle digit): Major feature addition (resets PATCH to 0)
- **MAJOR** (first digit): Release stage transition (resets MINOR and PATCH to 0)
  - v0.x.x = Alpha
  - v1.x.x = Beta
  - v2.x.x = Store release (production)

Rules:
1. Always prefix with 'v' (v1.2.3, not 1.2.3)
2. Git tags must match (v0.2.0)
3. STATUS.md must always show current version
4. Every deploy = at least a PATCH increment

Full spec: `~/1_jworks/A_ai/4_Apps/VERSIONING.md`

---

## Brand Naming System

| Suffix | Role | Replaces |
|--------|------|----------|
| **SAGE** | Scan + AI dialog | Lens |
| **JOURNEY** | Mastery + gamification | Quest |

Full 2x2 matrix:
| | Scan + AI Dialog | Mastery + Gamification |
|---|---|---|
| **Eigo** (English) | EigoSage (jworks:46) | EigoJourney (jworks:45) |
| **Kanji** (Japanese) | KanjiSage (jworks:43) | KanjiJourney (jworks:44) |

---

## Sub-Agent Information

**Identity**: [Claude-JWorks | EigoJourney-Dev] (jworks:45)
**Launcher**: `claude-jworks-eigojourney`
**Working Directory**: `/home/takuma/1_jworks/A_ai/4_Apps/EigoJourney/`
**tmux Window**: jworks:45

**Purpose**: Dedicated Android development agent for EigoJourney project

**Parent Agent**: Window 42 (Apps Division) - `jworks:42`

---

## Business Context

### Target Market

- **Primary**: ESL students (college/adult)
- **Secondary**: Test prep (TOEFL, GRE, SAT)
- **Future**: Corporate training, academic institutions

### Strategic Value

EigoJourney is part of JWorks' **educational app portfolio**:
1. Complements TutoringJay curriculum
2. Shares tech foundation with KanjiQuest (gamified learning)
3. Brand family: KanjiLens + KanjiQuest / EigoLens + EigoJourney
4. Potential B2B licensing to language schools
5. Data insights for TutoringJay teaching methods

---

## Related Projects

**Sibling Apps**:
- **KanjiJourney** (jworks:44) - Japanese kanji mastery (gamified SRS)
- **KanjiSage** (jworks:43) - Japanese reading assistant (scan + AI dialog)
- **EigoSage** (jworks:46) - English text analyzer (scan + AI dialog)

**Technology Sharing**:
- Gamification engine (shared with KanjiJourney)
- Spaced repetition algorithm (shared across all learning apps)
- J Coin economy (cross-app rewards)
- TutoringJay curriculum integration

---

## Next Steps

1. [ ] Define MVP scope and feature set
2. [ ] Set up Android Studio project
3. [ ] Create vocabulary data model
4. [ ] Implement basic flashcard UI
5. [ ] Add spaced repetition algorithm
6. [ ] Design gamification mechanics

---

## Resources

- **Jetpack Compose**: https://developer.android.com/jetpack/compose
- **Room Database**: https://developer.android.com/training/data-storage/room
- **Spaced Repetition (SM-2)**: https://en.wikipedia.org/wiki/SuperMemo
- **Academic Word List**: https://www.victoria.ac.nz/lals/resources/academicwordlist

---

**Last Updated**: 2026-03-01
**Version**: v0.2.0 (Alpha)
**Status**: Active development — glass UI, J Coin unified API, brand rename complete
**Next Milestone**: Beta (v1.0.0) and store release (v2.0.0)
