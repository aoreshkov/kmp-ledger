---
name: currency-baseline
description: Upstream-currency verification of kmp-ledger Compose UI + Navigation 3 against pinned CMP 1.11.1 / nav3 runtime 1.1.4 + ui 1.1.1, last re-checked 2026-07-16
metadata:
  type: project
---

Upstream-currency audit result (re-verified 2026-07-16; prior passes 2026-07-02,
2026-06-26): the Compose + Navigation 3 surface is CURRENT for the pinned
versions. UI code is materially unchanged across these passes.

**Version delta as of 2026-07-16:** project now pins navigation3-runtime 1.1.4
(Google `androidx.navigation3`) + navigation3-ui 1.1.1 (JetBrains
`org.jetbrains.androidx.navigation3`, CMP-aligned — treat like the material3
alpha pin, not a gap). CMP still 1.11.1 (latest stable; 1.12.0-alpha02 out
2026-06-16). lifecycle bumped to 2.11.0-rc01. These bumps land squarely on the
API surface already verified below — no code changes were required or observed.

**Navigation 3 (pinned runtime 1.1.4, released 2026-07-01, bug-fixes only):**
- Multi-back-stack shell (App.kt, added commit aad1d23 2026-06-29) matches the
  official recipe developer.android.com/guide/navigation/navigation-3/recipes/multiple-backstacks
  (page updated 2026-03-16) point for point: per-section `rememberNavBackStack`,
  SEPARATE decorator instances per stack via `rememberDecoratedNavEntries`,
  exit-through-home concatenation, entries-overload `NavDisplay`. App even goes
  beyond the recipe (adds per-stack `rememberViewModelStoreNavEntryDecorator`,
  remembers the combined entries list). Do NOT flag the unremembered
  `associateWith` maps — values are stable remembered instances; recipe does the
  same; allocation churn is rv-perf's lane.
- `rememberNavBackStack(SavedStateConfiguration, vararg)` (since 1.0.0-alpha08),
  `rememberDecoratedNavEntries` (alpha09), entries-overload NavDisplay (alpha10)
  all exist in 1.1.x — confirmed via release notes.
- `NavDisplay.onBack` is `() -> Unit` (called N times for N pops). App.kt correct.
- Decorator order: SaveableStateHolder decorator MUST be first — App.kt correct.
- `rememberSceneSetupNavEntryDecorator` removed from public API in 1.0.0-alpha11;
  auto-included. Do NOT flag its absence.
- `sceneStrategies` takes `List<SceneStrategy>`; remembered list — correct.
- `ListDetailSceneStrategy.listPane()/detailPane()` metadata in PostingModule /
  navigation<> DSL entries — current API shape.
- NEWER-STABLE watch: nav3 1.1.4 (2026-07-01) is a bug-fix patch — safe optional
  bump. 1.2.0-alpha line (alpha05 2026-07-01) adds NavigationBackHandler,
  deep links, ResultEventBus; breaking DeepLinkRequest changes in alpha05.
  Revisit only if project bumps off 1.1.x.

**Compose MP (1.11.1, released 2026-06-02 — still latest stable as of 2026-07-02):**
- All screens use `collectAsStateWithLifecycle()`; theme flow collected with
  `initialValue = ThemeMode.SYSTEM` on a `remember(getThemeMode)`-ed cold flow — current.
- LazyColumn `items(key = { it.id })`; method-ref callbacks; strong skipping
  (Kotlin 2.4) memoizes the loop lambdas in NavigationSuiteScaffold items and
  ThemeModeRow — no stability gaps.
- `staticCompositionLocalOf` for LocalNavigator — correct.
- No `derivedStateOf` and none warranted; `remember(keys)` for displayedEntries
  is the documented choice (state change should recompose).
- `Icons.AutoMirrored.Filled.ArrowBack` — current (plain ArrowBack deprecated).
- Compose resources via `org.jetbrains.compose.resources.stringResource` in
  composition; snackbar messages resolved in composition then captured into
  LaunchedEffect — matches docs pattern.
- `currentTopLevel` is plain `remember { mutableStateOf }` not rememberSaveable —
  DELIBERATE, documented in App.kt comment (section stacks are restored; only
  selected tab resets). Recipe does the same. Do not flag.

**Deferred (out of bp-compose lane):** LaunchedEffect-on-boolean snackbar
pattern in PostingEditScreen.kt and SettingsScreen.kt — event-modeling
correctness owned by rv-compose, not an upstream-deprecated pattern.

**Sources:** developer.android.com/jetpack/androidx/releases/navigation3;
developer.android.com/guide/navigation/navigation-3/recipes/multiple-backstacks
(2026-03-16); developer.android.com/guide/navigation/navigation-3/save-state;
kotlinlang.org/docs/multiplatform/whats-new-compose-111.html.
