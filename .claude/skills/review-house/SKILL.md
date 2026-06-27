---
name: review-house
description: Orchestrate a full house-rules review of the entire project from the nine rv-* review specialists, dispatched in three parallel waves, then synthesize one prioritized report. Makes no code changes (each subagent may persist notes to its own project memory). Do not invoke automatically.
disable-model-invocation: true
allowed-tools: Read, Grep, Glob, Bash, Agent
---

Run a full-codebase **house-rules** review using the nine `rv-*` specialist
subagents in `.claude/agents/review/`, then merge their findings into one
prioritized report. This skill makes **no code changes** — it reviews and reports
only.

This is the project-rules lens: *"does the code obey this project's own rules?"* Its
upstream-currency counterpart is `/review-currency`. Follow the shared orchestration
rules in `.claude/REVIEW-CONVENTIONS.md` (waves, scope, synthesize, next steps).

## The nine specialists
1. `rv-arch` — module layering, dependency direction, API/impl split
2. `rv-concurrency` — coroutines, Flow, dispatchers, DataResult pipeline
3. `rv-compose` — state hoisting, recomposition, Navigation 3
4. `rv-data` — Room DAOs, mappers, repository, platform DB builders
5. `rv-di` — Koin annotation graph, scopes, DSL boundary
6. `rv-testing` — fakes-not-mocks, dispatcher setup, Kover policy
7. `rv-security` — secrets, input validation, platform data handling
8. `rv-perf` — allocations, query patterns, Flow efficiency, startup
9. `rv-build` — convention plugins, version catalog, Kover wiring, CI

## Waves
Dispatch in three parallel waves (see `.claude/REVIEW-CONVENTIONS.md` for why), each
in a single message carrying the scope, and synthesize only after all nine return:
- **Wave 1 — structural:** `rv-arch`, `rv-di`, `rv-build`
- **Wave 2 — behavioral/runtime:** `rv-concurrency`, `rv-data`, `rv-compose`
- **Wave 3 — cross-cutting:** `rv-testing`, `rv-security`, `rv-perf`

## Findings-discipline rule (end every spawn's prompt with this)
> Report only gaps that affect correctness or the stated project rules. Give
> severity (Critical / Should-fix / Optional), `file:line`, and a concrete fix. Do
> not invent findings if the area is sound.

Then **synthesize** the nine reports and **offer next steps** exactly as described in
`.claude/REVIEW-CONVENTIONS.md`.
