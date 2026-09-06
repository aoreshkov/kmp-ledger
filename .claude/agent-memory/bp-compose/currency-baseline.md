---
name: currency-baseline
description: Upstream-currency verification of kmp-ledger Compose UI + Navigation 3 against pinned CMP 1.12.0 / nav3 runtime 1.1.7 + ui 1.1.1 / material3 1.12.0-alpha03, last re-checked 2026-09-06
metadata:
  type: project
---

Upstream-currency audit result, re-verified 2026-09-06 (prior passes 2026-07-16,
2026-07-02, 2026-06-26). **Read the pins from `gradle/libs.versions.toml` before
using anything below** — every version named here is a snapshot of what was pinned
on the date of the pass, not a constant.

**Baseline on 2026-09-06:** CMP 1.12.0 (released 2026-08-26, latest stable),
navigation3-runtime 1.1.7, navigation3-ui 1.1.1 (JetBrains), material3
1.12.0-alpha03, adaptive 1.3.0-beta02, lifecycle 2.11.0, material-icons 1.7.3.
The material3 and adaptive alphas are the exact coordinates the CMP 1.12.0
Dependencies table declares — verified still aligned; treat as deliberate pins,
never as staleness.

## Two corrections to earlier passes — do not restore the old claims

- **"No stability gaps" was WRONG.** `core/model/build.gradle.kts` applies only
  `ledger.kotlin.multiplatform` — no Compose compiler — so `Posting` / `NewPosting`
  are **unstable** to the Compose compiler, and that propagates to
  `PostingListUiState.Success(List<Posting>)` (`PostingListViewModel.kt:24`),
  `PostingDetailsUiState.Success(Posting)` (`PostingDetailsViewModel.kt:27`) and
  `PostingListItem(posting:)` (`PostingListScreen.kt:116`). Strong skipping compares
  unstable params by `===`, and the Room flow re-allocates every `Posting` on any
  write, so `===` never holds. The repo has **no** `@Stable`/`@Immutable` anywhere and
  **no** `stabilityConfigurationFile` (grep-verified 2026-09-06). Report as Should-fix.
  Fix options, in the order that fits this project's layering: a
  `stabilityConfigurationFile` naming `app.oreshkov.ledger.core.model.**` wired in
  `ledger.kotlin.multiplatform.koin.compose.gradle.kts`; or Compose-runtime +
  `@Immutable` on the model types; or `@Immutable` on the three UI-state classes.
  Verify with the existing `-Pledger.composeCompilerReports=true` switch. Quantifying
  the runtime cost is rv-perf's lane. Source:
  developer.android.com/develop/ui/compose/performance/stability/fix (2026-06-13).
- **"`currentTopLevel` plain `remember` — recipe does the same" is NO LONGER TRUE, but
  the replacement API shape is UNVERIFIED.** The behaviour gap is real: each section's
  stack is restored via `rememberNavBackStack(savedStateConfiguration, root)`
  (`App.kt:74-76`), so after process death the user returns to a fully-restored *start*
  section with their selected tab lost. The in-code comment at `App.kt:78-80` justifies
  the plain `remember` by appeal to the upstream recipe, and that appeal no longer holds.
  **However** — corrected 2026-09-06 against the pinned artifacts — the snippet this note
  previously recommended,
  `rememberSerializable(..., serializer = MutableStateSerializer(NavKeySerializer()))`,
  does **not** resolve on these pins: `androidx.navigation3:navigation3-runtime:1.1.7`
  ships only `NavBackStackSerializer`/`NavBackStackSerializerKt` in
  `androidx.navigation3.runtime.serialization` — there is **no `NavKeySerializer`**.
  `MutableStateSerializer` does exist (`androidx.savedstate:savedstate-compose:1.4.0`,
  resolved transitively), but `rememberSerializable` was not located on the pinned
  classpath. Do not propose that snippet without first confirming which serializer shape
  1.1.7 actually offers.
  The portable fix that needs no serializer plumbing: persist the selected section as an
  `Int` index into `sectionRoots` with `rememberSaveable`, deriving the `NavKey` from it.
  Raise as a note for the user's call — whether losing the tab is acceptable UX is a
  product decision, and the state-holder wiring itself is rv-compose's lane.

