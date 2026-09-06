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
code edits), `model: opus`, `memory: project`, `maxTurns: 40`, `effort: high`,
`experimental.cacheTtl: 1h`.

## Two standing rules for this family

**1. Read-only is enforced, not asked for.** `memory: project` makes the harness grant
`Write`/`Edit` even though `tools:` omits them — otherwise the agent could not persist
memory. So every agent carries a `PreToolUse` hook, matcher `"Write|Edit"`, running
`.claude/hooks/guard-agent-memory-writes.sh`: a write under `.claude/agent-memory/`
passes, anything else is blocked (exit 2) with a reason telling the agent to report the
change as a finding instead. The hook lives in **agent frontmatter, not
`settings.json`**, so it is active only while a review subagent runs and never
constrains the main session. Agent-level hooks require trusting the folder containing
the agent file. Do **not** "simplify" this to `disallowedTools: Write, Edit` — that
would break `memory: project`.

**2. Never hardcode what you can read from the repo.** An agent prompt must not assert a
library version, a module name, or a target list. It names the *source* —
`gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, `build-logic/`
— and instructs the agent to read it. Copies of these facts rot silently: an audit on
2026-09-06 found `bp-compose`, `bp-room` and `bp-testing` measuring the code against
version pins that were ten weeks out of date, while `bp-gradle` and `bp-android` — the
two that derived their versions — were still correct.

The `bp-*` family also shares one reporting contract, factored into the
`currency-findings-contract` skill and preloaded via each agent's `skills:` frontmatter
rather than copy-pasted into nine bodies. Each `bp-*` body keeps only its own
domain-specific *deliberate choices* line. The `rv-*` reporting rules are genuinely
per-domain and stay inline.

## Pairing / ownership matrix

Each row is one domain. Where both a review and a currency agent exist, they are
**paired**: the `rv-*` agent owns project-rules correctness, the `bp-*` agent owns
upstream currency for the same files. When findings overlap, the currency agent
defers the internal-correctness call to its review pair.

| Domain | Review (`rv-*`) | Currency (`bp-*`) | Shared color |
|---|---|---|---|
| Architecture / layering | `rv-arch` | — | yellow |
| Kotlin + coroutines | `rv-concurrency` | `bp-kotlin` | purple |
| KMP structure / Swift export | `rv-kmp` | `bp-kmp` | pink |
| Compose + Navigation 3 | `rv-compose` | `bp-compose` | green |
| Room / DataStore / data layer | `rv-data` | `bp-room` | cyan |
| Koin / DI | `rv-di` | `bp-koin` | orange |
| Gradle / build | `rv-build` | `bp-gradle` | blue |
| CI / supply chain | `rv-ci` | `bp-ci` | red |
| Android platform | — | `bp-android` | — |
| Testing | `rv-testing` | `bp-testing` | yellow |
| Security | `rv-security` | — | — |
| Performance | `rv-perf` | — | — |

## Color scheme
A **paired domain shares one hue** so the pairing reads at a glance in `/agents`:
purple (Kotlin), green (Compose), cyan (Room), orange (Koin), blue (Gradle), red (CI),
pink (KMP), yellow (Testing). With eight paired domains the eight available hues are
now fully consumed by pairs, so hue is **no longer a unique pairing signal** — the four
remaining single-family agents (`rv-arch`, `rv-security`, `rv-perf`, `bp-android`)
reuse hues, and **the matrix above is the authoritative source of pairing**, not the
color. Treat the shared hue as a convenience, not a guarantee.

## Coverage asymmetry (deliberate — do not "fill the gaps" blindly)
A few domains are single-family on purpose:
- **`rv-`only** (`rv-arch`, `rv-security`, `rv-perf`): house-judgment / correctness
  lenses with little fast-moving upstream "currency" to track. Android privacy
  currency is covered by `bp-android`; Kotlin/coroutine currency by `bp-kotlin`.
- **`bp-`only** (`bp-android`): a platform-currency lens whose project-rules angle is
  already covered by `rv-arch` / `rv-ci`.

Add a new agent only when a domain genuinely needs the *other* lens — not for symmetry
alone.

## Memory
Each agent has `memory: project`, stored at `.claude/agent-memory/<name>/` (keyed by
`name`, not by folder). Renaming an agent means renaming its memory dir too.

## Adding or renaming an agent
1. Put the file in the right family folder; set a unique `name` (the identity).
2. Add it to this matrix and to the orchestrating skill's specialist list
   (`/review-house` or `/review-currency`), **and** to `/review-all`'s roster.
3. If it has a pair, give both the same color and note the lane boundary here rather
   than restating it in each agent body. Check the pair reference in **both** bodies —
   a stale one (`bp-ci` pointed at `rv-build` for ten weeks) silently breaks the
   cross-lens de-duplication rule.
4. Copy the shared frontmatter block (`model`, `memory`, `maxTurns`, `effort`,
   `experimental`, `hooks`) from a sibling; for a `bp-*` agent also add
   `skills: [currency-findings-contract]`.
5. Keep the `description` short — it loads at every session start, while the body
   loads only when the agent runs. Target the ~220–260 char band the existing agents
   sit in, and put sources, checklists and boundaries in the body.
