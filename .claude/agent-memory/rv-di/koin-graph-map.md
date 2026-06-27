---
name: koin-graph-map
description: kmp-ledger Koin annotation graph — module include tree, scopes, platform actuals, DSL boundary, entry points
metadata:
  type: project
---

Snapshot of the kmp-ledger Koin DI graph (verify against code before acting; structure may drift).

**Root assembly:** `BootstrapModule` (core:bootstrap) is `@Module(includes = [AppModule, PostingModule])`.
- Provides `@Single StartDestination(PostingList)` and `@Single SavedStateConfiguration`.

**Include tree (annotation @Module includes):**
- AppModule (core:ui) -> LoggingModule (core:common, @Single Logger)
- PostingModule (feature:posting:impl) -> DataModule, DomainModule; plus `@ComponentScan("...feature.posting.impl")` picks up the 3 @KoinViewModel classes.
- DataModule (core:data) -> DatabaseModule, DomainModule, DispatcherModule; provides `@Single PostingRepository`.
- DomainModule (core:domain) -> `@ComponentScan("...core.domain")` picks up the 4 @Factory use cases.
- DatabaseModule (core:database) -> PlatformDatabaseModule (expect/actual); `@Single LedgerDatabase`, `@Single PostingDao`.
- DispatcherModule (core:common) -> `@Single AppDispatchers`.

Note: DomainModule is included by both DataModule and PostingModule. Koin annotation `includes` dedupes by type, so this is not a duplicate-binding bug.

**Scopes:** use cases are `@Factory` (stateless, one repo method each). Repository, DAO, DB, dispatchers, logger are `@Single` (stateless shared). ViewModels are `@KoinViewModel`. OfflineFirstPostingRepository holds no mutable state (only dao+dispatchers) — safe as @Single.

**ViewModels (feature:posting:impl):** PostingListViewModel (deps @Provided), PostingDetailsViewModel + PostingEditViewModel use `@InjectedParam` postingId (String / String?) passed via `parametersOf(route.id)` in nav DSL.

**DSL boundary:** the ONLY production DSL is `postingNavigationModule` (val module {} in PostingModule.kt) for Navigation3 `navigation<NavKey>` entries. It is NOT annotation-includable, so each platform entry point loads it explicitly via `modules(postingNavigationModule)`. Other `module {}/single {}` occurrences are test-only (core:ui AppTest, desktopApp DesktopUiTest) — allowed.

**Platform actuals:** `expect class PlatformDatabaseModule` in commonMain; actuals in android/jvm/ios each `@Module actual class` with `@Single provideRoomBuilder`. Android actual takes `@Provided context: Context` supplied by `androidContext()` at startup.

**Entry points (`@KoinApplication(modules = [BootstrapModule::class])` + startKoin):**
- androidApp/LedgerApp.kt (Application) — also `androidContext(this)`.
- desktopApp/main.kt.
- iosExport/MainViewController.kt `initializeKoin()` called from iosApp.swift.
All three additionally load `postingNavigationModule`.

**Compile-time validation:** Koin compiler plugin checks the annotated graph. Two runtime `.verify()` tests back it:
- `feature/posting/impl/src/jvmTest/.../KoinModuleTest.kt` — verifies `PostingModule().module()` and `postingNavigationModule`, declaring the String injected params.
- `core/bootstrap/src/jvmTest/.../KoinModuleVerificationTest.kt` — verifies the full `BootstrapModule().module()` (whole root graph).
