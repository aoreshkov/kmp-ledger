---
name: test-stack-currency-notes
description: Verified upstream currency facts for the test toolchain (coroutines-test 1.11.0, Compose MP 1.11.1 v2 UI-test API, Kover 0.9.8) as of 2026-07-02
metadata:
  type: project
---

Test-stack currency facts verified 2026-07-02 (re-verify before reusing; versions move):

- **kotlinx-coroutines 1.11.0 is the latest release** (per CHANGES.md on master). Its only
  test-module change: advanced deprecations of the `runTest(dispatchTimeout=...)` overloads
  to ERROR and removed the long-ERROR `TestCoroutineScope` APIs (PR #4604). This repo uses
  none of them.
- **Compose MP 1.11.1 is the latest stable** (released 2026-06-02; 1.12.0-beta01 out
  2026-06-30, pre-release only). CMP 1.11.0 made the **v2 test APIs the default and
  deprecated v1** `runComposeUiTest`/`runDesktopComposeUiTest`/`runSkikoComposeUiTest`.
  v2 lives in `androidx.compose.ui.test.v2` (common) and
  `androidx.compose.ui.test.junit4.v2` (Android rules: `createAndroidComposeRule` etc.).
  v2 defaults to **StandardTestDispatcher** (queued) instead of UnconfinedTestDispatcher,
  and adds an `effectContext` parameter. The API is still `@ExperimentalTestApi`.
  Sources: https://kotlinlang.org/docs/multiplatform/whats-new-compose-111.html ,
  https://kotlinlang.org/docs/multiplatform/compose-test.html ,
  https://developer.android.com/develop/ui/compose/testing/migrate-v2
- **This repo is already fully on v2** everywhere (common screen tests, DesktopUiTest's
  `runDesktopComposeUiTest`, androidApp SmokeTest's `junit4.v2.createAndroidComposeRule`).
  Do not re-flag v1→v2 migration.
- **Kover 0.9.8 is the latest release** (2026-03-25).
- coroutines-test docs still mark much of the module `@ExperimentalCoroutinesApi`, so the
  blanket `@OptIn(ExperimentalCoroutinesApi::class)` on test classes using
  `UnconfinedTestDispatcher`/`setMain` is required, not stale.
- Audit outcome 2026-07-02: **zero upstream-currency gaps** in the test suites. setMain is
  paired with resetMain in every class; no legacy runBlockingTest/TestCoroutineDispatcher;
  no real-time waits (uses `waitForIdle`/`waitUntilExactlyOneExists`).

**How to apply:** in future audits, diff against these baselines instead of re-deriving;
only re-fetch release pages to see if anything newer than coroutines 1.11.0 / CMP 1.11.1 /
Kover 0.9.8 shipped.
