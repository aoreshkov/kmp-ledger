---
name: test-stack-currency-notes
description: Test-toolchain currency facts (coroutines-test, Compose MP v2 UI-test API, robolectric/junit/espresso). Every version claim is a dated observation verified 2026-09-06 against the artifacts — re-read the pins before reuse
metadata:
  type: project
---

Test-stack currency facts. **Every version number below is a dated observation, not a
standing fact.** Read `gradle/libs.versions.toml` first; where a pin differs from the
version a claim names, treat that claim as unverified and re-derive it.

## Verified 2026-09-06 (against the cached artifacts, not from recall)

- **CMP v2 UI-test API is STILL `@ExperimentalTestApi` at Compose Multiplatform 1.12.0.**
  Evidence: `javap -v` on `org.jetbrains.compose.ui:ui-test-desktop:1.12.0` shows
  `Landroidx/compose/ui/test/ExperimentalTestApi;` directly on
  `androidx.compose.ui.test.v2.ComposeUiTest_skikoKt.runComposeUiTest` and on
  `ComposeUiTest_desktopKt.runDesktopComposeUiTest`. The v2 signature is unchanged from
  1.11.1: `(effectContext, runTestContext, testTimeout, block: suspend ComposeUiTest.() -> Unit)`,
  default dispatcher **StandardTestDispatcher**. kotlinlang.org/docs/multiplatform/compose-test.html
  still shows `@OptIn(ExperimentalTestApi::class)` + `v2.runComposeUiTest` as the recommended form.
  → The repo's per-class `@OptIn(ExperimentalTestApi::class)` is **required**, not stale.
- **coroutines-test 1.11.0**: `UnconfinedTestDispatcher()`, `Dispatchers.setMain`,
  `Dispatchers.resetMain`, `TestScope.advanceUntilIdle/runCurrent` are all still
  `@ExperimentalCoroutinesApi`. `TestCoroutineScheduler` and its `advanceUntilIdle()` are
  NOT experimental. Only deprecations in the module are the `dispatchTimeoutMs` `runTest`
  overloads (ERROR since 1.11.0) — unused here.
- **Robolectric 4.16.1 tops out at SDK 36** (`DefaultSdkProvider` highest entry = 36 /
  Android 16). So `@Config(sdk = [36])` in `core/test/.../PlatformComposeUiTest.android.kt`
  is a live constraint, not stale, while `android-sdk-compile = 37`.
  Robolectric **4.17-beta-1 (2026-07-15) is the release that adds SDK 37** — still beta as of
  2026-09-06 (latest prerelease 4.17-beta-4, 2026-08-23; latest *stable* 4.16.1, 2026-01-21).
  When 4.17 goes stable, the `@Config(sdk = [36])` pin and its comment can be dropped.
- Latest-stable check 2026-09-06: kotlinx-coroutines **1.11.0** = pinned; CMP **1.12.0** =
  pinned (released 25 Aug 2026); robolectric **4.16.1** = pinned; junit **4.13.2** = pinned
  (final JUnit 4 release); espresso-core **3.7.0** = pinned; androidx.test.ext:junit **1.3.0**
  = pinned; bcprov-jdk18on **1.85.2** = pinned. Whole test stack was at latest stable.
- **Audit outcome 2026-09-06: zero upstream-currency gaps.** Repo is fully on the v2 test
  API everywhere (common screen tests, `runDesktopComposeUiTest`, androidApp
  `junit4.v2.createAndroidComposeRule`); `setMain` paired with `resetMain` in every class;
  no `runBlockingTest`/`TestCoroutineDispatcher`/`Thread.sleep`/`delay` real-time waits;
  only non-deprecated `kotlin.test` assertions (`assertEquals/True/False/Is/Null/NotNull/
  NotEquals/Same/FailsWith`). Do not re-flag a v1→v2 migration; it is already done.

**Why:** an earlier version of this note framed dated observations as standing facts
("CMP 1.11.1 is latest", "Kover 0.9.8 is latest") and told the next run to diff against
those baselines — which is exactly how a stale number survives an audit.

**How to apply:** re-read the pins, then re-derive any claim whose named version moved.
Cheapest re-derivation path (used above): the Gradle cache under
`$GRADLE_USER_HOME/caches/modules-2/files-2.1/` already holds the resolved jars/sources —
`javap -v` on a class file shows opt-in markers directly, which beats reading docs prose.
Correct this note in place when you re-derive; writes under `.claude/agent-memory/` are permitted.
