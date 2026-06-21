---
name: readme-plan
description: Analyse source changes since the last release and produce a plan describing what in README.md needs updating. Does not edit any files. Do not invoke automatically.
disable-model-invocation: true
allowed-tools: Bash(git tag*), Bash(git diff*), Read
---

## Previous tag
!`git tag --sort=-version:refname | head -1`

Analyse the changes since the previous tag against the current README.md and produce a plan of what needs updating. Do not edit any files.

## Steps

### 1. Collect the source diff

The previous tag is pre-loaded above. Run the following commands in order and read the output carefully:

1. `git diff <last_tag>..HEAD --name-only` — full list of changed files
2. `git diff <last_tag>..HEAD -- '*.kt'` — Kotlin sources
3. `git diff <last_tag>..HEAD -- '*.kts' 'gradle/libs.versions.toml' 'gradle.properties'` — build and dependency files
4. `git diff <last_tag>..HEAD -- '.github/'` — CI/config files

**Do not use git commit messages.** Derive everything from the actual diffs.

### 2. Read README.md

Read the full contents of `README.md`.

### 3. Identify what needs updating

Compare the diff findings against the README content. For each README section, decide whether it is:
- **Out of date** — the source changed something this section describes
- **Missing content** — the source contains something new not yet documented
- **Up to date** — no action needed (do not list these)

Sections to check:

| README section | What to look for in the diff |
|---|---|
| Badges (top) | Version numbers in `gradle/libs.versions.toml` or `gradle.properties` |
| Tech Stack table | Any library version change in `libs.versions.toml` |
| Architecture diagram / module list | New modules, removed modules, renamed packages |
| Module responsibilities table | New or removed classes/files that change a module's stated role |
| Design Patterns | New patterns introduced, existing patterns significantly changed |
| Testing Strategy | New test utilities, new test layers, changed coverage approach |
| Project Structure tree | New top-level directories or modules |
| Getting Started / prerequisites | Toolchain version changes (JDK, AGP, Xcode notes) |
| Releasing section | Changes to the release workflow |

### 4. Output the plan

Present the findings as a numbered list of proposed changes. For each item include:

- **Section**: which README section needs updating
- **Why**: what in the diff makes this necessary (be specific — name the file or symbol that changed)
- **What to change**: a concrete description of the edit (e.g. "update Room version from 3.0.0-alpha06 to 3.0.0-rc01 in the Tech Stack table")

End with a one-sentence summary of the overall scope (e.g. "3 version bumps in the Tech Stack table and 1 new module to add to the Architecture section").

Do not make any edits. Output the plan only.
