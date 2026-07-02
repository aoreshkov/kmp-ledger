---
name: koin-compiler-plugin-currency
description: Upstream-currency baseline for kmp-ledger's Koin setup — compiler plugin (not KSP), @Provided is a pinned choice, verify() retention justified here
metadata:
  type: reference
---

Durable upstream facts for auditing this repo's Koin DI against insert-koin.io. Pinned: Koin 4.2.2, compiler plugin 1.0.1, Kotlin 2.4.0.

**Version currency (re-verified 2026-07-02):** Koin 4.2.2 is latest stable (github.com/InsertKoinIO/koin/releases); compiler plugin 1.0.1 (2026-06-12) is latest, adds Kotlin 2.4.0 support (github.com/InsertKoinIO/koin-compiler-plugin/releases). Repo pins both — CURRENT.

**This repo uses the new Koin Kotlin Compiler Plugin** (`io.insert-koin.compiler.plugin`), NOT the old KSP processor. Applied in `build-logic/src/main/kotlin/ledger.kotlin.multiplatform.koin.gradle.kts`. So KSP args like `KOIN_CONFIG_CHECK` are N/A — don't go looking for them. Validation tiers per docs: A2 per-module, A3 full graph at `startKoin<T>()`, A4 call-site (`get`/`inject`/`koinViewModel`). Docs: https://insert-koin.io/docs/intro/koin-compiler-plugin/

**`@Provided` — PINNED PROJECT DECISION, do not recommend removal.** Docs define it as "External dependency (skip validation)" (framework types like Android `Context`). The repo also uses it on cross-module use-case/ViewModel constructor params (14 sites in core/domain + feature impls). Per user project memory this is load-bearing: without it, compiler-plugin verification FAILS on those cross-Gradle-module edges (A2 per-module validation can't see definitions compiled in other Gradle modules). Report only the *consequence* as informational: those edges are excluded from compile-time validation, so the docs' "you can remove verify()" guidance does NOT apply to this repo.

**`verify()` retention is justified here (not a currency gap).** Docs say compile-time safety "replaces verify() and checkModules() — no runtime test harness needed", BUT in this repo runtime `verify()` remains the only check for (a) the `@Provided`-excluded edges and (b) the DSL navigation modules (DSL isn't compiler-validated). verify() tests live in: `feature/settings/impl/src/jvmTest/.../di/KoinModuleTest.kt`, `feature/posting/impl/src/jvmTest/.../di/KoinModuleTest.kt`, `core/bootstrap/src/jvmTest/.../di/KoinModuleVerificationTest.kt` (uses `injectedParameters`/`definition<TopLevelDestination>` for DSL literals). CLAUDE.md's Kover section documents this reliance.

**Current & correct in repo (do not flag):** `@KoinViewModel` + `@InjectedParam` for route ids + `parametersOf(route.id)` at call site; `@Module`/`@ComponentScan`/`@Single`/`@Factory`; `@KoinApplication(modules=[BootstrapModule::class])` + typed `startKoin<LedgerApp>` (`org.koin.plugin.module.dsl.startKoin`) in androidApp/desktopApp/iosExport `initializeKoin()`; `koinViewModel()` from koin-compose-viewmodel; nav3 integration = `navigation<NavKey>` DSL + `koinEntryProvider<NavKey>()` from koin-compose-navigation3, exactly matching https://insert-koin.io/docs/reference/koin-compose/navigation3 (still `@KoinExperimentalAPI` in code — docs don't mark it experimental, harmless). DSL is confined to `postingNavigationModule`/`settingsNavigationModule` (nav entries + `single(named("<feature>_top_level"))` multibinding workaround — annotations have no multibinding). expect-class `PlatformDatabaseModule`/`PlatformDataStoreModule` as `@Module` hosts work fine with the compiler plugin.
