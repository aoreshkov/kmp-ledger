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
core:ui, core:navigation          ← App composable, theme, NavDisplay
    ↓
feature:posting:impl              ← Screens, ViewModels, DI
    ↓  (via api module only)
feature:posting:api               ← NavKey sealed types only, no logic
    ↓
core:domain                       ← Use cases (one class per operation)
    ↓
core:data                         ← PostingRepository impl + mappers
    ↓
core:database                     ← Room 3 DAOs, entities, platform builders
```

Cross-cutting modules: `core:model` (pure domain types), `core:common` (DataResult, logging), `core:compose` (shared UI components), `core:bootstrap` (root Koin module), `core:test` (fakes, test utilities).

### Key Patterns

**DataResult + asResult()** — all async data in the UI layer flows through a sealed `DataResult<T>` (Loading / Success / Error). ViewModels call `flow.asResult()` and map to a sealed UI state (e.g. `PostingListUiState`). This pattern is in `core:common` and must be used consistently.

**Cancellation-safe result wrapping** — never use stdlib `runCatching` in suspend/coroutine code: it captures `CancellationException` and turns structured-concurrency cancellation into a spurious Error state. Use `runCatchingCancellable` from `core:common` (`result/RunCatchingCancellable.kt`) instead — a `suspend inline` helper (per kotlinx.coroutines#1814) that rethrows `CancellationException` and wraps every other `Throwable`. Applies to use cases and any one-shot suspend load.

**Feature API/Impl split** — `feature:posting:api` contains only `@Serializable` NavKey types. `feature:posting:impl` contains screens, ViewModels, and Koin DI. No other module may depend on `:impl`. Navigation between features goes through `:api` types only.

**Koin annotation-driven DI** — all dependencies use `@Module`, `@Factory`, `@Single`, `@KoinViewModel`. The Koin Compiler plugin validates the graph at compile time. Never use Koin DSL for domain/data/database modules — only use DSL in `postingNavigationModule` for Compose navigation entries.

**Expect/Actual for platform database** — `PlatformDatabaseModule` is an `expect class` in `commonMain`. Each platform provides the `RoomDatabase.Builder<LedgerDatabase>` with OS-appropriate paths.

**No mocking — fakes only** — `core:test` provides `FakePostingRepository`, a full in-memory implementation backed by `MutableStateFlow`. Unit tests use `UnconfinedTestDispatcher` set as the main dispatcher in `@BeforeTest`.

**Entities never cross layer boundaries** — DAOs return `PostingEntity`, mappers in `core:data` convert to `Posting`/`NewPosting` before returning. Domain models only flow upward.

### Navigation

Uses Navigation 3 (`androidx.navigation3`). Screens are registered as Koin entries using `navigation<NavKey>` DSL in `postingNavigationModule`. Screens obtain ViewModels via `koinViewModel()`. Navigation actions use `LocalNavigator.current` (a `CompositionLocal` wrapping a `Navigator` that manages the `NavBackStack`).

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
| Room | 3.0.0-rc01 |
| Navigation 3 | 1.1.3 |
| Coroutines | 1.11.0 |
| Kover | 0.9.8 |

## Module Conventions

- Database entities live in `core:database`, never elsewhere.
- Use cases in `core:domain` each take exactly one repository method as their primary action.
- Kover excludes generated classes automatically: `*ComposableSingletons*`, `*_Factory`, `*$serializer`, `*.generated.resources.*`, `@Preview`-annotated methods.
- iOS entry point (`iosExport`) uses Swift Export (experimental, direct Kotlin→Swift, no Objective-C bridging). Swift calls `initializeKoin()` before `MainViewController`.
