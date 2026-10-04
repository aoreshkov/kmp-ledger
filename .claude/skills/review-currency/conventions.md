# Currency review conventions

Orchestration rules for `/review-currency`. It dispatches the nine `bp-*` specialists
from `.claude/agents/currency/` in parallel waves and merges their findings into one
prioritized review **document**. That document, a dated markdown file under `docs/` (see
*Synthesize* below), is the skill's only output. It makes **no other changes** to source,
build, test or config files; each subagent may persist notes to its own project memory.
That read-only posture is **enforced, not just asked for**: every `bp-*` agent carries a
`PreToolUse` hook (`.claude/hooks/guard-agent-memory-writes.sh`) that blocks any
`Write`/`Edit` outside `.claude/agent-memory/`. If a specialist reports being blocked,
it tried to edit something it shouldn't, so record the proposed change as a finding. See
`.claude/agents/README.md` for the roster.

## Why waves
Dispatching every specialist at once floods the synthesis step with summaries to merge.
As a project convention we dispatch in **small parallel waves of three**, each wave in
parallel, and synthesize only after all specialists return. This keeps each merge
tractable. The official docs set no fixed concurrency limit, so the split is our own
tuning, not a documented rule.

## Establish scope
If the user passed arguments (a module path, "since last release", or a single domain
like "just compose"), scope to that and note it explicitly, so each subagent receives it
in its task prompt. Otherwise review the whole repository.

## Handle partial specialist output
A subagent that hits its `maxTurns` limit returns output the harness marks **partial**.
Treat a partial return as *partially reviewed*, never as *clean*: in the review document,
under the specialist's own findings, name the sub-areas it did not reach. Silence from a
truncated agent is absence of evidence, not evidence of health. If a domain came back
badly truncated, say so in the health summary and offer to re-run that one specialist.

## Synthesize — write the review document
Merge all specialist reports into a single markdown document and **write it to**
`docs/<today>-currency-review.md`, using the date the skill injected. The date-first shape
is the repo's `docs/` convention (`YYYY-MM-DD-<topic>.md`). Writing this one file **is**
the skill's deliverable; it is not a "code change". If a doc with that name already
exists (a re-run on the same day), overwrite it.

### Part 1 — Findings, in three tiers
- **Critical**: must fix (a divergence with real correctness, security or data-loss
  risk).
- **Should-fix**: real gaps worth addressing soon.
- **Optional**: improvements the user may skip.

For each item keep the owning specialist, `file:line`, the problem, the fix, and the
**source URL + version/date**. When two specialists flag the same thing, merge them into
one entry with both attributions. Include the *pinned version vs. latest stable* table.

### Part 2 — Phased implementation plan
Turn the actionable findings (Critical + Should-fix, plus any Optional worth batching)
into a fix roadmap grouped into **phases ordered by risk/value**. Each phase is a
self-contained, committable unit. For each phase list the findings it resolves (by id),
the files to touch, and a concrete **verify** step (the relevant `./gradlew` tasks). Call
out the repo gates the fix triggers: `./gradlew apiDump` + committing the `*/api/` dumps
on any public-API change, the Kover floors, and the project commit style. List findings
that are deferred by design with their one-line rationale rather than dropping them.

End the document with a short currency summary and the top 3 things to address first.

## Offer next steps
After writing the doc, tell the user its path and ask whether they want you to
**implement the fixes by phase** (all phases, Should-fix only, or just Phase 1), run the
bundled `/code-review` on a specific diff, or stop here. Implementing any phase is a
separate, explicitly-approved step.
