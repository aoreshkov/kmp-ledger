---
name: ios-entry-point
description: iOS entry-point wiring — Swift init order and the Kotlin MainViewController/initializeKoin split
metadata:
  type: project
---

iOS entry point is structurally sound. Kotlin side (`iosExport/src/iosMain/.../MainViewController.kt`): `@KoinApplication(modules=[BootstrapModule::class]) class LedgerApp`, plus two exported top-level funs — `MainViewController()` (ComposeUIViewController { App() }) and `initializeKoin()` (startKoin<LedgerApp> { modules(postingNavigationModule) }).

Swift side: `iosApp/iosApp/iosApp.swift` calls `initializeKoin()` in `App.init()`; `ContentView.swift` calls `MainViewController()` inside the WindowGroup body. So initializeKoin() runs before MainViewController() — satisfies the house rule.

Swift Export (not cinterop/ObjC bridging): `iosExport` uses `swiftExport { moduleName="Ledger"; flattenPackage="app.oreshkov.ledger" }` under `@OptIn(ExperimentalSwiftExportDsl)`. `core:bootstrap` also sets `swiftExport { moduleName="Ledger" }`. Swift imports `import Ledger`.

**Why:** This is the one ordering invariant I own on the iOS side.

**How to apply:** [[no-ios-test-environment]] — user has no iOS build env, so frame any iOS finding as advisory, never as an actionable build/test step. If the Swift init order ever inverts (MainViewController before initializeKoin), that's a real Should-fix because Koin graph wouldn't be started when Compose first composes.
