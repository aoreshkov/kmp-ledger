---
paths:
  - "core/database/**"
  - "core/data/**"
---

# Room migration posture

Room migration posture (pre-release): `DatabaseModule.provideDatabase` uses `.fallbackToDestructiveMigration(dropAllTables = true)`, so bumping the `@Database` `version` on `LedgerDatabase` **drops and recreates all data**. Before shipping real user data, replace this with explicit `Migration` objects plus a CI check that the exported schema dir changed on the version bump.
