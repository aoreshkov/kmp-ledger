---
name: supply-chain-posture
description: kmp-ledger GitHub Actions supply-chain hardening posture — what already matches upstream best practice, structural gaps (Dependabot blind spot, Scorecard Signed-Releases), and open items (re-audited 2026-09-06)
metadata:
  type: project
---

Snapshot of the CI/supply-chain posture, re-audited **2026-09-06** against the GitHub
Actions secure-use reference, GitHub artifact-attestation / immutable-release docs,
gradle/actions v6 release notes, and OpenSSF Scorecard v5.5.0. Five workflows: build,
dependency-review, release, dependency-submission, scorecard — plus one **composite
action**, `.github/actions/gradle-setup/action.yml`.

**Already current (do not re-flag as gaps):**
- All `uses:` pinned to full 40-hex SHA with version comments; every pin re-resolved to
  a real tag on 2026-09-06. Every workflow-file pin was exactly latest at audit time.
- Top-level `permissions: contents: read` (scorecard uses `read-all`); jobs widen
  minimally. `persist-credentials: false` on every checkout. Every job has
  `timeout-minutes`. No `pull_request_target`; no `github.event.*` in `run:`.
- Concurrency deliberate: PR-triggered cancel-in-progress true, release false.
- Release uses `actions/attest` directly (provenance from `subject-checksums`), with
  id-token + attestations write scoped to create-release only; signing secrets scoped to
  the protected `environment: release`.
- scorecard.yml matches the official OpenSSF template.
- All pinned third-party actions run on **node24** — no runtime-deprecation exposure.
- Repo settings verified via API: **immutable releases ON** (every release since v1.5.0
  reports `immutable: true`; the softprops/action-gh-release create-with-assets flow is
  compatible), secret scanning + push protection + Dependabot security updates enabled.
  The old "enable immutable releases" Optional item is **resolved — do not re-report**.

**Structural gaps that keep recurring (check these first on re-audit):**
1. **Dependabot has a blind spot on the composite action.** `dependabot.yml` uses
   `directory: "/"` for github-actions, which per GitHub docs only covers
   `.github/workflows` + a *root* `action.yml`. `.github/actions/gradle-setup/action.yml`
   has therefore **never** received a Dependabot bump (confirmed via `git log` on the
   file), so its pins silently rot while every workflow pin sits at latest. Fix is
   `directories: ["/", "/.github/actions/gradle-setup"]`. If a pin looks stale, check
   whether it lives in the composite action before blaming the cooldown window.
2. **Scorecard Signed-Releases scores 0** despite provenance being published. Scorecard
   only inspects *release assets* for `*.intoto.jsonl` / `*.sigstore.json` / `*.sig` etc.;
   attestations stored in the GitHub attestations API (and the auto release attestation
   from immutable releases) are invisible to it. Fix: capture
   `steps.<attest>.outputs.bundle-path` and attach it as a release asset.

**Version currency verified 2026-09-06 (pin = latest unless noted):**
checkout v7.0.1, upload-artifact v7.0.1, download-artifact v8.0.1,
dependency-review v5.0.0, actions/attest v4.2.2, softprops/action-gh-release v3.0.3,
ossf/scorecard-action v2.4.4 (Scorecard core v5.5.0), codeql-action v4.37.9,
mikepenz/action-junit-report v6.5.0, madrapps/jacoco-report v1.8.0,
dorny/paths-filter v4.0.3, gradle/actions v6.3.0 in build.yml + dependency-submission.yml.
Stale, both in the composite action: **setup-java v5.3.0** (latest v6.0.0, 2026-08-24)
and **gradle/actions/setup-gradle v6.2.0** (latest v6.3.0, 2026-08-02).

**gradle/actions v6.3.0 is the one that matters:** it fixes the Windows
"Path Validation Error" cache-save defect that build.yml and release.yml work around with
`cache-disabled: ${{ matrix.os == 'windows-latest' }}`. Once the composite is on v6.3.0
those workarounds (and their long comments) are obsolete.

**Open findings (2026-09-06) — Optional, carried:**
- No SBOM attestation; `actions/attest` v4.2.2 takes `sbom-path` directly.
- README advertises provenance but gives consumers no `gh attestation verify` recipe —
  GitHub's own guidance is that attestations only pay off if verified.
- CodeQL default setup is configured for **`actions` language only**, not `java-kotlin`,
  so app code gets no SAST (Scorecard still scores SAST 10 off the Scorecard SARIF).
  Repo config, and autobuild on a KMP Gradle build is not free.

**Why:** This agent (bp-ci currency lens) verifies CI rules still match *current* upstream
guidance and surfaces newly-recommended controls. rv-ci covers the same files from a
project-rules angle.

**How to apply:** On re-audit, re-resolve every pinned SHA to a tag (`gh api
repos/<r>/git/refs/tags`), diff against `releases/latest`, and check the composite action
separately from the workflows. Trust current file/API state over this snapshot.
