# Review orchestration conventions

Shared rules for the multi-agent review skills **`/review-house`** and
**`/review-currency`**. Both dispatch specialist subagents from `.claude/agents/` in
parallel waves and merge their findings into one prioritized report. Neither skill
edits code — they review and report only (each subagent may persist notes to its own
project memory). The two skills are siblings: `review-house` is the project-rules
lens, `review-currency` the upstream-currency lens; see `.claude/agents/README.md`
for the full agent roster and ownership matrix.

## Why waves
Dispatching every specialist at once floods the synthesis step with summaries to
merge. As a project convention we dispatch in **waves of three** (the final wave may
be smaller), each wave in parallel, and synthesize only after all specialists
return — this keeps each merge tractable. The official docs recommend spawning
multiple subagents for independent work but set no fixed concurrency limit, so this
split is our own tuning, not a documented rule.

## Establish scope
If the user passed arguments (a module path, "since last release", or a single
domain like "just compose"), scope to that and note it explicitly so each subagent
receives it in its task prompt. Otherwise review the whole repository.

## Findings-discipline rule
End every spawn's task prompt with the discipline rule (each skill states its exact
wording). The shared shape: report only real gaps; give **severity** (Critical /
Should-fix / Optional), **`file:line`**, and a **concrete fix**; respect deliberate
pinned project decisions; do not invent findings when an area is sound.

## Synthesize
Merge all specialist reports into one document with three tiers:
- **Critical** — must fix (correctness / security / data-loss, or divergence with
  real risk).
- **Should-fix** — real gaps worth addressing soon.
- **Optional** — improvements the user may skip.

For each item keep: the owning specialist, `file:line`, the problem, the fix.
De-duplicate overlapping findings (merge when two specialists flag the same thing).
End with a short health/currency summary and the top 3 things to address first.

## Offer next steps — do not edit
Do not edit anything. Ask the user whether they want you to fix the Critical items,
run the sibling review skill, run the bundled `/code-review` on a specific diff, or
stop here.
