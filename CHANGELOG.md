# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Enabled iOS Kotlin tests in CI.
- Added `derivedStateOf` optimizations to UI components.

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

[Unreleased]: https://github.com/aoreshkov/kmp-ledger/compare/v1.1.0...HEAD
[1.1.0]: https://github.com/aoreshkov/kmp-ledger/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/aoreshkov/kmp-ledger/releases/tag/v1.0.0
