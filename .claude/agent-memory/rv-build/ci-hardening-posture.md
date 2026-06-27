---
name: ci-hardening-posture
description: CI supply-chain/least-privilege posture already in place (SHA pins, permissions, concurrency, timeouts) so reviews don't re-flag what's intentional
metadata:
  type: project
---

As of 2026-06-24 the `.github/workflows` are fully hardened. Baseline to compare against in future reviews:

**Action pinning:** every `uses:` (including first-party `actions/*`) is pinned to a 40-hex commit SHA with a trailing `# vX` comment. Dependabot (`.github/dependabot.yml`, github-actions ecosystem, weekly, grouped) bumps both the SHA and the comment, so SHA-pinning stays maintainable.

**Permissions:** every workflow has top-level `permissions: contents: read`. Jobs widen at job level only: `build.yml` check job adds `checks: write` + `pull-requests: write`; `dependency-review.yml` adds `pull-requests: write`; `release.yml` create-release job adds `contents: write` + `id-token: write` + `attestations: write` (OIDC build provenance).

**Concurrency:** `build.yml` and `dependency-review.yml` (PR-triggered) set `cancel-in-progress: true`. `release.yml` (tag-triggered) sets `cancel-in-progress: false` — intentional, must not cancel a release mid-flight.

**Timeouts:** every job sets `timeout-minutes`.

**Untrusted input:** no `pull_request_target`; release workflow passes `GITHUB_REF_NAME` via shell env (GitHub-controlled, not user-controlled event text); checkouts in release use `persist-credentials: false`.

**Required check:** branch protection should require the `ci-success` aggregator job in `build.yml` (passes when build jobs succeed OR are skipped for docs-only changes via the `dorny/paths-filter` `changes` job).

Relates to [[kover-floor-topology]].
