---
name: runcatching-cancellation
description: runCatching in suspend code swallows CancellationException — recurring pattern in kmp-ledger use cases
metadata:
  type: feedback
---

`runCatching { ... }` around suspend calls catches `CancellationException` too, which breaks structured-concurrency cancellation (the coroutine keeps going / reports a fake failure after its scope was cancelled).

Sites in kmp-ledger using this shape (all run inside `viewModelScope` or use-case suspend fns):
- `SavePostingUseCase` / `DeletePostingUseCase`: `runCatching { repository... }` returning `Result<Unit>`.
- `PostingEditViewModel.loadPosting()`: `runCatching { getPostingUseCase(id).first() }`.

**Why:** structured concurrency relies on CancellationException propagating. Swallowing it can leak work past viewModelScope cancellation and emit spurious Error states. Low practical impact here because failures map to terminal UI states and the work is short, but it is a real correctness seam.
**How to apply:** when reviewing, recommend rethrowing CancellationException (e.g. a `runCatchingCancellable`/`coroutineContext.ensureActive()` helper) rather than raw `runCatching` around suspend calls. Note severity as Should-fix, not Critical, given current short-lived operations.
