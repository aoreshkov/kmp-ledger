# `.claude/` — project Claude Code setup

This directory configures Claude Code for the kmp-ledger repo: review subagents,
skills (slash commands), a guard hook, and per-agent memory. Everything here is
checked into version control so the whole team shares it.

## Agents (`agents/`)
20 read-only review subagents in two families. Full roster, pairing, color scheme,
and ownership matrix: **[`agents/README.md`](agents/README.md)**.
- **`agents/review/`** — 11 `rv-*` house-rules reviewers (no web).
- **`agents/currency/`** — 9 `bp-*` upstream-currency auditors (web-enabled).

Subfolders are organizational only; an agent's identity is its `name` frontmatter, so
moving a file between folders does not change how it is invoked.

## Skills (`skills/`)
Skills must be direct children of `skills/` (the directory name is the `/command`);
they cannot be grouped into subfolders, so they are grouped by name instead.

**Review orchestration** (dispatch the specialists in parallel waves; shared rules in
[`REVIEW-CONVENTIONS.md`](REVIEW-CONVENTIONS.md)):
- `/review-house` — full house-rules review via the eleven `rv-*` agents.
- `/review-currency` — upstream-currency audit via the nine `bp-*` agents.
- `/review-all` — both lenses at once (all twenty agents), one deduplicated report;
  heavyweight, for release gates / periodic audits.

**Meta:**
- `/audit-claude-config` — audit this `.claude/` setup against the latest official
  Claude Code docs.

**Workflow** (bare, ergonomic names — frequently typed):
- `/commit` — conventional commit from the staged diff.
- `/release` — cut a release (bump version, changelog, commit).
- `/readme-plan` — plan README updates from source changes since the last tag.

## Shared conventions
- [`REVIEW-CONVENTIONS.md`](REVIEW-CONVENTIONS.md) — the wave strategy,
  findings-discipline rule, synthesize structure, and next-steps shared by the two
  review orchestrators (single source of truth, so the two skills don't drift).
- All review agents: `model: opus`, `memory: project`, `maxTurns: 40`, `effort: high`,
  read-only (no `Edit`/`Write`).

## Memory (`agent-memory/`)
One directory per agent, keyed by the agent's `name` (`agent-memory/<name>/`), holding
that agent's durable project notes. Renaming an agent requires renaming its memory dir.

## Reports
Dated review/audit outputs are written to the repo-root `docs/` directory (e.g.
`docs/full-review-<date>.md`). They are a historical record — older reports keep the
agent/skill names that were current when they were written.
