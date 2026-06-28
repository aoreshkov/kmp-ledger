---
name: test-suite-map
description: kmp-ledger test suite layout, fakes/dispatcher conventions, Kover floors, and known coverage characteristics
metadata:
  type: project
---

Map of the kmp-ledger test suite as of 2026-06-24 review. Verify against current code before acting — files get renamed.

**Fakes (no mocking libraries; confirmed clean via grep).**
- `core:test` `FakePostingRepository` — MutableStateFlow-backed. Flags: `failNextWrite`, `shouldThrowOnGetAll`, `shouldThrowOnGetById`. Exposes `insertedPostings`/`deletedPostings`/`updatedPostings` lists.
- `core:test` `Fixtures.kt` — Object Mother: `posting()`, `newPosting()`, `postings(vararg)`, `postings(count)`.
- `OfflineFirstPostingRepositoryTest` has its own `FakePostingDao` + `TestAppDispatchers` (dispatcher seam injected).

**Dispatcher convention.** ViewModel/data unit tests create `private val testDispatcher = UnconfinedTestDispatcher()`, set it in `@BeforeTest fun setUp{ Dispatchers.setMain(testDispatcher) }`, reset in `@AfterTest`. One-shot Channel events asserted via `EventCollect.kt` `collectToList(flow, dispatcher)` launched in `backgroundScope` + `advanceUntilIdle()`. Compose screen tests use `StandardTestDispatcher` instead.

**Two-scheduler arrangement — assessed SOUND (2026-06-28).** `testDispatcher` is built standalone, so it has its own scheduler distinct from runTest's. VM coroutines run on Main(=testDispatcher); event collectors are `launch(testDispatcher)` inside `backgroundScope`; tests drain via `testDispatcher.scheduler.advanceUntilIdle()`. Advancing testDispatcher's scheduler (not runTest's) is correct because that is where the producer emits. Side effect: runTest's leftover-coroutine check never sees the VM's `stateIn(WhileSubscribed(5000))` collector (separate scheduler), which is why these tests don't trip the "uncompleted coroutines" error. Events are `Channel(BUFFERED)` so pre-subscription sends are retained — subscription ordering can't drop events. No flakiness: fresh `FakePostingRepository`/use cases per test instance, no shared static state.

**Coverage map (use case / VM / mapper -> test).**
- Use cases: Save/Get/GetPostings/GetPosting/Delete all tested in `core:domain` commonTest incl. failure paths.
- ViewModels: List/Edit/Details VMs each test Loading->Success/Empty/Error/NotFound + retry + events. Details VM also covers delete success/failure: deletedEvent fires only onSuccess, deleteFailedEvent fires onFailure (both directions asserted — added commit adca851).
- Validation: `PostingEditUiState.Editing.isValid`/`narrativeError` derived props tested directly in `validation/PostingValidationTest.kt`.
- Mappers (`PostingMappers.kt asExternalModel/asEntity`) covered indirectly via `OfflineFirstPostingRepositoryTest` (no dedicated mapper test — acceptable, exercised both directions through repo).
- NavKeys serialization: `feature:posting:api` PostingNavKeysTest (roundtrip, polymorphic, missing-field, equals branches).
- Navigator: `core:navigation` NavigatorTest. Common helpers: DataResultTest, RunCatchingCancellableTest, AppDispatchersTest, UuidTest (all core:common commonTest).
- Koin graph verify(): `feature:posting:impl` jvmTest KoinModuleTest (PostingModule + postingNavigationModule), `core:bootstrap` jvmTest KoinModuleVerificationTest.

**Kover floors.** Aggregate in root build.gradle.kts: LINE 88, BRANCH 60, INSTRUCTION 84. Per-module: core:data, core:domain, feature:posting:api each LINE 90 / BRANCH 85; feature:posting:impl LINE 90 / BRANCH 60 (lower branch floor — Compose codegen synthetic branches in @Composable screens; added commit 9c7a10f). Other modules (common, database, navigation, ui, compose, model, bootstrap) carry no per-module floor — aggregate-only. Excludes: `*ComposableSingletons*`, `*_Factory`, `*$$serializer`, `*.generated.resources.*`, `*.compose.resources.*`, `*.di.*`, `@Preview`. The `*.di.*` exclusion covers Koin @Module classes AND `postingNavigationModule` (a real DSL block with @Composable nav lambdas in PostingModule.kt) — defensible: lambdas are Compose-only, module wiring validated by verify(). See user auto-memory `kover-coverage-policy` for branch-floor rationale.
