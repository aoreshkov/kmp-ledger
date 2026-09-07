---
name: datastore-currency-baseline
description: DataStore Preferences currency baseline for core:datastore — re-verified 2026-09-07 against pinned androidx-datastore 1.2.1, with the artifact-graph and native-IOException facts settled from the published artifacts
metadata:
  type: project
---

DataStore half of this agent's remit. **Re-verified 2026-09-07** against the pin
`androidx-datastore = 1.2.1` (read from `gradle/libs.versions.toml:28`).

Sources read this round:
- `developer.android.com/kotlin/multiplatform/datastore` — last updated **2026-08-17**.
- `developer.android.com/jetpack/androidx/releases/datastore` — **1.2.1 (2026-03-11) is
  still the newest stable**; no 1.2.2. Latest preview is **1.3.0-alpha10 (2026-07-29)**.
- The published artifacts themselves, pulled from **Google Maven**
  (`https://dl.google.com/dl/android/maven2/androidx/datastore/...`) — `.module` metadata,
  `-sources.jar`, and the `.aar`/`.jar` class listings.

**Fetch artifacts from Google Maven, not Maven Central.** androidx is not on
repo1.maven.org (404s). Also: enumerating the Gradle cache under
`C:\work\settings\gradle\caches\modules-2\files-2.1\androidx.datastore` with `find`
times out — go to the network instead, it is far faster.

## The 1.2.1 artifact graph (settled — this is the evidence for the artifact finding)

From the `.module` metadata (`metadataApiElements`, i.e. commonMain):
- `datastore-preferences-core` -> api: `datastore-core`, `datastore-core-okio`, `okio`,
  kotlinx-serialization-core/protobuf.
- `datastore` -> api: `datastore-core`, `datastore-core-okio`.
- `datastore-preferences` -> api: **`datastore`** + **`datastore-preferences-core`**.

Consequences, both verified by class-listing diff:
- **`implementation(libs.androidx.datastore)` is 100% redundant** — `datastore-preferences`
  api-depends on it. (This is a correction to the earlier note, which framed the finding
  only as "umbrella vs `-core`" and missed the redundancy.)
- Umbrella minus core, JVM: exactly one class each — `androidx/datastore/DataStoreDelegateUtils`
  and `androidx/datastore/preferences/PreferencesDataStoreDelegateUtils`.
- Umbrella minus core, Android AAR: `DataStoreDelegateKt`, `DataStoreFile`,
  `DataStoreSingletonDelegate`, `OkioSerializerWrapper`, `migrations/SharedPreferencesMigration`,
  `SharedPreferencesView`; and `PreferenceDataStoreDelegateKt`, `PreferenceDataStoreFile`,
  `PreferenceDataStoreSingletonDelegate`, `SharedPreferencesMigrationKt`.
- Grepped 2026-09-07: **nothing in the repo references any of them.** So swapping to
  `datastore-preferences-core` alone compiles unchanged — `PreferenceDataStoreFactory.createWithPath`
  lives in `datastore-preferences-core`, and `okio.Path` stays on the classpath because that
  artifact exposes okio as `api`.

## Native `IOException` mapping (settled — evidence for the catch-clause finding)

From `datastore-core-1.2.1-sources.jar` / `datastore-core-jvm-1.2.1-sources.jar`:
- `commonMain/androidx/datastore/core/Expect.kt:21`:
  `expect open class IOException(message: String?, cause: Throwable?) : Exception`
- `nativeMain/.../Actual.native.kt:25`: `actual open class IOException ... : Exception(...)`
  — its **own class**, no relation to `okio.IOException`.
- jvm actual: `actual typealias IOException = java.io.IOException` (and okio's jvm
  `IOException` is the same typealias) -> on JVM/Android one catch covers everything.
- `CorruptionException : IOException` (the *datastore* one), so on native it is **not**
  caught by `catch (e: okio.IOException)`.
- Escape path is real: `ReplaceFileCorruptionHandler`'s own KDoc — "If the handler
  encounters an exception when attempting to replace data, the new exception is added as a
  suppressed exception to the original exception and **the original exception is thrown**."

Both types must be caught, because on native they are disjoint and each covers a different
failure (okio -> `OkioStorage` read failures; datastore -> `CorruptionException`):

```kotlin
import androidx.datastore.core.IOException as DataStoreIOException
import okio.IOException as OkioIOException
// ...
.catch { exception ->
    if (exception is OkioIOException || exception is DataStoreIOException) {
        emit(emptyPreferences())
    } else {
        throw exception
    }
}
```
On JVM the two aliases collapse to `java.io.IOException`; the doubled `is` check is legal
and warning-free there (the subject is `Throwable`, so neither branch is statically true).

## Matching current guidance — do not flag

- **`createWithPath` is NOT stale.** The KMP guide builds per-platform `Storage`
  (`FileStorage` on android/jvm, `OkioStorage` on iOS); `PreferenceDataStoreFactory.createWithPath`
  picks *exactly those* per platform internally. The shared factory is equivalent and simpler.
  **Never propose rewriting `PreferencesDataStore.kt` into the guide's per-platform form.**
- `ReplaceFileCorruptionHandler { emptyPreferences() }` on the factory.
- One instance per file: one `@Single` per platform actual; `core:datastore` jvmTest and
  `desktopApp/src/test/.../DesktopUiTest.kt:47` each build their own store in a per-test
  temp dir. No duplicate instance on one path anywhere.
- `ledger.preferences_pb` — the factory asserts the `preferences_pb` extension on both
  jvm/android and native, so the name is load-bearing.
- `Flow` reads via `dataStore.data.map`, `edit {}` writes, no blocking reads.
- Read-side recovery emits `emptyPreferences()` — the documented recovery.
- Paths: Android `filesDir`, iOS `NSDocumentDirectory`, JVM OS-aware app-data dirs (better
  than the guide's `java.io.tmpdir`).

## Forward-looking (not a gap at 1.2.1)

1.3.0-alpha07+ adds a `DataStore.Builder` API taking a `CoroutineContext` and recommends
migrating off `DataStoreFactory`; also `datastore-tink` encryption, WASM/JS storage, and
`createWithTracing`. All alpha — revisit only when 1.3.0 goes stable.

**How to apply:** read the `androidx-datastore` pin before reusing anything here. If it is
no longer 1.2.1, re-derive. Room has its own note: [[currency-baseline]].
