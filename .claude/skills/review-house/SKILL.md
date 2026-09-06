---
name: review-house
description: Orchestrate a full house-rules review of the project from the eleven rv-* specialists, dispatched in three parallel waves, then synthesize one prioritized review document under docs/ (findings + a phased fix plan). Writes only that doc. Do not invoke automatically.
disable-model-invocation: true
argument-hint: "[optional scope, e.g. 'core:data' | 'since last release' | 'just compose']"
allowed-tools: Read, Grep, Glob, Bash(git ls-files*), Bash(git status*), Bash(git log*), Bash(git diff*), Agent, Write
---

## Today
!`date +%F`

**First, read `${CLAUDE_PROJECT_DIR}/.claude/REVIEW-CONVENTIONS.md`** — it carries the
shared scope, wave, synthesis and next-step rules this skill depends on.

Run a full-codebase **house-rules** review using the eleven `rv-*` specialist
subagents in `.claude/agents/review/`, then merge their findings into one
prioritized review document. The skill's sole output is that document —
`docs/<today>-house-review.md` (use the date printed above), holding the findings
**and a phased plan to implement the fixes**; it makes **no other code changes**.

This is the project-rules lens: *"does the code obey this project's own rules?"* Its
upstream-currency counterpart is `/review-currency`.

## Scope
`$ARGUMENTS`

If that is non-empty, scope the sweep to it (a module path, "since last release", a
single domain like "just compose") and pass the scope verbatim into every spawn's
prompt. If it is empty, review the whole repository.

## The eleven specialists
1. `rv-arch` — module layering, dependency direction, API/impl split
2. `rv-kmp` — source-set wiring, expect/actual placement, commonMain purity
3. `rv-concurrency` — coroutines, Flow, dispatchers, DataResult pipeline
4. `rv-compose` — state hoisting, recomposition, Navigation 3
5. `rv-data` — Room DAOs, mappers, repository, platform DB builders
6. `rv-di` — Koin annotation graph, scopes, DSL boundary
7. `rv-testing` — fakes-not-mocks, dispatcher setup, Kover policy
8. `rv-security` — secrets, input validation, platform data handling
9. `rv-perf` — allocations, query patterns, Flow efficiency, startup
10. `rv-build` — convention plugins, version catalog, Kover wiring, iOS export
11. `rv-ci` — CI gating, action pinning, permissions, timeouts

## Waves
Dispatch in three parallel waves (see `.claude/REVIEW-CONVENTIONS.md` for why), each
in a single message carrying the scope, and synthesize only after all eleven return:
- **Wave 1 — structural:** `rv-arch`, `rv-kmp`, `rv-di`, `rv-build`
- **Wave 2 — behavioral/runtime:** `rv-concurrency`, `rv-data`, `rv-compose`
- **Wave 3 — cross-cutting:** `rv-testing`, `rv-security`, `rv-perf`, `rv-ci`

## Findings-discipline rule (end every spawn's prompt with this)
> Report only gaps that affect correctness or the stated project rules. Give
> severity (Critical / Should-fix / Optional), `file:line`, and a concrete fix. Do
> not invent findings if the area is sound.

Then **synthesize** the eleven reports into `docs/<today>-house-review.md` exactly
as described in `.claude/REVIEW-CONVENTIONS.md` (*Synthesize — write the review
document*): a two-part doc with the tiered findings **and a phased implementation plan**
for the fixes (phases ordered by risk/value, each committable, with files + a `./gradlew`
verify step and any apiDump/Kover gates). Writing that one doc is the only file change;
then **offer next steps** — including implementing the fixes by phase, or running the
sibling `/review-currency` or the combined `/review-all`.
