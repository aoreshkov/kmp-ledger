---
name: kover-floor-topology
description: Where Kover coverage floors live (aggregate vs per-module) and the line/branch values, so a regression review knows the expected layout
metadata:
  type: project
---

Kover coverage is enforced at two levels; both must stay in sync with the excludes list.

**Aggregate floors** (root `build.gradle.kts`): line 88, branch 60, instruction 84. Branch is deliberately modest because Compose codegen emits synthetic branches.

**Per-module floors** (each module's own `build.gradle.kts`, `kover { reports { verify { rule ... } } }`):
- `core:domain`, `core:data`, `feature:posting:api` — pure-logic / serialization contract: line 90, branch 85.
- `feature:posting:impl` — most logic-dense (3 ViewModels) but holds line 90 with branch lowered to 60 because `@Composable` screens drag branch coverage down.
- Other modules (model, common, compose, navigation, ui, bootstrap, test) have no per-module rule; they ride the aggregate floor only.

**Excludes** are duplicated in TWO places that must stay identical: root `build.gradle.kts` aggregate filters AND the base convention plugin `ledger.kotlin.multiplatform.gradle.kts` `kover { reports { filters { excludes } } }`. Both exclude: `*ComposableSingletons*`, `*_Factory`, `*$$serializer`, `*.generated.resources.*`, `*.compose.resources.*`, `*.di.*`, and `@Preview`-annotated methods. (DI is Koin-based, so no codegen-module exclusion pattern is needed.)

CI gate: `build.yml` `check` job runs `./gradlew allTests koverXmlReport koverVerify`; `koverVerify` is what enforces the floors. The `madrapps/jacoco-report` PR comment step is informational only (min-coverage 0).

Relates to [[jvm-target-config]].
