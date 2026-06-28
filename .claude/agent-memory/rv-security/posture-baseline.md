---
name: posture-baseline
description: Security posture of kmp-ledger as of 2026-06-23 — local-first ledger, threat model, what's sound, open items
metadata:
  type: project
---

kmp-ledger is a **local-first**, offline ledger app (Android/iOS/Desktop). No network module is wired in (`core:network` artifacts exist in .idea but no network code is referenced). Data model is trivial: `Posting(id, narrative)` — a free-text string. No amounts, accounts, PII, or auth yet. This bounds the threat model: no remote attack surface, no secrets to leak at runtime, no untrusted input source other than the local user typing a narrative.

**Why:** Reviews should not flag hardening for threats this app doesn't face (e.g. TLS pinning, token storage, deserialization of remote data). Keep findings to real gaps.

**How to apply:** When reviewing, treat the narrative string as the only trust-boundary input, and the local DB file as the only data-at-rest concern.

## What is sound (verified 2026-06-23)
- No hardcoded secrets anywhere. `local.properties` holds only `sdk.dir` and is gitignored + untracked. `.idea/` untracked.
- Android signing: keystore + passwords come only from CI env vars (`ANDROID_KEYSTORE_*`), never committed. `androidApp/build.gradle.kts` creates the release signingConfig only when `ANDROID_KEYSTORE_PATH` env is present.
- Room DAO (`core:database/.../PostingDao.kt`) uses only `@Query` parameterized binding + `@Insert/@Update/@Delete` — no raw/`@RawQuery`, no SQL injection surface.
- Input validation: `SavePostingUseCase` does `require(narrative.isNotBlank())`; VM mirrors with `isValid`. No unchecked parsing (no amounts to parse yet).
- DB paths are app-private per platform: Android `context.getDatabasePath`, iOS `NSDocumentDirectory`, JVM platform appdata dirs.
- Logging (`core:common`): Kermit -> Logcat/OSLog/SLF4J. Logger is injected but **not actually called anywhere** in app code (grep for `logger.`/`.d(`/`.e(` finds only the SLF4J writer plumbing). No PII/secret logging.
- NavKeys (`feature/posting/api/.../navigation/PostingNavKeys.kt`) are `@Serializable`: `PostingRoute` sealed interface with `PostingList` (data object), `PostingDetail(id: String)`, `PostingEdit(id: String?)`. Carry only opaque IDs — no sensitive payload. Polymorphic `serializerPostings` module; round-trip + missing-field-rejection covered by tests. Persisted via SavedState; local-only.
- CI: all GitHub Actions SHA-pinned; `persist-credentials: false`; least-privilege `permissions:`; OIDC build provenance attestation; dependency-review workflow present. Repos are HTTPS google+mavenCentral only.
- Dependencies in `libs.versions.toml` are current/mainstream; some alphas/betas/rc (Room 3 rc01, material3 alpha07) — intentional, pinned, not abandoned.

## Open items
- None. Posture sound for a local-first ledger as of 2026-06-28.

## Resolved
- Android backup (was open 2026-06-23): now `android:allowBackup="false"` + `dataExtractionRules`/`fullBackupContent` allowlists in `res/xml/data_extraction_rules.xml` + `backup_rules.xml`, both excluding all domains (cloud-backup + device-transfer). Ledger DB can no longer leave the sandbox via backup/transfer. Verified 2026-06-24.
- Desktop `logback.xml` root level (was open 2026-06-23 at `debug`): now `<root level="info">` in `desktopApp/src/main/resources/logback.xml`. Verified 2026-06-28.

## Re-verified 2026-06-28
No source changes since baseline — commits since 2026-06-23 are CI/`.claude`/version-bump (1.3.0)/dep-bumps (logback 1.5.37, gradle wrapper 9.6.1) only. Data model still `Posting(id, narrative)`. `SavePostingUseCase` still `require(narrative.isNotBlank())`. No iOS `.entitlements` file exists (none needed — no capabilities requested). All findings re-confirmed against current files, not just memory.
