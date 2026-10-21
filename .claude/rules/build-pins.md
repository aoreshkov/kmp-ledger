---
paths:
  - "gradle/**"
  - "gradle.properties"
  - "build-logic/**"
  - "**/*.gradle.kts"
---

# Deliberate build pins

Material3, Material3 Adaptive and JetBrains Navigation 3 (`androidx-material3`, `androidx-adaptive`, `androidx-navigation3-ui` in `gradle/libs.versions.toml`) are pinned to the exact coordinates the pinned Compose Multiplatform release declares itself aligned to, prerelease or not. Material3 (`1.12.0-alpha03`) and `navigation3-ui` (`1.2.0-rc01`) are prereleases on purpose: they are not debt to pay down, and moving either to some other "stable" number would de-align the stack. Adaptive happens to be stable (`1.3.0`) for CM 1.12.2. `androidx-navigation3-runtime` (Jetpack) follows the "Based on Jetpack" column of the Navigation3 row. Re-check the alignment table in the Compose Multiplatform release notes on every CM bump.

AGP is pinned to the version the bundled IntelliJ IDEA plugin supports (the reason is commented in `gradle/libs.versions.toml`). `./gradlew help --warning-mode all` reports configuration-phase deprecations — *"Using a Project object as a dependency notation … will fail with an error in Gradle 10"* — all attributed to `com.android.internal.*` and none to this repo's own build scripts, so there is nothing here to fix. It is a constraint instead: **do not move to Gradle 10 while AGP is held at the IDEA ceiling.** When IDEA raises its ceiling and AGP is bumped, re-run that command and confirm none remain before evaluating Gradle 10.
