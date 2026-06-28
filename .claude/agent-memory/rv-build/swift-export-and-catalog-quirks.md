---
name: swift-export-and-catalog-quirks
description: Non-obvious build quirks — Swift Export opt-in handling and the material-icons version line that diverges from Compose MP on purpose
metadata:
  type: project
---

Two build details that look like bugs on first read but are intentional/benign. Re-verify before flagging in a review.

**Swift Export DSL opt-in is inconsistent but compiles.**
- `iosExport/build.gradle.kts` annotates the `swiftExport {}` block with `@OptIn(ExperimentalSwiftExportDsl::class)` (+ import).
- `core/bootstrap/build.gradle.kts` has a bare `swiftExport { moduleName = "Ledger" }` with NO `@OptIn` and no import.
- It still configures cleanly because `gradle.properties` sets `kotlin.swift-export.experimentalFeature.nowarn=true` and `kotlin.swift-export.experimental.nowarn=true`, and the DSL opt-in is not ERROR-level in KGP 2.4.0. So the omission is a cosmetic inconsistency, not a build break.
- **How to apply:** don't flag bootstrap's missing `@OptIn` as a correctness gap. At most an Optional consistency nit. If a future KGP bump flips the opt-in to ERROR, it becomes Should-fix — revisit then.

**material-icons-core rides its own version line.**
- `compose-material-icons = "1.7.3"` in `libs.versions.toml` is deliberately decoupled from `compose-multiplatform = "1.11.1"` (only `compose-material-icons-core` uses it). This is the JetBrains icons-package pin, not a stale lag.
- It is NOT documented with a comment in the catalog (unlike the material3 alignment, see [[material3-version-pin]] in user auto-memory). Adding a one-line comment is the only Optional improvement here.
- **How to apply:** treat the divergence as intentional; the only actionable note is "document it with a comment," and even that is Optional.

Relates to [[kover-floor-topology]], [[jvm-target-config]].
