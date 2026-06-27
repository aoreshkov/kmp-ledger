---
name: concurrency-architecture
description: Map of where dispatchers, the asResult pipeline, coroutine scopes, and Flow wiring live in kmp-ledger
metadata:
  type: project
---

Concurrency layout as of 2026-06-23 review.

- Dispatcher seam: `AppDispatchers` interface (io/default) in `core:common`, prod impl `DefaultAppDispatchers` wraps `Dispatchers.IO`/`Default`. Injected via Koin. Only legitimate `Dispatchers.` references in production are this seam plus `DatabaseModule.setQueryCoroutineContext(Dispatchers.IO)`.
- Repository `OfflineFirstPostingRepository`: suspend writes wrapped in `withContext(dispatchers.io)`; read Flows use `.map {}.flowOn(dispatchers.io)`. This is the correct pattern — keep new repo methods consistent.
- `asResult()` in `core:common` = `map -> Success`, `onStart -> Loading`, `catch -> Error`. catch is last, so it covers map+upstream. ViewModels map DataResult to a sealed UiState.
- ViewModels use `viewModelScope` + `stateIn(WhileSubscribed(5_000), initialValue = Loading)`. Retry implemented via `MutableStateFlow<Int>` + `flatMapLatest`.
- One-shot events (`deletedEvent`, `navigationEvent`) use `Channel(BUFFERED).receiveAsFlow()`, collected in screen `LaunchedEffect`.
- `PostingEditViewModel.loadPosting()` deliberately uses `.first()` on the Room flow (one-shot snapshot) so retry does not stack never-completing collectors. Documented in-code.

**Why:** orient future reviews quickly to the canonical wiring so deviations stand out.
**How to apply:** when reviewing new async code, compare against these patterns; flag hardcoded dispatchers, GlobalScope, or DataResult bypasses.
