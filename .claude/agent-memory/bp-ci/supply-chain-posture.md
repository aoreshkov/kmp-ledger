---
name: supply-chain-posture
description: kmp-ledger GitHub Actions supply-chain hardening posture, what already matches upstream best practice, and open items (as of 2026-07-02 audit)
metadata:
  type: project
---

Snapshot of `.github/workflows/` CI/supply-chain posture as re-audited 2026-07-02 against the GitHub Actions secure-use reference, gradle/actions v6 docs, and OpenSSF Scorecard. Four workflows now: build, dependency-review, release, **dependency-submission** (added ~2026-06, commit 12f37b3).

**Already current (do not re-flag as gaps):**
- All actions pinned to full 40-hex SHA with version comments; every pin verified to resolve to a real tag. Dependabot (github-actions, weekly, grouped) keeps them bumped — pins lagging latest by one patch between weekly runs is normal, not a finding.
- Top-level `permissions: contents: read` in all four workflows; jobs widen minimally. dependency-submission's job-level `contents: write` on pull_request is the upstream-documented pattern (fork PRs get read-only token automatically). Satisfies Token-Permissions.
- `persist-credentials: false` is now set on **every** checkout in all four workflows (earlier snapshot said build/dependency-review lacked it — fixed since).
- Concurrency deliberate and correct: PR-triggered cancel-in-progress true; release false. Every job has `timeout-minutes`. No `pull_request_target`; no `github.event.*` in `run:` (ci-success interpolates only `needs.*.result` — trusted enum values).
- Release uses `actions/attest` v4 **directly** (deliberate, commit b65365b, replacing attest-build-provenance wrapper). Valid: actions/attest v4 auto-generates SLSA build provenance when no sbom/predicate inputs are given ("Provenance" mode per its README). id-token + attestations write scoped to create-release job only.
- Gradle wrapper validation: covered automatically — `validate-wrappers` defaults to `true` in gradle/actions/setup-gradle v6.
- Android signing secrets passed via `env:` to run steps, never inline — matches script-injection guidance.

**Open findings (reported 2026-07-02):**
- Should-fix: dependency-review.yml lacks `retry-on-snapshot-warnings: true` + `retry-on-snapshot-warnings-timeout`. Graph is submitted by a *separate* PR-triggered workflow (Gradle build, minutes) while dependency-review finishes in ~1 min → head snapshot often missing at review time. gradle/actions docs/dependency-submission.md "Integrating the dependency-review-action" prescribes it.
- Optional: enable repo-level **immutable releases** (GA 2025-10-28) — complements the attest step with tag/asset immutability + automatic release attestations.
- Optional: move ANDROID_KEYSTORE_* secrets into a protected deployment environment (secure-use reference: reviewer approval for environment secrets).
- Optional: fork PRs can't submit dependency graphs (read-only token); upstream pattern is generate-and-upload + workflow_run download-and-submit. Only matters if external contributors expected.
- Optional (carried over): no SBOM attestation; actions/attest v4 now supports `sbom-path` directly.

**Why:** This agent (bp-ci currency lens) verifies CI rules still match *current* upstream guidance and surfaces newly-recommended controls. rv-build covers the same files from a project-rules angle.

**How to apply:** On re-audit, re-verify major versions (checkout v7, setup-java v5, upload-artifact v7, download-artifact v8, dependency-review v5, actions/attest v4, gradle/actions v6, gh-release v3 as of this snapshot), check whether the open findings were adopted, and trust current file state over this snapshot.
