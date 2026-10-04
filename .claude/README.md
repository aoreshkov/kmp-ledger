# `.claude/`: project Claude Code setup

This directory configures Claude Code for the kmp-ledger repo: currency-review
subagents, skills (slash commands), a guard hook, and per-agent memory. Everything here
is checked into version control so the whole team shares it.

## Agents (`agents/`)
Nine read-only `bp-*` upstream-currency auditors (web-enabled) in `agents/currency/`.
Roster, domains and standing rules: **[`agents/README.md`](agents/README.md)**.

Subfolders are organizational only. An agent's identity is its `name` frontmatter, so
moving a file between folders does not change how it is invoked.

## Skills (`skills/`)
Skills must be direct children of `skills/` (the directory name is the `/command`).
They cannot be grouped into subfolders, so they are grouped by name instead.

**Review:**
- `/review-currency`: upstream-currency audit via the nine `bp-*` agents, in parallel
  waves. Its orchestration rules are in the supporting file
  [`skills/review-currency/conventions.md`](skills/review-currency/conventions.md).
- `currency-findings-contract`: not a command. It is the reporting contract preloaded
  into every `bp-*` agent.

**Meta:**
- `/audit-claude-config`: audit this `.claude/` setup against the latest official
  Claude Code docs.
- `/check-agp-ide-currency`: the latest Kotlin + AGP that both Android Studio and
  IntelliJ IDEA accept, compared with the pins.

**Workflow** (bare, ergonomic names, typed often):
- `/release`: cut a release (bump version, changelog, commit).
- `/readme-plan`: plan README updates from source changes since the last tag.

## Rules (`rules/`)
Path-scoped instructions that load only when Claude works on matching files (`paths:`
frontmatter), which keeps area detail out of the always-loaded `CLAUDE.md`:
`build-pins.md`, `kover-coverage.md`, `compose-stability.md`, `compose-resources.md`,
`room-migrations.md`.

## Memory (`agent-memory/`)
One directory per agent, keyed by the agent's `name` (`agent-memory/<name>/`), holding
that agent's durable project notes. Renaming an agent requires renaming its memory dir.

## Reports
Dated review/audit outputs are written to the repo-root `docs/` directory (e.g.
`docs/<date>-currency-review.md`). They are a historical record: older reports keep the
agent/skill names that were current when they were written.
