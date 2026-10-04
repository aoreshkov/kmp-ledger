---
paths:
  - "gradle/**"
  - "gradle.properties"
  - "build-logic/**"
  - "**/*.gradle.kts"
---

# Deliberate build pins

Material3 and Material3 Adaptive (`androidx-material3`, `androidx-adaptive` in `gradle/libs.versions.toml`) are pinned to prerelease versions on purpose: they are the exact coordinates the pinned Compose Multiplatform release declares itself aligned to. They are not debt to pay down, and moving either to a "stable" number would de-align the stack. Re-check the alignment table in the Compose Multiplatform release notes on every CM bump.

AGP is pinned to the version the bundled IntelliJ IDEA plugin supports (the reason is commented in `gradle/libs.versions.toml`). `./gradlew help --warning-mode all` reports configuration-phase deprecations — *"Using a Project object as a dependency notation … will fail with an error in Gradle 10"* — all attributed to `com.android.internal.*` and none to this repo's own build scripts, so there is nothing here to fix. It is a constraint instead: **do not move to Gradle 10 while AGP is held at the IDEA ceiling.** When IDEA raises its ceiling and AGP is bumped, re-run that command and confirm none remain before evaluating Gradle 10.
