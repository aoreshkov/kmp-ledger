---
name: release
description: Cut a new release — analyse source changes, bump gradle.properties, write CHANGELOG.md, and commit. Invoke with the new version number. Do not invoke automatically.
disable-model-invocation: true
argument-hint: [version]
arguments: version
allowed-tools: Bash(git tag*), Bash(git diff*), Bash(git add gradle.properties CHANGELOG.md), Bash(git commit*), Read, Edit
context: fork
---

## Previous tag
!`git tag --sort=-version:refname | head -1`

New version: $version

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

### 3. Read current versions

Read `gradle.properties` and note `ledger.version.name` and `ledger.version.code`.

### 4. Update `gradle.properties`

Set:
```
ledger.version.name=$version
ledger.version.code=<current code + 1>
```

### 5. Update `CHANGELOG.md`

Insert a new section immediately after the `# Changelog` header line (before the existing first `## [` entry).
Use today's date in `YYYY-MM-DD` format. Only include categories that have at least one entry.

```markdown
## [$version] - YYYY-MM-DD

### Added
- …

### Changed
- …

### Fixed
- …
```

Then update the two link lines at the bottom of the file:

- Replace the `[Unreleased]` line with:
  `[Unreleased]: https://github.com/aoreshkov/kmp-ledger/compare/v$version...HEAD`
- Add a new line for the new version above the previous top version line:
  `[$version]: https://github.com/aoreshkov/kmp-ledger/compare/<previous_tag_version>...v$version`

### 6. Commit

Stage exactly these two files — nothing else:
```
git add gradle.properties CHANGELOG.md
```

Commit message (no Co-Authored-By trailer):
```
chore: bump version to $version
```

### 7. Print next steps

Tell the user to run the following commands to complete the release:
```
git push origin main
git tag v$version
git push origin v$version
```

Explain that pushing the tag triggers the GitHub Actions release workflow, which builds the Android APK and Desktop binaries and creates the GitHub Release automatically.
