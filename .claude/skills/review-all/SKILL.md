---
name: review-all
description: Run BOTH review lenses over the whole project — the eleven rv-* house-rules specialists and the nine bp-* currency specialists — in parallel waves, then synthesize ONE prioritized, cross-deduplicated report. Heavyweight (twenty subagents); for release gates / periodic full audits, not per-diff. Makes no code changes. Do not invoke automatically.
disable-model-invocation: true
allowed-tools: Read, Grep, Glob, Bash, Agent
---

Run a **combined** full-project review across both lenses at once: the eleven `rv-*`
house-rules specialists (`.claude/agents/review/`) **and** the nine `bp-*`
upstream-currency specialists (`.claude/agents/currency/`), then merge everything into
**one** prioritized, source-cited, cross-deduplicated report. This skill makes **no
code changes** — it reviews and reports only.

This is the union of `/review-house` (*"does the code obey this project's own
rules?"*) and `/review-currency` (*"do the code and our rules still match the latest
official upstream guidance?"*). Use it for a release gate or a periodic full audit —
**not** on every diff. It dispatches **twenty subagents**; reach for the single-lens
siblings or the bundled `/code-review` when you don't need the full sweep. Follow the
shared orchestration rules in `.claude/REVIEW-CONVENTIONS.md`.

## What it runs
- **House (`rv-*`, no web):** `rv-arch`, `rv-kmp`, `rv-concurrency`, `rv-compose`,
  `rv-data`, `rv-di`, `rv-testing`, `rv-security`, `rv-perf`, `rv-build`, `rv-ci`.
- **Currency (`bp-*`, web):** `bp-kotlin`, `bp-kmp`, `bp-compose`, `bp-room`,
  `bp-koin`, `bp-gradle`, `bp-ci`, `bp-android`, `bp-testing`.

See `.claude/agents/README.md` for the per-domain pairing matrix.

## Waves
Run the **house waves first, then the currency waves** (keep the lenses separate so
each synthesis stays tractable), each wave in a single message carrying the scope, and
synthesize only after all twenty return:
- **House** — Wave H1 structural: `rv-arch`, `rv-kmp`, `rv-di`, `rv-build`; Wave H2
  behavioral: `rv-concurrency`, `rv-data`, `rv-compose`; Wave H3 cross-cutting:
  `rv-testing`, `rv-security`, `rv-perf`, `rv-ci`.
- **Currency** — Wave C1 language + UI: `bp-kotlin`, `bp-kmp`, `bp-compose`; Wave C2
  data + DI + build: `bp-room`, `bp-koin`, `bp-gradle`; Wave C3 platform + supply +
  test: `bp-ci`, `bp-android`, `bp-testing`.

## Findings-discipline rule (end every spawn's prompt with this)
Use each agent's family rule verbatim: the **`rv-*`** spawns get the house rule (report
only correctness / project-rule gaps; severity, `file:line`, concrete fix), the
**`bp-*`** spawns get the currency rule (report only upstream-currency gaps; severity,
`file:line`, fix, **and source URL + version/date**; defer internal correctness to the
`rv-*` pair). Neither invents findings when an area is sound.

## Synthesize — one report
Merge all twenty reports into a single document with the three tiers (Critical /
Should-fix / Optional). Apply the **Cross-skill de-duplication** rule in
`.claude/REVIEW-CONVENTIONS.md`: a paired domain that surfaces the same `file:line`
from both its `rv-*` and `bp-*` agent becomes **one** entry — `rv-*` owns the
correctness verdict, `bp-*` carries the source citation. Append the *pinned version
vs. latest stable* currency table from the currency lens. Then **offer next steps** as
described in `.claude/REVIEW-CONVENTIONS.md` — do not edit anything.
