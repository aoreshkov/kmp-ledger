# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Development Commands

```bash
# Run all tests across all platforms
./gradlew allTests

# Run tests for a specific module
./gradlew :core:domain:allTests
./gradlew :feature:posting:impl:allTests

# Run JVM target tests only (fastest, no emulator)
./gradlew jvmTest

# Run checks (includes linting and verification)
./gradlew check

# Generate aggregated code coverage report (HTML)
./gradlew koverHtmlReport

# Build & install Android debug APK
./gradlew :androidApp:installDebug

# Run Desktop (JVM) application
./gradlew :desktopApp:run

# Run Desktop with hot reload (JetBrains Runtime is provisioned on first use);
# :desktopApp:hotMcpServer exposes the same session to MCP clients (see .mcp.json)
./gradlew :desktopApp:hotRun

# iOS: open iosApp/iosApp.xcodeproj in Xcode
```

## Architecture

This is a Kotlin Multiplatform project targeting Android, iOS (arm64 + simulator), and Desktop (JVM). The architecture enforces strict unidirectional layering — dependencies only flow downward:

```
Platform Apps (androidApp, desktopApp, iosApp/iosExport)
    ↓
core:ui, core:navigation          ← App composable, theme, NavigationSuiteScaffold, NavDisplay
    ↓
feature:posting:impl   feature:settings:impl   ← Screens, ViewModels, DI
    ↓  (via api module only)        ↓
feature:posting:api    feature:settings:api    ← NavKey sealed types only, no logic
    ↓
core:domain                       ← Use cases (one class per operation); PostingRepository + SettingsRepository interfaces
    ↓
core:data            core:datastore            ← PostingRepository impl + mappers / DataStore-backed settings
    ↓
core:database                     ← Room 3 DAOs, entities, platform builders
```

Both repository interfaces are **declared in `core:domain` (`repository/`)**: `PostingRepository` is implemented in `core:data` (`OfflineFirstPostingRepository`), `SettingsRepository` in `core:datastore` (`DataStoreSettingsRepository`). The diagram shows call flow; the compile edge `core:data → core:domain` (and `core:test → core:domain`) is that dependency inversion, not an upward leak. Both flow only domain models upward.

Cross-cutting modules: `core:model` (pure domain types incl. `ThemeMode`), `core:common` (DataResult, logging), `core:compose` (shared UI components), `core:datastore` (DataStore-backed settings persistence), `core:bootstrap` (root Koin module), `core:test` (fakes, test utilities).

### Key Patterns

**DataResult + asResult()** — all async data in the UI layer flows through a sealed `DataResult<T>` (Loading / Success / Error). ViewModels call `flow.asResult()` and map to a sealed UI state (e.g. `PostingListUiState`). This pattern is in `core:common` and must be used consistently.

