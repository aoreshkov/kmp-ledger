---
name: supply-chain-posture
description: kmp-ledger GitHub Actions supply-chain hardening posture, what already matches upstream best practice, and open items (as of 2026-07-16 re-audit)
metadata:
  type: project
---

Snapshot of `.github/workflows/` CI/supply-chain posture, re-audited 2026-07-16 against the GitHub Actions secure-use reference, gradle/actions v6 docs, and OpenSSF Scorecard. **Five** workflows now: build, dependency-review, release, dependency-submission, and **scorecard** (added since the 2026-07-02 audit).

**Already current (do not re-flag as gaps):**
- All actions pinned to full 40-hex SHA with version comments; every pin re-verified 2026-07-16 to resolve to a real tag. Dependabot (github-actions, weekly, grouped) keeps them bumped — pins lagging latest by a patch/minor between weekly runs is normal, not a finding.
- Top-level `permissions: contents: read` (scorecard uses `read-all`) in all workflows; jobs widen minimally. dependency-submission's job-level `contents: write` on pull_request is the upstream-documented pattern. Satisfies Token-Permissions.
- `persist-credentials: false` set on **every** checkout.
- Concurrency deliberate and correct: PR-triggered cancel-in-progress true; release false. Every job has `timeout-minutes`. No `pull_request_target`; no `github.event.*` in `run:` (ci-success interpolates only `needs.*.result` enum values).
- Release uses `actions/attest` v4.1.1 **directly** (Provenance mode, auto SLSA build provenance). id-token + attestations write scoped to create-release job only.
- **Signing secrets now scoped to a protected `environment: release`** in release.yml build-android (resolves the prior Optional item about moving ANDROID_KEYSTORE_* into a deployment environment).
- **scorecard.yml matches the official OpenSSF template**: SHA-pinned scorecard-action + codeql-action/upload-sarif, `read-all` top-level, job-level `security-events: write` + `id-token: write`, `publish_results: true`, triggers branch_protection_rule/schedule/push-main/workflow_dispatch.
- Gradle wrapper validation covered (setup-gradle default true) plus an explicit gradle/actions/wrapper-validation step in build.yml (needed for Scorecard Binary-Artifacts gradle-wrapper exemption).

**Version currency verified 2026-07-16 (pin = latest unless noted):**
checkout v7.0.0, setup-java **v5.3.0** (latest v5.5.0 — minor lag, Dependabot), gradle/actions v6.2.0, upload-artifact v7.0.1, download-artifact v8.0.1, dependency-review v5.0.0, actions/attest v4.1.1, gh-release **v3.0.1** (latest v3.0.2 — patch lag), scorecard-action v2.4.3 (latest; the v5.x is the *CLI* ossf/scorecard, NOT the action), codeql-action v4.36.3, junit-report v6.4.2, jacoco-report v1.8.0, paths-filter v4.0.2.
- Note: actions/checkout backported "safer pull_request_target defaults" to all majors on 2026-07-16 — **no impact** here (no pull_request_target, already on v7).

**Resolved since 2026-07-02:**
- Should-fix `retry-on-snapshot-warnings` in dependency-review.yml — **adopted** (true + 600s timeout, lines 34-35).
- Optional ANDROID_KEYSTORE_* into protected environment — **adopted** (`environment: release`).

**Open findings (2026-07-16) — all Optional, carried:**
- Enable repo-level **immutable releases** (GA 2025-10-28) — repo config, not workflow; complements attest with tag/asset immutability + auto release attestations.
- No SBOM attestation; actions/attest v4 supports `sbom-path` directly.
- (Very optional / third-party, not GitHub/OpenSSF-mandated) step-security/harden-runner egress filtering.

**Why:** This agent (bp-ci currency lens) verifies CI rules still match *current* upstream guidance and surfaces newly-recommended controls. rv-build covers the same files from a project-rules angle.

**How to apply:** On re-audit, re-verify major versions, check whether Optional items were adopted, and trust current file state over this snapshot.
