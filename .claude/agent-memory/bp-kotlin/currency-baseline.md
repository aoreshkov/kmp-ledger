---
name: currency-baseline
description: Baseline result of the Kotlin/coroutines upstream-currency audits (2026-06-26, re-audited 2026-07-02 and 2026-07-16) — what was checked and found current, so future audits can diff
metadata:
  type: project
---

Full-repo Kotlin-language + kotlinx.coroutines/Flow currency audits against pinned Kotlin 2.4.0 / Coroutines 1.11.0 / Serialization 1.11.0. Outcome all three times: code matches current official best practice — no Critical/Should-fix currency gaps.

**Version currency confirmed 2026-07-16:** Kotlin 2.4.0 still latest stable (2.4.20 only in Beta1, planned Sep 2026 per kotlinlang.org/docs/releases.html + whatsnew-eap.html). kotlinx.coroutines 1.11.0 still latest stable per GitHub releases. kotlinx-serialization 1.11.0 pinned (aligned with the KMP release train). All three pins fully current — nothing newer to note. Between 2026-07-02 and 2026-07-16 NO `.kt` files changed (only dep bumps: Room 3.0.0 stable, Nav3 1.1.4, lifecycle 2.11.0-rc01), so the prior audit fully carries forward. Coroutines 1.11.0's one new deprecation (CoroutineDispatcher as context-key -> use ContinuationInterceptor) does NOT apply — repo has no `context[CoroutineDispatcher]` usage. No GlobalScope / runBlocking-in-main / manual Job() / `.values()`.

Patterns confirmed current (don't re-flag without a version bump or new upstream guidance):
- `DataResult` + `Flow.asResult()` (map -> onStart emit Loading -> catch emit Error) — matches official Flow exception-transparency pattern.
- ViewModels: `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)`, `flatMapLatest` over a retry `MutableStateFlow`, `combine` for UI state, `Channel(BUFFERED) + receiveAsFlow()` for one-shot events, `collectAsStateWithLifecycle` in screens. All current.
- Repository: `withContext(dispatchers.io)` for writes, `.flowOn(dispatchers.io)` for cold read flows; injectable `AppDispatchers` seam.
- 2026-07-02 additions audited and current: `DataStoreSettingsRepository` (`catch` + `emit(emptyPreferences())` on okio `IOException` only, rethrow others — correct Flow exception transparency), `SettingsViewModel` (combine + stateIn), theme flow in `App()` via `collectAsStateWithLifecycle(initialValue = SYSTEM)`, `ThemeMode.entries` (not `values()`), Navigator/NavBackStack code, `SetThemeModeUseCase` via `runCatchingCancellable`.
- Build: no explicit languageVersion/apiVersion (defaults, correct); `-Xexpect-actual-classes` applied only in the 4 modules with expect classes — still required in 2.4.0 (expect/actual classes NOT in the 2.4.0 stable-features list per whatsnew24.html, checked 2026-07-02).
- Language: `data object` for stateless sealed cases, exhaustive `when` over sealed interfaces, no deprecated stdlib APIs.

Kotlin 2.4.0 stabilized: context parameters, explicit backing fields, `@all` meta-target, Uuid API (except V4/V7 generation — see [[currency-optins]]). Explicit backing fields could replace `_uiState`/`asStateFlow()` in PostingEditViewModel, but the official coding conventions (checked 2026-07-02) still document the `_name` backing-property convention and `asStateFlow()` prevents downcasting where an explicit backing field exposes the MutableStateFlow instance — reported as Optional-only; do not push as churn. Same for `extraWarnings.set(true)` (`-Wextra`, since Kotlin 2.1) — offered as Optional build hardening, not a gap.

Out of lane (owned by rv-concurrency, not re-reviewed here): `runCatchingCancellable` cancellation correctness, asResult pipeline correctness. Note: as of coroutines 1.11.0 there is still no official stdlib/coroutines replacement for the runCatchingCancellable pattern (kotlinx.coroutines#1814 unresolved), so the custom helper remains the current best practice.

See [[currency-optins]] for the experimental opt-ins that are intentionally justified.
