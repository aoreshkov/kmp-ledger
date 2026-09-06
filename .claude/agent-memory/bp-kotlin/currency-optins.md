---
name: currency-optins
description: Which experimental @OptIn annotations in the codebase are justified for pinned Kotlin 2.4.0 / Coroutines 1.11.0 — and which are now stale (the Uuid one IS removable)
metadata:
  type: project
---

Status of every stdlib/coroutines/serialization experimental opt-in in the repo, against the pinned versions.

**STALE — flag this one as removable:**

- `@OptIn(ExperimentalUuidApi::class)` in `core/common/src/commonMain/kotlin/app/oreshkov/ledger/core/common/util/Uuid.kt:6` (plus the `kotlin.uuid.ExperimentalUuidApi` import on line 3) is **unnecessary** and should be reported as an Optional finding. `Uuid.random()` is Stable in Kotlin 2.4.0 — it carries no experimental marker and opts in internally: `public fun random(): Uuid = @OptIn(ExperimentalUuidApi::class) generateV4()` (stdlib 2.4.0, `commonMain/kotlin/uuid/Uuid.kt:581`). The `Uuid` class itself is `@SinceKotlin("2.4") @WasExperimental(ExperimentalUuidApi::class)` (`:53-55`), i.e. stable with no opt-in required **as long as the module's `apiVersion` is >= 2.4** — this repo sets no explicit `apiVersion`, so it defaults to 2.4 and removal is safe. Only the **generation** functions stay Experimental: `generateV4()` is `@SinceKotlin("2.3") @ExperimentalUuidApi` (`:617-619`), likewise `generateV7()` / `generateV7NonMonotonicAt()`. Re-verified 2026-09-06 (third confirmation) by unzipping `kotlin-stdlib-2.4.0-common-sources.jar` from the Gradle cache and reading the annotations, corroborated by https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.uuid/-uuid/-companion/random.html (shows the signature with no opt-in requirement) and https://kotlinlang.org/docs/whatsnew24.html ("the `kotlin.uuid.Uuid` API becomes Stable. The only exceptions are the functions for generating V4 and V7 UUIDs"). Still open in the code as of 2026-09-06.

  > **Correction of a prior note.** Through 2026-08 this file claimed the opposite — that "the V4/V7 generation functions (`Uuid.random()`) remain Experimental" — and told future runs not to flag it. That conflated `Uuid.random()` with `Uuid.generateV4()`; they are different declarations with different markers. The old claim suppressed a valid finding for three audit passes. Do not restore it.

**STILL JUSTIFIED — do not propose removing these:**

- `@OptIn(ExperimentalCoroutinesApi::class)` for `flatMapLatest` (`PostingListViewModel.kt:27`, `PostingDetailsViewModel.kt:32`): still `@ExperimentalCoroutinesApi` in kotlinx.coroutines 1.11.0. Re-verified 2026-09-06 against https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/flat-map-latest.html (page shows API version 1.11.0, signature annotated `@ExperimentalCoroutinesApi`).
- `@OptIn(ExperimentalSerializationApi::class)` on `subclassesOfSealed()` in the polymorphic `SerializersModule` (`PostingNavKeys.kt:21`, `SettingsNavKeys.kt:15`): still `@ExperimentalSerializationApi` in kotlinx-serialization-core 1.11.0. Re-verified 2026-09-06 against https://kotlinlang.org/api/kotlinx.serialization/kotlinx-serialization-core/kotlinx.serialization.modules/subclasses-of-sealed.html (API version 1.11.0).
- `@OptIn(ExperimentalSwiftExportDsl::class)` in `iosExport/build.gradle.kts:1`: still required in KGP 2.4.0. (Owned by [[kmp-structure-posture]] / bp-kmp; noted here so it is not double-flagged.)

**Why:** "still experimental?" is a per-declaration, per-version fact, and the marker on a *wrapper* says nothing about the marker on what it calls. Reading the release notes alone is what produced the wrong Uuid entry — the notes say "the functions for generating V4 and V7 UUIDs remain Experimental", which is true but is not a statement about `random()`.

**How to apply:** Re-check only when the pinned `kotlin` / `kotlinx-coroutines` / `kotlinx-serialization` versions in `gradle/libs.versions.toml` change. Verify by reading the declaration's own annotations in the published sources (the `kotlin-lib` MCP tools resolve these directly), not by inferring from a prose release note. Removing a stale `@OptIn` is source-only — no `apiDump` needed, since `@OptIn` has source retention and never appears in the `api/` dumps.
