---
name: currency-baseline
description: Which parts of the Room KMP layer already match official Room 3.0.0-rc01 guidance (so future audits don't re-litigate them)
metadata:
  type: project
---

Room KMP layer (`core:database`) currency baseline, verified against the official
KMP Room guide (developer.android.com/kotlin/multiplatform/room, last updated
2026-06-17) and the Room 3.0 release page (.../releases/room3, 2026-06-17) on
2026-06-26 for pinned Room 3.0.0-rc01.

These already match current best practice — do not flag on re-audit unless docs change:
- `DatabaseModule.provideDatabase`: `.setDriver(BundledSQLiteDriver())` +
  `.setQueryCoroutineContext(Dispatchers.IO)` + `.build()` — exactly the documented
  common-code shape. (setQueryCoroutineContext defaults to Dispatchers.IO; setting it
  explicitly is fine.)
- Per-platform `RoomDatabase.Builder` providers: Android `databaseBuilder(context, name=
  absolutePath)`, iOS NSDocumentDirectory pattern, JVM file path — all match docs
  (JVM uses a real app-data dir, better than the docs' tmpdir example).
- DAO: suspend for writes, `Flow<...>` for reads — current.
- `@Database` schema export via `room3 { schemaDirectory(...) }`; `schemas/...
  /LedgerDatabase/1.json` is present and tracked.
- Migration posture: `.fallbackToDestructiveMigration(dropAllTables = true)` is the
  current API (dropAllTables gained a default in 3.0.0-alpha02). This is a DELIBERATE
  pre-release pin (see CLAUDE.md) — the comment describes it accurately. Mechanism is
  current; before real data ship, replace with explicit `Migration` + exported-schema
  CI check. Don't flag the posture itself.
- KSP configured for all 4 compile targets (android, jvm, iosArm64, iosSimulatorArm64).

Entity is intentionally minimal (id + narrative) — see user memory [[posting-entity-stays-minimal]];
`SELECT *` in PostingDao is fine because the DAO returns the full (2-col) entity.

Conflict-strategy intent, mapper correctness, repository impl are owned by the
`data-persistence` agent — defer those, don't duplicate.
