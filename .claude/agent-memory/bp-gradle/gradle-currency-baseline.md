---
name: gradle-currency-baseline
description: Gradle build areas already matching current best practice — don't re-raise these. Structural verdicts durable; version claims are dated observations, re-derive every run
metadata:
  type: project
---

State of the kmp-ledger Gradle build vs upstream best practice. Structure re-verified
2026-09-06 against docs.gradle.org 9.7.1; prior passes 2026-07-16, 2026-07-02.

**Read every version from `gradle/libs.versions.toml` and
`gradle/wrapper/gradle-wrapper.properties` before judging anything.** Numbers written
below are dated observations, not current facts.

## Already current — do not re-flag (structural, durable)

- `build-logic/` uses precompiled script plugins (`kotlin-dsl`), `compileOnly` plugin deps,
  and three composable `ledger.kotlin.multiplatform[.koin][.compose]` plugins. Matches
  "Sharing build logic with convention plugins" and "Favor `build-logic` composite builds".
  No `subprojects {}`/`allprojects {}`, no `afterEvaluate`, no eager `tasks.create` anywhere
  in the repo.
- Lazy APIs: `composeCompiler` reads `providers.gradleProperty(...)`; androidApp version
  code/name via `providers.gradleProperty`; signing secrets via
  `providers.environmentVariable`. The remaining `.get()` calls (androidApp:27,31-34,
  desktopApp:76) feed *non-lazy* DSL properties (`compileSdk: Int`, `versionCode: Int`,
  `packageVersion: String`) — the tasks best practice "don't call `get()` outside a task
  action" does not apply there. **Do not flag these.**
- Configuration cache + build cache + `org.gradle.parallel=true` set in `gradle.properties`.
  CC `problems` left at default `fail` (correct for a CC-clean build — never suggest `warn`;
  the docs call `warn` a migration aid only).
- Version catalog: `version.ref` everywhere, plugin aliases via `alias(libs.plugins.*)`,
  `-plugin` suffix on plugin libraries, `common-test` bundle declared AND consumed. No
  hardcoded versions anywhere in `*.gradle.kts` (grep for `"\d+\.\d+\.\d+"` returns none).
- No `gradle.properties` in any subproject (matches the general best practice).
- Daemon JVM pinned via `gradle/gradle-daemon-jvm.properties` (`toolchainVersion=21` plus
  foojay `toolchainUrl` entries from `updateDaemonJvm`) — current recommended daemon-JVM
  provisioning. No toolchain resolver plugin needed.
- Repositories centralized in root settings (`dependencyResolutionManagement`), `google()`
  content-filtered, `RepositoriesMode.FAIL_ON_PROJECT_REPOS` enforced, `rootProject.name`
  set in both settings files. **Exception:** `build-logic/settings.gradle.kts` does *not*
  content-filter its `google()` — raised 2026-09-06 as Optional.
- `distributionSha256Sum` present in gradle-wrapper.properties; `-bin` distribution used.
  Both match the security/performance best practices — do not re-flag.
- `core/database` already uses target-specific KSP configurations (`kspAndroid`, `kspJvm`,
  `kspIosArm64`, `kspIosSimulatorArm64`), which is what KSP 2.3.10 deprecated the plain
  `ksp(...)` configuration in favour of. No migration owed.

## Verified empirically 2026-09-06 — the cheap check that pays

`./gradlew help --warning-mode all --console=plain` then read
`build/reports/problems/problems-report.html`. On that date: **5 problems, all one
deprecation, all attributed to AGP internal plugin IDs** (`com.android.internal.application`,
`com.android.internal.kotlin.multiplatform.library`) — zero from KGP, zero from project
build scripts, CC entry stored cleanly. Re-run this before asserting the build is
deprecation-clean; the problems report carries `pluginId` attribution, which is what tells
you whether a warning is yours or a plugin's.

## Resolved 2026-09-06 — the Kotlin/Gradle matrix overrun is NOT a finding

Kotlin's own table (kotlinlang.org "Configure a Gradle project") gives KGP 2.4.0–2.4.10 a
fully-supported Gradle range of 7.6.3–9.5.0 and AGP 8.5.2–9.1.0, then says verbatim: "You
can also use Gradle and AGP versions up to the latest releases, but if you do, keep in mind
that you might encounter deprecation warnings or some new features might not work."
Counter-evidence from the Gradle side (docs.gradle.org/9.7.1 compatibility matrix): Gradle
9.7 **embeds Kotlin 2.4.0**, is tested with Kotlin 2.0.0–2.4.20-Beta1 and AGP 9.0–9.4.0-alpha03.
The empirical check above found zero KGP deprecations. The AGP overrun is a single patch
inside the same minor line. **Verdict: no action. Do not re-raise this as a finding** —
re-check only if the empirical run starts showing KGP-sourced warnings.

## Version-dependent claims — re-derive, do not trust

- **Wrapper vs latest.** On 2026-09-06 the wrapper pinned **9.6.1** while latest stable was
  **9.7.1** (2026-08-19; 9.7.0 was 2026-08-06). Gradle's general best practice is "Use the
  Latest Minor Version of Gradle". Re-check gradle.org/releases every run.
- **AGP is deliberately pinned below latest** to the user's IntelliJ IDEA plugin ceiling —
  see [[deliberate-gradle-divergences]], which now also records the Gradle 10 expiry this
  pin acquired. Do not flag AGP as behind latest.
- **Pins that were AT latest on 2026-09-06** (so a bump report is wrong unless upstream
  moved): Kover 0.9.9 (0.9.9 itself is the release that fixed Gradle 9.6 deprecation
  warnings), BCV 0.18.2, compose-hot-reload 1.2.0 (1.3.0-alpha01 exists — prerelease, not a
  gap). KSP was **behind**: pinned 2.3.9, latest 2.3.11.
- **BCV**: the standalone `binary-compatibility-validator` plugin is a deliberate pin.
  Kotlin's built-in KGP `abiValidation {}` DSL is intended to replace it but was still
  `@ExperimentalAbiValidation` in Kotlin 2.4, and the standalone plugin is NOT deprecated
  (deprecation follows stabilization, KT-71172). Staying on standalone is correct;
  migration is informational only.

## Standing Optional items (raise only as Optional, never higher)

- No `gradle/verification-metadata.xml` (dependency verification). Gradle 9.7.0 added
  `origin`/`reason` attributes on trusted PGP keys if this is ever adopted.
- `org.gradle.configuration-cache.parallel` is still **incubating** as of 9.7.1 (docs warn it
  can cause `ConcurrentModificationException`). Isolated Projects auto-enables parallel CC,
  so this is mostly subsumed by that migration.
- `org.gradle.tooling.parallel=true` is a REAL documented property since Gradle 9.4.0
  (parallel Tooling API / IDE sync). The comment in `gradle.properties` is accurate — do not
  flag it as invented.

**Why:** avoids spending audit budget re-deriving the same green areas every run — but the
structural verdicts and the version claims rot at completely different rates, so they are
kept in separate sections. Mixing them is what let "Kover 0.9.8 is latest" sit under a "do
not re-flag" heading for seven weeks.

**How to apply:** trust the structural section; re-derive every version claim against the
pins and gradle.org/releases; run the empirical `--warning-mode all` check before claiming
deprecation cleanliness; then correct this note in place — writes under
`.claude/agent-memory/` are permitted. Respect [[deliberate-gradle-divergences]].

Internal-correctness items (defer to rv-build): duplicated Kover exclude lists between root
`build.gradle.kts` and the base convention plugin; the base plugin's `commonTest` using bare
`kotlin-test` rather than the `common-test` bundle the modules use.
