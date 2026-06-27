---
name: runcatching-cancellation
description: Cancellation-safe wrapping rule in kmp-ledger — use runCatchingCancellable, never stdlib runCatching, in suspend code
metadata:
  type: feedback
---

Stdlib `runCatching { ... }` around suspend calls also catches `CancellationException`, which breaks structured-concurrency cancellation (the coroutine reports a fake failure / leaks work after its scope was cancelled).

Resolution status (verified 2026-06-24): the codebase now has `runCatchingCancellable` in `core:common` (`result/RunCatchingCancellable.kt`) — a `suspend inline` helper (per kotlinx.coroutines#1814) that rethrows `CancellationException` and wraps every other `Throwable`. CLAUDE.md documents this as a mandatory project rule. All known sites converted:
- `SavePostingUseCase`, `DeletePostingUseCase` — use `runCatchingCancellable`.
- `PostingEditViewModel.loadPosting()` — uses `runCatchingCancellable { getPostingUseCase(id).first() }`.

No stdlib `runCatching` remains in suspend/coroutine code as of 2026-06-24.

**Why:** structured concurrency relies on CancellationException propagating; swallowing it leaks work past viewModelScope cancellation and emits spurious Error states.
**How to apply:** when reviewing NEW suspend code, flag any stdlib `runCatching` (Critical if around long-lived work, Should-fix otherwise) and point to `runCatchingCancellable`. The asResult Flow path is already cancellation-safe because `.catch` is a Flow operator (CancellationException is not delivered to it).
