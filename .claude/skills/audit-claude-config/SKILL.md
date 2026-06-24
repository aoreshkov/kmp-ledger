---
name: audit-claude-config
description: Audit this project's `.claude/` subagents and skills against the LATEST official Claude Code docs, then write a dated report to docs/. Researches current guidance live (does not rely on memory). Read-then-offer — proposes fixes but does not apply them without confirmation. Do not invoke automatically.
disable-model-invocation: true
argument-hint: "[optional: extra focus, e.g. 'skills only' or 'check new fields']"
allowed-tools: Read, Grep, Glob, Bash, Agent, Write, Edit
---

## Today
!`date +%F`

Audit the project's Claude Code customizations under `.claude/` against the
**current** official documentation and produce a dated best-practices report in
`docs/`. The prior report (`docs/agents-skills-best-practices-audit-2026-06-24.md`)
is the reference format and a baseline to diff against.

This skill **makes no config changes on its own** — it researches, reports, and
then offers to apply fixes.

## Steps

### 1. Inventory the local config

Enumerate and read what exists today:
- `git ls-files '.claude/agents/*.md' '.claude/skills/*/SKILL.md'` (plus untracked:
  `Glob .claude/agents/** ` and `.claude/skills/**`).
- Read every agent and skill file. Record each one's frontmatter fields and values
  (`name`, `description`, `tools`, `model`, `memory`, `color`, `maxTurns`, `effort`,
  `allowed-tools`, `disable-model-invocation`, `context`, `argument-hint`,
  `arguments`, etc.) and note the agent-memory dirs under `.claude/agent-memory/`.

### 2. Research the latest official guidance (live — do not trust memory)

Spawn the **`claude-code-guide`** subagent (it has WebSearch/WebFetch). Its task:

> Research the LATEST official Claude Code docs (docs.claude.com / code.claude.com:
> the sub-agents, skills, best-practices, and agent-teams pages) plus the changelog.
> Use WebSearch/WebFetch — do not answer from memory. Cite the doc URLs. Report, as
> of today's date:
> 1. The complete set of currently-supported frontmatter fields for **subagents**
>    (`.claude/agents/*.md`) and what each does. Flag any field that is NOT
>    officially supported (would be ignored by the harness).
> 2. The complete set of currently-supported frontmatter fields for **skills**
>    (`SKILL.md`), same treatment. Confirm the in-SKILL dynamic-injection syntax
>    (an exclamation mark followed by a backtick-quoted shell command) and any
>    supporting-file reference convention are still current.
> 3. Best-practice guidance for: the `description` field / automatic delegation,
>    `tools` restriction (read-only review agents), `model` selection, `memory`
>    scopes, multi-agent orchestration / concurrency, and the
>    skills-vs-subagents-vs-slash-commands decision framework.
> 4. Any fields or guidance that are NEW or CHANGED versus mid-2026.
> Where the docs are silent, say "not documented" rather than guessing.

If a `claude-code-guide` agent from this session is still running or recently
finished, continue it via SendMessage instead of spawning a fresh one.

### 3. Compare and write the dated report

Compare the live guidance (step 2) against the inventory (step 1) and against the
previous report. Write `docs/agents-skills-best-practices-audit-<today>.md` (use the
date printed above). **If that file already exists (a same-day re-run), do not
overwrite it** — append a run suffix instead (`-rerun`, then `-2`, `-3`, …) and note
in the report that it is a re-run of the same-day baseline. Use these sections:

- **Scope & method** — what was audited; the official source URLs the agent cited.
- **Compliance summary** — a table per surface (subagents, skills): each field in
  use → Official / Custom-but-valid / **Unsupported (ignored)**.
- **Findings & resolutions** — every gap, each with: the problem, severity
  (Critical / Should-fix / Optional), the affected `file`, and the concrete fix.
  Explicitly call out: inaccurate descriptions, misattributed claims, missing
  guardrails, and any field the docs no longer support.
- **What changed since the last report** — diff against the previous dated audit:
  new official fields, newly-deprecated fields, drift in the config since then.
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
