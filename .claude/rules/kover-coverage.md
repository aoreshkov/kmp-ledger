---
paths:
  - "build.gradle.kts"
  - "build-logic/**"
  - "**/src/*Test/**"
---

# Kover coverage policy

- Kover excludes generated classes automatically: `*ComposableSingletons*`, `*_Factory` (Koin), `*$serializer` (kotlinx.serialization), `*_Impl*` and `*DatabaseConstructor` (Room KSP), `app.oreshkov.ledger.*.resources.*`, `*.compose.resources.*`, `@Preview`-annotated methods. It also excludes `*.di.*` packages — DI wiring is validated by Koin `verify()`, not by execution, so it stays out of coverage — plus `MainKt` and `LedgerApp`, the desktop entry points, which by convention carry no logic. Room's exclusion **has** to be by name pattern: its output carries `@javax.annotation.processing.Generated`, which is `@Retention(SOURCE)` and so absent from the bytecode Kover reads, making `annotatedBy("*Generated*")` useless against it. Room codegen was 237 of 860 lines — 27.6% — of the aggregate before it was excluded, and ~96% of `core:database`'s own report. The resources pattern is tied to the `packageOfResClass` convention below — if a module picks a package outside `app.oreshkov.ledger.*.resources`, its generated `Res` class silently re-enters coverage. **Keep this list in sync with the copy in the `ledger.kotlin.multiplatform` convention plugin**, which drives the per-module reports; the root build and a precompiled script plugin cannot share a constant.

- `:desktopApp` is in the root `kover(project(...))` list even though it is an app module, and that is deliberate. `DesktopUiTest` boots the real `App()` over a real in-memory Room database, DataStore, Koin graph and `NavDisplay`, and it already runs on every `check`; without that entry Kover discards its execution data and credits none of the library code it exercises — it is worth +1.1 pp line and +5.0 pp branch on the aggregate. Do not drop it. Do **not** add `:androidApp`: its `androidTest` needs an emulator and never runs in CI, so it would only add uncovered lines.

- **Coverage is JVM-only, by construction.** Kover instruments JVM bytecode, so `jvmTest` and `testAndroidHostTest` are the only measured source sets. `core:database`'s `iosTest` runs real tests on `iosArm64`/`iosSimulatorArm64` and none of it is measured, and every `iosMain` `actual` (`PlatformDatabaseModule.ios`, `PlatformDataStoreModule.ios`, `PlatformLogWriter.ios`, `MainViewController`) is permanently invisible to the report. That is a Kover limitation, not a gap to close — do not try to "fix" it.

- The aggregate **branch** floor stays well below the line/instruction floors on purpose, and this is measured, not folklore: adding `annotatedBy("androidx.compose.runtime.Composable")` to the report filters takes branch coverage from 77.20% (237/307) to 94.55% (104/110). 64% of the report's branches are Compose compiler `$changed`/`$default` bitmask plumbing, which re-shapes on every Compose compiler bump. `@Composable` is `AnnotationRetention.BINARY`, so `annotatedBy` can see it — run that as a one-off diagnostic when the branch number looks wrong, but never ship it as a filter: it also deletes the composable content the UI tests genuinely exercise (145 lines when last measured). Raise the line floor instead; do not chase aggregate branch.

