# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.6.2] - 2026-07-18

### Fixed
- Pinned `desktopApp` Java source/target compatibility to 21 so the module builds even when the Gradle daemon runs on a newer JDK (e.g. an IDE overriding the daemon JVM pin with its own runtime), instead of failing Kotlin's JVM-target validation with `compileJava` targeting the daemon JDK.

## [1.6.1] - 2026-07-18

### Fixed
- Committed the generated Gradle daemon JVM pin (`gradle/gradle-daemon-jvm.properties`, JDK 21) so consumers building from source get the correct toolchain automatically, instead of relying on a gitignored, locally-generated file.

## [1.6.0] - 2026-07-18

### Added
- Added GitHub Sponsors funding (`.github/FUNDING.yml`) plus a Sponsor badge and Sponsors section in the README.
- Added a "Generate This Project" README section cross-linking the KMP Project Wizard IntelliJ plugin.
- Added an explicit Gradle wrapper-validation step to the build workflow for the OpenSSF Scorecard Binary-Artifacts exemption.
- Added a `workflow_dispatch` trigger to the Scorecard workflow.
- Added Dependabot supply-chain cooldowns (3 days default, 7 for majors) and grouped Gradle minor/patch updates, ignoring Compose `material3` artifacts to stay aligned with Compose Multiplatform.

### Changed
- Upgraded AndroidX Lifecycle to 2.11.0-rc01.
- Enabled parallel IDE sync (`org.gradle.tooling.parallel=true`, Gradle 9.4+).
- Bumped pinned GitHub Actions: `actions/attest` v4.2.0, `softprops/action-gh-release` v3.0.2, `codeql-action/upload-sarif` v4.37.1.
- Extended `.gitignore` to cover `docs/`, `.env`, and `Secrets.xcconfig`.

## [1.5.0] - 2026-07-04

### Added
- Added an OpenSSF Scorecard CI workflow and a README badge.
- Added a `CONTRIBUTING` guide and a Contributor Covenant 2.1 code of conduct.

### Changed
- Upgraded Room and SQLite from `-rc01` to 3.0.0 / 2.7.0 stable.
- Upgraded the Navigation 3 runtime to 1.1.4.
- Enabled R8 minification and resource shrinking for the Android release build.
- Enabled Kotlin `extraWarnings` in the base multiplatform convention plugin.
- Enforced `FAIL_ON_PROJECT_REPOS` repository mode and pinned the Gradle wrapper distribution SHA-256.
- Constrained BouncyCastle `bcprov` to 1.84 to clear the robolectric-transitive GHSA-574f-3g2m-x479 alert.
- Gated the Android release CI job to the `release` environment.
- Hardened the dependency-review and dependency-submission workflows, excluding build-tooling and Swift-export worker deps from the submitted dependency graph.
- Split the README screenshots into Android and Desktop groups.

## [1.4.0] - 2026-06-30

### Added
- Added a Settings feature (`feature:settings:api` + `:impl`) with a theme-mode picker (System / Light / Dark).
- Added the `core:datastore` module, persisting settings via DataStore Preferences (`ledger.preferences_pb`), with per-platform path actuals and corruption/IO recovery.
- Added `SettingsRepository` (declared in `core:domain`) plus `GetThemeModeUseCase` and `SetThemeModeUseCase`.
- Added the `ThemeMode` model in `core:model`.
- Added adaptive top-level navigation: a `NavigationSuiteScaffold` shell that aggregates each feature's `TopLevelDestination` via DI.
- Added `FakeSettingsRepository` in `core:test`.
- Added a `dependency-submission` CI workflow.

### Changed
- Reworked `Navigator` to hold one back stack per section with exit-through-home back handling.
- Drove the theme from the stored preference in `App()` via `LedgerTheme(themeMode)`.
- Build CI now runs `./gradlew check` (tests + lint) instead of `allTests`.
- Read Android signing secrets via the Gradle provider API for config-cache correctness.
- Updated the Desktop CI matrix (`macos-latest` → `macos-26`) and disabled the Windows desktop Gradle cache.
- Added `androidx-datastore` 1.2.1 and `material3-adaptive-navigation-suite` to the version catalog.
- Resolved the Room `schemaDirectory` via the Gradle `layout` API.

### Removed
- Dropped the unused `room3-sqlite-wrapper` dependency and the redundant `kotlin.swift-export.experimental.nowarn` property.

## [1.3.0] - 2026-06-27

### Added
- Added `runCatchingCancellable`, a cancellation-safe `Result` wrapper in `core:common` that rethrows `CancellationException`.
- Added delete-failure feedback on the posting details screen via a `deleteFailedEvent` snackbar and the `posting_details_delete_failed` string.
- Added a per-module Kover coverage rule for `feature:posting:impl` (line 90 / branch 60).
- Added a `common-test` version-catalog bundle for shared test dependencies.

