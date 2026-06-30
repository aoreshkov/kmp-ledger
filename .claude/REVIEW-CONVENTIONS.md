# Review orchestration conventions

Shared rules for the multi-agent review skills **`/review-house`**,
**`/review-currency`**, and the combined **`/review-all`**. All dispatch specialist
subagents from `.claude/agents/` in parallel waves and merge their findings into one
prioritized review **document**. The skills' sole output is that document — a dated
markdown file under `docs/` (see *Synthesize* below); they make **no other changes**
to source, build, test, or config files (each subagent may persist notes to its own
project memory). `review-house` is the project-rules lens, `review-currency` the
upstream-currency lens, `review-all` runs both; see `.claude/agents/README.md` for the
full agent roster and ownership matrix.

## Why waves
Dispatching every specialist at once floods the synthesis step with summaries to
merge. As a project convention we dispatch in **small parallel waves (three to four)**
each wave in parallel, and synthesize only after all specialists return — this keeps
each merge tractable. The official docs recommend spawning multiple subagents for
independent work but set no fixed concurrency limit, so this split is our own tuning,
not a documented rule.

## Establish scope
If the user passed arguments (a module path, "since last release", or a single
domain like "just compose"), scope to that and note it explicitly so each subagent
receives it in its task prompt. Otherwise review the whole repository.

## Findings-discipline rule
End every spawn's task prompt with the discipline rule (each skill states its exact
wording). The shared shape: report only real gaps; give **severity** (Critical /
Should-fix / Optional), **`file:line`**, and a **concrete fix**; respect deliberate
pinned project decisions; do not invent findings when an area is sound.

## Synthesize — write the review document
Merge all specialist reports into a single markdown document and **write it to**
`docs/<lens>-review-YYYY-MM-DD.md`, using the review date and the lens prefix:
`full-review-` for `/review-all`, `house-review-` for `/review-house`,
`currency-review-` for `/review-currency`. Writing this one file **is** the skill's
deliverable — it is not a "code change"; do not edit any source, build, test, or config
file. If a doc with that name already exists (a re-run on the same day), overwrite it.

The document has two parts.

### Part 1 — Findings, in three tiers
- **Critical** — must fix (correctness / security / data-loss, or divergence with
  real risk).
- **Should-fix** — real gaps worth addressing soon.
- **Optional** — improvements the user may skip.

For each item keep: the owning specialist, `file:line`, the problem, and the fix — and
for currency findings, the **source URL + version/date**. De-duplicate overlapping
findings (merge when two specialists flag the same thing; see *Cross-skill
de-duplication*). Include the *pinned version vs. latest stable* currency table whenever
the currency lens ran.

### Part 2 — Phased implementation plan
Turn the actionable findings (Critical + Should-fix, plus any Optional worth batching)
into a fix roadmap grouped into **phases ordered by risk/value**, where each phase is a
self-contained, committable unit. For each phase list: the findings it resolves
(by id), the files to touch, and a concrete **verify** step (the relevant `./gradlew`
tasks). Call out repo gates the fix triggers — `./gradlew apiDump` + committing the
`*/api/` dumps on any public-API change, the Kover floors, and the project commit style.
List findings deferred-by-design with their one-line rationale rather than dropping them.

End the document with a short health/currency summary and the top 3 things to address
first.

### Cross-skill de-duplication
When both lenses have run — either back-to-back or together under `/review-all` — a
paired domain (see the matrix in `.claude/agents/README.md`) will often surface the
same line twice: once from the `rv-*` agent and once from its `bp-*` pair. Merge these
into a single entry. The lane split decides who owns the call:
- the **`rv-*`** agent owns the **correctness / project-rules** verdict;
- the **`bp-*`** agent owns the **currency** verdict and carries the **source URL +
  version/date**.

Keep both attributions on the merged item but state the issue once. Never list the same
`file:line` problem as two separate findings.

## Offer next steps — only the review doc is written
The only file you write is the `docs/` review document; do not touch source, build,
test, or config files. After writing it, tell the user its path and ask whether they
want you to **implement the fixes by phase** (all phases, Should-fix only, or just
Phase 1), run the sibling review skill (`/review-house` ↔ `/review-currency`), run both
at once with `/review-all`, run the bundled `/code-review` on a specific diff, or stop
here. Implementing any phase is a separate, explicitly-approved step.
