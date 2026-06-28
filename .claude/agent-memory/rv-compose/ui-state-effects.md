---
name: ui-state-effects
description: Posting screens' sealed UiState mapping, one-shot Channel events, and LaunchedEffect keying conventions
metadata:
  type: project
---

Posting feature UI conventions (as of 2026-06 review):
- Each screen has a public stateful composable (collects `viewModel.uiState`
  via `collectAsStateWithLifecycle`) delegating to an `internal` stateless
  `*Content` composable that takes the sealed UiState + lambdas. Good hoisting.
- One-shot navigation/side-effect signals (delete done, save done) use
  `Channel(BUFFERED).receiveAsFlow()` collected in `LaunchedEffect(viewModel)`.
  Keyed on `viewModel` (stable across recomposition) — fires once, correct.
- Edit screen shows save-failure snackbar with `LaunchedEffect(saveError)` where
  `saveError` is derived from the Editing state's `saveError` flag. ViewModel
  resets the flag to false at the start of each `savePosting()`, so a second
  failure re-triggers the effect correctly. This is NOT a correctness bug vs the
  Channel pattern used elsewhere (delete failed/done) — it's a stylistic
  inconsistency. The only behavioral divergence: after composition recreation
  (e.g. Android rotation) while the ViewModel survives with saveError still true,
  a fresh `LaunchedEffect(true)` re-shows the snackbar, whereas a consumed Channel
  event would not. Minor/debatable UX, not a defect. Don't re-flag as Critical.

**Why:** this is the validated pattern in the codebase; don't flag it as wrong.
**How to apply:** when reviewing new screens, expect this stateful/stateless
split and Channel-for-events approach. A new screen collecting state directly
in the registered `navigation<>` lambda, or putting business logic in the
composable, is the deviation to flag — see [[nav3-wiring]].
