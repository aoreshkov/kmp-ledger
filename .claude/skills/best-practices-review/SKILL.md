---
name: best-practices-review
description: Orchestrate a currency audit of the whole project against the latest official upstream best practices, using the eight bp-* specialist subagents dispatched in parallel waves, then synthesize one prioritized, source-cited report. Makes no code changes (each subagent may persist notes to its own project memory). Do not invoke automatically.
disable-model-invocation: true
allowed-tools: Read, Grep, Glob, Bash, Agent
---

Run a **currency audit** of the codebase against the latest official upstream
best practices, using the eight `bp-*` specialist subagents in `.claude/agents/`,
then merge their findings into one prioritized report. This skill makes **no code
changes** — it reviews and reports only.

This is the upstream-currency counterpart to `full-review`. Where `full-review`
asks *"does the code obey this project's own rules?"*, this skill asks *"do those
rules and the code still match the latest official guidance for the stack?"* The
`bp-*` agents have web access; the `full-review` agents do not. Run this one
occasionally (e.g. on dependency bumps or periodically), not on every diff.

## The eight specialists
1. `bp-kotlin` — Kotlin language + kotlinx.coroutines/Flow idioms
2. `bp-kmp` — KMP source-set hierarchy, expect/actual, Swift export
3. `bp-compose` — Compose Multiplatform state/perf + Navigation 3
4. `bp-room` — Room 3 KMP DAOs, queries, migration posture
5. `bp-koin` — Koin 4.2 annotation DI, compile-time verify, scopes
6. `bp-gradle` — Gradle convention plugins, version catalog, config/build cache
7. `bp-ci` — GitHub Actions hardening / OpenSSF supply chain
8. `bp-android` — Android manifest, targetSdk, privacy, backup, predictive back

## Why waves
Dispatching all eight at once floods the synthesis step. As with `full-review`,
run them in **waves of three** (3 + 3 + 2), each wave in parallel, and synthesize
after all eight return — this keeps each merge tractable. The official docs
recommend spawning multiple subagents for independent work but set no fixed
concurrency limit, so this split is our own tuning, not a documented rule.

## Steps

### 1. Establish scope
If the user passed arguments (a module path, "since last release", or a single
domain like "just compose"), scope to that. Otherwise audit the whole repository.
Note the scope explicitly so each subagent receives it in its task prompt.

### 2. Dispatch wave 1 (parallel)
Spawn in a single message, each with the scope: `bp-kotlin`, `bp-kmp`,
`bp-compose` (language + UI surface).

### 3. Dispatch wave 2 (parallel)
After wave 1 returns: `bp-room`, `bp-koin`, `bp-gradle` (data + DI + build).

### 4. Dispatch wave 3 (parallel)
After wave 2 returns: `bp-ci`, `bp-android` (platform + supply chain).

For every spawn, end the task prompt with the findings-discipline rule:
> Report only **upstream-currency** gaps — where the code diverges from current
> official best practice for the pinned version. Give severity (Critical /
> Should-fix / Optional), `file:line`, a concrete fix, and **the source URL +
> its version/date**. Respect deliberate pinned project decisions; defer
> internal-correctness findings to the matching `full-review` agent. Do not
> invent findings if the area is already current.

### 5. Synthesize
Merge all eight reports into one document:

- **Critical** — divergence from current guidance with real risk (security,
  deprecation that will break on the next bump, data-loss posture).
- **Should-fix** — meaningful drift from current best practice worth addressing.
- **Optional** — newer-recommended improvements the user may skip.

For each item keep: the owning specialist, `file:line`, the gap, the fix, and the
**source citation**. De-duplicate overlapping findings (e.g. `bp-gradle` and
`bp-ci` both touching the wrapper/toolchain — merge). End with a **currency
summary**: a table of *pinned version vs. latest stable* per domain, plus the top
3 things to address first.

### 6. Offer next steps
Do not edit anything. Ask the user whether they want you to fix the Critical
items, run `full-review` for the project-rules lens, or stop here.
