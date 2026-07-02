---
name: sdk-and-behavior-currency
description: Android SDK levels (compile/target 37, min 24) and which targetSdk-37 behavior changes are relevant vs N/A for this app
metadata:
  type: project
---

SDK levels live in `gradle/libs.versions.toml`: `android-sdk-compile = 37`, `android-sdk-target = 37`, `android-sdk-min = 24`. AGP 9.2.1.

Currency facts (re-verified 2026-07-02): API 37 = Android 17, stable June 16 2026 — the latest level, exceeding Google Play's Aug 31 2026 requirement of API 36. AGP 9.2.1 is on the current stable line (9.2.x, max supported API 37). So SDK/AGP currency is ahead of, not behind, requirements.

Open currency gap (flagged 2026-07-02, not a pinned decision — present since initial commit as template default): release build has `isMinifyEnabled = false` and no proguardFiles; official guidance says always enable R8 + resource shrinking for release. If the user later rejects this, record it as deliberate.

targetSdk-37 behavior changes and how they map to this app:
- **Edge-to-edge**: enforced (targetSdk 35+). `MainActivity` calls `enableEdgeToEdge()`. OK.
- **Predictive back**: on by default for targetSdk 36+; the `android:enableOnBackInvokedCallback` flag is NOT needed and is correctly absent. Actual back *handling* in Compose is bp-compose's lane.
- **Large-screen orientation/resizability restrictions**: mandatory on API 37 (no opt-out). App is adaptive (material3 adaptive-navigation3) and sets no orientation/resizeable restrictions in the manifest — consciously ready. Layout adaptation itself is bp-compose's lane.
- Most other API-37 changes (background audio, contacts/CP2, local-network permission, SMS OTP, native DCL) are N/A — app has no such features.

**Why:** keeps future currency audits from re-deriving the same SDK/behavior-change mapping.
**How to apply:** re-verify the "latest level / Play requirement" facts when the calendar advances; the behavior-change mapping is stable unless the app gains features. Related: [[backup-and-privacy-posture]].
