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
core:domain                       ← Use cases (one class per operation); SettingsRepository interface
    ↓
core:data            core:datastore            ← PostingRepository impl + mappers / DataStore-backed settings
    ↓
core:database                     ← Room 3 DAOs, entities, platform builders
```

Two repository implementations plug in at different boundaries: `PostingRepository` is **declared in `core:data`** and implemented there (`OfflineFirstPostingRepository`); `SettingsRepository` is **declared in `core:domain` (`repository/`)** and implemented in `core:datastore` (`DataStoreSettingsRepository`). Both flow only domain models upward.

Cross-cutting modules: `core:model` (pure domain types incl. `ThemeMode`), `core:common` (DataResult, logging), `core:compose` (shared UI components), `core:datastore` (DataStore-backed settings persistence), `core:bootstrap` (root Koin module), `core:test` (fakes, test utilities).

### Key Patterns

**DataResult + asResult()** — all async data in the UI layer flows through a sealed `DataResult<T>` (Loading / Success / Error). ViewModels call `flow.asResult()` and map to a sealed UI state (e.g. `PostingListUiState`). This pattern is in `core:common` and must be used consistently.

**Cancellation-safe result wrapping** — never use stdlib `runCatching` in suspend/coroutine code: it captures `CancellationException` and turns structured-concurrency cancellation into a spurious Error state. Use `runCatchingCancellable` from `core:common` (`result/RunCatchingCancellable.kt`) instead — a `suspend inline` helper (per kotlinx.coroutines#1814) that rethrows `CancellationException` and wraps every other `Throwable`. Applies to use cases and any one-shot suspend load.

**Feature API/Impl split** — `feature:posting:api` contains only `@Serializable` NavKey types. `feature:posting:impl` contains screens, ViewModels, and Koin DI. No other module may depend on `:impl`. Navigation between features goes through `:api` types only.

**Koin annotation-driven DI** — all dependencies use `@Module`, `@Factory`, `@Single`, `@KoinViewModel`. The Koin Compiler plugin (1.1.0) validates the graph at compile time **only at the `@KoinApplication` entry points** — `androidApp`, `desktopApp`, `iosExport` — where it assembles the full `BootstrapModule` closure. There is no per-module validation: a library module compiled on its own gets code generation but no diagnostics, so a wiring error surfaces when you build an app module, not the library that broke it. The per-module net is the runtime `verify()` tests (`core:bootstrap`, `feature:*:impl`). `desktopApp` additionally sets `compileSafety = false` for a plugin bug documented in its build file, so `androidApp` is the compile-time check that matters in practice. Never use Koin DSL for domain/data/database modules. DSL is sanctioned only in the feature `*NavigationModule`s (e.g. `postingNavigationModule`, `settingsNavigationModule`) for exactly two things: (1) Compose `navigation<NavKey>` screen entries, and (2) contributing each feature's top-level nav item via `single(named("<feature>_top_level")) { TopLevelDestination(...) }`. The `named()` qualifier is required because Koin annotations have **no multibinding** — two `@Single TopLevelDestination` would override each other instead of aggregating, whereas DSL `single(named(...))` + `getAll<TopLevelDestination>()` returns every definition regardless of qualifier.

**Expect/Actual for platform database** — `PlatformDatabaseModule` is an `expect class` in `commonMain`. Each platform provides the `RoomDatabase.Builder<LedgerDatabase>` with OS-appropriate paths.

**Expect/Actual for platform DataStore** — settings persist via AndroidX **DataStore Preferences** in `core:datastore`. `PlatformDataStoreModule` is an `expect class` in `commonMain` with Android/iOS/JVM actuals that each supply the absolute file path for `ledger.preferences_pb` (JVM resolves the same OS-aware data dirs as the Room DB). The shared `createPreferencesDataStore` factory installs a `ReplaceFileCorruptionHandler` and `DataStoreSettingsRepository` recovers from read `IOException`s by emitting `emptyPreferences()`; an unknown/missing stored value falls back to `ThemeMode.SYSTEM`.

**Adaptive top-level navigation** — the app shell in `core:ui` (`App()`) renders a `NavigationSuiteScaffold` (bottom bar / rail / drawer by window size) wrapping `NavDisplay`. Each feature contributes its own `TopLevelDestination` (in `core:navigation`) through DI; the shell aggregates them with `getKoin().getAll<TopLevelDestination>().sortedBy { it.order }` and never references feature routes directly. `Navigator` holds **one `NavBackStack` per section** keyed by section root: `switchTopLevel` preserves each section's stack (re-selecting the current section resets it to root), and `goBack` is exit-through-home (pop within section, then fall back to the start section). Inactive sections keep their ViewModels/saved UI state alive via per-section entry decorators.

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

### Tech Stack Versions

| Technology | Version |
|---|---|
| Kotlin | 2.4.0 |
| Compose Multiplatform | 1.11.1 |
| Koin | 4.2.2 |
| Room | 3.0.0 |
| Navigation 3 | 1.1.4 (runtime) / 1.1.1 (ui) |
| AndroidX DataStore | 1.2.1 |
| Coroutines | 1.11.0 |
| Kover | 0.9.9 |

## Module Conventions

- Database entities live in `core:database`, never elsewhere.
- Settings persistence: the `SettingsRepository` interface lives in `core:domain` (`repository/`) and is implemented in `core:datastore` (`DataStoreSettingsRepository`) — unlike `PostingRepository`, whose interface lives in `core:data`. The DataStore file is `ledger.preferences_pb`, stored under the same OS-aware data directories as the Room database.
- Room migration posture (pre-release): `DatabaseModule.provideDatabase` uses `.fallbackToDestructiveMigration(dropAllTables = true)`, so bumping the `@Database` `version` on `LedgerDatabase` **drops and recreates all data**. Before shipping real user data, replace this with explicit `Migration` objects plus a CI check that the exported schema dir changed on the version bump.
- Use cases in `core:domain` each take exactly one repository method as their primary action.
- Kover excludes generated classes automatically: `*ComposableSingletons*`, `*_Factory`, `*$serializer`, `*.generated.resources.*`, `*.compose.resources.*`, `@Preview`-annotated methods. It also excludes `*.di.*` packages — DI wiring is validated by Koin `verify()`, not by execution, so it stays out of coverage.
- iOS entry point (`iosExport`) uses Swift Export (Alpha as of Kotlin 2.4.0, direct Kotlin→Swift, no Objective-C bridging; the DSL is still gated behind `@OptIn(ExperimentalSwiftExportDsl)`). Swift calls `initializeKoin()` before `MainViewController`.
- Binary-compatibility-validator guards every module's public API. `check` runs `apiCheck` (both `jvmApiCheck` and `klibApiCheck`) against the committed dumps in `<module>/api/`, so **changing a public API fails CI until you run `./gradlew apiDump` and commit the updated `*/api/` files** alongside the code change. No public API change → no action needed.
