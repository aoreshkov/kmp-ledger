---
name: insert-conflict-strategy
description: PostingDao.insert uses default @Insert (onConflict = ABORT); inserting a duplicate primary key throws instead of upserting
metadata:
  type: project
---

`PostingDao.insert` is annotated with a bare `@Insert` (no
`onConflict`), so the strategy is `OnConflictStrategy.ABORT`. Inserting a
`PostingEntity` whose `id` already exists throws a SQLite constraint
exception, which propagates uncaught out of
`OfflineFirstPostingRepository.insertPosting` (no try/catch there).

In practice `insertPosting(NewPosting)` always generates a fresh UUID in
`NewPosting.asEntity()` (`randomUuidString()`), so a real collision is
near-impossible today. Edits go through `update` (`@Update`), not insert.

**Why:** Insert path only ever sees server-fresh random ids, so ABORT is
currently harmless.

**How to apply:** If a future feature inserts with a caller-supplied id
(import, sync, restore), reconsider `@Insert(onConflict = REPLACE)` or wrap
the call so the constraint error surfaces as data, not a crash. Repository
write methods currently do not convert exceptions to a result type.
See [[schema-migration-posture]].
