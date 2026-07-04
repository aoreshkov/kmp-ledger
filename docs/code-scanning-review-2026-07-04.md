# Code-Scanning Alert Review — 2026-07-04

Scope: every alert on <https://github.com/aoreshkov/kmp-ledger/security/code-scanning>
as of the review date. All six alerts were produced by the **OpenSSF Scorecard**
workflow (`.github/workflows/scorecard.yml`, added per
[`github-repo-review-2026-07-03.md`](github-repo-review-2026-07-03.md) Phase 5) —
none are CodeQL findings. Alert state was read live via the GitHub API; the
Scorecard exemption logic was verified against `ossf/scorecard`
`checks/raw/binary_artifact.go` on `main`.

Triage decision (owner, 2026-07-04): **no alerts are dismissed** — the
non-actionable ones stay open on the security tab with their rationale recorded
here instead.

---

## Alert summary

| # | Check | Severity | State | Disposition |
|---|---|---|---|---|
| 1 | Binary-Artifacts (`gradle/wrapper/gradle-wrapper.jar`) | high | open | **Fixed in code** — explicit wrapper-validation step (see S1) |
| 2 | Maintained (repo < 90 days old) | high | open | Leave open — auto-resolves ~2026-08-08 |
| 3 | Code-Review (0/29 approved changesets) | high | open | Leave open — structural, single-maintainer repo |
| 6 | Fuzzing (no fuzzer integration) | medium | open | Leave open — no meaningful fuzz surface |
| 5 | CII-Best-Practices (no OpenSSF badge) | low | open | Leave open — badge not pursued |
| 4 | SAST | medium | fixed 2026-07-04 | Nothing to do |

---

## Findings

### Actionable

**S1 — Binary-Artifacts flags `gradle-wrapper.jar` (alert #1, high, score 9/10)**
- Scorecard treats any committed binary as unreviewable code. It carves out one
  exemption for the Gradle wrapper jar: the file is reclassified `BinaryVerified`
  (score → 10, alert auto-closes) **only** when a workflow file contains an explicit
  step whose `uses:` starts with `gradle/wrapper-validation-action@` or
  `gradle/actions/wrapper-validation@`.
- Two subtleties made this a latent gap despite the control already existing:
  - `gradle/actions/setup-gradle` v6 (used via the `gradle-setup` composite action)
    **already validates the wrapper by default**, as recorded in
    [`currency-review-2026-07-02.md`](currency-review-2026-07-02.md) (CI section).
    Scorecard's static check simply doesn't recognize the embedded validation —
    the fix is recognition, not new protection.
  - Scorecard parses `.github/workflows/*.yml` only. Placing the step inside the
    composite action (`.github/actions/gradle-setup/action.yml`) would not count.
- **Fix (applied):** one step in `.github/workflows/build.yml` (`check` job, after
  Checkout, before Gradle setup):
  `gradle/actions/wrapper-validation@3f131e8…` — the same SHA/tag (v6) already
  pinned for `setup-gradle`, so Dependabot keeps bumping both together. Placed as
  a step rather than a new job to avoid an extra runner spin-up; a wrapper-jar
  change can never be docs-only, so the `changes` path filter always routes it
  through `check`.
- **Verify:** next push to `main` triggers the Scorecard workflow; alert #1 then
  reports `state: fixed`
  (`gh api repos/aoreshkov/kmp-ledger/code-scanning/alerts/1 -q .state`).
- Source: <https://github.com/ossf/scorecard/blob/main/checks/raw/binary_artifact.go>;
  <https://github.com/ossf/scorecard/blob/main/docs/checks.md#binary-artifacts>.

### Not actionable — left open deliberately

**S2 — Maintained (alert #2, high)**
- Score 0 purely because the repository was created 2026-05-10, inside Scorecard's
  90-day probation window. Commit cadence is otherwise healthy. The check
  re-evaluates weekly; with continued activity it self-resolves around
  **2026-08-08**. Revisit only if the alert is still open after mid-August.

**S3 — Code-Review (alert #3, high)**
- "0/29 approved changesets" — the check wants each recent changeset approved by a
  reviewer other than the author. A single-maintainer repo that commits directly
  to `main` (deliberate workflow, see branch-protection note in
  [`github-repo-review-2026-07-03.md`](github-repo-review-2026-07-03.md)) cannot
  satisfy this without a second human. Compensating controls: required `CI Success`
  check, BCV API gating, Scorecard + dependency review on every change.

**S4 — Fuzzing (alert #6, medium)**
- No fuzzer integration detected. The project is a Compose Multiplatform UI app
  over Room/DataStore with no parser, codec, or untrusted-input surface where
  coverage-guided fuzzing pays off; OSS-Fuzz does not target this shape of project.
  Revisit if a wire format or import/parse feature is ever added.

**S5 — CII-Best-Practices (alert #5, low)**
- Scorecard awards this score only for holding an OpenSSF Best Practices badge
  (<https://www.bestpractices.dev>). Registering is a manual questionnaire-driven
  process and is not being pursued for this repo at this stage. Most individual
  projects score 0 here; low severity, no action.

### Already resolved

**S6 — SAST (alert #4)**
- Reported "no SAST tool detected"; flipped to `fixed` on 2026-07-04 after CodeQL
  default setup was enabled per G3 of the 2026-07-03 repo review. No action.

---

## Outcome

One code change (S1) closes the only actionable alert. Alerts #2/#3/#5/#6 remain
open by decision — #2 expires on its own; #3/#5/#6 are structural trade-offs of a
solo pre-release project, documented above so future reviews don't re-litigate them.
