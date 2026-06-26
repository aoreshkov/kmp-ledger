---
name: kmp-structure-posture
description: kmp-ledger's KMP source-set/target/expect-actual/swift-export posture and which choices are deliberate-and-current
metadata:
  type: project
---

kmp-ledger KMP structure is current against JetBrains guidance for the pinned Kotlin 2.4.0. Durable facts so future audits don't re-derive:

- **Targets**: `iosArm64()`, `iosSimulatorArm64()`, `jvm()`, plus Android via the **new AGP KMP library plugin** `com.android.kotlin.multiplatform.library` with the `android { }` DSL (configured in convention plugins via `extensions.configure<KotlinMultiplatformAndroidLibraryTarget>("android")`). This is the modern replacement for `com.android.library` + `androidTarget()` — current, not deprecated.
- **Source-set hierarchy**: relies on the **default hierarchy template** implicitly. No `applyDefaultHierarchyTemplate()` call and no manual `dependsOn` anywhere (grep-verified). `iosMain` is auto-created; code lives in `src/iosMain`. This is the recommended posture — do not propose manual wiring.
- **expect/actual**: `PlatformDatabaseModule` (core:database) is an `expect class` annotated Koin `@Module`. This is DELIBERATE and defensible — the class IS the Koin DI module, so the "replace with interface + DI" guidance does not apply (an interface can't be a `@Module`). Other expect: `getPlatformLogWriters()` (fun), `LedgerDatabaseConstructor` (Room expect object), `createTestDatabase()` (fun). `-Xexpect-actual-classes` is correctly set in core:database and core:test build files (expect/actual classes are still **Beta** in 2.4.0).
- **Swift export**: `iosExport` uses `swiftExport { moduleName="Ledger"; flattenPackage }` under `@OptIn(ExperimentalSwiftExportDsl::class)`. Swift export is **Alpha** as of Kotlin 2.4.0 (promoted from Experimental); the DSL opt-in annotation is still required. Swift calls `initializeKoin()` in `iOSApp.init()` before `MainViewController()` — sound ordering.

**Why:** avoids re-litigating settled, current choices on each currency audit.
**How to apply:** treat the above as current best practice; only flag genuine drift from newer docs. See [[ios-findings-advisory]].
