---
name: schema-migration-posture
description: Room schema export is configured; database is at version 1 with no migrations and no destructive-migration fallback
metadata:
  type: project
---

Room schema export is enabled in `core:database/build.gradle.kts` via
`room3 { schemaDirectory("$projectDir/schemas") }`, and `schemas/.../1.json`
is committed. The DB (`LedgerDatabase`) is at `version = 1`.

There are NO `Migration` objects and NO `fallbackToDestructiveMigration*`
call on any platform builder (Android/iOS/JVM all just
`Room.databaseBuilder(...)` -> `DatabaseModule.provideDatabase` adds
`BundledSQLiteDriver` + `setQueryCoroutineContext`).

**Why:** Only one entity (`PostingEntity`: id, narrative), schema has never
changed, so the gap is latent, not active.

**How to apply:** The first time `PostingEntity`/`LedgerDatabase` schema
changes and `version` is bumped, a migration OR an explicit destructive
fallback MUST be added, or the app crashes at runtime
(`IllegalStateException: A migration from N to M was required but not found`).
Flag this on any entity/schema change review. See [[insert-conflict-strategy]].
