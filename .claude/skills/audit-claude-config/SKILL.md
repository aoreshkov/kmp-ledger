---
name: audit-claude-config
description: Audit this project's `.claude/` subagents, skills, and hooks against the LATEST official Claude Code docs, then present a dated best-practices report inline. Researches current guidance live (does not rely on memory). Read-then-offer — proposes fixes but does not apply them without confirmation. Do not invoke automatically.
disable-model-invocation: true
argument-hint: "[optional: extra focus, e.g. 'skills only' or 'check new fields']"
allowed-tools: Read, Grep, Glob, Bash(git ls-files*), Bash(git status*), Agent, Write, Edit
---

## Today
!`date +%F`

Audit the project's Claude Code customizations under `.claude/` — subagents,
skills, and hooks — against the **current** official documentation and present a
dated best-practices report **inline** (in the conversation). If the user still
has a prior audit report, use it as the reference format and a baseline to diff
against; otherwise treat this run as a fresh baseline.

This skill **makes no config changes on its own** — it researches, reports, and
then offers to apply fixes. It does not persist the report to a file unless the
user explicitly asks where to save it.

## Steps

### 1. Inventory the local config

Enumerate and read what exists today:
- `git ls-files '.claude/agents/**/*.md' '.claude/skills/*/SKILL.md'` (agents live in
  family subfolders `agents/review/` and `agents/currency/`, so recurse; plus untracked:
  `Glob .claude/agents/**` and `.claude/skills/**`).
- Read every agent and skill file. Record each one's frontmatter fields and values
  (`name`, `description`, `tools`, `model`, `memory`, `color`, `maxTurns`, `effort`,
  `allowed-tools`, `disable-model-invocation`, `context`, `argument-hint`,
  `arguments`, etc.) and note the agent-memory dirs under `.claude/agent-memory/`.
- Read the `hooks` blocks in `.claude/settings.json` and `.claude/settings.local.json`
  and every script under `.claude/hooks/`. Record each hook's event, `matcher`,
  handler `type`, and the command/script it runs, plus how each script blocks
  (exit-code contract).

### 2. Research the latest official guidance (live — do not trust memory)

Spawn the **`claude-code-guide`** subagent (it has WebSearch/WebFetch). Its task:

> Research the LATEST official Claude Code docs (docs.claude.com / code.claude.com:
> the sub-agents, skills, hooks, settings, best-practices, and agent-teams pages)
> plus the changelog. Use WebSearch/WebFetch — do not answer from memory. Cite the
> doc URLs. Report, as of today's date:
> 1. The complete set of currently-supported frontmatter fields for **subagents**
>    (`.claude/agents/*.md`) and what each does. Flag any field that is NOT
>    officially supported (would be ignored by the harness).
> 2. The complete set of currently-supported frontmatter fields for **skills**
>    (`SKILL.md`), same treatment. Confirm the in-SKILL dynamic-injection syntax
>    (an exclamation mark followed by a backtick-quoted shell command) and any
>    supporting-file reference convention are still current.
> 3. For **hooks**: the current list of hook events, the `settings.json` hook
>    schema (`matcher`, handler `type`, `command`, `timeout`), and the stdin-input
>    and exit-code/JSON output contract (exit 2 = block, etc.). Flag anything NEW
>    or CHANGED, and any event our config does not yet use.
> 4. Best-practice guidance for: the `description` field / automatic delegation,
>    `tools` restriction (read-only review agents), `model` selection, `memory`
>    scopes, multi-agent orchestration / concurrency, when to use a **hook vs a
>    skill vs CLAUDE.md** (deterministic guardrail vs request), and the
>    skills-vs-subagents-vs-slash-commands decision framework.
> 5. Any fields or guidance that are NEW or CHANGED versus mid-2026.
> Where the docs are silent, say "not documented" rather than guessing.

If a `claude-code-guide` agent from this session is still running or recently
finished, continue it via SendMessage instead of spawning a fresh one.

### 3. Compare and present the dated report

Compare the live guidance (step 2) against the inventory (step 1) and against any
prior audit the user still has. Present the report **inline** in the conversation,
headed with today's date (use the date printed above). Do not write it to a file
unless the user asks. Use these sections:

- **Scope & method** — what was audited; the official source URLs the agent cited.
- **Compliance summary** — a table per surface (subagents, skills, **hooks**): each
  field/event in use → Official / Custom-but-valid / **Unsupported (ignored)**. For
  hooks also confirm each script's exit-code contract matches the docs.
- **Findings & resolutions** — every gap, each with: the problem, severity
  (Critical / Should-fix / Optional), the affected `file`, and the concrete fix.
  Explicitly call out: inaccurate descriptions, misattributed claims, missing
  guardrails, and any field the docs no longer support.
- **What changed since the last report** — if the user has a prior audit, diff
  against it: new official fields, newly-deprecated fields, drift in the config
  since then. If none, say so and state this is the baseline.
- **Recommended changes** — a numbered, apply-ready list (file + exact edit).
- **Deliberately not adopted** — fields considered and skipped, with one-line reasons.

If the user passed an argument, scope or bias the audit accordingly (e.g. "skills
only", "focus on new fields").

### 4. Offer next steps — do not edit yet

Print the top findings and ask the user which to apply:
**"Apply all recommended changes, apply a subset, or stop here?"**

- **apply all / subset** — make exactly the edits listed in the report's
  *Recommended changes* section. Change nothing else. After editing, re-grep to
  confirm each edit landed and report what changed.
- **stop** — leave all config untouched; the report stands on its own.

Never commit. If the user wants a commit afterward, follow the project's `commit`
conventions (conventional message, no `Co-Authored-By` trailer).
