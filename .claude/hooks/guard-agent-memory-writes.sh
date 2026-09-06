#!/usr/bin/env sh
# PreToolUse guard for the rv-*/bp-* review specialists.
#
# The review agents advertise a read-only posture, but `memory: project` makes the
# harness grant them Write/Edit so they can persist notes. This hook keeps both:
# a Write/Edit under `.claude/agent-memory/` is allowed, anything else is blocked.
#
# Wired from each agent's frontmatter (`hooks.PreToolUse`, matcher "Write|Edit"), so
# it is active only while a review subagent is running — the main session is
# unaffected. Do NOT move it to settings.json.
#
# Contract (docs.claude.com PreToolUse): stdin is the hook JSON; exit 0 = no opinion,
# exit 2 = block with stderr as the reason. No jq dependency — jq is not guaranteed on
# Windows/Git Bash, which is the default hook shell here.

set -u

payload=$(cat)

# First "file_path" occurrence is the one in tool_input; a later one can only come
# from a `content` payload, so take the head match.
raw=$(printf '%s' "$payload" \
  | grep -oE '"file_path"[[:space:]]*:[[:space:]]*"([^"\\]|\\.)*"' \
  | head -n 1)

if [ -z "$raw" ]; then
  echo "Blocked: could not determine the target file_path, so this write cannot be \
confirmed to stay inside .claude/agent-memory/. Review agents are read-only; persist \
findings to your agent memory instead and report the rest in your summary." >&2
  exit 2
fi

# Strip the key and the surrounding quotes, then normalise Windows separators and any
# JSON escaping of them so the path test works on every platform.
path=$(printf '%s' "$raw" | sed -e 's/^"file_path"[[:space:]]*:[[:space:]]*"//' -e 's/"$//')
path=$(printf '%s' "$path" | tr '\\' '/' | sed -e 's://*:/:g')

case "$path" in
  */.claude/agent-memory/*|.claude/agent-memory/*)
    exit 0
    ;;
esac

echo "Blocked: '$path' is outside .claude/agent-memory/. Review agents propose fixes, \
they never apply them — put durable notes in your agent-memory directory and report \
the proposed change (file:line + the fix) in your findings instead." >&2
exit 2
