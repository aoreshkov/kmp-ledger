---
name: ios-findings-advisory
description: User has no iOS build/test environment — frame all iOS/Swift-export findings as advisory, never as actionable build steps
metadata:
  type: feedback
---

Frame every iOS / Swift-export finding as advisory only; never present it as an actionable step that requires building or testing on iOS.

**Why:** the user cannot build or test iOS in their environment (also recorded in the project's auto-memory `no-ios-test-environment`).
**How to apply:** for `iosExport`, `iosApp`, framework/swiftExport config, and `src/iosMain` findings, mark severity but add a "non-actionable for user (iOS)" note and avoid "run/verify on iOS" instructions. See [[kmp-structure-posture]].
