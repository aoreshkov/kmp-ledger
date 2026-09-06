---
name: review-all
description: Run BOTH review lenses over the project — the eleven rv-* house-rules specialists and the nine bp-* currency specialists — in parallel waves, then synthesize ONE prioritized, cross-deduplicated review document under docs/ (findings + a phased fix plan). Heavyweight (twenty subagents); for release gates, not per-diff. Do not invoke automatically.
disable-model-invocation: true
argument-hint: "[optional scope, e.g. 'core:data' | 'since last release' | 'just compose']"
allowed-tools: Read, Grep, Glob, Bash(git ls-files*), Bash(git status*), Bash(git log*), Bash(git diff*), Agent, Write
---

## Today
!`date +%F`

**First, read `${CLAUDE_PROJECT_DIR}/.claude/REVIEW-CONVENTIONS.md`** — it carries the
shared scope, wave, synthesis, de-duplication and next-step rules this skill depends on.

Run a **combined** full-project review across both lenses at once: the eleven `rv-*`
house-rules specialists (`.claude/agents/review/`) **and** the nine `bp-*`
upstream-currency specialists (`.claude/agents/currency/`), then merge everything into
**one** prioritized, source-cited, cross-deduplicated review document. The skill's sole
output is that document — `docs/<today>-full-review.md` (use the date printed above),
holding the findings **and a phased plan to implement the fixes**; it makes **no other
code changes**.

## Scope
`$ARGUMENTS`

If that is non-empty, scope the sweep to it (a module path, "since last release", a
single domain like "just compose") and pass the scope verbatim into every spawn's
prompt. If it is empty, review the whole repository.

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
`file:line`, fix, **and source URL + version/date**; read the pins out of
`gradle/libs.versions.toml` rather than asserting a version; defer internal correctness
to the `rv-*` pair — the full contract is the `currency-findings-contract` skill
preloaded into every `bp-*` agent). Neither invents findings when an area is sound.

## Synthesize — one review document
Merge all twenty reports and **write `docs/<today>-full-review.md`** (the date printed
above) per the *Synthesize — write the review document* section of
`.claude/REVIEW-CONVENTIONS.md`. The doc has two parts: **(1) findings** in the three
tiers (Critical / Should-fix / Optional), and **(2) a phased implementation plan** for
the fixes (phases ordered by risk/value, each committable, with files + a `./gradlew`
verify step and any apiDump/Kover gates).

Apply the **Cross-skill de-duplication** rule in `.claude/REVIEW-CONVENTIONS.md`: a
paired domain that surfaces the same `file:line` from both its `rv-*` and `bp-*` agent
becomes **one** entry — `rv-*` owns the correctness verdict, `bp-*` carries the source
citation. Include the *pinned version vs. latest stable* currency table from the
currency lens. Writing that one doc is the only file change; then **offer next steps**
(including implementing the fixes by phase) as described in
`.claude/REVIEW-CONVENTIONS.md`.
