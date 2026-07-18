---
name: release
description: Cut a new release — analyse source changes, recommend the next version, confirm it, then bump gradle.properties, write CHANGELOG.md, and commit. Optionally invoke with a version number to pre-fill the recommendation. Do not invoke automatically.
disable-model-invocation: true
argument-hint: [version]
arguments: version
allowed-tools: Bash(git tag*), Bash(git diff*), Bash(git add gradle.properties CHANGELOG.md), Bash(git commit*), Read, Edit, AskUserQuestion
context: fork
---

## Previous tag
!`git tag --sort=-version:refname | head -1`

Requested version (optional): $version

If `$version` is empty, you will recommend one in step 3. If it is set, treat it
as the operator's preferred version and pre-select it during confirmation.

## Steps

### 1. Resolve previous tag

The previous tag is pre-loaded above. Note the tag (e.g. `v1.1.1`).

### 2. Analyse actual source changes

**Do not use git commit messages.** Derive the changelog entirely from the source diff.

Run the following commands in order, one group at a time, and read the output carefully:

1. **Full file list** — `git diff <last_tag>..HEAD --name-only`
2. **Kotlin sources** — `git diff <last_tag>..HEAD -- '*.kt'`
3. **Build & dependency files** — `git diff <last_tag>..HEAD -- '*.kts' 'gradle/libs.versions.toml' 'gradle.properties'`
4. **CI/config files** — `git diff <last_tag>..HEAD -- '.github/'`

From these diffs identify:
- **Added**: new screens, new use cases, new public functions or classes, new features, new CI steps
- **Changed**: modified behaviour, refactored internals worth noting, dependency version upgrades (read exact versions from the toml diff)
- **Fixed**: corrected logic, test fixes that revealed a real bug, build/config corrections
- **Removed**: deleted functionality or modules

Rules:
- One bullet per distinct user- or developer-visible change.
- Omit: pure formatting, comment edits, generated file changes, test-only additions that don't correspond to a bug fix.
- For dependency upgrades write the library name and new version, e.g. "Upgraded Kotlin to 2.5.0."

### 3. Recommend the next version & confirm

Derive a recommended version from the change buckets in step 2 (do **not** ask
the operator to decide unaided — propose, then confirm).

Parse the previous tag's `MAJOR.MINOR.PATCH` (e.g. `v1.2.0` → `1`, `2`, `0`).
The project is already past `1.0.0`, so standard SemVer applies (no `0.x`
special-casing). Map the buckets to a bump:

- Any **Removed** entry, or any breaking/incompatible **Changed** entry →
  **major**: `<major+1>.0.0`.
- Otherwise any **Added** entry → **minor**: `<major>.<minor+1>.0`.
- Otherwise (only **Fixed**, or internal/non-breaking **Changed**) →
  **patch**: `<major>.<minor>.<patch+1>`.

If step 2 found no user- or developer-visible changes, say so and default the
recommendation to a **patch** bump rather than inventing changelog entries.

Compute all three candidate versions from the previous tag (e.g. from `v1.2.0`:
major `2.0.0`, minor `1.3.0`, patch `1.2.1`).

Then call **`AskUserQuestion`** with a single question — "Confirm the release
version" — and these options:

- **First option = the version to release.** If `$version` was supplied, use it
  here and note it is operator-supplied; otherwise use the recommended version,
  labelled with its bump type and a one-line justification (e.g.
  "1.3.0 — minor: new features added, no breaking changes").
- The other two SemVer candidates (the major/minor/patch versions not chosen as
  the first option), each labelled with its bump type.

The tool automatically offers an "Other" entry for a custom version, so do not
add one yourself. Whatever the operator confirms (or types) becomes **the
confirmed version** used by every step below — do not write any files before
this confirmation.

### 4. Read current versions

Read `gradle.properties` and note `ledger.version.name` and `ledger.version.code`.

### 5. Update `gradle.properties`

Set:
```
ledger.version.name=<confirmed version>
ledger.version.code=<current code + 1>
```

### 6. Update `CHANGELOG.md`

Insert a new section immediately after the `# Changelog` header line (before the existing first `## [` entry).
Use today's date in `YYYY-MM-DD` format. Only include categories that have at least one entry.

```markdown
## [<confirmed version>] - YYYY-MM-DD

### Added
- …

### Changed
- …

### Fixed
- …
```

Then update the two link lines at the bottom of the file. Read the existing
`[Unreleased]:` link line there; everything before `/compare/` is the repository
base URL (`<repo_url>`).

- Replace the `[Unreleased]` line with:
  `[Unreleased]: <repo_url>/compare/v<confirmed version>...HEAD`
- Add a new line for the new version above the previous top version line:
  `[<confirmed version>]: <repo_url>/compare/<previous_tag_version>...v<confirmed version>`

### 7. Commit

Stage exactly these two files — nothing else:
```
git add gradle.properties CHANGELOG.md
```

Commit message (no Co-Authored-By trailer):
```
chore: bump version to <confirmed version>
```

### 8. Print next steps

Tell the user to run the following commands to complete the release:
```
git push origin main
git tag v<confirmed version>
git push origin v<confirmed version>
```

Explain that pushing the tag triggers the GitHub Actions release workflow, which builds the Android APK and Desktop binaries and creates the GitHub Release automatically.
