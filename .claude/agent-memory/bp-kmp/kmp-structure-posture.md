---
name: kmp-structure-posture
description: kmp-ledger's KMP source-set/target/expect-actual/swift-export/BCV posture and which choices are deliberate-and-current (re-verified 2026-07-16 vs Kotlin 2.4.0 docs)
metadata:
  type: project
---

kmp-ledger KMP structure is current against JetBrains guidance for the pinned Kotlin 2.4.0 (re-verified 2026-07-16 against live docs; Swift export doc dated 2026-05-29, still Alpha; expect/actual classes still Beta; no drift since 2026-07-02). Durable facts so future audits don't re-derive:

- **Targets**: `iosArm64()`, `iosSimulatorArm64()`, `jvm()`, plus Android via the **new AGP KMP library plugin** `com.android.kotlin.multiplatform.library` with the `android { }` DSL (configured in convention plugins via `extensions.configure<KotlinMultiplatformAndroidLibraryTarget>("android")`, `withHostTest {}`). Current, not deprecated.
- **Source-set hierarchy**: relies on the **default hierarchy template** implicitly. No `applyDefaultHierarchyTemplate()` call and no manual `dependsOn` anywhere (grep-verified). `iosMain` is auto-created. Hierarchy doc (2026-03-16) unchanged — do not propose manual wiring.
- **expect/actual**: `PlatformDatabaseModule` (core:database) AND `PlatformDataStoreModule` (core:datastore) are `expect class`es annotated Koin `@Module`. DELIBERATE and defensible — the class IS the Koin DI module; "replace with interface + DI" guidance doesn't apply. Tracked as advisory O8 in docs/full-review-2026-06-30.md. Other expect: `getPlatformLogWriters()` (fun), `LedgerDatabaseConstructor` (Room expect object), `createTestDatabase()` (fun). `-Xexpect-actual-classes` correctly set in core:database, core:datastore, core:test, androidApp. Expect/actual classes still **Beta** per doc dated 2026-05-13.
- **Swift export**: **Alpha** in Kotlin 2.4.0 (doc 2026-05-29). `iosExport` uses `swiftExport { moduleName="Ledger"; flattenPackage }` under `@OptIn(ExperimentalSwiftExportDsl::class)` — DSL opt-in still required. Xcode invokes `:iosExport:embedSwiftExportForXcode`. `initializeKoin()` before `MainViewController()` in `iOSApp.init()` — sound. Kotlin 2.4.0 raised min iOS deployment to 15.0; iosApp targets 18.2, fine.
- **Swift-export dead config (known, Optional)**: (1) `iosExport` also declares `binaries.framework "LedgerBinary"` — never consumed (Xcode only calls embedSwiftExportForXcode); docs confirm no framework needed with Swift export. Tracked as O3 in docs/best-practices-review-2026-06-26-outstanding.md. (2) `core/bootstrap/build.gradle.kts:10-12` has an **inert** `swiftExport { moduleName="Ledger" }` — bootstrap is never a swift-export root and iosExport has no `export(project(":core:bootstrap"))`; per docs dependency export goes via `export()` in the root block. Flagged Optional 2026-07-02. Missing `@OptIn` there is cosmetic (see rv-build memory swift-export-and-catalog-quirks).
- **BCV**: standalone plugin 0.18.1 = latest release (2025-07-09), with experimental `klib { enabled; strictValidation }`. KGP built-in `abiValidation` is still **Experimental** and JetBrains does not yet recommend it for production; standalone BCV remains maintained. Project posture is current — do not propose migrating to KGP abiValidation until it stabilizes.

**Why:** avoids re-litigating settled, current choices on each currency audit.
**How to apply:** treat the above as current best practice; only flag genuine drift from newer docs. See [[ios-findings-advisory]].
