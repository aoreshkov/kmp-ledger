---
name: kmp-target-set
description: The canonical KMP target set, where it's declared, and the Android KMP plugin's host-test source-set layout
metadata:
  type: project
---

Target set for every shared module: `android` (via `com.android.kotlin.multiplatform.library`), `jvm`, `iosArm64`, `iosSimulatorArm64`. Declared once in the `ledger.kotlin.multiplatform` convention plugin (`build-logic/src/main/kotlin/`), so all modules inherit the same set coherently. No module re-declares or omits targets except `iosExport`, which re-opens `iosArm64`/`iosSimulatorArm64` only to attach `framework` binaries (baseName `LedgerBinary`, static) — same target set, just binary config.

**Why:** Centralizing targets in the convention plugin is what keeps target declarations consistent across modules — a house rule I own.

**How to apply:** If a module declares targets manually (jvm(), iosX64(), wasm, etc.) instead of via the convention plugin, that's an inconsistency to flag. iosExport adding binaries is fine.

Android KMP plugin specifics:
- Host unit tests live in the `androidHostTest` source set (NOT `androidUnitTest`/`test`). Enabled with `withHostTest {}`.
- No `androidDeviceTest` (instrumented) is enabled in shared modules, so `commonTest` expect decls only need actuals in `androidHostTest` (+ jvmTest + iosTest).
- `core:bootstrap` shares `src/jvmTest/kotlin` into `androidHostTest` via `kotlin.srcDir(...)` — deliberate, runs the JVM tests on the Android host too.

`-Xexpect-actual-classes` freeCompilerArg is required wherever an expect/actual *class or object* is declared (silences Beta warning-as-error): present in `core:database`, `core:test`, `androidApp`. Not needed for expect *fun* (so `core:common` correctly omits it).
