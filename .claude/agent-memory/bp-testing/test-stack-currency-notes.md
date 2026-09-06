---
name: test-stack-currency-notes
description: Test-toolchain currency facts (coroutines-test, Compose MP v2 UI-test API, Kover). Content verified 2026-07-16 against CMP 1.11.1 / Kover 0.9.8 — BOTH PINS HAVE SINCE MOVED; re-derive before reuse
metadata:
  type: project
---

> **STALE BASELINE — corrected 2026-09-06.** This note asserted "Compose MP 1.11.1 is
> the latest stable" and "Kover 0.9.8 is the latest release". Both were true on
> 2026-07-16 and are false now: the catalog pins **CMP 1.12.0** (stable, released
> 2026-08-26) and **Kover 0.9.9**. Its old "How to apply" told the next run to diff
> against those baselines instead of re-deriving — which is exactly how a stale number
> survives an audit. **Read the pins from `gradle/libs.versions.toml` first; every
> version claim below is a dated observation, not a current fact.**

Test-stack currency facts, last genuinely verified 2026-07-02 and re-verified
2026-07-16 against the versions pinned *at that time*.

- **kotlinx-coroutines 1.11.0** (still the pinned version as of 2026-09-06, and still
  latest stable at that date). Its only test-module change: advanced deprecation of the
  `runTest(dispatchTimeout=...)` overloads to ERROR and removal of the long-ERROR
  `TestCoroutineScope` APIs (PR #4604). This repo uses none of them.
- **Compose MP v2 UI-test API** — verified against CMP 1.11.1; **not re-checked against
  the pinned 1.12.0.** CMP 1.11.0 made the v2 test APIs the default and deprecated v1
  `runComposeUiTest`/`runDesktopComposeUiTest`/`runSkikoComposeUiTest`. v2 lives in
  `androidx.compose.ui.test.v2` (common) and `androidx.compose.ui.test.junit4.v2`
  (Android rules: `createAndroidComposeRule` etc.), defaults to **StandardTestDispatcher**
  (queued) rather than UnconfinedTestDispatcher, and adds an `effectContext` parameter.
  The API was still `@ExperimentalTestApi` at 1.11.1 — **re-confirm on 1.12.0 before
  asserting the opt-in is still required.**
  Sources: https://kotlinlang.org/docs/multiplatform/whats-new-compose-111.html ,
  https://kotlinlang.org/docs/multiplatform/compose-test.html ,
  https://developer.android.com/develop/ui/compose/testing/migrate-v2
- **The repo is already fully on v2** everywhere (common screen tests, DesktopUiTest's
  `runDesktopComposeUiTest`, androidApp SmokeTest's `junit4.v2.createAndroidComposeRule`).
  Structural, not version-dependent — do not re-flag a v1 to v2 migration.
- coroutines-test still marks much of the module `@ExperimentalCoroutinesApi`, so the
  blanket `@OptIn(ExperimentalCoroutinesApi::class)` on test classes using
  `UnconfinedTestDispatcher`/`setMain` is required, not stale. (Verified for 1.11.0, the
  pinned version — re-check only if that pin moves.)
- Audit outcome 2026-07-02 and 2026-07-16: **zero upstream-currency gaps** in the test
  suites. setMain paired with resetMain in every class; no legacy
  `runBlockingTest`/`TestCoroutineDispatcher`; no real-time waits (uses `waitForIdle` /
  `waitUntilExactlyOneExists`). This verdict predates the CMP 1.12.0 bump.

**Why:** the previous version of this note is the clearest example in the repo of how a
currency memory fails — it was accurate when written, framed dated observations as
standing facts, and instructed the next run to trust them. A note that says "X is the
latest" is wrong the moment X ships a successor.

**How to apply:** read the pinned `compose-multiplatform`, `kotlinx-coroutines` and
`kover` versions from `gradle/libs.versions.toml` and compare them to the versions each
claim above names. Where they differ, treat that claim as **unverified** and re-derive it
from the release notes or the pinned artifact's own sources before reusing it. When you
re-derive one, correct this note in place — writes under `.claude/agent-memory/` are
permitted.
