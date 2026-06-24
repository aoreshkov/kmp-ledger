---
name: full-review
description: Orchestrate a full exhaustive review of the entire project from 9 senior specialist subagents, dispatched in three parallel waves, then synthesize one prioritized report. Makes no code changes (each subagent may persist notes to its own project memory). Do not invoke automatically.
disable-model-invocation: true
allowed-tools: Read, Grep, Glob, Bash, Agent
---

Run a full-codebase review using the nine specialist subagents in
`.claude/agents/`, then merge their findings into one prioritized report.
This skill makes **no code changes** — it reviews and reports only.

## The nine specialists
1. `arch-reviewer` — module layering, dependency direction, API/impl split
2. `kotlin-concurrency` — coroutines, Flow, dispatchers, DataResult pipeline
3. `compose-ui` — state hoisting, recomposition, Navigation 3
4. `data-persistence` — Room DAOs, mappers, repository, platform DB builders
5. `di-koin` — Koin annotation graph, scopes, DSL boundary
6. `testing-quality` — fakes-not-mocks, dispatcher setup, Kover policy
7. `security-secrets` — secrets, input validation, platform data handling
8. `performance` — allocations, query patterns, Flow efficiency, startup
9. `build-ci` — convention plugins, version catalog, Kover wiring, CI

## Why waves
Dispatching all nine at once floods the synthesis step with summaries to merge.
As a project convention we run **three waves of three**, each wave in parallel,
and synthesize after all nine return — this keeps each merge tractable. The
official docs recommend spawning multiple subagents for independent work but set
no fixed concurrency limit, so this 3×3 split is our own tuning, not a documented
rule.

## Steps

### 1. Establish scope
If the user passed arguments (e.g. a module path or "since last release"), scope
the review to that. Otherwise review the entire repository. Note the scope
explicitly so each subagent gets it in its task prompt.

### 2. Dispatch wave 1 (parallel)
Spawn these three subagents in a single message, each with the scope:
`arch-reviewer`, `di-koin`, `build-ci` (structural lenses).

### 3. Dispatch wave 2 (parallel)
After wave 1 returns: `kotlin-concurrency`, `data-persistence`, `compose-ui`
(behavioral/runtime lenses).

### 4. Dispatch wave 3 (parallel)
After wave 2 returns: `testing-quality`, `security-secrets`, `performance`
(cross-cutting lenses).

For every spawn, the task prompt must end with the findings-discipline rule:
> Report only gaps that affect correctness or the stated project rules. Give
> severity (Critical / Should-fix / Optional), `file:line`, and a concrete
> fix. Do not invent findings if the area is sound.

### 5. Synthesize
Merge all nine reports into one document:

- **Critical** — must fix (correctness bugs, rule violations, security risks)
- **Should-fix** — real gaps worth addressing soon
- **Optional** — improvements the user may skip

For each item keep: the owning specialist, `file:line`, the problem, the fix.
De-duplicate overlapping findings (e.g. compose-ui and performance both flagging
list recomposition — merge into one). End with a one-paragraph health summary:
what is solid, and the top 3 things to address first.

### 6. Offer next steps
Do not edit anything. Ask the user whether they want you to fix the Critical
items, run the bundled `/code-review` on a specific diff, or stop here.
