---
name: sdk-and-behavior-currency
description: Android SDK levels (compile/target 37, min 24) and which targetSdk-37 behavior changes are relevant vs N/A for this app
metadata:
  type: project
---

SDK levels live in `gradle/libs.versions.toml`: `android-sdk-compile = 37`, `android-sdk-target = 37`, `android-sdk-min = 24`. AGP 9.1.0.

Currency facts (re-verified 2026-07-16): API 37 = Android 17, stable June 2026 — the latest level, exceeding Google Play's Aug 31 2026 requirement (API 36 for new apps/updates, API 35 to stay visible on existing apps). AGP 9.1.0 is on a current stable line and supports API 37. So SDK/AGP currency is ahead of, not behind, requirements. **Do not flag AGP as "behind latest" (9.3.0):** it is deliberately pinned to 9.1.0 because that is the ceiling the latest IntelliJ IDEA Android plugin supports (IDEA lags Android Studio on AGP). See [[gradle-currency-baseline]].

R8 release config: RESOLVED (verified 2026-07-16). `androidApp/build.gradle.kts` release buildType now has `isMinifyEnabled = true`, `isShrinkResources = true`, and `proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")`. `proguard-rules.pro` is intentionally near-empty (Room 3 / Compose / kotlinx-serialization ship consumer keep rules; Koin Annotations is compile-time codegen). The earlier "isMinifyEnabled = false" gap is closed — do not re-flag.

targetSdk-37 behavior changes and how they map to this app:
- **Edge-to-edge**: enforced (targetSdk 35+). `MainActivity` calls `enableEdgeToEdge()`. OK.
- **Predictive back**: on by default for targetSdk 36+; the `android:enableOnBackInvokedCallback` flag is NOT needed and is correctly absent. Actual back *handling* in Compose is bp-compose's lane.
- **Large-screen orientation/resizability restrictions**: mandatory on API 37 (no opt-out). App is adaptive (material3 adaptive-navigation3) and sets no orientation/resizeable restrictions in the manifest — consciously ready. Layout adaptation itself is bp-compose's lane.
- Most other API-37 changes (background audio, contacts/CP2, local-network permission, SMS OTP, native DCL) are N/A — app has no such features.

**Why:** keeps future currency audits from re-deriving the same SDK/behavior-change mapping.
**How to apply:** re-verify the "latest level / Play requirement" facts when the calendar advances; the behavior-change mapping is stable unless the app gains features. Related: [[backup-and-privacy-posture]].
