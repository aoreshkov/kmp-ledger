---
name: deliberate-gradle-divergences
description: Intentional Gradle choices in kmp-ledger to respect — do not report as currency gaps
metadata:
  type: project
---

Deliberate, documented divergences from generic upstream guidance. Respect these; at most mention as "intentional" — never raise as Should-fix.

- **Explicit per-target `jvmTarget = JVM_21` instead of `jvmToolchain(21)`/Java toolchain.** Set on base plugin `jvm()`, `androidApp`, `desktopApp`. Reproducibility comes from the daemon being pinned to JDK 21 (`gradle/gradle-daemon-jvm.properties` → `toolchainVersion=21`) + CI Temurin 21. Decision recorded in `docs/full-review-2026-06-23.md` (C1/S1) and build-ci memory `jvm-target-config.md`. 21 is a hard ceiling (daemon JDK).
- **material3 pinned to `1.11.0-alpha07`** — aligned with Compose MP 1.11.1, not a stale pin. See user memory `material3-version-pin`.
- **Kover floors** (aggregate 88/60/84 + per-module) are a deliberate policy; branch floor kept modest for Compose synthetic branches. See user memory `kover-coverage-policy`.
- **Room destructive fallback** (`fallbackToDestructiveMigration(dropAllTables=true)`) is an intentional pre-release posture, not an oversight.

**Why:** These were chosen with reasons; re-raising them wastes the user's time and contradicts prior review conclusions.
**How to apply:** If an audit surfaces one of these, note it's intentional and move on. Owner of toolchain/target internal correctness is the build-ci agent.
