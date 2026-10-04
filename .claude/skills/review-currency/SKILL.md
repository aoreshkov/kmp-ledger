---
name: review-currency
description: Orchestrate a currency audit of the project against the latest official upstream best practices, using the nine bp-* specialists in parallel waves, then synthesize one prioritized, source-cited review document under docs/ (findings + a phased fix plan). Writes only that doc. Do not invoke automatically.
disable-model-invocation: true
argument-hint: "[optional scope, e.g. 'core:data' | 'since last release' | 'just compose']"
allowed-tools: Read, Grep, Glob, Bash(git ls-files*), Bash(git status*), Bash(git log*), Bash(git diff*), Agent, Write
---

## Today
!`date +%F`

**First, read [conventions.md](${CLAUDE_SKILL_DIR}/conventions.md).** It holds the
scope, wave, synthesis and next-step rules this skill depends on.

Run a **currency audit** of the codebase against the latest official upstream best
practices, using the nine `bp-*` specialist subagents in `.claude/agents/currency/`, then
merge their findings into one prioritized, source-cited review document. That document
is the skill's only output: `docs/<today>-currency-review.md` (use the date printed
above), holding the findings **and a phased plan to implement the fixes**. The skill
makes **no other code changes**.

This is the upstream-currency lens: *"do the code and our rules still match the latest
official guidance for the stack?"* The `bp-*` agents have web access. House-rules
correctness is out of their lane. Run this occasionally (on dependency bumps or
periodically), not on every diff.

## Scope
`$ARGUMENTS`

If that is non-empty, scope the sweep to it (a module path, "since last release", a
single domain like "just compose") and pass the scope verbatim into every spawn's
prompt. If it is empty, review the whole repository.

## The nine specialists
1. `bp-kotlin`: Kotlin language + kotlinx.coroutines/Flow idioms
2. `bp-kmp`: KMP source-set hierarchy, expect/actual, Swift export
3. `bp-compose`: Compose Multiplatform state/perf + Navigation 3
4. `bp-room`: Room 3 KMP DAOs, queries, migration posture, DataStore
5. `bp-koin`: Koin annotation DI, compile-time verify, scopes
6. `bp-gradle`: Gradle convention plugins, version catalog, config/build cache
7. `bp-ci`: GitHub Actions hardening / OpenSSF supply chain
8. `bp-android`: Android manifest, targetSdk, privacy, backup, predictive back
9. `bp-testing`: coroutines-test, Compose UI test, kotlin-test currency

## Waves
Dispatch in waves of three (3 + 3 + 3), each in a single message carrying the scope,
and synthesize only after all nine return:
- **Wave 1, language + UI:** `bp-kotlin`, `bp-kmp`, `bp-compose`
- **Wave 2, data + DI + build:** `bp-room`, `bp-koin`, `bp-gradle`
- **Wave 3, platform + supply chain + test:** `bp-ci`, `bp-android`, `bp-testing`

## Findings-discipline rule (end every spawn's prompt with this)
> Report only **upstream-currency** gaps, where the code diverges from current
> official best practice for the pinned version. Read the pins out of
> `gradle/libs.versions.toml`; never assert a version from memory. Give severity
> (Critical / Should-fix / Optional), `file:line`, a concrete fix, and **the source
> URL + its version/date**. Respect deliberate pinned project decisions; leave
> internal-correctness findings out. Do not invent findings if the area is already
> current. Full contract: the `currency-findings-contract` skill preloaded into your
> context.

Then **synthesize** the nine reports into `docs/<today>-currency-review.md` exactly as
described in conventions.md (*Synthesize — write the review document*). Writing that one
doc is the only file change. Then **offer next steps** as conventions.md describes.
