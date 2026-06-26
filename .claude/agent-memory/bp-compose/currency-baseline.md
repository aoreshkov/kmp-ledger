---
name: currency-baseline
description: Upstream-currency verification of kmp-ledger Compose UI + Navigation 3 against pinned CMP 1.11.1 / nav3 1.1.3, checked 2026-06-26
metadata:
  type: project
---

Upstream-currency audit result (2026-06-26): the Compose + Navigation 3 surface
is CURRENT for the pinned versions. No currency gaps found. Details so future
reviews can confirm-fast instead of re-deriving:

**Navigation 3 (1.1.3, released 2026-06-17):**
- `NavDisplay.onBack` is `() -> Unit` since nav3 1.0.0-alpha11 (the old
  `(Int)->Unit` count form was removed; lambda is now called N times for N pops).
  App.kt uses `onBack = { navigator.goBack() }` — correct for the new contract.
- `entryDecorators` order: `SaveableStateHolderNavEntryDecorator` MUST be first
  (it provides SaveableStateProvider). App.kt has
  `listOf(saveableStateDecorator, viewModelStoreDecorator)` — correct.
- `rememberSceneSetupNavEntryDecorator` was REMOVED from public API in
  1.0.0-alpha11; scene-setup is auto-included by NavDisplay. Do NOT flag its
  absence from the decorators list.
- `sceneStrategies` takes `List<SceneStrategy>` since 1.1.0-alpha05. App.kt passes
  a remembered `listOf(listDetailStrategy)` — correct.
- sceneStrategies + decorator lists are remembered (commit 8a13107) — already
  resolved, don't re-flag. See [[../compose-ui/nav3-wiring]] (other agent's note).
- NEWER-STABLE watch (not findings, we are pinned to 1.1.3): nav3 1.2.0-alpha
  line adds `NavigationBackHandler` (1.2.0-alpha02) and deep-link support
  (1.2.0-alpha03). Revisit if the project bumps off 1.1.x.

**Compose state/recomposition (CMP 1.11.1):**
- All screens use `collectAsStateWithLifecycle()` from androidx.lifecycle.compose
  (current MP equivalent) — correct.
- LazyColumn `items(..., key = { it.id })` with stable keys; callbacks are method
  refs / stable lambdas — current.
- `staticCompositionLocalOf` for LocalNavigator is correct (Navigator instance is
  stable, rarely changes).
- No `derivedStateOf` — and none is warranted; cheap derivations off uiState that
  already drives recomposition (don't over-apply derivedStateOf).
- Android `enableEdgeToEdge()` + Scaffold inset handling — current.

**Deferred (out of bp-compose lane):** PostingEditScreen.kt uses
`LaunchedEffect(saveError-boolean)` to show a snackbar while the two other events
use Channels. Event-modeling correctness is owned by the compose-ui agent, not a
deprecated-upstream pattern — don't report as a currency gap.

**Source:** developer.android.com/jetpack/androidx/releases/navigation3
(nav3 release notes, 1.1.3 dated 2026-06-17);
developer.android.com/guide/navigation/navigation-3/naventrydecorators.
