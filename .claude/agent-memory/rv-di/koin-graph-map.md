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

**Why @Provided is load-bearing (and CANNOT be restructured away):**
- Gradle dep direction is `core:data -> core:domain` (data depends on domain). The `PostingRepository` *interface* lives in core:domain; its Koin binding (`providePostingRepository` -> OfflineFirstPostingRepository) lives in DataModule in core:data. So `GetPostingsUseCase` etc. (core:domain) inject a binding that lives in a DOWNSTREAM module core:domain cannot depend on (would be a cycle). The Koin compiler plugin verifies each gradle module's KSP in isolation, so core:domain's check cannot see the PostingRepository binding. `@Provided` tells the per-module check "contributed by another module — don't fail." Removing it = compile failure OR a layering/dependency-cycle violation. STRUCTURAL, not a smell.
- ViewModel @Provided (use cases from core:domain): even though PostingModule includes DomainModule+DataModule so it resolves at runtime, Koin's per-gradle-module KSP check does not deep-resolve definitions generated in other gradle modules; @Provided marks those cross-module params. Restructuring includes does NOT remove the need.
- Verdict on sibling-review concern ("@Provided doc purpose = external/non-Koin dep"): doc headline is about external deps, but in Koin annotation multi-module setups @Provided is also the sanctioned marker for "binding contributed by a different KSP/gradle module." Usage here is correct. The android `provideRoomBuilder(@Provided context: Context)` is the textbook external-dep case (Context from `androidContext()`).
- Note: full-graph `verify()` in BootstrapModule does NOT declare the @Provided use-cases/repository as injected params (only @InjectedParam String + Logger's LoggerConfig) — because at full assembly those bindings are all present and resolve normally. Confirms @Provided only affects per-module compile-time check, not assembled runtime.

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
