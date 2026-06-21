---
name: commit
description: Review all staged changes and create a conventional commit message derived from the actual diff. Use after running git add. Do not invoke automatically.
disable-model-invocation: true
allowed-tools: Bash(git diff*), Bash(git log --oneline*), Bash(git commit*)
---

## Staged files
!`git diff --cached --name-only`

## Staged diff
!`git diff --cached`

Review the staged diff above and commit with an appropriate conventional commit message. Do not stage any additional files.

## Steps

### 1. Check staged changes exist

If the staged files list above is empty, stop and tell the user there is nothing staged to commit.

### 2. Determine the commit type

Pick exactly one type based on what the diff does:

| Type | When to use |
|---|---|
| `feat` | New user-visible feature or behaviour |
| `fix` | Bug fix — corrects wrong behaviour |
| `test` | Adding or correcting tests only, no production code change |
| `refactor` | Code restructuring with no behaviour change and no new tests |
| `build` | Build files, dependencies, version bumps (`*.kts`, `libs.versions.toml`, `gradle.properties`) |
| `ci` | CI/CD workflow changes (`.github/`) |
| `docs` | Documentation only (`README.md`, `CHANGELOG.md`, `*.md`) |
| `chore` | Anything else that doesn't fit the above |

If the diff spans multiple types, pick the one that best describes the primary intent.

### 3. Write the commit message

Rules:
- Format: `<type>(<optional scope>): <short description>`
- Subject line: imperative mood, lowercase after the colon, no trailing period, ≤72 characters
- Scope (optional): the most affected module in kebab-case, e.g. `core-domain`, `posting-impl`, `build-logic`
- Body (optional): include only if the *why* is non-obvious from the subject alone. One short paragraph, wrapped at 72 characters. Do not explain what the code does — only why.
- No `Co-Authored-By` trailer.
- No plan labels, ticket numbers, or issue references unless they appear in the staged files themselves.

### 4. Show the message and ask for confirmation

Display the proposed commit message to the user and ask: **"Commit with this message? (yes / edit / cancel)"**

- **yes** — proceed to step 5
- **edit** — ask the user for their preferred message, then proceed to step 5 with their version
- **cancel** — stop, make no changes

### 5. Commit

Run the commit using a Bash heredoc (not PowerShell syntax):

```bash
git commit -m "$(cat <<'EOF'
<subject line>

<body if any>
EOF
)"
```

After the commit succeeds, show the one-line output of `git log --oneline -1` so the user can confirm what was recorded.
