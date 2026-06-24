#!/usr/bin/env bash
# PreToolUse(Bash) guard: this project forbids Co-Authored-By trailers in commits
# (CLAUDE.md commit conventions). Hooks enforce deterministically what the model
# may otherwise forget. Dependency-free (no jq): scan the raw hook payload — the
# command lives in tool_input.command and a Co-Authored-By trailer appears
# literally in the JSON (no chars JSON would escape), so no parsing is needed.
set -euo pipefail
input="$(cat)"

if printf '%s' "$input" | grep -qiE 'git[[:space:]]+commit' \
   && printf '%s' "$input" | grep -qi 'Co-Authored-By'; then
  echo "Blocked: this commit carries a Co-Authored-By trailer, which this project's" \
       "conventions forbid (CLAUDE.md). Remove the trailer and retry the commit." >&2
  exit 2
fi
exit 0
