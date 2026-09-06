---
name: koin-compiler-plugin-currency
description: Upstream-currency baseline for kmp-ledger's Koin setup — compiler plugin (not KSP), @Provided is a pinned choice, verify() retention justified, and the annotation-multibinding correction
metadata:
  type: reference
---

Durable upstream facts for auditing this repo's Koin DI against insert-koin.io.
**Read the pins yourself every run** (`gradle/libs.versions.toml`: `koin`, `koin-compiler`, `kotlin`).
Rule, not numbers: `koin` drives every `io.insert-koin:*` artifact; `koin-compiler` versions
independently and drives the `io.insert-koin.compiler.plugin` Gradle plugin only.

**Version currency (re-verified 2026-09-06 against Maven Central metadata, not memory):**
`koin` runtime line — latest release equals what the repo pins; Central metadata `<lastUpdated>`
for `io.insert-koin:koin-core` was 2026-06-15. Compiler plugin — latest release equals what the
repo pins; the plugin marker's `<release>` was published 2026-07-29. Check both here each run:
- https://repo1.maven.org/maven2/io/insert-koin/koin-core/maven-metadata.xml
- https://repo1.maven.org/maven2/io/insert-koin/compiler/plugin/io.insert-koin.compiler.plugin.gradle.plugin/maven-metadata.xml
Plugin release history: 1.0.0 (2026-05-20), 1.0.1 (2026-06-12), 1.0.2 (2026-07-10), 1.1.0 (2026-07-29).

**Kotlin compatibility — upstream contradicts itself, so check both.** The plugin README (main)
says one artifact spans **Kotlin 2.3.20 → 2.4.x**; the 1.1.0 release note's own Compatibility
section says "verified range 2.3.0–2.3.10". README is the one consistent with 1.0.1's changelog
(the version-adapter layer). Repo is on Kotlin 2.4.0 and builds. Plugin requires Koin 4.2.0+.
If the "unverified Kotlin version" warning ever fires, the knob is `versionCheckSeverity` —
docs say only mute it after assessing the risk yourself, so leaving it at the "warning" default
(as the repo does) is the correct posture, not a gap.

