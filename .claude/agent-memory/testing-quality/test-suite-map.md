---
name: test-suite-map
description: kmp-ledger test suite layout, fakes/dispatcher conventions, Kover floors, and known coverage characteristics
metadata:
  type: project
---

Map of the kmp-ledger test suite as of 2026-06-23 review. Verify against current code before acting — files get renamed.

**Fakes (no mocking libraries; confirmed clean via grep).**
- `core:test` `FakePostingRepository` — MutableStateFlow-backed. Flags: `failNextWrite`, `shouldThrowOnGetAll`, `shouldThrowOnGetById`. Exposes `insertedPostings`/`deletedPostings`/`updatedPostings` lists.
- `core:test` `Fixtures.kt` — Object Mother: `posting()`, `newPosting()`, `postings(vararg)`, `postings(count)`.
- `OfflineFirstPostingRepositoryTest` has its own `FakePostingDao` + `TestAppDispatchers` (dispatcher seam injected).

**Dispatcher convention.** ViewModel/data unit tests create `private val testDispatcher = UnconfinedTestDispatcher()`, set it in `@BeforeTest fun setUp{ Dispatchers.setMain(testDispatcher) }`, reset in `@AfterTest`. One-shot Channel events asserted via `EventCollect.kt` `collectToList(flow, dispatcher)` launched in `backgroundScope` + `advanceUntilIdle()`. Compose screen tests use `StandardTestDispatcher` instead.

**Coverage map (use case / VM / mapper -> test).**
- Use cases: Save/Get/GetPostings/GetPosting/Delete all tested in `core:domain` commonTest incl. failure paths.
- ViewModels: List/Edit/Details VMs each test Loading->Success/Empty/Error/NotFound + retry + events.
- Mappers (`PostingMappers.kt asExternalModel/asEntity`) covered indirectly via `OfflineFirstPostingRepositoryTest` (no dedicated mapper test — acceptable, exercised through repo).
- NavKeys serialization: `feature:posting:api` PostingNavKeysTest (roundtrip, polymorphic, missing-field, equals branches).
- Navigator: `core:navigation` NavigatorTest.

**Kover floors.** Aggregate in root build.gradle.kts: LINE 88, BRANCH 60, INSTRUCTION 84. Per-module: core:data, core:domain, feature:posting:api each LINE 90 / BRANCH 85. `feature:posting:impl` (holds the ViewModels) has NO per-module floor — only the aggregate covers it. Excludes: `*ComposableSingletons*`, `*_Factory`, `*$$serializer`, `*.generated.resources.*`, `*.compose.resources.*`, `*_HiltModules*`, `*.di.*`, `@Preview`. See user auto-memory `kover-coverage-policy` for branch-floor rationale.
