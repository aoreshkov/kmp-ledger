---
paths:
  - "compose_stability.conf"
  - "core/model/**"
  - "core/ui/**"
  - "core/compose/**"
  - "feature/*/impl/**"
  - "build-logic/**"
---

# Compose stability

Compose stability is declared out-of-band in `compose_stability.conf` at the repo root, wired into every Compose module through `stabilityConfigurationFiles` in the `ledger.kotlin.multiplatform.koin.compose` convention plugin. It exists because `core:model` deliberately carries no Compose compiler plugin, so its types would otherwise reach consumers with no stability metadata and be inferred **unstable** — and strong skipping (default since Kotlin 2.0) then compares them by instance identity, which the Room-backed flow never preserves. The file asserts two invariants, and nothing checks them for you: everything under `app.oreshkov.ledger.core.model` stays an immutable `val`-only holder, and `List`s handed to composables are never mutated in place (they are declared `kotlin.collections.List<*>`, so a list is exactly as stable as its element type rather than blanket-stable). Verify a change with `javap -p -c` on a UI-state class: `$stable = 0` is stable, `8` is unstable. Editing the conf file does **not** invalidate the compile tasks, so re-measure with `--rerun-tasks`.