**Cancellation-safe result wrapping** — never use stdlib `runCatching` in suspend/coroutine code: it captures `CancellationException` and turns structured-concurrency cancellation into a spurious Error state. Use `runCatchingCancellable` from `core:common` (`result/RunCatchingCancellable.kt`) instead — a `suspend inline` helper (per kotlinx.coroutines#1814) that rethrows `CancellationException` and wraps every other `Throwable`. Applies to use cases and any one-shot suspend load.

**One-off ViewModel events are UI state** — ViewModels expose no `Channel`/`SharedFlow` events. Following Android's UI-events guidance, every outcome the screen must act on is a field in the sealed UI state, which a screen that is not composed at that moment (rotation, section switch) still sees when it returns. Messages carry an acknowledgement call that the screen makes once the snackbar has shown (`saveError` + `onSaveErrorShown()`, `deleteError` + `onDeleteErrorShown()`). Navigate-away flags (`isSaved`, `isDeleted`) carry none: the screen reacting to them pops its own entry, which clears its ViewModel, so the flag cannot fire twice. A flag that navigates *forward*, leaving its screen on the back stack, would need an acknowledgement. The one `SharedFlow` signal, `Navigator.reselections`, is not an exception to this rule. It is a UI-to-UI nudge that only the screen composed at that moment can act on (scroll to top), so dropping it when nothing collects is the correct behaviour.

**Feature API/Impl split** — `feature:posting:api` contains only `@Serializable` NavKey types. `feature:posting:impl` contains screens, ViewModels, and Koin DI. No other module may depend on `:impl`. Navigation between features goes through `:api` types only.

**Koin annotation-driven DI** — all dependencies use `@Module`, `@Factory`, `@Single`, `@KoinViewModel`. The Koin Compiler plugin validates the graph at compile time **only at the `@KoinApplication` entry points** — `androidApp`, `desktopApp`, `iosExport` — where it assembles the full `BootstrapModule` closure. There is no per-module validation: a library module compiled on its own gets code generation but no diagnostics, so a wiring error surfaces when you build an app module, not the library that broke it. The per-module net is the runtime `verify()` tests (`core:bootstrap`, `feature:*:impl`). All three entry points have compile safety on. Never use Koin DSL for domain/data/database modules. DSL is sanctioned only in the feature `*NavigationModule`s (e.g. `postingNavigationModule`, `settingsNavigationModule`) for exactly two things: (1) Compose `navigation<NavKey>` screen entries, and (2) contributing each feature's top-level nav item via `single(named("<feature>_top_level")) { TopLevelDestination(...) }`. The `named()` qualifier keeps the two definitions on distinct keys, so `getAll<TopLevelDestination>()` aggregates them instead of one overriding the other. That is a deliberate DSL choice, not a limitation of the annotations: koin-annotations **does** support multibinding — `@Named` is legal on a module function, and several `@Single @Named(...)` definitions of one type aggregate through the same `getAll()` the DSL generates underneath. These two stay in the DSL so each nav item sits beside the `navigation<NavKey>` entries it ships with, and loads with the nav modules passed at `startKoin` rather than joining the `BootstrapModule` closure. Only `navigation<NavKey>` has no annotation equivalent at all.

**Expect/Actual for platform database** — `PlatformDatabaseModule` is an `expect class` in `commonMain`. Each platform provides the `RoomDatabase.Builder<LedgerDatabase>` with OS-appropriate paths.

**Expect/Actual for platform DataStore** — settings persist via AndroidX **DataStore Preferences** in `core:datastore`. `PlatformDataStoreModule` is an `expect class` in `commonMain` with Android/iOS/JVM actuals that each supply the absolute file path for `ledger.preferences_pb` (JVM resolves the same OS-aware data dirs as the Room DB). The shared `createPreferencesDataStore` factory installs a `ReplaceFileCorruptionHandler` and `DataStoreSettingsRepository` recovers from read `IOException`s by emitting `emptyPreferences()`; an unknown/missing stored value falls back to `ThemeMode.SYSTEM`.

**Adaptive top-level navigation** — the app shell in `core:ui` (`App()`) renders a `NavigationSuiteScaffold` (bottom bar / rail / drawer by window size) wrapping `NavDisplay`. Each feature contributes its own `TopLevelDestination` (in `core:navigation`) through DI; the shell aggregates them with `getKoin().getAll<TopLevelDestination>().sortedBy { it.order }` and never references feature routes directly. `Navigator` holds **one `NavBackStack` per section** keyed by section root: `switchTopLevel` preserves each section's stack, and re-selecting the current section never touches it. The re-select emits on `Navigator.reselections` instead, and the visible screen scrolls to its top (the posting list does), as in the Navigation 3 multiple-back-stacks recipe. Resetting to the root would let a double tap on another section switch to it and then discard the stack the switch just restored, an unsaved edit included. Meanwhile `goBack` is exit-through-home (pop within section, then fall back to the start section). Screens never call the unkeyed `goBack()`: they pass their own entry key, `goBack(from = route)`, which acts only while that entry is on top, so a double tap or a tap racing a state-driven navigation cannot pop the screen beneath. Only system back (`NavDisplay.onBack`, invoked once per popped entry) uses `goBack()`. `goTo` is single-top: a no-op while the destination is already on top, so a repeat tap cannot stack a duplicate entry (which would share the first entry's content key, ViewModel and saved state). In the two-pane list-detail layout the scene never transitions, so only this back-stack check catches it. Inactive sections keep their ViewModels/saved UI state alive via per-section entry decorators.

**Theme preference flow** — `App()` collects `GetThemeModeUseCase()` with `collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)` and wraps content in `LedgerTheme(themeMode)`, which resolves `SYSTEM` via `isSystemInDarkTheme()`. DataStore reads are async, so a cold start may briefly show the system theme before the stored preference loads.

**No mocking — fakes only** — `core:test` provides `FakePostingRepository`, a full in-memory implementation backed by `MutableStateFlow`. Unit tests use `UnconfinedTestDispatcher` set as the main dispatcher in `@BeforeTest`.

