---
name: currency-optins
description: Which experimental @OptIn annotations in the codebase are justified for pinned Kotlin 2.4.0 / Coroutines 1.11.0 (don't flag as removable)
metadata:
  type: project
---

The two stdlib/coroutines experimental opt-ins in the repo are CORRECT for the pinned versions — do not propose removing them as "stale opt-ins."

- `@OptIn(ExperimentalUuidApi::class)` on `Uuid.random()` (core/common util/Uuid.kt): in Kotlin 2.4.0 the `kotlin.uuid.Uuid` class is Stable, BUT the V4/V7 generation functions (`Uuid.random()`) remain Experimental and still require opt-in. Verified 2026-06-26 against https://kotlinlang.org/docs/whatsnew24.html
- `@OptIn(ExperimentalCoroutinesApi::class)` for `flatMapLatest` (PostingListViewModel, PostingDetailsViewModel): still `@ExperimentalCoroutinesApi` in kotlinx.coroutines 1.11.0. Verified 2026-06-26 against https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/flat-map-latest.html

**Why:** "latest" is time-sensitive; re-confirm against the same docs if Kotlin/Coroutines versions are bumped. If Uuid generation graduates (post-2.4.0) the Uuid.kt opt-in becomes removable; if flatMapLatest graduates the ViewModel opt-ins become removable.
**How to apply:** Only revisit these when the pinned versions in CLAUDE.md / libs.versions.toml change. Re-fetch the docs before asserting either way.
