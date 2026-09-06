---
name: currency-baseline
description: Room KMP currency baseline for core:database — re-verified against the pinned Room 3.0.2 on 2026-09-06 against the official KMP Room guide and the room3 release notes
metadata:
  type: project
---

Room KMP layer (`core:database`) currency baseline. **Re-verified 2026-09-06 against the
pin `androidx-room = 3.0.2` / `androidx-sqlite = 2.7.0`** (read from
`gradle/libs.versions.toml`, not from this note).

Sources read this round:
- `developer.android.com/kotlin/multiplatform/room` — page last updated **2026-08-26**.
- `developer.android.com/jetpack/androidx/releases/room3` — 3.0.2 released **2026-08-26**,
  3.0.1 released **2026-07-29**. Both are **bug-fix only**: no API changes, no
  deprecations, no setup changes vs 3.0.0. 3.0.2 is the newest Room 3 listed — there is
  no 3.1.x. So the pin sits on the latest stable.
- The pinned artifacts' own sources under the Gradle cache
  (`C:\work\settings\gradle\caches\modules-2\files-2.1\androidx.room3\...`).

**Behaviour change to remember from 3.0.2:** suspending queries and invalidation-tracker
operations now throw `IllegalStateException` if called after the database is closed
(b/543076356). Harmless here — the DB is a Koin `@Single` that is never closed — but it
would matter if a close/reopen lifecycle is ever added.

These match current guidance — do not flag unless the docs or a future release note move:
- `DatabaseModule.provideDatabase`: `.setDriver(BundledSQLiteDriver())` +
  `.setQueryCoroutineContext(Dispatchers.IO)` + `.build()` — the exact common-code shape
  in the KMP guide. (`setQueryCoroutineContext` defaults to `Dispatchers.IO`; setting it
  explicitly is still what the guide shows.)
- Per-platform `RoomDatabase.Builder` providers: Android `databaseBuilder(context, name =
  absolutePath)`, iOS `NSDocumentDirectory`, JVM absolute path — all match the guide (the
  JVM one uses a real OS data dir, better than the guide's `java.io.tmpdir` example).
- `@ConstructedBy` + `expect object ... : RoomDatabaseConstructor<...>`. The guide now
  shows `@Suppress("KotlinNoActualForExpect")` on the object where the code has
  `@Suppress("NO_ACTUAL_FOR_EXPECT")` on the override — both compile; stylistic, not a gap.
- DAO: `suspend` for writes/one-shot reads, `Flow<...>` for observed reads — current.
- `SELECT *` in `PostingDao` is fine **because every one of those queries returns the whole
  `PostingEntity`**, not a subset/POJO. (Correcting an earlier version of this note: the
  reason is *not* "the entity is only 2 columns". `PostingEntity` has carried
  `updatedAt` / `isDeleted` / `pendingSync` alongside `id` + `narrative` since 1.7.0. The
  *domain* `Posting` is still id + narrative — see [[posting-entity-stays-minimal]] — but
  the entity is not.)
- Schema export via `room3 { schemaDirectory(...) }`; `schemas/.../LedgerDatabase/1.json`
  present and tracked. Note the extension really is named `room3` here even though the
  doc snippet writes `room { ... }`.
- Migration posture `.fallbackToDestructiveMigration(dropAllTables = true)` — current API,
  and a DELIBERATE pre-release choice (CLAUDE.md + the comment above it). Never flag the
  posture. Before real user data ships: explicit `Migration` objects (KMP `Migration`
  takes `SQLiteConnection`, not `SupportSQLiteDatabase`) + a CI check that the exported
  schema changed on a version bump.
- KSP configured for all four compile targets (android, jvm, iosArm64, iosSimulatorArm64).
- In-memory test builders (`Room.inMemoryDatabaseBuilder<...>().setDriver(BundledSQLiteDriver())`)
  omit `setQueryCoroutineContext`. Checked 2026-09-06: no official doc recommends pinning a
  test dispatcher on the Room builder, so this is **not** a finding — don't invent one.

**`room3-sqlite-wrapper` — deliberately absent, do not re-flag.** The KMP Room guide
(2026-08-26) still lists `implementation(libs.androidx.room3.sqlite.wrapper)` under
`androidMain`. This repo dropped it on purpose (CHANGELOG 1.5.0-era: "Dropped the unused
`room3-sqlite-wrapper` dependency"; earlier review classed it hygiene, not currency). The
reason still holds — verified 2026-09-06 that nothing in the repo references
`SupportSQLite*` / `androidx.sqlite.db`. Re-open only if that stops being true.

`androidx-sqlite = 2.7.0` is exactly the version `room3-runtime-3.0.2.pom` declares, so
the two pins are aligned; the KMP guide's own version table also shows room3 3.0.2 with
sqlite 2.7.0.

Conflict-strategy *intent*, mapper correctness and the repository impl belong to the
`rv-data` agent — defer, don't duplicate.

**How to apply:** always read the `androidx-room` / `androidx-sqlite` pins first. If they
differ from 3.0.2 / 2.7.0, re-derive from the release notes for *those* versions before
reusing any verdict here, then correct this note in place. DataStore has its own note:
[[datastore-currency-baseline]].
