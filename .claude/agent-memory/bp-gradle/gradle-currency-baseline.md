---
name: gradle-currency-baseline
description: Gradle build areas already matching current best practice — don't re-raise these as findings
metadata:
  type: project
---

State of the kmp-ledger Gradle build vs upstream best practice (re-verified 2026-07-16 against docs.gradle.org for Gradle 9.6.1; prior pass 2026-07-02).

**Already current — do not re-flag:**
- Wrapper `gradle-9.6.1-bin.zip` = latest stable (released 2026-06-26). Re-check `gradle/wrapper/gradle-wrapper.properties` against gradle.org/releases each audit.
- Toolchain versions current stable as of 2026-07-02: Kotlin 2.4.0, Kover 0.9.8 (Mar 2026), Compose MP 1.11.1.
- AGP is **deliberately pinned to 9.1.0** (changed 2026-07-19, was 9.2.1) — NOT the latest AGP (9.3.0). Reason: IntelliJ IDEA's bundled Android plugin lags the AGP release train (and lags Android Studio), and the user's latest IDEA caps at AGP 9.1.0; 9.2.1 made IDEA refuse to sync ("incompatible version … Latest supported version is AGP 9.1.0"). Pinning to the IDEA ceiling keeps the project openable in both IDEA and Android Studio with no per-machine IDE flags. **Do not flag AGP as behind latest** — bump only after IDEA raises its ceiling (track JetBrains IDEA-385007). Gradle 9.6.1 already exceeds the min for AGP 9.1/9.2/9.3, so a later AGP bump won't need a Gradle change.
- `build-logic/` uses precompiled script plugins (`kotlin-dsl`), `compileOnly` plugin deps, three composable `ledger.kotlin.multiplatform[.koin][.compose]` plugins. Matches "Sharing build logic with convention plugins". No `subprojects {}`/`allprojects {}`, no `afterEvaluate`, no eager `tasks.create` anywhere.
- Lazy APIs: `composeCompiler` reads `providers.gradleProperty(...)`; androidApp version code/name via `providers.gradleProperty`; signing secrets via `providers.environmentVariable` (commit 9e0c19b). The old `project.property(...)` nit is fixed.
- Configuration cache + build cache + `org.gradle.parallel=true` all set in `gradle.properties`. CC `problems` left at default `fail` (correct for a CC-clean build — don't suggest `warn`). `org.gradle.configuration-cache.parallel` is still incubating in 9.6.1 — at most Optional, with the "some builds may not work" caveat.
- Version catalog: version.ref everywhere, plugin aliases via `alias(libs.plugins.*)`, `common-test` bundle declared AND consumed (core:common/domain/database/data/datastore). No hardcoded versions.
- Daemon JVM pinned via `gradle/gradle-daemon-jvm.properties` (toolchainVersion=21 + foojay toolchainUrl entries from `updateDaemonJvm`) — this is the current recommended daemon-JVM provisioning.
- Repositories centralized in settings (`dependencyResolutionManagement`), google() content-filtered; `rootProject.name` set in both settings files.

**Resolved since 2026-07-02 (do not re-flag):**
- `distributionSha256Sum` IS now present in gradle-wrapper.properties (line 3) — wrapper checksum verification done.
- `RepositoriesMode.FAIL_ON_PROJECT_REPOS` IS now enforced in settings.gradle.kts:19.

**Standing Optional items (raise only as Optional, never higher):**
- No `gradle/verification-metadata.xml` (dependency verification).
- `org.gradle.tooling.parallel=true` (gradle.properties:23) is a REAL documented property since Gradle 9.4.0 (parallel Tooling API / IDE sync). Comment is accurate — do not flag.
- BCV: project uses standalone `binary-compatibility-validator` 0.18.1 (deliberate pin). Kotlin 2.2+ ships a built-in KGP `abiValidation {}` DSL intended to replace it, but it is still `@ExperimentalAbiValidation` in Kotlin 2.4 and the standalone plugin is NOT yet officially deprecated (deprecation follows stabilization, KT-71172). Staying on standalone is current-appropriate; migration is informational only.

**Why:** Avoids spending audit budget re-deriving the same green areas every run.
**How to apply:** On the next currency pass, confirm these are unchanged and move on; respect [[deliberate-gradle-divergences]].

Internal-correctness items (defer to rv-build): duplicated Kover exclude lists between root `build.gradle.kts` and the base convention plugin.
