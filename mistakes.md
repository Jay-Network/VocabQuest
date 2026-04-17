# EigoJourney — Mistakes & Lessons Learned

## 2026-04-17: `entire` CLI missing — commits blocked

**What happened**: Tried to commit ScoringEngine tests but `.git/hooks/commit-msg` requires the `entire` CLI which is no longer in PATH. Hook has existed since 2026-03-01. Commits worked on Apr 13 but fail now.

**Root cause**: Unknown — `entire` binary is not installed or was removed. Not in npm, bun, snap, or ~/bin.

**Impact**: All git commits blocked for this repo (and likely other Jay-Network repos with same hook).

**Resolution**: Need Jay to either reinstall `entire` or remove the hook. Do NOT bypass with `--no-verify`.

**Lesson**: Check that git hooks work before starting a commit-heavy work session. If a hook fails, investigate the root cause rather than spending time on workarounds.

## 2026-04-17: versionCode/versionName not bumped in build.gradle.kts

**What happened**: STATUS.md claimed versionCode 4 / v0.2.2 but `android-app/build.gradle.kts` still had versionCode 2 / versionName "0.2.0". Docs and build file diverged.

**Root cause**: Previous version bumps updated STATUS.md and VERSION file but forgot to update the actual Android build config.

**Impact**: APKs built from v0.2.1 and v0.2.2 would have reported version 0.2.0 to Google Play, causing upload conflicts or user confusion.

**Lesson**: When bumping versions, always update ALL three sources: `VERSION` file, `STATUS.md`, AND `android-app/build.gradle.kts` (versionCode + versionName). Add this to a version bump checklist.
