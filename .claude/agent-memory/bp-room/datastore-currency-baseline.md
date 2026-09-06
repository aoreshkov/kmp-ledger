---
name: datastore-currency-baseline
description: DataStore Preferences currency baseline for core:datastore — first audit 2026-09-06 against the pinned androidx-datastore 1.2.1; records what matches the KMP DataStore guide and the one artifact-choice gap
metadata:
  type: project
---

First real audit of the DataStore half of this agent's remit: **2026-09-06**, against the
pin `androidx-datastore = 1.2.1` (read from `gradle/libs.versions.toml`).

Sources read this round:
- `developer.android.com/kotlin/multiplatform/datastore` — page last updated **2026-08-17**.
- `developer.android.com/topic/libraries/architecture/datastore` — last updated **2026-08-27**.
- `developer.android.com/jetpack/androidx/releases/datastore` — **1.2.1 stable 2026-03-11**
  (no API/behaviour change vs 1.2.0, infra fixes only); 1.2.0 stable 2025-11-19. 1.2.1 is
  the newest listed, so the pin sits on the latest stable. 1.2.x added `datastore-guava`,
  Direct Boot (`createInDeviceProtectedStorage()` / `deviceProtectedDataStore()`),
  `PreferencesFileSerializer`, a public `CorruptionHandler`, and a default constructor for
  `ReplaceFileCorruptionHandler` — none of which changes this project's setup.
- The pinned artifacts' own sources under the Gradle cache
  (`...\modules-2\files-2.1\androidx.datastore\*\1.2.1\*-sources.jar`).

**The single most useful thing verified this round — `createWithPath` is NOT stale.**
The KMP guide's snippet builds the store per platform as
`DataStoreFactory.create(storage = FileStorage(...))` on Android/JVM and
`OkioStorage(FileSystem.SYSTEM, ...)` on iOS. The repo instead calls one shared
`PreferenceDataStoreFactory.createWithPath { ... }` in commonMain. Reading the 1.2.1
sources: the jvm/android actual of `createWithPath` delegates to
`FileStorage(PreferencesFileSerializer)` and the native actual to
`OkioStorage(FileSystem.SYSTEM, PreferencesSerializer)` — i.e. **exactly the storage the
guide names, chosen per platform for you**. (The "default moved from OkioStorage to
FileStorage" change is already inside the factory.) So the shared factory is equivalent to
and simpler than the doc snippet. **Do not flag it, and do not propose rewriting
`PreferencesDataStore.kt` into the guide's per-platform `Storage` form.**

Also matching current guidance (do not flag):
- `ReplaceFileCorruptionHandler { emptyPreferences() }` passed to the factory.
- One `DataStore` instance per file: one `@Single` per platform actual, and the desktop UI
  test builds its own store in a per-test temp dir. The docs' hard rule ("never more than
  one instance for a given file in the same process; DataStore throws `IllegalStateException`")
  is respected.
- File name `ledger.preferences_pb` — the factory asserts the `preferences_pb` extension on
  both jvm/android and native, so the name is load-bearing.
- Reads: `dataStore.data` `Flow` + `.map`; writes: `edit {}`. No blocking reads anywhere.
- Read-side `IOException` -> `emit(emptyPreferences())` — the documented recovery.
- Per-platform paths: Android `filesDir` (matches the guide), iOS `NSDocumentDirectory`
  (matches verbatim), JVM OS-aware app-data dirs (better than the guide's `java.io.tmpdir`,
  and the guide itself says to prefer an app-support dir).

**Open gap found (Optional): artifact choice.** `core/datastore/build.gradle.kts` (and the
desktopApp test deps) declare the umbrella `androidx.datastore:datastore` +
`androidx.datastore:datastore-preferences`, while the KMP guide's commonMain block names
`datastore-core` + `datastore-preferences-core`. Verified from the sources jars that the
umbrella artifacts are the `-core` ones plus delegate glue (`DataStoreDelegateUtils`,
`PreferencesDataStoreDelegateUtils`, and on Android `PreferenceDataStoreDelegate`,
`PreferenceDataStoreFile`, `SharedPreferencesMigration`) that this project never uses.

**okio types in commonMain are legitimate here.** The code imports `okio.Path.Companion.toPath`
and `okio.IOException` without declaring okio: `datastore-preferences-core` exposes okio as
`api` (its `createWithPath` signature takes an `okio.Path`), so it is on the compile
classpath either way. Platform mapping checked in the 1.2.1/okio 3.x sources:
- jvm/android: `okio.IOException` *is* `java.io.IOException` (typealias), and
  `androidx.datastore.core.IOException` is the same typealias -> one catch covers all.
- native: `androidx.datastore.core.IOException` is its **own class extending `Exception`**,
  unrelated to `okio.IOException`. Read failures on iOS come out of `OkioStorage` as
  `okio.IOException` (covered), but `CorruptionException` extends the *datastore*
  `IOException` and so is **not** covered on iOS if it ever escapes the corruption handler.

**How to apply:** read the `androidx-datastore` pin before reusing anything here. If it is
no longer 1.2.1, re-derive from that release's notes and the cached sources. Room has its
own note: [[currency-baseline]].
