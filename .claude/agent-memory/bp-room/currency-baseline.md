---
name: currency-baseline
description: Which parts of the Room KMP layer already match official Room 3.0.0 stable guidance (so future audits don't re-litigate them)
metadata:
  type: project
---

Room KMP layer (`core:database`) currency baseline, verified against the official
KMP Room guide (developer.android.com/kotlin/multiplatform/room) and the Room 3.0
release page (.../releases/room3). Last re-verified 2026-07-02 (docs last-updated
2026-07-01; guide content substantively unchanged from the 2026-06-17 revision).

**Stable is adopted (2026-07-16 re-verify):** repo now pins `androidx-room = "3.0.0"`
and `androidx-sqlite = "2.7.0"` (both stable, released 2026-07-01). The earlier
recommendation to bump off rc01 is DONE — no action. Release notes document no API
delta between 3.0.0-rc01 and 3.0.0 stable.

**rc01 API rename note (N/A here):** 3.0.0-rc01 renamed `@TypeConverter` ->
`@ColumnTypeConverter` (symmetry with `@DaoReturnTypeConverter`). The layer uses NO
type converters (minimal 2-col String entity), so this rename does not apply. If a
converter is ever added, use `@androidx.room3.ColumnTypeConverter`, not `@TypeConverter`.

The former `room3-sqlite-wrapper` version-pin defect is RESOLVED: the wrapper
dependency was dropped from the repo entirely (see CHANGELOG). Don't re-flag.

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
`rv-data` agent — defer those, don't duplicate.
