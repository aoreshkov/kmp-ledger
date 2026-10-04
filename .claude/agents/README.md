# Agent roster

This project's subagents are the nine **`bp-*` upstream-currency auditors** in
`agents/currency/`, orchestrated by `/review-currency`. Each asks one question of its
domain: *"Do the code and our rules still match the latest official upstream guidance?"*
They have `Read, Grep, Glob, Bash, WebSearch, WebFetch`. Claude Code discovers agents
recursively and identifies them only by their `name` frontmatter, so the subfolder is
purely organizational.

House-rules correctness ("does the code obey this project's own rules?") is **out of
lane** for this family. It is reviewed outside this repo's config, against the rules in
`CLAUDE.md`.

All agents share the same posture: **read-only review** (they propose fixes and make no
code edits), `model: opus`, `memory: project`, `maxTurns: 40`, `effort: high`,
`experimental.cacheTtl: 1h`.

## Two standing rules for this family

**1. Read-only is enforced, not asked for.** `memory: project` makes the harness grant
`Write`/`Edit` even though `tools:` omits them; otherwise the agent could not persist
memory. So every agent carries a `PreToolUse` hook with matcher `"Write|Edit"` that runs
`.claude/hooks/guard-agent-memory-writes.sh`. A write under `.claude/agent-memory/`
passes. Anything else is blocked (exit 2), with a reason telling the agent to report the
change as a finding instead. The hook lives in **agent frontmatter, not
`settings.json`**, so it is active only while a currency subagent runs and never
constrains the main session. Agent-level hooks require trusting the folder that contains
the agent file. Do **not** "simplify" this to `disallowedTools: Write, Edit`, which would
break `memory: project`.

**2. Never hardcode what you can read from the repo.** An agent prompt must not assert a
library version, a module name, a target list, a sanctioned exception or an exclusion
list. It names the *source*
(`gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`,
`build-logic/`, or the `CLAUDE.md` section that owns a rule) and instructs the agent to
read it. Copies of these facts rot silently:
an audit on 2026-09-06 found `bp-compose`, `bp-room` and `bp-testing` measuring the code
against version pins that were ten weeks out of date. `bp-gradle` and `bp-android`, the
two that derived their versions, were still correct. Project facts rot the same way: on
2026-10-04, `bp-koin` still listed a `compileSafety = false` workaround that had been
removed a release earlier.

The family shares one reporting contract. It lives in the `currency-findings-contract`
skill, which each agent preloads through its `skills:` frontmatter, rather than being
copy-pasted into nine bodies. Each body keeps only its own domain-specific *deliberate
choices* line.

## Domains

| Domain | Agent | Color |
|---|---|---|
| Kotlin + coroutines | `bp-kotlin` | purple |
| KMP structure / Swift export | `bp-kmp` | pink |
| Compose + Navigation 3 | `bp-compose` | green |
| Room / DataStore | `bp-room` | cyan |
| Koin / DI | `bp-koin` | orange |
| Gradle / build | `bp-gradle` | blue |
| CI / supply chain | `bp-ci` | red |
| Android platform | `bp-android` | yellow |
| Testing | `bp-testing` | yellow |

Where two domains touch the same files, the boundary is stated in each body's
*Ownership boundaries* section (for example, CI workflows belong to `bp-ci`, the build
system to `bp-gradle`).

## Memory
Each agent has `memory: project`, stored at `.claude/agent-memory/<name>/` (keyed by
`name`, not by folder). Renaming an agent means renaming its memory dir too.

## Adding or renaming an agent
1. Put the file in `agents/currency/` and set a unique `name` (that is its identity).
2. Add it to the table above and to `/review-currency`'s specialist list and waves.
3. Copy the shared frontmatter block (`model`, `memory`, `maxTurns`, `effort`,
   `experimental`, `hooks`, `skills: [currency-findings-contract]`) from a sibling.
4. Keep the `description` short. It loads at every session start, while the body loads
   only when the agent runs. Aim for the ~220–260 character band the existing agents sit
   in, and put sources, checklists and boundaries in the body.