**Entities never cross layer boundaries** — DAOs return `PostingEntity`, mappers in `core:data` convert to `Posting`/`NewPosting` before returning. Domain models only flow upward.

### Navigation

Uses Navigation 3 (`androidx.navigation3`). Screens are registered as Koin entries using `navigation<NavKey>` DSL in each feature's `*NavigationModule` (e.g. `postingNavigationModule`, `settingsNavigationModule`). Screens obtain ViewModels via `koinViewModel()`. Navigation actions use `LocalNavigator.current` (a `CompositionLocal` wrapping a `Navigator` that manages the `NavBackStack`).

### Convention Plugins (build-logic)

Three composable Gradle plugins — modules declare one of these instead of configuring targets manually:
- `ledger.kotlin.multiplatform` — base KMP, JVM 21, kotlin-test, Kover
- `ledger.kotlin.multiplatform.koin` — adds Koin core, annotations, compiler plugin
- `ledger.kotlin.multiplatform.koin.compose` — adds Compose Multiplatform, resources, UI test, `core:test` dependency

### Versions and pins

All versions live in `gradle/libs.versions.toml`; read them there rather than trusting a copy. The deliberate pins (Material3/Adaptive prereleases aligned to Compose Multiplatform, AGP held at the IntelliJ IDEA ceiling, no Gradle 10 until AGP moves) are explained in `.claude/rules/build-pins.md`, which loads when you touch build files.

## Module Conventions

- Database entities live in `core:database`, never elsewhere.
- Settings persistence: the `SettingsRepository` interface lives in `core:domain` (`repository/`), next to `PostingRepository`, and is implemented in `core:datastore` (`DataStoreSettingsRepository`). The DataStore file is `ledger.preferences_pb`, stored under the same OS-aware data directories as the Room database.
- Area rules in `.claude/rules/` load automatically when you work on matching files: Room migration posture (`room-migrations.md`: bumping the DB version currently **drops all data**), Compose stability config (`compose-stability.md`), Kover coverage policy (`kover-coverage.md`), and `packageOfResClass` pinning (`compose-resources.md`).
- Use cases in `core:domain` each take exactly one repository method as their primary action.
- iOS entry point (`iosExport`) uses Swift Export (Alpha; direct Kotlin→Swift, no Objective-C bridging; the DSL is still gated behind `@OptIn(ExperimentalSwiftExportDsl)`). Swift calls `initializeKoin()` before `MainViewController`.
- Binary-compatibility-validator guards every module's public API. `check` runs `apiCheck` (both `jvmApiCheck` and `klibApiCheck`) against the committed dumps in `<module>/api/`, so **changing a public API fails CI until you run `./gradlew apiDump` and commit the updated `*/api/` files** alongside the code change. No public API change → no action needed. Compose's `ComposableSingletons$<File>Kt` holders are excluded from the JVM dumps by an explicit `ignoredClasses` list in the root `build.gradle.kts` (BCV has no wildcards, and KGP's glob-capable `abiValidation` is still experimental). If one appears in a dump diff, add it to that list instead of committing it.

## Deliberate choices — not defects

Reviews keep re-raising these. Each one is intentional; flag it only if the stated condition changes.

- `feature:*:impl → core:data` / `core:datastore` build edges exist only so the feature Koin module can `includes` the data module (`PostingModule`, `SettingsModule`). A defect would be impl code importing concrete data types (repository impls, mappers, entities).
- `SavePostingUseCase` branching on `id == null` between insert and update is one operation ("persist a posting"), not a breach of one-repository-method-per-use-case.
- `PostingDao.getAllPostings()` is unbounded and maps every row on each emission. That is fine for a single-user local ledger; pagination is needed only if the product scope moves to large datasets.
- `PostingEditViewModel.loadPosting()` takes `.first()` of the Room flow on purpose: a one-shot snapshot, so a retry does not stack never-completing collectors.
- `core:bootstrap` adds `src/jvmTest/kotlin` to `androidHostTest` via `srcDir` so the same JVM tests also run on the Android host.
- `compose-material-icons` has its own version line, separate from `compose-multiplatform`: it is the JetBrains icons package pin, not a stale lag.
- `release.yml` uses `cancel-in-progress: false` (unlike the PR workflows) so a release is never cancelled mid-flight.
