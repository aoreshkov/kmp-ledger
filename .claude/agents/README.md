# Agent roster & ownership matrix

This project's subagents come in **two families**, grouped into subfolders. Claude
Code discovers agents recursively and identifies them only by their `name`
frontmatter, so the subfolder is purely organizational — it does not change how an
agent is invoked.

| Family | Folder | Tools | Lens | Orchestrated by |
|---|---|---|---|---|
| **Review** (`rv-*`) | `agents/review/` | `Read, Grep, Glob, Bash` (no web) | *"Does the code obey this project's own rules?"* | `/review-house` |
| **Currency** (`bp-*`) | `agents/currency/` | `+ WebSearch, WebFetch` | *"Do the code and our rules still match the latest official upstream guidance?"* | `/review-currency` |

All agents share the same posture: **read-only review** (they propose fixes, make no
code edits), `model: opus`, `memory: project`, `maxTurns: 40`, `effort: high`.

## Pairing / ownership matrix

Each row is one domain. Where both a review and a currency agent exist, they are
**paired**: the `rv-*` agent owns project-rules correctness, the `bp-*` agent owns
upstream currency for the same files. When findings overlap, the currency agent
defers the internal-correctness call to its review pair.

| Domain | Review (`rv-*`) | Currency (`bp-*`) | Shared color |
|---|---|---|---|
| Architecture / layering | `rv-arch` | — | yellow |
| Kotlin + coroutines | `rv-concurrency` | `bp-kotlin` | purple |
| KMP structure / Swift export | — | `bp-kmp` | pink |
| Compose + Navigation 3 | `rv-compose` | `bp-compose` | green |
| Room / data layer | `rv-data` | `bp-room` | cyan |
| Koin / DI | `rv-di` | `bp-koin` | orange |
| Gradle / build | `rv-build` | `bp-gradle` | blue |
| CI / supply chain | `rv-build` (CI part) | `bp-ci` | — |
| Android platform | — | `bp-android` | — |
| Testing | `rv-testing` | — | — |
| Security | `rv-security` | — | — |
| Performance | `rv-perf` | — | — |

## Color scheme
A **paired domain shares one hue** (purple/green/cyan/orange/blue) so the pairing is
visible at a glance in `/agents`. Single-family lenses use the remaining hues
(red/yellow/pink) and may repeat among themselves — only the five pair hues signal a
review↔currency pairing.

## Coverage asymmetry (deliberate — do not "fill the gaps" blindly)
Some domains are single-family on purpose:
- **`rv-`only** (`rv-arch`, `rv-testing`, `rv-security`, `rv-perf`): house-judgment /
  correctness lenses with little fast-moving upstream "currency" to track. Android
  privacy currency is covered by `bp-android`; Kotlin/coroutine currency by
  `bp-kotlin`.
- **`bp-`only** (`bp-kmp`, `bp-android`): structural-currency lenses whose
  project-rules angle is already folded into `rv-arch` / `rv-build`.

Add a new agent only when a domain genuinely needs the *other* lens — not for symmetry
alone.

## Memory
Each agent has `memory: project`, stored at `.claude/agent-memory/<name>/` (keyed by
`name`, not by folder). Renaming an agent means renaming its memory dir too.

## Adding or renaming an agent
1. Put the file in the right family folder; set a unique `name` (the identity).
2. Add it to this matrix and to the orchestrating skill's specialist list
   (`/review-house` or `/review-currency`).
3. If it has a pair, give both the same color and note the lane boundary here rather
   than restating it in each agent body.
