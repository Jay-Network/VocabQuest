# EigoJourney Roadmap

**Current Version**: v0.2.0 (Alpha)
**Target Store Release**: v2.0.0
**Last Updated**: 2026-03-01

---

## Roadmap

### v0.2.1 — March 2026
- Complete EigoQuest → EigoJourney rename (package, strings, branding, CI, keystore)
- Glass UI theme applied to all screens
- Commit and push rename + glass theme changes

### v0.3.0 — March 2026
- Cross-app word transfer finalized (EigoSage → EigoJourney via received_words)
  - **Depends on**: EigoSage (jworks:46) device ID sharing protocol
- Resolve 8 known medium-priority bugs (see STORE_READINESS.md)
- APK size investigation (verify audio files are bundled)
- Device testing on real hardware + multiple screen sizes

### v0.4.0 — April 2026
- Settings screen (notification preferences, theme selection, daily goal config)
- Word detail screen (full word info, examples, audio, SRS history)
- Improved onboarding flow (CEFR level assessment quiz)
- Analytics integration (study patterns, retention rates)

### v0.5.0 — April 2026
- Visual assets: feature graphic + store screenshots
  - **Depends on**: jayhub:31 Vision agent delivery
- Data safety form completed in Play Console
- Accessibility audit (TalkBack, content descriptions, font scaling)
- Performance optimization (startup time, memory usage)

---

### v1.0.0 — May 2026 (Beta)
**Stage transition: Alpha → Beta**

Requirements for beta:
- All 8 medium-priority bugs resolved
- Visual assets delivered and uploaded to Play Console
- Real device testing passed on 3+ devices
- Store listing fully complete (all assets, data safety, rating)
- Cross-app word transfer working end-to-end
- Closed beta testing with 10+ users
- Crash-free rate > 99%

### v1.1.0 — May 2026
- Leaderboards (daily/weekly/all-time, anonymous)
- Achievement system (milestone badges, streak rewards)
- Study reminders (push notifications at user-preferred time)
- Sentence cloze mode (fill-in-the-blank with context sentences)

### v1.2.0 — June 2026
- Word groups/categories (academic, business, medical, legal)
- Custom word lists (user-created study sets)
- Export progress (CSV/PDF study reports)
- Dark theme polish (glass theme dark variant)

### v1.3.0 — June 2026
- Offline-first improvements (full offline capability, sync on reconnect)
- Widget (Word of the Day home screen widget)
- Pronunciation practice (speech-to-text comparison)
  - **Depends on**: Shared speech engine (potential cross-app component)

---

### v2.0.0 — July 2026 (Store Release)
**Stage transition: Beta → Production**

Requirements for production:
- Open beta with 50+ users and positive feedback
- All P0/P1 bugs resolved
- Store listing approved by Google Play review
- Subscription flow tested end-to-end (Stripe + Google Play Billing)
- Privacy policy and data safety verified
- Performance benchmarks met (cold start < 2s, 60fps scrolling)

### v2.1.0 — August 2026
- AI-powered word context (generate contextual examples using Claude API)
  - **Depends on**: Claude API integration (shared across Sage/Journey apps)
- Professional vocabulary packs (medical, legal, conference interpretation)
  - Aligns with Jay's interpreter expertise
- Multi-turn AI dialog for word exploration (Sage↔Journey bridge)
  - **Depends on**: EigoSage (jworks:46) AI dialog engine

### v2.2.0 — September 2026
- Social features (friend word challenges, shared lists)
- Classroom mode (teacher assigns word sets, tracks student progress)
  - Aligns with TutoringJay curriculum integration
- Google Play Billing migration (replace Stripe for in-app purchases)

---

## Cross-App Dependencies

| Feature | Depends On | Status |
|---------|-----------|--------|
| Received words sync | EigoSage (jworks:46) device ID protocol | In progress |
| Visual assets | jayhub:31 Vision agent | Pending |
| AI word context | Claude API shared integration | Planned |
| AI dialog bridge | EigoSage AI dialog engine | Planned |
| Speech engine | Shared speech component | Planned |
| J Coin economy | J Coin unified API (all apps) | Done |
| Glass UI theme | Shared glass theme system (all apps) | Done |

## Brand Identity Notes

EigoJourney is a **JOURNEY** app — focused on mastery and gamification:
- Progress-driven: levels, streaks, XP, achievements
- Collection mechanics: gacha, word encounters, rarity tiers
- Competitive elements: leaderboards, challenges
- Long-term engagement: spaced repetition, daily goals

Complemented by **EigoSage** (scan + AI dialog):
- Scan → understand → converse → master flow
- Words discovered in EigoSage transfer to EigoJourney for mastery
- Shared J Coin economy incentivizes using both apps
