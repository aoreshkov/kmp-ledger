---
name: hot-paths
description: Performance-sensitive code paths in kmp-ledger (single-table local ledger) and their state as of 2026-06-23
metadata:
  type: project
---

kmp-ledger is a single-table (`postings`) local Room ledger. The only list query is
`getAllPostings()`. Performance work centers on how this list scales.

**Hot paths:**
- `PostingDao.getAllPostings()` — `SELECT * FROM postings`, unbounded, no LIMIT/ORDER. Drives the whole list screen.
- `OfflineFirstPostingRepository.getAllPostings()` — maps entity list to domain on `dispatchers.io`.
- `PostingListViewModel.uiState` — `flatMapLatest` + `asResult` + `map`, `stateIn(WhileSubscribed(5000))`. Sound.
- `PostingListScreen` LazyColumn — uses `key = { it.id }`, `Posting` is a stable data class. Sound.

**State as of 2026-06-23 review:** No N+1 (single table, no relations). Work placement
correct (`flowOn`/`withContext(io)`). Flow sharing correct. The only scaling concern is the
unbounded `getAllPostings()` query — fine for a personal local ledger, would need pagination
(PagingSource) only if a user accumulates very large numbers of postings.

**Why:** This is a local single-user ledger; realistic data sizes are small. Micro-allocation
in the mapper (one Posting per row per emission) is unavoidable given the entity/domain boundary
rule and not worth optimizing at these sizes.

**How to apply:** Don't flag the per-row mapper allocation or the unbounded query as defects
unless the product scope changes to bulk/large datasets. If pagination is ever needed, the seam
is `getAllPostings()` through to the LazyColumn.
