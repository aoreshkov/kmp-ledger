---
name: kmp-structure-posture
description: kmp-ledger's KMP source-set/target/expect-actual/swift-export posture and which choices are deliberate-and-current (re-verified 2026-09-06 against Kotlin 2.4.0 docs + KGP v2.4.0 sources)
metadata:
  type: project
---

kmp-ledger KMP structure re-verified 2026-09-06 against the pins read that day
(`kotlin = 2.4.0`, `kotlinx-binary-compatibility-validator = 0.18.2`). Treat every
version below as a **dated observation**, not a standing fact — re-read
`gradle/libs.versions.toml` first on each run.

- **Targets**: `iosArm64()`, `iosSimulatorArm64()`, `jvm()`, plus Android via the AGP KMP
  library plugin `com.android.kotlin.multiplatform.library` with the `android { }` DSL
  (`extensions.configure<KotlinMultiplatformAndroidLibraryTarget>("android")`,
  `withHostTest {}`) in `build-logic/src/main/kotlin/ledger.kotlin.multiplatform.gradle.kts`.
  Current, not deprecated. Grep-verified 2026-09-06: no build script uses any API the
  Kotlin 2.4.0 compatibility guide removed (`targetHierarchy`, `KotlinTarget.sourceSets`,
  `compileKotlinTask*`, `enforcedPlatform`/`platform`, `withoutCompilations`,
  `defaultSourceSetName`, `kotlinArtifacts`, `js(IR|LEGACY)`, native task
  `languageSettings`/`additionalCompilerOptions`/`konanHome`).
- **Source-set hierarchy**: relies on the **default hierarchy template** implicitly — no
  `applyDefaultHierarchyTemplate()` call and no manual `dependsOn` anywhere. That is what
  the docs prescribe; the explicit call is only needed when adding custom source sets.
  Hierarchy doc last modified 16 March 2026. Do not propose manual wiring.
- **expect/actual**: expect/actual **classes are Beta** and `-Xexpect-actual-classes` is
  still the documented way to silence the warning (expect/actual doc, 13 May 2026). The
  project sets it in module-level `kotlin { compilerOptions { } }` in core:database,
  core:datastore, core:test and androidApp — exactly the doc's shape.
  The doc *does* say "use interfaces where they'd be sufficient", but every expect here
  survives that test: `PlatformDatabaseModule` / `PlatformDataStoreModule` (the class **is**
  the Koin `@Module` — deliberate, never flag), `PlatformComposeUiTest` (carries platform
  `@RunWith(RobolectricTestRunner)`/`@Config` annotations — not expressible as an interface),
  `getPlatformLogWriters()` and `createTestDatabase()` (expect *functions*, i.e. the doc's
  own recommended factory shape), `LedgerDatabaseConstructor` (required by Room KMP).
- **Swift export**: **Alpha** (doc last modified 12 August 2026). `@ExperimentalSwiftExportDsl`
  **still exists in KGP v2.4.0 sources** (verified via GitHub at tag v2.4.0), so
  `iosExport/build.gradle.kts`'s `@OptIn(ExperimentalSwiftExportDsl::class)` is correct even
  though the doc snippet omits it. Xcode run script calls `:iosExport:embedSwiftExportForXcode`
  (direct integration — the only mode swift export supports). `initializeKoin()` before
  `MainViewController()` — sound. iOS min deployment rose to 15.0 in 2.4.0; iosApp targets 18.2.
- **Swift-export gradle property (open finding, 2026-09-06)**: `gradle.properties:24` sets
  `kotlin.swift-export.experimentalFeature.nowarn=true`, but the property KGP actually reads is
  **`kotlin.swift-export.experimental.nowarn`** (`PropertiesProvider.kt`, unchanged across
  v2.2.20 → v2.4.0; it feeds `SwiftExportTask.ignoreExperimentalDiagnostic`). The current name
  is inert. Note `kotlin.experimental.swift-export.enabled` is separately deprecated and is
  correctly absent here.
- **Swift-export dead config (Optional, still present)**: `iosExport/build.gradle.kts` declares
  `binaries.framework { baseName = "LedgerBinary" }` on both iOS targets; nothing consumes it
  (Xcode only calls `embedSwiftExportForXcode`) and Kotlin 2.4.0 removed the consumable
  configurations that exposed Apple frameworks as outgoing artifacts (KT-74503). Tracked as O3
  in docs/best-practices-review-2026-06-26-outstanding.md.
  **Corrected 2026-09-06:** the previously noted inert `swiftExport { }` block in
  `core/bootstrap/build.gradle.kts` is **gone** — that file no longer contains it. Do not
  re-report it.
- **BCV**: standalone plugin, pinned 0.18.2, which is the newest release (published 2026-09-02).
  KGP's built-in `abiValidation` DSL was *simplified* in 2.4.0 (KT-80685: `legacyDump`/`klib`
  nesting removed) but is still not the recommended replacement; the root `apiValidation { klib { … } }`
  block is the standalone plugin's own DSL and is unaffected. Rule: match the standalone plugin's
  newest release, don't migrate to KGP abiValidation until JetBrains marks it stable.

**Why:** avoids re-litigating settled, current choices on each currency audit.
**How to apply:** treat the above as current best practice *as re-derived on 2026-09-06*;
re-check any "latest"/"still exists" claim against the pins and primary sources before
reusing it. See [[ios-findings-advisory]].
