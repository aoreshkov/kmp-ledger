---
name: gradle-currency-baseline
description: Gradle build areas already matching current best practice — don't re-raise these as findings
metadata:
  type: project
---

State of the kmp-ledger Gradle build vs upstream best practice (verified 2026-06-26 against docs.gradle.org for Gradle 9.6.0).

**Already current — do not re-flag:**
- Wrapper `gradle-9.6.0-bin.zip` = latest stable (released 2026-06-20). Re-check `gradle/wrapper/gradle-wrapper.properties` against gradle.org/releases each audit.
- `build-logic/` uses precompiled script plugins (`kotlin-dsl`), `compileOnly` plugin deps, three composable `ledger.kotlin.multiplatform[.koin][.compose]` plugins. Matches "Sharing build logic with convention plugins". No `subprojects {}`/`allprojects {}`.
- Lazy APIs: no eager `tasks.create`, no `afterEvaluate`. `composeCompiler` block reads `providers.gradleProperty(...)`. Good.
- Configuration cache + build cache both enabled in `gradle.properties` (`org.gradle.configuration-cache=true`, `org.gradle.caching=true`). CC `problems` left at default `fail` (correct for an already-CC-clean build — don't suggest `warn`).
- Version catalog: version.ref everywhere, plugin aliases via `alias(libs.plugins.*)`, no hardcoded versions in build scripts.
- `rootProject.name` set in both root and `build-logic` settings.

**Why:** Avoids spending audit budget re-deriving the same green areas every run.
**How to apply:** On the next currency pass, confirm these are unchanged and move on; focus on the open optional items in [[deliberate-gradle-divergences]] and anything newly drifted.

Open optional (perf/idiom, not correctness): `org.gradle.parallel=true` absent (perf doc recommends it for multi-project builds); no catalog `[bundles]` (compose-ui set, test set are candidates); `androidApp` uses `project.property(...)` where `providers.gradleProperty(...)` is the lazier idiom.

Internal-correctness items (defer to rv-build): duplicated Kover exclude lists between root `build.gradle.kts` and the base convention plugin.
