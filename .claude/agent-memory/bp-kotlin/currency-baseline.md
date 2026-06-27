---
name: currency-baseline
description: Baseline result of the Kotlin/coroutines upstream-currency audit (2026-06-26) — what was checked and found current, so future audits can diff
metadata:
  type: project
---

Full-repo Kotlin-language + kotlinx.coroutines/Flow currency audit on 2026-06-26 against pinned Kotlin 2.4.0 / Coroutines 1.11.0. Outcome: code matches current official best practice — no Critical/Should-fix currency gaps found.

Patterns confirmed current (don't re-flag without a version bump or new upstream guidance):
- `DataResult` + `Flow.asResult()` (map -> onStart emit Loading -> catch emit Error) — matches the NowInAndroid/official Flow exception-transparency pattern.
- ViewModels: `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)`, `flatMapLatest` over a retry `MutableStateFlow`, `Channel(BUFFERED) + receiveAsFlow()` for one-shot events, `collectAsStateWithLifecycle` in screens. All current.
- Repository: `withContext(dispatchers.io)` for writes, `.flowOn(dispatchers.io)` for cold read flows. Correct dispatcher discipline.
- `Dispatchers.IO` used in commonMain via `kotlinx.coroutines.IO` (multiplatform, since coroutines 1.9.0). Fine on 1.11.0.
- Injectable `AppDispatchers` seam instead of hardcoded dispatchers — testable, current.
- Language: `data object` for stateless sealed cases, exhaustive `when` over sealed interfaces, no deprecated stdlib APIs.
- Tests use `backgroundScope` + collect-to-list per official coroutines-test guidance (EventCollect.kt).

Out of lane (owned by rv-concurrency agent, not re-reviewed here): `runCatchingCancellable` cancellation correctness, asResult pipeline correctness.

See [[currency-optins]] for the experimental opt-ins that are intentionally justified.