**1.1.0 validation model (this superseded the old A2/A3/A4 tiering; site docs still lag and
describe A2 per-module validation — ignore that page's tiering).** Per-module validation was
**removed entirely**; full-graph validation at `startKoin`/`koinApplication`/`@KoinApplication`
is the sole compile-safety verifier. A compilation with no entry point gets generation only —
`KOIN-D001/D004/D005/D006/P001` go silent there. So "per-module validation would catch X" is
never a valid claim about this repo. `KOIN-W002` deleted; `KOIN-D008` (hint-name collision)
added; `KOIN-W003` = dynamically-computed module set. `strictSafety` is now **mandatory** once an
aggregator is auto-detected — a plain `strictSafety = false` is ignored, and the explicit escape
hatch is `strictSafetyForceOff = true`. Gradle knobs: `compileSafety`, `strictSafety`,
`strictSafetyForceOff`, `logSeverity`, `versionCheckSeverity`, `userLogs`, `debugLogs`,
`unsafeDslChecks`, `skipDefaultValues` (+ undocumented `aiAssist`, which the repo sets false).
The repo's `logSeverity = "info"` in the koin convention plugin is exactly the documented remedy
for the new per-library-module "validation skipped" disclosure — current, do not flag.
Sources: https://github.com/InsertKoinIO/koin-compiler-plugin/releases/tag/1.1.0 ,
https://insert-koin.io/docs/setup/compiler-plugin/ , https://insert-koin.io/docs/reference/koin-annotations/options/

**Known 1.1.0 bug affecting this repo — don't re-diagnose.** Full-graph validation ignores the
`providerOnly` flag on a DSL `single<T> { … }` whose lambda builds `T`, so it walks `T`'s
constructor and reports those params missing. `desktopApp` keeps `compileSafety = false` for this
(DesktopUiTest's `RoomDatabase.Builder` override). NOT the multi-module false positive 1.0.2/1.1.0
fixed. Upstream InsertKoinIO/koin-compiler-plugin#83; `docs/2026-08-01-koin-compiler-1.1.0-upgrade.md`.

**Compiler plugin, not KSP** (`io.insert-koin.compiler.plugin`, applied in
`build-logic/src/main/kotlin/ledger.kotlin.multiplatform.koin.gradle.kts`). KSP args like
`KOIN_CONFIG_CHECK` are N/A. `koin-ksp-compiler` is the deprecated path per the 4.2 release notes.
Repo's catalog/plugin/dependency shape matches the documented setup verbatim.

**`@Provided` — PINNED PROJECT DECISION, never recommend removal.** Docs define it as "external
dependency (skip validation)". The repo also uses it on cross-module use-case/ViewModel constructor
params; per user memory it is load-bearing (verification fails without it). Note the *reason*
originally given (A2 per-module validation can't see other Gradle modules) no longer exists as of
1.1.0 — if it ever comes up, re-test empirically rather than reasoning from A2. Report only the
consequence, informationally: those edges are excluded from compile-time validation, so the docs'
"compile safety replaces verify()" line does not apply to this repo.

**`verify()` retention is justified here (not a currency gap).** `Module.verify()` in koin-test
4.2.2 carries **no `@Deprecated`** (checked in the tag's `VerifyModule.kt`) — only `checkModules()`
was replaced by the Verify API. Runtime `verify()` remains the only check for (a) `@Provided`-excluded
edges and (b) the DSL navigation modules, which are passed at `startKoin` and are not in the
`@KoinApplication(modules=[BootstrapModule])` closure. Tests: `core/bootstrap` and both
`feature/*/impl` jvmTest `di/KoinModule*Test.kt`.

**CORRECTED 2026-09-06 — "Koin annotations have no multibinding" is FALSE at 4.2.2.** The earlier
note (and CLAUDE.md) justified the DSL `single(named("<feature>_top_level")) { TopLevelDestination(…) }`
by claiming two `@Single TopLevelDestination` would override each other. Overriding only happens on an
identical (type, qualifier) key. `@Named` targets `CLASS, FUNCTION, VALUE_PARAMETER` in
koin-annotations 4.2.2 (`CoreAnnotations.kt`), the definitions docs document the aggregation pattern
(several `@Single @Named(...)` of one type, generated as `getAll()`), and 1.1.0 added regression
coverage for `@Named` qualifier matching across Gradle module boundaries. So an annotated
`@Single @Named("posting_top_level") fun …(): TopLevelDestination` is valid and would be picked up by
the app shell's `getKoin().getAll<TopLevelDestination>()`. Only `navigation<T>` genuinely has no
annotation equivalent. Source: https://insert-koin.io/docs/reference/koin-annotations/definitions/
Caveat before recommending again: moving the definitions into `PostingModule`/`SettingsModule`
changes *when* they load (BootstrapModule closure vs. the nav modules passed at `startKoin`) — that
consequence is rv-di's call, not a currency judgement.

**Verified current at 4.2.2 / plugin 1.1.0 — do not flag (re-checked 2026-09-06 against the 4.2.2
tag sources, not docs prose):** `@KoinViewModel` imported from `org.koin.core.annotation` (the
`org.koin.android.annotation` one is the deprecated path); `@InjectedParam` + `parametersOf` at the
call site; `@Module`/`@ComponentScan(pkg)`/`@Single`/`@Factory`; `@KoinApplication` + typed
`startKoin<LedgerApp>` (`org.koin.plugin.module.dsl.startKoin`) in all three entry points;
`koinInject`/`koinViewModel`/`getKoin()` (**`getKoin()` is NOT deprecated** — only
`LocalKoinApplication`/`LocalKoinScope` are, at ERROR level); `KoinIsolatedContext` (not deprecated);
`koinConfiguration<T>` + `KoinApplication(configuration = …)` in `core/ui` AppTest — note the
`KoinApplication(application = …)` lambda overload **is** `@Deprecated(WARNING)` at 4.2.2, the repo
already avoids it; nav3 = `navigation<T>(metadata = …)` DSL + `koinEntryProvider<NavKey>()`, both
still `@KoinExperimentalAPI` so the `@OptIn`s are required; `getAll<T>()` is explicitly sorted at
the call site in `core/ui/App.kt`, which 4.2 requires (`getAll` no longer sorts by default).
No Koin scopes are used anywhere in the repo, so the scope docs have no surface here.
