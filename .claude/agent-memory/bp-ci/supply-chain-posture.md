---
name: supply-chain-posture
description: kmp-ledger GitHub Actions supply-chain hardening posture and what already matches upstream best practice as of 2026-06
metadata:
  type: project
---

Snapshot of `.github/workflows/` CI/supply-chain posture as audited 2026-06-26 against current GitHub Actions secure-use reference + OpenSSF Scorecard.

**Already current (do not re-flag as gaps):**
- All third-party + first-party actions pinned to full 40-hex commit SHA with `# vX` comments; Dependabot (`.github/dependabot.yml`, github-actions ecosystem, grouped weekly) bumps both SHA and comment, so pins stay maintainable. Satisfies Scorecard Pinned-Dependencies.
- Top-level `permissions: contents: read` in all three workflows; jobs widen only what they need (check job: checks/pull-requests write; dependency-review: pull-requests write; create-release: contents/id-token/attestations write). Satisfies Token-Permissions.
- Concurrency posture is deliberate and correct: PR-triggered (build, dependency-review) cancel-in-progress true; release cancel-in-progress **false**. Do not propose flipping the release one.
- Every job sets `timeout-minutes`.
- No `pull_request_target`; no `github.event.*` interpolated into `run:` (release uses built-in `GITHUB_REF_NAME` env, not untrusted PR input). No script-injection / Dangerous-Workflow exposure.
- Release already adopts artifact attestation (`actions/attest-build-provenance` via goreleaser-style `subject-checksums: SHA256SUMS`) — the newly-recommended provenance control is in place.

**Open optionals (defense-in-depth, not blocking):**
- `persist-credentials: false` is set on every checkout in release.yml but NOT in build.yml or dependency-review.yml. Recommended by actions/checkout README. Low risk (token is contents:read there).
- No SBOM attestation (`actions/attest-sbom`) to complement build-provenance for fuller SLSA coverage.

**Why:** This agent (bp-ci currency lens) verifies the project's CI rules still match *current* upstream guidance and surfaces newly-recommended controls. Cross-references the `build-ci` agent which covers the same files from a project-rules angle.

**How to apply:** When re-auditing, re-verify action major versions are still current (checkout v7, setup-java v5, upload-artifact v7, download-artifact v8, dependency-review v5, attest-build-provenance v4 as of this snapshot) and that no new mandatory control has shipped. Trust current file state over this snapshot if they diverge.
