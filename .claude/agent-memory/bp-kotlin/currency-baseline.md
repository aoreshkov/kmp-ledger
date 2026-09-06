---
name: currency-baseline
description: Baseline result of the Kotlin/coroutines upstream-currency audits (2026-06-26, re-audited 07-02, 07-16, 09-06) — what was checked and found current, so future audits can diff
metadata:
  type: project
---

Full-repo Kotlin-language + kotlinx.coroutines/Flow currency audits against the pins in
`gradle/libs.versions.toml`. Outcome all four times: no Critical/Should-fix currency gaps;
one standing Optional (the stale `ExperimentalUuidApi` opt-in, see [[currency-optins]]).

**Rule, not numbers:** always re-read the pins, then re-derive "is the pin still the latest
stable?" from the release feeds. Do not trust the sentence below on a later run.

**Version currency observed 2026-09-06** (`gh api .../releases`, authoritative):
- Kotlin: pin `2.4.0`. Latest **stable is 2.4.10** (2026-07-14) — a bug-fix for 2.4.0;
  2.4.20 is at RC3 (2026-09-02), targeted Sep 2026. The pin is deliberate (the catalog
  comment ties it to the IntelliJ IDEA plugin ceiling, same reason as AGP), so report the
  2.4.x gap only as a *note*, never as a gap. **This supersedes the 2026-07-16 note that
  said "2.4.0 is still latest stable, 2.4.20 only in Beta1" — that expired on 2026-07-14.**
- kotlinx.coroutines: pin `1.11.0` (released 2026-05-08) — still the latest stable. Current.
- kotlinx-serialization: pin `1.11.0` (2026-04-09) — latest stable; 1.12.0-RC (2026-09-04)
  is prerelease only. Current.

Patterns confirmed current (don't re-flag without a pin bump or new upstream guidance):
- `DataResult` + `Flow.asResult()` (map -> onStart emit Loading -> catch emit Error) — matches
  the official Flow exception-transparency pattern.
- ViewModels: `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)`,
  `flatMapLatest` over a retry `MutableStateFlow`, `combine` for UI state,
  `Channel(BUFFERED) + receiveAsFlow()` for one-shot events, `collectAsStateWithLifecycle`
  in screens, `_x` + `asStateFlow()` backing property. All current.
- Repository: `withContext(dispatchers.io)` for writes, `.flowOn(dispatchers.io)` for cold
  read flows; injectable `AppDispatchers` seam; common `Dispatchers.IO` (no opt-in needed).
- `DataStoreSettingsRepository`: `catch` + `emit(emptyPreferences())` on okio `IOException`
  only, rethrow others — correct Flow exception transparency.
- Tests: `runTest`, `Dispatchers.setMain(UnconfinedTestDispatcher())` / `resetMain`,
  `backgroundScope` for never-completing collectors (`EventCollect.kt`). Current idioms.
- Language: `data object` for stateless sealed cases, exhaustive `when` over sealed
  interfaces, `@JvmInline value class StartDestination`, `ThemeMode.entries` (not
  `values()`). No `GlobalScope` / `runBlocking` / manual `Job()` / deprecated stdlib calls
  (swept 2026-09-06: no `capitalize`/`toUpperCase()`/`sumBy`/`@Suppress("DEPRECATION")`).
- Build: no explicit `languageVersion`/`apiVersion` (defaults, correct);
  `extraWarnings.set(true)` is now ON in `ledger.kotlin.multiplatform` — the old Optional
  suggestion is **done**, stop offering it. `-Xexpect-actual-classes` in the 4 modules with
  expect classes is still required: expect/actual *classes* remain **Beta** in the KMP docs
  (kotlinlang.org/docs/multiplatform-expect-actual.html, re-checked 2026-09-06).
- Coroutines 1.11.0's one deprecation (CoroutineDispatcher as context key -> use
  `ContinuationInterceptor`) does NOT apply: no `context[CoroutineDispatcher]` usage.
- 1.11.0's new `SharedFlow.asFlow()` ("hide hot flow characteristics") is itself
  `@ExperimentalCoroutinesApi`, so it is **not** a recommended replacement for the fakes'
  `asStateFlow()` / `flow { emitAll(...) }`. Do not raise it as a finding.

Kotlin 2.4.0 stabilized: context parameters (except context arguments/callable refs),
explicit backing fields, `@all` meta-target, the `kotlin.uuid.Uuid` API *except* the V4/V7
**generation** functions. Explicit backing fields could replace `_uiState`/`asStateFlow()`
in `PostingEditViewModel`, but kotlinlang.org/docs/coding-conventions.html still documents
only the `_name` backing-property convention (re-checked 2026-09-06) and `asStateFlow()`
prevents downcasting — Optional-only; do not push as churn.

Out of lane (owned by rv-concurrency): `runCatchingCancellable` cancellation correctness,
`asResult` pipeline correctness. As of coroutines 1.11.0 there is still no official
stdlib/coroutines replacement for the pattern (kotlinx.coroutines#1814 unresolved).

See [[currency-optins]] for per-declaration opt-in status.
