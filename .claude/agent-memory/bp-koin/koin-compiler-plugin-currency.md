---
name: koin-compiler-plugin-currency
description: Upstream-currency baseline for kmp-ledger's Koin setup — compiler plugin (not KSP), @Provided semantics, verify() superseded
metadata:
  type: reference
---

Durable upstream facts for auditing this repo's Koin DI against insert-koin.io. Pinned: Koin 4.2.2, compiler plugin 1.0.1, Kotlin 2.4.0.

**This repo uses the new Koin Kotlin Compiler Plugin** (`io.insert-koin.compiler.plugin`), NOT the old KSP processor. Applied in `build-logic/.../ledger.kotlin.multiplatform.koin.gradle.kts`. Compiler plugin `1.0.1` (released 2026-06-12) is the latest stable and the one that added Kotlin 2.4.0 support; plugin needs Kotlin >= 2.3.20 (K2 only), Koin >= 4.2.1. So versions are CURRENT as of the 2026-06 review.

**`@Provided` semantics (official):** "External dependency (skip validation)." It is meant ONLY for types NOT managed by the Koin graph — DSL-supplied or framework types (e.g. Android `Context` from `androidContext()`). It excludes the parameter from compiler auto-wiring AND from compile-time validation.
- CORRECT use in repo: `provideRoomBuilder(@Provided context: Context)` in `PlatformDatabaseModule.android.kt`.
- MISUSE pattern in repo: `@Provided` placed on cross-module annotation-managed deps (use cases get `@Provided repository: PostingRepository`; ViewModels get `@Provided getXxxUseCase`). Those ARE `@Single`/`@Factory` in the graph. The authors appear to use `@Provided` to mean "comes from another module I include" — that is wrong and silently disables the compile-time safety net on those edges. Fix: leave such params un-annotated (auto-wired); full-graph (A3) validation at `startKoin<T>()` covers cross-module deps. Defer module-include restructuring mechanics to the [[rv-di]] agent.
- Docs: https://insert-koin.io/docs/intro/koin-compiler-plugin/ and .../reference/koin-annotations/definitions/

**`verify()` is superseded** by the compiler plugin. Docs (koin-test/verify): "You can safely remove your `verify()` and `checkModules()` tests after enabling the compiler plugin." Repo still has two runtime `verify()` tests (core/bootstrap, feature/posting/impl). CAVEAT: while the `@Provided` misuse above is in place, those verify() tests are the only thing validating the opted-out edges — so removing verify() is only safe AFTER the `@Provided` misuse is fixed.

**Current & correct in repo (do not flag):** `@KoinViewModel`, `@Module`/`@ComponentScan`/`@Single`/`@Factory`, `@KoinApplication` + typed `startKoin<LedgerApp>()` (`org.koin.plugin.module.dsl.startKoin`), `koinViewModel()` from koin-compose-viewmodel, `navigation<NavKey>` DSL + `parametersOf` + `rememberViewModelStoreNavEntryDecorator` from koin-compose-navigation3. DSL is confined to `postingNavigationModule` per project rule.
