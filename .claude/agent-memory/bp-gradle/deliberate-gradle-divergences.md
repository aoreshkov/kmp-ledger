---
name: deliberate-gradle-divergences
description: Intentional Gradle choices in kmp-ledger to respect — do not report as currency gaps. States the rules, not version numbers
metadata:
  type: project
---

Deliberate, documented divergences from generic upstream guidance. Respect these; at most
mention as "intentional" — never raise as Should-fix.

- **Explicit per-target `jvmTarget = JVM_21` instead of `jvmToolchain(21)`/Java toolchain.**
  Set on base plugin `jvm()`, `androidApp`, `desktopApp`. Reproducibility comes from the
  daemon being pinned to JDK 21 (`gradle/gradle-daemon-jvm.properties` →
  `toolchainVersion=21`) plus CI Temurin 21. Decision recorded in
  `docs/full-review-2026-06-23.md` (C1/S1) and rv-build memory [[jvm-target-config]]. 21 is
  a hard ceiling (daemon JDK).
- **material3 and adaptive are pinned to prerelease versions on purpose.** The rule, not
  the number: they must be *the exact coordinates Compose Multiplatform declares alignment
  with* in its release-notes Dependencies table. Read `androidx-material3` and
  `androidx-adaptive` from `gradle/libs.versions.toml` and check them against the table for
  the pinned `compose-multiplatform`. A pin that matches the table is correct even when it
  looks like an old alpha; a pin that no longer matches is a real finding. See user memory
  `material3-version-pin`.
  *(Corrected 2026-09-06: this bullet used to hardcode "material3 1.11.0-alpha07, aligned
  with CMP 1.11.1" — both had moved, so the note would have vouched for a pin the project
  no longer has.)*
- **Kover floors** (aggregate + per-module) are a deliberate policy; the branch floor is
  kept modest for Compose synthetic branches. See user memory `kover-coverage-policy`.
- **Room destructive fallback** (`fallbackToDestructiveMigration(dropAllTables=true)`) is
  an intentional pre-release posture, not an oversight.
- **AGP is pinned below latest on purpose** — to the ceiling the user's bundled IntelliJ
  IDEA plugin supports; the catalog carries the reason and an inspection suppression on the
  line. Read the pin rather than naming a number here. Details in
  [[gradle-currency-baseline]].
  *(Added 2026-09-06: this pin now has an expiry. The pinned AGP's internal plugins
  (`com.android.internal.application`, `com.android.internal.kotlin.multiplatform.library`)
  pass a `Project` object as dependency notation — deprecated in Gradle 9.6, a hard **error
  in Gradle 10**. Nothing in this repo's build scripts triggers it, so there is no code fix;
  the pin stays deliberate. But the project cannot move to Gradle 10 until AGP is bumped
  past it. Verify with `./gradlew help --warning-mode all` and read the `pluginId` fields in
  `build/reports/problems/problems-report.html`.)*

**Why:** these were chosen with reasons; re-raising them wastes the user's time and
contradicts prior review conclusions. But a divergence is only "deliberate" while its
*reason* holds — an alignment pin whose upstream alignment moved is no longer the
decision the project made.

**How to apply:** if an audit surfaces one of these, check the stated reason still holds,
note it is intentional, and move on. Never restate a version number here that can be read
from the catalog. Owner of toolchain/target internal correctness is the rv-build agent.
