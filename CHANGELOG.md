# EigoJourney Changelog

All notable changes to this project are documented here.
Format follows [Keep a Changelog](https://keepachangelog.com/). Versioning: `vMAJOR.MINOR.PATCH`.
Single source of truth for version: `VERSION` file at repo root.

---

## [Unreleased]

_No unreleased changes._

## v0.3.1 (2026-05-07)

### Added
- Fleet CODEOWNERS generated from agent-repo-map.json.

## v0.3.0 (2026-04-17)

### Added
- SettingsScreen with daily goal slider (5-100 cards), study reminder toggle with time picker, subscription info, app version display
- SettingsViewModel backed by UserRepository (daily goal) and SharedPreferences (notification prefs)
- WordDetailScreen showing word header (phonetic, POS, CEFR, frequency rank), definition, SRS study progress (status, accuracy, reviews, next review), example sentences
- WordDetailViewModel using SavedStateHandle for wordId extraction, VocabRepository + SrsRepository
- Settings button added to HomeScreen bottom action row
- Navigation wired for Settings and WordDetail with real screens (replacing placeholders)
- Unit test suite: Sm2Algorithm (8), WordRarityCalculator (11), ScoringEngine (13), Domain models (13) — 45 total

### Fixed
- Deprecated Icons.Filled.Send replaced with Icons.AutoMirrored.Filled.Send
- expect/actual classes beta warnings silenced via -Xexpect-actual-classes compiler flag

### Verified
- Debug APK (84MB) confirmed to bundle all 10,000 .ogg audio files + 5.6MB SQLite database

## v0.2.2 (2026-04-09)

### Fixed
- observeBalance() Flow in HomeViewModel and ShopViewModel now catches exceptions to prevent silent coroutine death on DB errors
- SubscriptionViewModel now collects observeSubscription() Flow instead of one-shot getSubscription() for reactive plan updates
- TOCTOU race condition in purchaseItem() — balance check moved inside SQLDelight transaction with rollback on insufficient funds
- J Coin sync metadata now forwarded to Supabase edge functions in syncPendingEvents() body
- Settings and WordDetail nav routes registered in EigoJourneyNavHost with placeholder screens
- WeeklyActivityCard columns use weight(1f) instead of hardcoded 36dp width to prevent overflow on narrow screens
- "Backend not configured" raw error replaced with user-friendly "Feedback is not available offline" message
- FeedbackFAB only shows on Home screen to prevent overlap with sub-screen content

## v0.2.1 (2026-03-01)

### Changed
- Renamed package from `com.jworks.vocabquest` → `com.jworks.eigojourney` (all Kotlin sources, AndroidManifest, build.gradle.kts)
- Renamed display name EigoQuest → EigoJourney (strings.xml, themes.xml, Application class)
- Renamed CI workflow `vocabquest-ios.yml` → `eigojourney-ios.yml`
- Renamed keystore `vocabquest-release.jks` → `eigojourney-release.jks`
- Renamed nav host `VocabQuestNavHost` → `EigoJourneyNavHost`
- Renamed Application class `EigoQuestApplication` → `EigoJourneyApplication`
- Updated JCOIN_APP_KEY to new `eigojourney` backend key (was `eigoquests` key)
- Restyled FeedbackDialog with glass UI (semi-transparent background, blur, rounded corners)
- Restyled CollectionScreen with glass card backgrounds and updated grid layout
- Restyled FlashcardScreen with glass card styling and improved button layout
- Restyled QuizScreen with glass answer buttons and progress indicators
- Restyled HomeScreen with glass stat cards and simplified layout
- Restyled ProgressScreen with glass weekly activity and stats cards
- Restyled ShopScreen with glass item cards and purchase UI
- Restyled SubscriptionScreen with glass tier comparison cards
- Updated store-listing.md text for EigoJourney brand
- Updated CLAUDE.md with Sage/Journey brand naming matrix

### Added
- GlassTheme.kt — shared glass styling system (glassSurface, glassCard, glassButton modifiers)
- CHANGELOG.md (this file)
- ROADMAP.md with planned versions through v2.2.0
- VERSION file as single source of truth

## v0.2.0 (2026-02-27)

### Added
- Word collection system with gacha pull mechanic and 5-tier rarity (Common → Legendary) (a9cdf32)
- WordEncounterEngine — frequency-based encounter logic with guaranteed new word pity (a9cdf32)
- WordLevelEngine — XP and leveling for individual collected words (a9cdf32)
- WordRarityCalculator — CEFR-to-rarity mapping (a9cdf32)
- CollectionScreen — 3-column grid UI with rarity-colored borders (a9cdf32)
- CollectionViewModel with gacha pull and collection state management (a9cdf32)
- 16 J Coin earn triggers via EarnTriggers constants object (b5c7643)
- Cross-app received_words SQLDelight schema (ReceivedWords.sq) (f2889c3)
- ReceivedWordsRepository + ReceivedWordsRepositoryImpl for word transfer storage (f2889c3)
- CheckWordMasteryUseCase — fires `eigojourney_word_mastered` J Coin event on SRS graduation (f2889c3)
- ReceivedWordsSyncWorker — WorkManager job pulling from `eq_received_words` Supabase table every 15 min (71a04ab)
- DeviceIdProvider — UUID stored in SharedPreferences for cross-app device identity (71a04ab)
- VocabRepository.findByWord() — case-insensitive word lookup for linking received words (f2889c3)

### Fixed
- J Coin source_business changed from `'vocabquest'` to `'eigojourney'` in JCoin.sq default value (a3a9d42)

## v0.1.2 (2026-02-22)

### Changed
- Renamed app display name from VocabQuest → EigoQuest in strings.xml and themes.xml (9f59650)

## v0.1.1 (2026-02-20)

### Fixed
- Null-safe access in FlashcardViewModel when SRS card has no next review date (f03afd6)
- Quiz answer validation crash when options list is empty (f03afd6)
- Shop purchase flow not deducting coins on successful transaction (f03afd6)
- Progress screen crash when no sessions recorded yet (f03afd6)

### Added
- FeedbackFAB — floating action button on all screens for user feedback (ccf1bf3)
- FeedbackDialog — modal dialog with category picker, text input, and submission (ccf1bf3)
- FeedbackViewModel — handles feedback submission to Supabase (ccf1bf3)
- FeedbackFCMService — Firebase Cloud Messaging for feedback response notifications (ccf1bf3)
- FeedbackRepository + FeedbackRepositoryImpl for feedback CRUD (ccf1bf3)
- GitHub Actions CI workflow for Android build validation (ccf1bf3)
- Privacy policy page at jworks-ai.com/apps/eigojourney/privacy

## v0.1.0 (2026-02-09)

### Added
- Complete app infrastructure: Hilt DI, Jetpack Navigation, Supabase client, Material 3 theme (9731efc)
- HomeScreen with Word of the Day, daily stats, CEFR level breakdown, upgrade banner (9731efc)
- FlashcardScreen with SM-2 spaced repetition algorithm, card flip animation, answer grading (67ed647)
- QuizScreen with 4-choice multiple choice, timer, score tracking, premium gate (67ed647)
- ShopScreen with J Coin item catalog, purchase flow, premium gate (67ed647)
- ProgressScreen with weekly activity heatmap, streak counter, level stats (67ed647)
- SubscriptionScreen with FREE/PREMIUM tier comparison and upgrade CTA (14e2b53)
- NavRoutes sealed class and EigoJourneyNavHost navigation graph (9731efc)
- VocabRepository + VocabRepositoryImpl for word queries and random word selection (67ed647)
- SrsRepository + SrsRepositoryImpl for spaced repetition card management (67ed647)
- SessionRepository + SessionRepositoryImpl for study session tracking (67ed647)
- JCoinRepository + JCoinRepositoryImpl with Supabase sync queue (67ed647)
- UserRepository + UserRepositoryImpl for profile and XP management (67ed647)
- SubscriptionRepository + SubscriptionRepositoryImpl with tier gating logic (14e2b53)
- CollectionRepository + CollectionRepositoryImpl for word collection (67ed647)
- ScoringEngine — XP, combo multiplier, and streak bonus calculations (67ed647)
- SrsAlgorithm — SM-2 implementation with interval and ease factor updates (67ed647)
- SessionStats — session duration, accuracy, and word count tracking (67ed647)
- CompleteSessionUseCase — orchestrates session end (save stats, award coins, update XP) (67ed647)
- SupabaseClient singleton with anon key auth (9731efc)
- SQLDelight schema: vocabulary, srs_cards, coin_ledger, coin_sync_queue, study_sessions (9731efc)
- Pre-built SQLite database bundled as asset (10k words, 17k sentences, CEFR A1-C2) (9731efc)
- Stripe product/price IDs for VocabQuest Premium monthly subscription (d1ae849)
- Release signing config with v1+v2 APK signing schemes (c9b12ab, 3b8008a)
- ProGuard/R8 minification rules for release builds (c9b12ab)
- iOS CI workflow template (82e8e72)

### Fixed
- All compile errors resolved for clean debug build (977d843)

## v0.0.1 (2026-02-08)

### Added
- Initial project structure: KMP skeleton with commonMain, androidMain, jvmMain source sets (2a5b43e)
- SQLDelight schema definitions for vocabulary, SRS cards, J Coins, study sessions (2a5b43e)
- Data pipeline: Python scripts to generate 10,000-word vocabulary database from NLTK WordNet + Brown Corpus + CMU Dict (2a5b43e)
- 10,000 vocabulary words tagged with CEFR levels A1-C2 based on frequency analysis (2a5b43e)
- 17,000+ example sentences generated per word (2a5b43e)
- Audio pronunciation files in OGG Vorbis format (2a5b43e)
- Pre-built SQLite database (5.59 MB) (2a5b43e)
- build.gradle.kts with KMP, SQLDelight, and Compose dependencies (2a5b43e)
- settings.gradle.kts with module declarations (2a5b43e)
