---
name: sdk-and-behavior-currency
description: Android SDK levels (compile/target 37, min 24), AGP max-API support, Play target-API + 16 KB page-size requirements, and which API-37 behavior changes are relevant vs N/A
metadata:
  type: project
---

Pins live in `gradle/libs.versions.toml` — **always re-read them, never quote the numbers below as current**:
`android-sdk-compile`, `android-sdk-target`, `android-sdk-min`, `android-gradle-plugin`.
Values observed 2026-09-06: compile 37, target 37, min 24, AGP **9.1.1** (an earlier
version of this note said 9.1.0 — the pin moved; re-derive every run).

**Currency facts (re-verified 2026-09-06):**
- API 37 = Android 17 is the **latest stable**; no Android 18 preview yet, only Android 17
  QPR1 beta / Canary (developer.android.com/about/versions).
- Play target-API requirement is **now in force** (since 2026-08-31): new apps and updates
  must target API 36+; existing apps need API 35+ to stay visible on new devices; extension
  window to 2026-11-01 (developer.android.com/google/play/requirements/target-sdk).
  targetSdk 37 is one level *ahead* of the requirement.
- **AGP 9.1.1 supports API level 37 and below** — compileSdk 37 is a supported combination,
  not an over-reach (past-releases/agp-9-1-0-release-notes, updated 2026-08-28). Gradle min
  for AGP 9.1.1 is 9.3.1; wrapper is above that. Do not re-file "compileSdk exceeds AGP".
  The AGP pin itself is deliberate (IntelliJ IDEA plugin ceiling) — see [[gradle-currency-baseline]].
- **16 KB page size**: Play rejects updates without it from **2027-02-01** for apps targeting
  API 35+ that ship native code (guide/practices/page-sizes, updated 2026-08-23). This app
  *does* ship native code — `BundledSQLiteDriver` pulls `libsqliteJni.so` from
  `androidx.sqlite:sqlite-bundled`. Verified 2026-09-06 by parsing ELF program headers of the
  cached AAR: every LOAD segment has `p_align=16384` on arm64-v8a and x86_64. AGP >= 8.5.1
  does the APK-side alignment. Compliant — re-check only if the sqlite pin moves.

**targetSdk-37 behavior changes → this app** (mapping stable unless the app gains features;
list re-read 2026-09-02 revision of about/versions/17/behavior-changes-17):
- **Edge-to-edge**: enforced since targetSdk 35. `MainActivity` calls `enableEdgeToEdge()` and
  the activity sets `windowSoftInputMode="adjustResize"` — exactly the two steps the current
  setup-e2e guide (2026-09-02) lists. OK.
- **Predictive back**: default-on for targetSdk 36+; `android:enableOnBackInvokedCallback` is
  only an *opt-out* now and is correctly absent. Requires activity >= 1.6.0 (pin is far above).
  Back *handling* in Compose is bp-compose's lane.
- **Large-screen orientation/resizability**: ignored on sw>=600dp with **no opt-out** on API 37.
  Manifest sets no orientation/resizeability/aspect-ratio restriction — ready.
- N/A for this app: local-network permission, background-audio hardening, SMS OTP, CP2/contacts,
  ECH + certificate transparency (no INTERNET permission at all), RemoteViews memory limit,
  BluetoothSocket, `setContentCaptureEnabled`. Native DCL hardening is N/A too: the sqlite lib
  is packaged in the APK (`extractNativeLibs=false`), not `System.load()`-ed from writable storage.

**Test-device coverage caveat:** `androidApp/build.gradle.kts` defines one Gradle-managed device
at API 30 (`aosp-atd`). ATD system images exist **only at API 30** (studio/test/gradle-managed-devices,
2026-01-16), so covering API 37 behavior changes needs a second device on the `aosp`/`google` source.

R8 release config: RESOLVED (verified 2026-07-16, still true 2026-09-06) — release buildType has
`isMinifyEnabled`/`isShrinkResources` true with `proguard-android-optimize.txt`. Do not re-flag.

**Why:** keeps future currency audits from re-deriving the same SDK/behavior-change mapping.
**How to apply:** re-read the pins first, then re-verify the dated claims above; the N/A mapping
only changes when the app gains features. Related: [[backup-and-privacy-posture]].
