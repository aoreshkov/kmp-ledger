---
name: review-currency
description: Orchestrate a currency audit of the whole project against the latest official upstream best practices, using the eight bp-* specialist subagents dispatched in parallel waves, then synthesize one prioritized, source-cited report. Makes no code changes (each subagent may persist notes to its own project memory). Do not invoke automatically.
disable-model-invocation: true
allowed-tools: Read, Grep, Glob, Bash, Agent
---

Run a **currency audit** of the codebase against the latest official upstream best
practices, using the eight `bp-*` specialist subagents in
`.claude/agents/currency/`, then merge their findings into one prioritized,
source-cited report. This skill makes **no code changes** — it reviews and reports
only.

This is the upstream-currency lens: *"do the code and our rules still match the
latest official guidance for the stack?"* Its project-rules counterpart is
`/review-house`. The `bp-*` agents have web access; the `rv-*` agents do not. Run
this occasionally (on dependency bumps or periodically), not on every diff. Follow
the shared orchestration rules in `.claude/REVIEW-CONVENTIONS.md`.

## The eight specialists
1. `bp-kotlin` — Kotlin language + kotlinx.coroutines/Flow idioms
2. `bp-kmp` — KMP source-set hierarchy, expect/actual, Swift export
3. `bp-compose` — Compose Multiplatform state/perf + Navigation 3
4. `bp-room` — Room 3 KMP DAOs, queries, migration posture
5. `bp-koin` — Koin 4.2 annotation DI, compile-time verify, scopes
6. `bp-gradle` — Gradle convention plugins, version catalog, config/build cache
7. `bp-ci` — GitHub Actions hardening / OpenSSF supply chain
8. `bp-android` — Android manifest, targetSdk, privacy, backup, predictive back

## Waves
Dispatch in waves of three (3 + 3 + 2), each in a single message carrying the scope,
and synthesize only after all eight return:
- **Wave 1 — language + UI:** `bp-kotlin`, `bp-kmp`, `bp-compose`
- **Wave 2 — data + DI + build:** `bp-room`, `bp-koin`, `bp-gradle`
- **Wave 3 — platform + supply chain:** `bp-ci`, `bp-android`

## Findings-discipline rule (end every spawn's prompt with this)
> Report only **upstream-currency** gaps — where the code diverges from current
> official best practice for the pinned version. Give severity (Critical /
> Should-fix / Optional), `file:line`, a concrete fix, and **the source URL + its
> version/date**. Respect deliberate pinned project decisions; defer
> internal-correctness findings to the matching `rv-*` agent. Do not invent
> findings if the area is already current.

Then **synthesize** the eight reports — adding a *pinned version vs. latest stable*
currency table per domain — and **offer next steps** as described in
`.claude/REVIEW-CONVENTIONS.md`.
