---
name: jvm-target-config
description: All JVM targets standardized on JVM 21; the JDK-21 daemon/CI is the hard ceiling
metadata:
  type: project
---

As of 2026-06-23 all JVM targets are standardized on **JVM 21** (CLAUDE.md convention now reads "base KMP, JVM 21"). This replaced the earlier inconsistent state (Android JVM_21 / app JVM_17 / desktop default 1.8).

**Where jvmTarget is set (all 21):**
- Base convention plugin `ledger.kotlin.multiplatform`: Android target `compilerOptions { jvmTarget = JVM_21 }` AND `jvm()` target `compilerOptions { jvmTarget = JVM_21 }`. All `core:*` / `feature:*` modules inherit this.
- `androidApp` (`com.android.application`): `compileOptions` source/target = VERSION_21 and `jvmTarget = JVM_21`.
- `desktopApp` (`kotlin.jvm`): `kotlin { compilerOptions { jvmTarget = JVM_21 } }`.
- iOS targets are Kotlin/Native — `jvmTarget` does not apply.

The project sets the target explicitly per JVM target rather than via `jvmToolchain(...)`, matching the existing style; daemon and CI already run JDK 21 so no toolchain provisioning is needed.

**JVM ceiling = 21 (hard).** The Gradle daemon is pinned to JDK 21 (`gradle/gradle-daemon-jvm.properties` → `toolchainVersion=21`) and CI provisions JDK 21 (Temurin, `.github/actions/gradle-setup/action.yml`). Kotlin/javac cannot emit bytecode newer than the compiling JDK, so 21 is the max regardless of what Android/Desktop accept. (Local launcher JVM is 25, but Gradle compiles on the daemon, not the launcher.) Android via AGP 9.2.1 / compileSdk 37 accepts 21 and is not the limiting factor.

**To raise the target above 21 later:** bump `toolchainVersion` in `gradle-daemon-jvm.properties` AND CI `java-version`, then verify AGP/D8 accepts the higher class-file version for the Android target (the likely blocker above 21). Until both move, anything >21 won't compile.

Relates to [[material3-version-pin]].