### Changed
- Migrated use cases and `PostingEditViewModel` from stdlib `runCatching` to `runCatchingCancellable` for cancellation safety.
- Raised the Android and Desktop JVM/Java target from 17 to 21.
- Memoized the Navigation decorator and scene-strategy lists in `App.kt` to avoid per-recomposition reallocation.
- Switched the database to `fallbackToDestructiveMigration(dropAllTables = true)` to make the pre-release migration posture explicit.
- Read the project version via `providers.gradleProperty(...)` instead of `project.property(...)` in build scripts.
- Enabled Gradle parallel builds (`org.gradle.parallel=true`).
- Upgraded Logback to 1.5.37.
- Upgraded the Gradle wrapper to 9.6.1.
- Set `persist-credentials: false` on all CI checkouts and switched provenance to `actions/attest` v4.1.1.

### Fixed
- Fixed the Android Room builder to use `applicationContext`, avoiding a potential Activity/Context leak.
- Corrected `room3-sqlite-wrapper` to reference the `androidx-room` version instead of the wrong `androidx-sqlite` ref.

## [1.2.0] - 2026-06-21

### Added
- Added an injectable `AppDispatchers` seam (`DefaultAppDispatchers`) and `DispatcherModule` so coroutine dispatchers are provided via Koin.
- Added Kover code coverage with aggregate floors (line/branch/instruction) and per-module rules, enforced in CI via `koverVerify`.
- Added a signed Android release build configuration that activates when CI supplies keystore credentials via environment variables.
- Added a Gradle managed virtual device (`aospAtd30`) and an instrumented-test CI job.
- Added an opt-in Compose compiler stability/skippability report (`-Pledger.composeCompilerReports=true`).
- Added build provenance attestation, SHA-256 checksums, and a tag-versus-`gradle.properties` version verification job to the release workflow.
- Added a dependency-review workflow, Dependabot config, CODEOWNERS, security policy, and issue/PR templates.
- Added path-filtering plus JUnit and coverage reporting to the build CI workflow.

### Changed
- Moved repository database work off the main thread using `withContext`/`flowOn` on the injected IO dispatcher in `OfflineFirstPostingRepository`.
- Replaced `derivedStateOf` wrappers in posting screens with direct state reads and added test tags for UI testing.
- Removed manual `koinViewModel` keys from navigation entries, relying on the navigation back-stack ViewModel store decorator instead.
- Pinned GitHub Actions to commit SHAs and added concurrency, timeouts, and least-privilege permissions across workflows.
- Upgraded Room and SQLite to 3.0.0-rc01 / 2.7.0-rc01.
- Upgraded Navigation 3 runtime to 1.1.3.
- Upgraded the Gradle wrapper to 9.6.0.

### Fixed
- Fixed the posting edit screen seeding its form with a never-completing flow collector; it now takes a one-shot snapshot via `first()` so retries no longer stack collectors.
- Fixed the release workflow changelog extraction to anchor section headers to the start of the line.

## [1.1.1] - 2026-06-17

### Added
- Enabled iOS Kotlin tests in CI.
- Added `derivedStateOf` optimizations to UI components.

### Changed
- Upgraded Kotlin to 2.4.0.
- Upgraded Compose Multiplatform to 1.11.1.
- Upgraded Room and SQLite to 3.0.0-alpha06 / 2.7.0-alpha06.
- Upgraded Koin to 4.2.2.
- Upgraded Lifecycle to 2.11.0-beta02.
- Upgraded Adaptive libraries to 1.3.0-beta02.

### Fixed
- Fixed `KoinModuleVerificationTest` incorrectly declaring `postingId` as `Long`.
- Corrected `CHANGELOG.md` placeholder links.
- Fixed `junit` dependency leak in `core:test` affecting iOS targets.

## [1.1.0] - 2026-06-02

### Added
- Integrated **Kermit** for Kotlin Multiplatform logging across all platforms (Android, iOS, Desktop).
- Added SLF4J and Logback support for Desktop logging.
- Integrated logging into the DI graph via Koin.
- Standardized release process with GitHub Actions.
- Centralized versioning in `gradle.properties`.

### Changed
- Updated GitHub Actions to latest versions (v6/v7/v8) for improved CI reliability.
- Refactored `AppModule` to include `LoggingModule`.

## [1.0.0] - 2026-06-01

### Added
- Initial release of KMP Ledger.
- Core ledger functionality with Room database.
- Android, iOS, and Desktop (JVM) support.
- Clean Architecture implementation.
- Modular feature structure.

[Unreleased]: https://github.com/aoreshkov/kmp-ledger/compare/v1.6.2...HEAD
[1.6.2]: https://github.com/aoreshkov/kmp-ledger/compare/v1.6.1...v1.6.2
[1.6.1]: https://github.com/aoreshkov/kmp-ledger/compare/v1.6.0...v1.6.1
[1.6.0]: https://github.com/aoreshkov/kmp-ledger/compare/v1.5.0...v1.6.0
[1.5.0]: https://github.com/aoreshkov/kmp-ledger/compare/v1.4.0...v1.5.0
[1.4.0]: https://github.com/aoreshkov/kmp-ledger/compare/v1.3.0...v1.4.0
[1.3.0]: https://github.com/aoreshkov/kmp-ledger/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/aoreshkov/kmp-ledger/compare/v1.1.1...v1.2.0
[1.1.1]: https://github.com/aoreshkov/kmp-ledger/compare/v1.1.0...v1.1.1
[1.1.0]: https://github.com/aoreshkov/kmp-ledger/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/aoreshkov/kmp-ledger/releases/tag/v1.0.0
