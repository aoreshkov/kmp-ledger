---
name: gradle-currency-baseline
description: Gradle build areas already matching current best practice — don't re-raise these as findings
metadata:
  type: project
---

State of the kmp-ledger Gradle build vs upstream best practice (verified 2026-07-02 against docs.gradle.org for Gradle 9.6.1).

**Already current — do not re-flag:**
- Wrapper `gradle-9.6.1-bin.zip` = latest stable (released 2026-06-26). Re-check `gradle/wrapper/gradle-wrapper.properties` against gradle.org/releases each audit.
- Toolchain versions current stable as of 2026-07-02: Kotlin 2.4.0, AGP 9.2.1 (9.2.x line, Apr 2026), Kover 0.9.8 (Mar 2026), Compose MP 1.11.1.
- `build-logic/` uses precompiled script plugins (`kotlin-dsl`), `compileOnly` plugin deps, three composable `ledger.kotlin.multiplatform[.koin][.compose]` plugins. Matches "Sharing build logic with convention plugins". No `subprojects {}`/`allprojects {}`, no `afterEvaluate`, no eager `tasks.create` anywhere.
- Lazy APIs: `composeCompiler` reads `providers.gradleProperty(...)`; androidApp version code/name via `providers.gradleProperty`; signing secrets via `providers.environmentVariable` (commit 9e0c19b). The old `project.property(...)` nit is fixed.
- Configuration cache + build cache + `org.gradle.parallel=true` all set in `gradle.properties`. CC `problems` left at default `fail` (correct for a CC-clean build — don't suggest `warn`). `org.gradle.configuration-cache.parallel` is still incubating in 9.6.1 — at most Optional, with the "some builds may not work" caveat.
- Version catalog: version.ref everywhere, plugin aliases via `alias(libs.plugins.*)`, `common-test` bundle declared AND consumed (core:common/domain/database/data/datastore). No hardcoded versions.
- Daemon JVM pinned via `gradle/gradle-daemon-jvm.properties` (toolchainVersion=21 + foojay toolchainUrl entries from `updateDaemonJvm`) — this is the current recommended daemon-JVM provisioning.
- Repositories centralized in settings (`dependencyResolutionManagement`), google() content-filtered; `rootProject.name` set in both settings files.

**Standing Optional items (raise only as Optional, never higher):**
- No `distributionSha256Sum` in gradle-wrapper.properties (wrapper distribution checksum verification).
- No `gradle/verification-metadata.xml` (dependency verification).
- `RepositoriesMode.FAIL_ON_PROJECT_REPOS` not enforced (behavior already compliant; enforcement only).

**Why:** Avoids spending audit budget re-deriving the same green areas every run.
**How to apply:** On the next currency pass, confirm these are unchanged and move on; respect [[deliberate-gradle-divergences]].

Internal-correctness items (defer to rv-build): duplicated Kover exclude lists between root `build.gradle.kts` and the base convention plugin.
