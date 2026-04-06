# EigoJourney — Gemini Live Agent Development Log

**App**: EigoJourney (jworks:45)
**Lead Agent**: Claude Code (claude-jworks-eigojourney)
**Gemini Agent**: TBD (jworks-gemini:45)
**SDK**: @google/genai + @google/adk v0.5.0
**Reference**: jworks:104 (SheetMusicReader) for dual output pattern
**Started**: 2026-03-17

---

## Architecture: Dual Agent Pattern

| Role | Agent | Handles |
|------|-------|---------|
| Lead Dev | Claude Code | Complex features, architecture, KMP shared-core, build system |
| Live Agent | Gemini Live | AI word context, study recommendations, conversational vocab coaching |

---

## Gemini-Suitable Features (Identified)

### 1. AI Word Context Generator
**Priority**: High
**Description**: When a user encounters a new word, Gemini provides real-time contextual explanations — etymology, usage examples, collocations, register (formal/informal), and common mistakes.
**Why Gemini**: Conversational, low-latency, voice-friendly. Ideal for Gemini Live's streaming dialog.
**Input**: Word + CEFR level + user's native language
**Output**: Structured context card (meaning, examples, mnemonics, related words)

### 2. Study Recommendation Engine
**Priority**: High
**Description**: Analyzes user's SRS data (due cards, weak areas, streak) and recommends what to study next. Can also explain *why* a word keeps coming back (spacing logic).
**Why Gemini**: Personalized coaching conversation. Users can ask follow-up questions ("Why am I seeing this word again?", "What should I focus on today?").
**Input**: User SRS stats, recent session history, collection progress
**Output**: Prioritized study plan + motivational coaching

### 3. Vocabulary Quiz Conversation Mode
**Priority**: Medium
**Description**: Gemini conducts an interactive vocabulary quiz via voice — asks questions, provides hints, explains answers, adapts difficulty based on responses.
**Why Gemini**: Natural conversation flow, adaptive difficulty, immediate feedback — better than static UI quiz.
**Input**: Word list filtered by CEFR level and SRS state
**Output**: Scored conversation with corrections and explanations

### 4. Word Collection Storyteller
**Priority**: Medium
**Description**: Gemini creates short stories or scenarios incorporating words from the user's collection, reinforcing retention through narrative context.
**Why Gemini**: Creative generation, can be voice-narrated, engages different memory pathways.
**Input**: 5-10 collected words + user CEFR level
**Output**: Short story/scenario using the words naturally

### 5. Progress Coach
**Priority**: Low
**Description**: Weekly progress review — Gemini summarizes what the user learned, celebrates achievements, identifies areas for improvement, sets goals.
**Why Gemini**: Motivational conversation, personalized encouragement.
**Input**: Weekly stats (words learned, accuracy, streak, coins earned)
**Output**: Coaching summary with actionable goals

---

## Prompt Iterations

### Iteration 0 — Baseline (2026-03-17)
**Status**: Planning
**Prompt**: Not yet written
**Notes**: Identifying feature scope and data contracts before writing prompts. Next step: define the data schema that Claude exposes to Gemini (SRS stats, collection data, word metadata).

---

## Claude vs Gemini Comparison

| Capability | Claude (Lead) | Gemini (Live) |
|-----------|---------------|---------------|
| Code generation | Excellent — handles KMP, Gradle, SQLDelight | Not used for code |
| Real-time conversation | N/A (CLI agent) | Core strength — streaming dialog |
| Word context generation | Can do but overkill for runtime | Perfect — fast, conversational |
| Study recommendations | Generates logic/algorithms | Delivers recommendations conversationally |
| Architecture decisions | Primary responsibility | Not involved |
| User-facing interaction | None (dev-only) | Primary user touchpoint |

---

## Integration Points

### Data Flow: Claude → Gemini
1. Claude builds the app with SRS engine, collection system, scoring
2. App exposes user data via local APIs (Room/SQLDelight queries)
3. Gemini agent reads this data to personalize conversations
4. Gemini responses are displayed in-app or via voice

### Sync to 7_LiveAgents/
- Best-performing Gemini prompts will be synced to `~/1_jworks/7_LiveAgents/` for the Gemini Live Agent Challenge
- Criteria: response quality, latency, user engagement metrics

---

## Next Steps

- [ ] Define data contract (JSON schema) for word context requests
- [ ] Write first Gemini prompt for AI Word Context Generator
- [ ] Test with @google/genai SDK locally
- [ ] Compare Claude vs Gemini output quality for word explanations
- [ ] Build in-app Gemini integration endpoint
