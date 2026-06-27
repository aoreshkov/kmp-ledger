---
name: schema-migration-posture
description: Room schema export is on; DB at version 1 with a documented destructive-migration fallback (no Migration objects yet)
metadata:
  type: project
---

Room schema export is enabled in `core:database/build.gradle.kts` via
`room3 { schemaDirectory("$projectDir/schemas") }`, and `schemas/.../1.json`
is committed. The DB (`LedgerDatabase`) is at `version = 1`.

As of commit e227f08, `DatabaseModule.provideDatabase` calls
`.fallbackToDestructiveMigration(dropAllTables = true)` with a comment
documenting the pre-release rationale. There are still NO `Migration`
objects. So a `version` bump no longer crashes at open — it DROPS AND
RECREATES all tables (data loss), which is acceptable only pre-release.

**Why:** Pre-release, single entity (`PostingEntity`: id, narrative), no
real user data to preserve. Destructive fallback avoids a crash-on-open
while migrations are deferred.

**How to apply:** Before shipping real user data, the destructive fallback
MUST be replaced with explicit `Migration` objects plus a CI check that the
exported schema dir changed on the version bump (per CLAUDE.md "Module
Conventions"). On any entity/schema change review, confirm the team still
intends destructive behavior; flag it as data loss otherwise.
See [[insert-conflict-strategy]].