## Stale opt-ins to flag (both graduated upstream)

- `App.kt:11,41` — `ExperimentalMaterial3AdaptiveNavigationSuiteApi` no longer
  annotates any declaration in material3-adaptive-navigation-suite 1.12.0-alpha03;
  the marker class is a leftover. Removable. Keep `ExperimentalMaterial3AdaptiveApi`
  — `rememberListDetailSceneStrategy` and the `ListDetailSceneStrategy` companion
  are still experimental in adaptive 1.3.0-beta02.
- `@OptIn(ExperimentalMaterial3Api::class)` on all four screens
  (`PostingListScreen.kt:57`, `PostingEditScreen.kt:77`, `PostingDetailsScreen.kt:87`,
  `SettingsScreen.kt:72`) — the only experimental API they used was `TopAppBar`, which
  graduated in Jetpack Material3 1.5.0-alpha22 (2026-06-17), the release CMP's
  1.12.0-alpha03 is based on. Removable.

## Navigation 3 — verified current for the pinned 1.1.x

- The `NavDisplay(entries, …, sceneStrategies: List<SceneStrategy<T>>, …, onBack: () -> Unit)`
  overload `App.kt` binds to is **not** deprecated; the two `@Deprecated` overloads are
  `DeprecationLevel.HIDDEN` singular-`sceneStrategy` forms the project already avoids.
  nav3-runtime commonMain contains zero `@Deprecated` declarations.
- Multi-back-stack shell matches the recipe point for point apart from the
  `rememberSerializable` item above: per-section `rememberNavBackStack`, one
  `rememberSaveableStateHolderNavEntryDecorator` per stack (SaveableStateHolder first),
  `rememberDecoratedNavEntries` per stack, exit-through-home concatenation.
- `onBack` is `() -> Unit` in 1.1.x. The `onBackCompleted`/`onBackCancelled` split,
  `NavigationBackHandler` and the reshaped `DeepLinkRequest` are **1.2.0 only**.
- `NavigationSuiteScaffold` — `App.kt:123` binds the current `state`-carrying overload;
  the state-less ones are `DeprecationLevel.HIDDEN`.
- Do NOT flag the unremembered `associateWith` maps in `App.kt:74,98-110` — allocation
  churn is rv-perf's lane, and the recipe allocates the same way.
- **Alignment watch:** the CMP 1.12.0 Dependencies table names navigation3
  **1.2.0-alpha02**, while the catalog pins ui 1.1.1. Not a gap today — CMP's own
  `adaptive-navigation3:1.3.0-beta02` resolves navigation3-ui 1.1.0, 1.1.1 is the
  latest *stable*, and 1.2.0 is source-breaking. Re-evaluate when nav3-ui 1.2.0 ships
  stable or the next CMP bump forces it.

## Verified current — do not re-litigate

`collectAsStateWithLifecycle` everywhere (no bare `collectAsState`); theme flow
remembered on the use case then collected with `initialValue`; no
`LaunchedEffect(Unit)`/`(true)`; `staticCompositionLocalOf` for `LocalNavigator` with
the trade-off documented; every `Res`-generating module pins `packageOfResClass` and
only `core:compose` sets `publicResClass = true`; `Icons.AutoMirrored` for mirroring
glyphs; no CMP 1.12.0 deprecations hit (no `NativeCanvas`/`NativePaint`, no `SwingPanel`
background param) and `desktopApp` already on the `ui.window.v2` API.

**Deferred (out of bp-compose's lane):** `LaunchedEffect`-on-boolean snackbar
signalling and lifecycle-unaware `Channel` event collection → rv-compose;
allocation churn in `App.kt` → rv-perf.

**Why:** this file exists to stop re-deriving the same verdicts every pass — which
makes a wrong entry expensive: the two corrections above each suppressed a real
finding for multiple passes. An entry that says "do not flag X" must carry the
evidence that justified it and the date, so the next pass can tell a settled fact
from an expired one.

**How to apply:** re-read the pins first; anything above whose version moved is
unverified until re-checked against the pinned artifact's own sources (the Gradle
cache's `-sources.jar` is stronger evidence than a release note). Prefer correcting
an entry in place over appending a newer contradictory one.
