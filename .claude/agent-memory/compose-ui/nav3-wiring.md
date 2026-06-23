---
name: nav3-wiring
description: How Navigation 3 is wired in kmp-ledger (App.kt + postingNavigationModule) and recomposition pitfalls in the host
metadata:
  type: project
---

Navigation 3 wiring in this repo:
- `core:ui/App.kt` builds the `backStack` via `rememberNavBackStack`, wraps it in a
  `Navigator(backStack)` (`remember(backStack)`), and provides it through
  `LocalNavigator` (a `staticCompositionLocalOf`). `NavDisplay` consumes the
  back stack with `koinEntryProvider<NavKey>()` and a list-detail scene strategy.
- Screens are registered in `postingNavigationModule` (Koin DSL `navigation<NavKey>`),
  obtain ViewModels via `koinViewModel()`, and read `LocalNavigator.current` for
  goTo/goBack. ViewModel identity is scoped per back-stack entry by
  `rememberViewModelStoreNavEntryDecorator`.
- `Navigator.goBack()` is guarded by `canGoBack()` (size > 1), so repeated
  back calls cannot empty the stack below the start destination — relevant when
  judging whether a Channel-driven nav event could over-pop.

**Why:** baseline so future reviews don't re-derive the topology each time.
**How to apply:** check App.kt host first for per-recomposition allocations
(entryProvider/decorators/sceneStrategies were allocated unremembered as of the
2026-06 review — minor, NavDisplay tolerates it but remembering is cheaper).
